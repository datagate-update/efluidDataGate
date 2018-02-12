package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.services.types.CommitEditData;
import fr.uem.efluid.services.types.DiffDisplay;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.LocalPreparedDiff;
import fr.uem.efluid.services.types.MergePreparedDiff;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.utils.TechnicalException;

/**
 * <p>
 * Service for Commit preparation, using async execution. <b>Only one execution can be
 * launched (defined as a <tt>PilotedCommitPreparation</tt>)</b>. Commit prepared this way
 * can be of different type (for "local commit" or for "merge commit" after import when a
 * diff exists).
 * </p>
 * <p>
 * Everything associated to commit preparation implies very heavy and very memory
 * consuming processes : that's why this service can only manage ONE preparation at the
 * time (whatever is the kind of the preparation). All preparation are asynchronous : its
 * holder entity <tt>PilotableCommitPreparation</tt> gives details on the status of it.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
public class PilotableCommitPreparationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PilotableCommitPreparationService.class);

	@Autowired
	private PrepareDiffService diffService;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private CommitService commitService;

	// TODO : use cfg entry.
	private ExecutorService executor = Executors.newFixedThreadPool(4);

	// One active only
	private PilotedCommitPreparation<?> current;

	/**
	 * Start async diff analysis before commit
	 */
	public PilotedCommitPreparation<?> startLocalCommitPreparation(boolean force) {

		// On existing preparation
		if (this.current != null) {

			// Forced restart asked : close current, start a new one
			if (force) {
				LOGGER.info("Request for a new commit preparation - preparation exist already, and "
						+ "force restart asked, so will drop current preparation {}", this.current.getIdentifier());

				// Cancel for any existing reference ...
				this.current.setStatus(PilotedCommitStatus.CANCEL);

				// ... but droping should be enough
				this.current = null;
			}

			// Keep current else
			else {

				LOGGER.info("Request for a new commit preparation - preparation already running / "
						+ "available, so use existing {}", this.current.getIdentifier());

				// Default will provides existing if still running
				return getCurrentCommitPreparation();
			}
		}

		LOGGER.info("Request for a new commit preparation - start a new one");

		// For CommitState LOCAL => Use PreparedIndexEntry
		PilotedCommitPreparation<LocalPreparedDiff> preparation = new PilotedCommitPreparation<>(CommitState.LOCAL);

		// Specify as active one
		this.current = preparation;

		CompletableFuture.runAsync(() -> processAllDiff(preparation));

		return getCurrentCommitPreparation();
	}

	/**
	 * Start async diff analysis before commit
	 */
	public ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> startMergeCommitPreparation(ExportImportFile file) {

		// On existing preparation
		if (this.current != null) {

			// Impossible situation
			LOGGER.error("Cannot proced to import entry point for processing merge commit will a preparation is still running.");
			throw new TechnicalException("Cannot proced to import entry point for processing merge commit will a "
					+ "preparation is still running.");
		}

		LOGGER.info("Request for a new merge commit preparation from an import - start a new one");

		// For CommitState MERGE => Use PreparedMergeIndexEntry (completed in != steps)
		PilotedCommitPreparation<MergePreparedDiff> preparation = new PilotedCommitPreparation<>(CommitState.MERGED);

		// First step is NOT async : load the package and identify the appliable index
		ExportImportResult<PilotedCommitPreparation<MergePreparedDiff>> importResult = this.commitService.importCommits(file,
				preparation);

		// Specify as active one
		this.current = preparation;

		CompletableFuture.runAsync(() -> processAllMergeDiff(preparation));

		return importResult;
	}

	/**
	 * @return
	 */
	public PilotedCommitPreparation<?> getCurrentCommitPreparation() {
		return this.current;
	}

	/**
	 * 
	 */
	public void completeCommitPreparation() {
		this.current = null;
	}

	/**
	 * <p>
	 * Asynchronous task which is itself a process of asynchronous execution of managed
	 * table diffs (one task for each managed table). Similar to a "git status"
	 * </p>
	 * <p>
	 * Use parallele processes, but not asyncronous by itself : can be launched as a
	 * CompletableFuture in call processes
	 * </p>
	 */
	private void processAllDiff(PilotedCommitPreparation<LocalPreparedDiff> preparation) {

		LOGGER.info("Begin diff process on commit preparation {}", this.current.getIdentifier());
		long startTimeout = System.currentTimeMillis();

		try {
			List<LocalPreparedDiff> fullDiff = this.executor
					.invokeAll(this.dictionary.findAll().stream().map(this::callDiff).collect(Collectors.toList())).stream()
					.map(this::gatherResult)
					.sorted()
					.collect(Collectors.toList());

			// Keep in preparation for commit build
			preparation.setPreparedContent(fullDiff);

			// Mark preparation as completed
			preparation.setEnd(LocalDateTime.now());
			preparation.setStatus(PilotedCommitStatus.COMMIT_CAN_PREPARE);

			LOGGER.info("Diff process completed on commit preparation {}. Found {} index entries. Total process duration was {} ms",
					this.current.getIdentifier(),
					Integer.valueOf(fullDiff.size()),
					Long.valueOf(System.currentTimeMillis() - startTimeout));

		} catch (InterruptedException e) {
			LOGGER.error("Error will processing diff", e);
			preparation.setErrorDuringPreparation(e);
		}
	}

	/**
	 * <p>
	 * Asynchronous task which is itself a process of asynchronous execution of managed
	 * table diffs (one task for each managed table). Similar to a "git status"
	 * </p>
	 * <p>
	 * Use parallele processes, but not asyncronous by itself : can be launched as a
	 * CompletableFuture in call processes
	 * </p>
	 */
	private void processAllMergeDiff(PilotedCommitPreparation<MergePreparedDiff> preparation) {

		LOGGER.info("Begin diff process on merge-commit preparation {}", this.current.getIdentifier());
		long startTimeout = System.currentTimeMillis();

		Map<UUID, DictionaryEntry> dictByUuid = this.dictionary.findAll().stream()
				.collect(Collectors.toMap(DictionaryEntry::getUuid, d -> d));

		long searchTimestamp = preparation.getCommitData().getCreatedTime().atZone(ZoneId.systemDefault()).toEpochSecond();

		try {
			List<MergePreparedDiff> fullDiff = this.executor
					.invokeAll(preparation.getPreparedContent().stream()
							.map(p -> callMergeDiff(dictByUuid.get(p.getDictionaryEntryUuid()), searchTimestamp, p))
							.collect(Collectors.toList()))
					.stream()
					.map(this::gatherResult)
					.sorted()
					.collect(Collectors.toList());

			// Keep in preparation for commit build, once completed
			preparation.setPreparedContent(fullDiff);

			// Mark preparation as completed
			preparation.setEnd(LocalDateTime.now());
			preparation.setStatus(PilotedCommitStatus.COMMIT_CAN_PREPARE);

			LOGGER.info("Diff process completed on commit preparation {}. Found {} index entries. Total process duration was {} ms",
					this.current.getIdentifier(),
					Integer.valueOf(fullDiff.size()),
					Long.valueOf(System.currentTimeMillis() - startTimeout));

		} catch (InterruptedException e) {
			LOGGER.error("Error will processing diff", e);
			preparation.setErrorDuringPreparation(e);
		}
	}

	/**
	 * <p>
	 * Join future execution and gather exception if any
	 * </p>
	 * 
	 * @param future
	 * @return
	 */
	private <T> T gatherResult(Future<T> future) {
		try {
			return future.get();
		}

		// TODO : better error identification
		catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Error will processing diff", e);
			this.current.setErrorDuringPreparation(e);
			throw new TechnicalException("Aborted on exception ", e);
		}
	}

	/**
	 * <p>
	 * Execution for one table, as a <tt>Callable</tt>, for a basic local diff.
	 * </p>
	 * 
	 * @param dict
	 * @return
	 */
	private Callable<LocalPreparedDiff> callDiff(DictionaryEntry dict) {

		return () -> {
			LocalPreparedDiff tableDiff = LocalPreparedDiff.initFromDictionaryEntry(dict);
			tableDiff.setDiff(this.diffService.currentContentDiff(dict).stream()
					.sorted(Comparator.comparing(PreparedIndexEntry::getKeyValue)).collect(Collectors.toList()));
			return tableDiff;
		};
	}

	/**
	 * <p>
	 * Execution for one table, as a <tt>Callable</tt>, for a merge process diff. The list
	 * of diff in <tt>MergePreparedDiff</tt> is regenerated, and DictionaryEntry data are
	 * completed.
	 * 
	 * @param dict
	 * @param lastLocalCommitTimestamp
	 * @param correspondingDiff
	 * @return
	 */
	private Callable<MergePreparedDiff> callMergeDiff(
			DictionaryEntry dict,
			long lastLocalCommitTimestamp,
			MergePreparedDiff correspondingDiff) {

		return () -> {
			// Complete dictionary entry
			correspondingDiff.completeFromEntity(dict);

			// Then run one merge action for dictionaryEntry
			correspondingDiff.setDiff(this.diffService.mergeIndexDiff(dict, lastLocalCommitTimestamp, correspondingDiff.getDiff()).stream()
					.sorted(Comparator.comparing(PreparedIndexEntry::getKeyValue)).collect(Collectors.toList()));

			// For chained process
			return correspondingDiff;
		};
	}

	/**
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static enum PilotedCommitStatus {

		NOT_LAUNCHED,
		DIFF_RUNNING,
		CANNOT_PREPARE,
		CANCEL,
		COMMIT_CAN_PREPARE,
		COMMIT_PREPARED;

	}

	/**
	 * <p>
	 * A <tt>PilotedCommitPreparation</tt> is a major load event associated to a
	 * preparation of index or index related data. Their is only ONE preparation of any
	 * kind which is available in the application, due to memory use and data extraction
	 * heavy load. But this preparation can be of various type.
	 * </p>
	 * <p>
	 * Common rules for a preparation :
	 * <ul>
	 * <li>Used to prepare a commit of a fixed {@link CommitState}</li>
	 * <li>Identified by uuid, but not exported. (currently not realy used)</li>
	 * <li>Identified with start and end time of preparation</li>
	 * <li>Associated to an evolving status : defines how far we are in the preparation.
	 * Can evolve to include a full "% remaining" process</li>
	 * <li>Holds a content, the "result" of the preparation. Supposed to be related to
	 * <tt>DiffLine</tt> (but type is free in this vearsion)</li>
	 * <li>Associated to a commit definition which will embbed the result of the
	 * preparation once completed and validated.</li>
	 * </ul>
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 * @param <T>
	 */
	public static final class PilotedCommitPreparation<T extends DiffDisplay<?>> {

		private final UUID identifier;

		private final LocalDateTime start;

		private final CommitState preparingState;

		private LocalDateTime end;

		private PilotedCommitStatus status;

		private String errorKey;

		private Throwable errorDuringPreparation;

		private List<T> preparedContent;

		private CommitEditData commitData;

		/**
		 * 
		 */
		protected PilotedCommitPreparation(CommitState preparingState) {
			this.identifier = UUID.randomUUID();
			this.status = PilotedCommitStatus.DIFF_RUNNING;
			this.start = LocalDateTime.now();
			this.preparingState = preparingState;
		}

		/**
		 * @return the errorDuringPreparation
		 */
		public Throwable getErrorDuringPreparation() {
			return this.errorDuringPreparation;
		}

		/**
		 * @param errorDuringPreparation
		 *            the errorDuringPreparation to set
		 */
		public void setErrorDuringPreparation(Throwable errorDuringPreparation) {
			this.errorDuringPreparation = errorDuringPreparation;
		}

		/**
		 * @return the status
		 */
		public PilotedCommitStatus getStatus() {
			return this.status;
		}

		/**
		 * @param status
		 *            the status to set
		 */
		public void setStatus(PilotedCommitStatus status) {
			this.status = status;
		}

		/**
		 * @return the identifier
		 */
		public UUID getIdentifier() {
			return this.identifier;
		}

		/**
		 * @return the start
		 */
		public LocalDateTime getStart() {
			return this.start;
		}

		/**
		 * @return the end
		 */
		public LocalDateTime getEnd() {
			return this.end;
		}

		/**
		 * @return the errorKey
		 */
		public String getErrorKey() {
			return this.errorKey;
		}

		/**
		 * @param errorKey
		 *            the errorKey to set
		 */
		public void setErrorKey(String errorKey) {
			this.errorKey = errorKey;
		}

		/**
		 * @return the preparedContent
		 */
		public List<T> getPreparedContent() {
			return this.preparedContent;
		}

		/**
		 * @param preparedContent
		 *            the preparedContent to set
		 */
		public void setPreparedContent(List<T> preparedContent) {
			this.preparedContent = preparedContent;
		}

		/**
		 * @return the commitData
		 */
		public CommitEditData getCommitData() {
			return this.commitData;
		}

		/**
		 * @param commitData
		 *            the commitData to set
		 */
		public void setCommitData(CommitEditData commitData) {
			this.commitData = commitData;
		}

		/**
		 * @param end
		 *            the end to set
		 */
		public void setEnd(LocalDateTime end) {
			this.end = end;
		}

		/**
		 * @return the preparingState
		 */
		public CommitState getPreparingState() {
			return this.preparingState;
		}

	}
}
