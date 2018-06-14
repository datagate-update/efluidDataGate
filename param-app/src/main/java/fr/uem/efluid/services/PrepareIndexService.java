package fr.uem.efluid.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.entities.LobProperty;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import fr.uem.efluid.services.types.CombinedSimilar;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import fr.uem.efluid.services.types.Rendered;
import fr.uem.efluid.services.types.SimilarPreparedIndexEntry;
import fr.uem.efluid.services.types.SimilarPreparedMergeIndexEntry;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Core service for diff processes and index preparation. Support 2 flows :
 * <ul>
 * <li>The standard diff process for local index creation, using a basic difference
 * analaysis between the actual managed DB, and a simulated "knew" managed DB generated
 * from actual index. This produce the exact "modification identification", ie, the index
 * content for a new commit</li>
 * <li>For merging process, the initial diff is using the same process, but the
 * "simulated" DB is constructed from Actual Index <b>until last commit import</b> and the
 * import index <b>starting the last commit import</b>. This produces a view of
 * differences between "actual DB" and "what we should have if we process all the import".
 * The produced diff is a "merge" result. This merge result is then completed, searching
 * for each corresponding index KeyValue, by a combined diff of all similar local index
 * ("what was really modified locally") and similar imported index ("what was really
 * modified in remote instance"), giving help to the user to select the diff entries to
 * apply or to cancel.</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
@Service
@Transactional
public class PrepareIndexService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrepareIndexService.class);

	private static final Logger MERGE_LOGGER = LoggerFactory.getLogger("merge.analysis");

	@Autowired
	private ManagedExtractRepository rawParameters;

	@Autowired
	private ManagedRegenerateRepository regeneratedParamaters;

	@Autowired
	private ManagedValueConverter valueConverter;

	@Value("${param-efluid.display.combine-similar-diff-after}")
	private long maxSimilarBeforeCombined;

	@Autowired
	private IndexRepository indexes;

	private boolean useParallelDiff = false;

	/**
	 * @param dictionaryEntryUuid
	 * @return
	 */
	public Collection<PreparedIndexEntry> currentContentDiff(DictionaryEntry entry, Map<String, byte[]> lobs) {

		LOGGER.debug("Processing new diff for all content for managed table \"{}\"", entry.getTableName());

		// Here one of the app complexity : diff check using JDBC, for one table. Backlog
		// construction + restoration then diff.

		LOGGER.info("Start regenerate knew content for table \"{}\"", entry.getTableName());
		Map<String, String> knewContent = this.regeneratedParamaters.regenerateKnewContent(entry);

		LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());
		Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry, lobs);

		// Completed diff
		Collection<PreparedIndexEntry> index = generateDiffIndexFromContent(PreparedIndexEntry::new, knewContent, actualContent, entry);

		// Detect and process similar entries for display
		return combineSimilarDiffEntries(index, SimilarPreparedIndexEntry::fromSimilar);
	}

	/**
	 * <p>
	 * For merge diff, we run basically the standard process, but building the "knew
	 * content" as a combination of local knew content "until last imported commit" and
	 * the imported diff. => Produces the diff between current real content and "what we
	 * should have if the imported diff was present". This way we produce the exact result
	 * of the merge for an import.
	 * </p>
	 * 
	 * @param entry
	 * @param lobs
	 *            for any extracted lobs
	 * @param timeStampForSearch
	 * @param mergeContent
	 * @return
	 */
	public Collection<PreparedMergeIndexEntry> mergeIndexDiff(
			DictionaryEntry entry,
			Map<String, byte[]> lobs,
			long timeStampForSearch,
			List<PreparedMergeIndexEntry> mergeContent) {

		LOGGER.debug("Regenerating values from combined local + specified index for managed table \"{}\", using"
				+ " timestamp for local index search {}", entry.getTableName(), Long.valueOf(timeStampForSearch));

		// Here one other complexity in the app : diff between 2 different indexes, which
		// can be related themselves to a content diff previously generated. Used for
		// merging process

		List<IndexEntry> localIndexToTimeStamp = this.indexes.findByDictionaryEntryAndTimestampGreaterThanEqualOrderByTimestamp(entry,
				timeStampForSearch);

		// Prepare simulated "knew index"
		List<DiffLine> toProcess = new ArrayList<>();
		toProcess.addAll(localIndexToTimeStamp);
		toProcess.addAll(mergeContent);

		LOGGER.info("Start regenerate knew content for table \"{}\"", entry.getTableName());
		Map<String, String> knewContent = this.regeneratedParamaters.regenerateKnewContent(toProcess);

		LOGGER.info("Regenerate done, start extract actual content for table \"{}\"", entry.getTableName());
		Map<String, String> actualContent = this.rawParameters.extractCurrentContent(entry, lobs);

		Collection<PreparedMergeIndexEntry> diff = generateDiffIndexFromContent(PreparedMergeIndexEntry::new, knewContent, actualContent,
				entry);

		LOGGER.info("Diff prepared. Complete merge data for table \"{}\"", entry.getTableName());

		// Then apply from local and from import to identify diff changes
		completeMergeIndexes(entry, localIndexToTimeStamp, mergeContent, diff);

		return combineSimilarDiffEntries(diff, SimilarPreparedMergeIndexEntry::fromSimilar);
	}

	/**
	 * 
	 */
	public void resetDiffCaches() {
		this.regeneratedParamaters.refreshAll();
	}

	/**
	 * <p>
	 * Utilitary fonction to complete given index entries with their respective HR Payload
	 * for user friendly rendering. Provides the completed rendering list in return, as
	 * some display process may require to combine contents when they are similar. In this
	 * case they are provided as SimilarPreparedIndexEntry
	 * </p>
	 * 
	 * @param dic
	 * @param index
	 * @return List adapted for rendering : some results may be combined
	 */
	List<PreparedIndexEntry> prepareDiffForRendering(DictionaryEntry dic, List<PreparedIndexEntry> index) {

		// Get all previouses for HR payload init
		Map<String, IndexEntry> previouses = this.indexes.findAllPreviousIndexEntries(dic,
				index.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()),
				index.stream().map(PreparedIndexEntry::getId).collect(Collectors.toList()));

		// Complete HR payloads
		index.forEach(e -> {
			IndexEntry previous = previouses.get(e.getKeyValue());
			String hrPayload = getConverter().convertToHrPayload(e.getPayload(), previous != null ? previous.getPayload() : null);
			e.setHrPayload(hrPayload);
		});

		// And then combine for rendering
		return combineSimilarDiffEntries(index, SimilarPreparedIndexEntry::fromSimilar);
	}

	/**
	 * <p>
	 * Process the map compare to provide new <tt>IndexEntry</tt> describing the diffences
	 * as additions, removals or updates.
	 * </p>
	 * <p>
	 * Their is no need for natural order process, so use parallel processes whenever it's
	 * possible.
	 * </p>
	 * 
	 * @param knewContent
	 * @param actualContent
	 * @param dic
	 * @return
	 */
	<T extends PreparedIndexEntry> Collection<T> generateDiffIndexFromContent(
			final Supplier<T> diffTypeBuilder,
			final Map<String, String> knewContent,
			final Map<String, String> actualContent,
			DictionaryEntry dic) {

		LOGGER.info("Start diff index Generate for table \"{}\"", dic.getTableName());

		final Set<T> diff = ConcurrentHashMap.newKeySet();

		// Use parallel process for higher distribution of verification
		switchedStream(actualContent).forEach(actualOne -> searchDiff(diffTypeBuilder, actualOne, knewContent, diff, dic));

		LOGGER.info("Existing vs Knew content diff completed for table \"{}\". Start not managed knew content", dic.getTableName());

		// Remaining in knewContent are deleted ones
		switchedStream(knewContent).forEach(e -> {
			LOGGER.debug("New endex entry for {} : REMOVE from \"{}\"", e.getKey(), e.getValue());
			diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.REMOVE, e.getKey(), null, e.getValue(), dic));
		});

		LOGGER.info("Not managed knew content completed for table \"{}\".", dic.getTableName());

		return diff;
	}

	/**
	 * <p>
	 * Search on each diff for used lob hashes, and prepare the corresponding LobProperty
	 * to save
	 * </p>
	 * 
	 * @param diffs
	 * @param lobs
	 * @return
	 */
	List<LobProperty> prepareUsedLobsForIndex(List<? extends DiffLine> diffs, Map<String, byte[]> lobs) {

		return diffs.stream()
				.map(d -> this.valueConverter.extractUsedBinaryHashs(d.getPayload()))
				.filter(v -> v != null && !v.isEmpty())
				.flatMap(List::stream).map(h -> {
					LobProperty lob = new LobProperty();
					lob.setHash(h);
					lob.setData(lobs.get(h));
					return lob;
				}).collect(Collectors.toList());

	}

	/**
	 * @param diffWithSimilars
	 * @return
	 */
	@SuppressWarnings("unchecked")
	<T extends PreparedIndexEntry> List<T> splitCombinedSimilar(List<T> diffWithSimilars) {

		// Check if their is similar items
		if (diffWithSimilars.stream().anyMatch(Rendered::isDisplayOnly)) {

			return diffWithSimilars.stream().flatMap(d -> {

				// Combined to split + stream
				if (d.isDisplayOnly()) {
					return ((CombinedSimilar<T>) d).split().stream();
				}

				// Standard to stream
				return Stream.of(d);
			}).collect(Collectors.toList());
		}

		// If no similar : return provided list
		return diffWithSimilars;
	}

	/**
	 * For testability
	 */
	protected ManagedValueConverter getConverter() {
		return this.valueConverter;
	}

	/**
	 * @param parallel
	 */
	protected void applyParallelMode(boolean parallel) {
		this.useParallelDiff = parallel;
	}

	/**
	 * @param source
	 * @return
	 */
	private Stream<Map.Entry<String, String>> switchedStream(Map<String, String> source) {

		if (this.useParallelDiff) {
			return source.entrySet().parallelStream();
		}

		return source.entrySet().stream();
	}

	/**
	 * <p>
	 * Atomic search for update / addition on one actual item
	 * </p>
	 * 
	 * @param actualOne
	 * @param knewContent
	 * @param diff
	 * @param debug
	 */
	private <T extends PreparedIndexEntry> void searchDiff(
			final Supplier<T> diffTypeBuilder,
			final Map.Entry<String, String> actualOne,
			final Map<String, String> knewContent,
			final Set<T> diff,
			final DictionaryEntry dic) {

		// Found : for delete identification immediately remove from found ones
		String knewPayload = knewContent.remove(actualOne.getKey());

		// Exist already
		if (knewPayload != null) {

			// TODO : add independency over column model

			// Content is different : it's an Update
			if (!actualOne.getValue().equals(knewPayload)) {
				LOGGER.debug("New endex entry for {} : UPDATED from \"{}\" to \"{}\"", actualOne.getKey(), knewPayload,
						actualOne.getValue());
				diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.UPDATE, actualOne.getKey(), actualOne.getValue(), knewPayload,
						dic));
			}
		}

		// Doesn't exist already : it's an addition
		else {
			LOGGER.debug("New endex entry for {} : ADD with \"{}\" to \"{}\"", actualOne.getKey(), actualOne.getValue());
			diff.add(preparedIndexEntry(diffTypeBuilder, IndexAction.ADD, actualOne.getKey(), actualOne.getValue(), null, dic));
		}
	}

	/**
	 * @param action
	 * @param key
	 * @param currentPayload
	 * @param previousPayload
	 * @param dic
	 * @return
	 */
	private <T extends PreparedIndexEntry> T preparedIndexEntry(
			final Supplier<T> diffTypeBuilder,
			IndexAction action,
			String key,
			String currentPayload,
			String previousPayload,
			DictionaryEntry dic) {

		T entry = diffTypeBuilder.get();

		entry.setAction(action);
		entry.setKeyValue(key);

		// No need to save from / to payload : from is always already in index table !
		entry.setPayload(currentPayload);

		// But for human readability, need a custom display payload (not saved)
		entry.setHrPayload(getConverter().convertToHrPayload(currentPayload, previousPayload));

		// TODO : other source of timestamp ?
		entry.setTimestamp(System.currentTimeMillis());

		// Complete from dict
		entry.setDictionaryEntryUuid(dic.getUuid());

		return entry;
	}

	/**
	 * @param dict
	 * @param indexStartTime
	 * @param preparingMergeIndexToComplete
	 * @param mergeSource
	 */
	private void completeMergeIndexes(
			DictionaryEntry dict,
			List<? extends DiffLine> local,
			List<? extends DiffLine> mergeSource,
			Collection<PreparedMergeIndexEntry> preparingMergeIndexToComplete) {

		LOGGER.debug("Completing merge data from index for parameter table {}", dict.getTableName());

		Map<String, List<DiffLine>> minesByKey = local.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));
		Map<String, List<DiffLine>> theirsByKey = mergeSource.stream().collect(Collectors.groupingBy(DiffLine::getKeyValue));

		// Prepare "previous" if any for HR Payload
		Map<String, IndexEntry> previouses = this.indexes.findAllPreviousIndexEntries(dict,
				preparingMergeIndexToComplete.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()), null);

		if (MERGE_LOGGER.isDebugEnabled()) {
			MERGE_LOGGER.debug(
					"Beggin Merge resolution search for dict {} (table {}). Will process on {} previous, with {} \"mines\" and {} \"theirs\"",
					dict.getUuid(), dict.getTableName(), Integer.valueOf(previouses.size()), Integer.valueOf(minesByKey.size()),
					Integer.valueOf(theirsByKey.size()));
		}

		// For each merge result (Only one possible for each keyValue)
		preparingMergeIndexToComplete.stream().forEach(mergeEntry -> {

			// Adapt auto-resolution of entry
			mergeResolution(
					mergeEntry,
					previouses.get(mergeEntry.getKeyValue()),
					DiffLine.combinedOnSameTableAndKey(minesByKey.get(mergeEntry.getKeyValue())),
					DiffLine.combinedOnSameTableAndKey(theirsByKey.remove(mergeEntry.getKeyValue())));

			// Mark as a diff which need action
			mergeEntry.setNeedAction(true);

			LOGGER.debug("Completion on merge data for table {}, key \"{}\", \"HrPayload\"=\"{}\", \"mine\"={}, \"their\"={}",
					dict.getTableName(), mergeEntry.getKeyValue(), mergeEntry.getHrPayload(), mergeEntry.getMine(), mergeEntry.getTheir());
		});

		// Add references to remaining "theirs"
		theirsByKey.entrySet().stream().forEach(r -> {

			// Create "nothing to do" MergeEntry
			PreparedMergeIndexEntry merge = mergeAutoApplyTheir(previouses.get(r.getKey()),
					DiffLine.combinedOnSameTableAndKey(r.getValue()));

			// If not found in the diff : was already imported
			merge.setNeedAction(false);

			preparingMergeIndexToComplete.add(merge);
		});
	}

	/**
	 * Apply "their" immediately
	 * 
	 * @param localPrevious
	 * @param foundTheir
	 * @return
	 */
	private PreparedMergeIndexEntry mergeAutoApplyTheir(
			IndexEntry localPrevious,
			DiffLine foundTheir) {

		// Prepare payload for "their" regarding local previous (if any)
		String theirHrPayload = getConverter().convertToHrPayload(foundTheir.getPayload(),
				localPrevious != null ? localPrevious.getPayload() : null);

		// Corresponding details for the "their"
		PreparedIndexEntry theirEntry = PreparedIndexEntry.fromCombined(foundTheir, theirHrPayload);

		// Create "nothing to do" MergeEntry
		PreparedMergeIndexEntry their = PreparedMergeIndexEntry.fromExistingTheir(theirEntry);

		if (MERGE_LOGGER.isDebugEnabled()) {
			MERGE_LOGGER.debug(
					"Auto applied \"their\" on Entry {}, entry key {} (no resolution needed): localPrevious={}/{}, foundTheir={}/{}, preparedTheir={}/{}/{}",
					foundTheir.getDictionaryEntryUuid(), foundTheir.getKeyValue(),
					localPrevious != null ? localPrevious.getAction() : "?",
					localPrevious != null ? localPrevious.getPayload() : " - N/A - ",
					foundTheir.getAction(), foundTheir.getPayload(),
					their.getAction(), their.getPayload(), their.getHrPayload());
		}

		return their;
	}

	/**
	 * Process resolution of merge
	 * 
	 * @param mergeEntry
	 * @param localPrevious
	 * @param foundMine
	 * @param foundTheir
	 */
	private void mergeResolution(
			PreparedMergeIndexEntry mergeEntry,
			IndexEntry localPrevious,
			DiffLine foundMine,
			DiffLine foundTheir) {

		String previousPayload = localPrevious != null && !localPrevious.equals(foundMine) ? localPrevious.getPayload() : null;

		// Complete current entry HR payload for rendering
		String currentHrPayload = getConverter().convertToHrPayload(mergeEntry.getPayload(), previousPayload);
		mergeEntry.setHrPayload(currentHrPayload);

		// If mine is there, complete payload and add combined
		if (foundMine != null) {
			String mineHrPayload = getConverter().convertToHrPayload(foundMine.getPayload(), previousPayload);
			mergeEntry.setMine(PreparedIndexEntry.fromCombined(foundMine, mineHrPayload));
		}

		// Else copy proposition (case if current diff entry not yet commited)
		else {
			mergeEntry.setMine(PreparedIndexEntry.fromCombined(mergeEntry, currentHrPayload));
		}

		// If their is there, complete payload and add combined
		if (foundTheir != null) {
			String theirHrPayload = getConverter().convertToHrPayload(foundTheir.getPayload(), previousPayload);
			mergeEntry.setTheir(PreparedIndexEntry.fromCombined(foundTheir, theirHrPayload));

			// Case : their was modified after mine => Default resolution became "their"
			if (foundMine == null || foundMine.getTimestamp() < foundTheir.getTimestamp()) {
				String combinedHrPayload = localPrevious != null
						? getConverter().convertToHrPayload(foundTheir.getPayload(), localPrevious.getPayload()) : theirHrPayload;
				mergeEntry.applyResolution(foundTheir, combinedHrPayload);
			}

			// Default resolution is always their : can select individually
			else {
				mergeEntry.setHrPayload(getConverter().convertToHrPayload(foundTheir.getPayload(), foundMine.getPayload()));
				mergeEntry.setPayload(foundTheir.getPayload());
			}
		}

		if (MERGE_LOGGER.isDebugEnabled()) {
			MERGE_LOGGER.debug(
					"Merge Resolution on Entry {}, entry key {} (resolution needed) : previous={}/{}/{}, mine={}/{}/{}, their={}/{}/{}, mergedResolution={}/{}/{}",
					mergeEntry.getDictionaryEntryUuid(), mergeEntry.getKeyValue(),
					localPrevious != null ? localPrevious.getAction() : "?",
					localPrevious != null ? localPrevious.getPayload() : " - N/A - ", currentHrPayload,
					mergeEntry.getMine().getAction(), mergeEntry.getMine().getPayload(), mergeEntry.getMine().getHrPayload(),
					mergeEntry.getTheir().getAction(), mergeEntry.getTheir().getPayload(), mergeEntry.getTheir().getHrPayload(),
					mergeEntry.getAction(), mergeEntry.getPayload(), mergeEntry.getHrPayload());
		}

	}

	/**
	 * <p>
	 * Combines the diff which are similar as <tt>SimilarPreparedIndexEntry</tt> from
	 * already prepared list of <tt>PreparedIndexEntry</tt>. The resulting list contains
	 * the modified rendering
	 * </p>
	 * 
	 * @param readyToRender
	 *            List of <tt>PreparedIndexEntry</tt> for similar content search
	 * @return list to finally render
	 */
	private <T extends PreparedIndexEntry> List<T> combineSimilarDiffEntries(Collection<T> readyToRender,
			Function<Collection<T>, ? extends T> similarConvert) {

		List<T> listToRender = new ArrayList<>();

		// Combine by HR payload
		Map<String, List<T>> combineds = readyToRender.stream().collect(Collectors.groupingBy(p -> p.getHrPayload()));

		// Rendering display is based on combined
		combineds.values().stream().forEach(e -> {

			// Only one : not combined
			if (e.size() < this.maxSimilarBeforeCombined) {
				listToRender.addAll(e);
			}

			// Else a combined rendering
			else {
				listToRender.add(similarConvert.apply(e));
			}
		});

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Full combined result : ");
			listToRender.stream().forEach(i -> {
				if (i.isDisplayOnly()) {
					LOGGER.debug("[COMBINED] {} for {} : {}", i.getKeyValue(),
							((SimilarPreparedIndexEntry) i).getKeyValues().stream().collect(Collectors.joining(",")), i.getHrPayload());
				} else {
					LOGGER.debug("[DIFF-ENTRY] {} : {}", i.getKeyValue(), i.getHrPayload());
				}
			});
		}

		return listToRender;
	}
}