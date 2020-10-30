package fr.uem.efluid.model.repositories;

import com.google.common.collect.Lists;
import com.hazelcast.internal.jmx.ManagedDescription;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import org.hibernate.mapping.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Core index data provider, using JPA
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public interface IndexRepository extends JpaRepository<IndexEntry, Long>, JpaSpecificationExecutor<IndexEntry> {

    /**
     * For tests env reset only !
     */
    @Query(value = "DELETE FROM INDEXES", nativeQuery = true)
    @Modifying
    void dropAll();

    /**
     * <p>
     * Search for existing commit indexes
     * </p>
     */
    List<IndexEntry> findByCommitUuid(UUID commitUuid);

    /**
     * All index lines without "previous" value for a specified commit
     *
     * @param commitUuid selected commit
     * @return index to upgrade
     */
    @Query(value = "select i.* "
            + "from INDEXES i "
            + "where i.COMMIT_UUID = :commitUuid "
            + "AND i.ACTION != 'ADD' AND i.PREVIOUS is null", nativeQuery = true)
    List<IndexEntry> findWithUpgradablePreviosByCommitUuid(@Param("commitUuid") String commitUuid);

    /**
     * <p>
     * Search for existing commit indexes
     * </p>
     */
    long countByCommitUuid(UUID commitUuid);

    @Query(value = "SELECT MAX(i.timestamp) FROM INDEXES i " +
            "INNER JOIN COMMITS c on c.UUID = i.COMMIT_UUID " +
            "WHERE C.IMPORTED_TIME = (SELECT MAX(IMPORTED_TIME) FROM COMMITS c)", nativeQuery = true)
    Long findMaxIndexTimestampOfLastImportedCommit();

    /**
     * <p>
     * Load full index detail for one DictionaryEntry (= on managed table)
     * </p>
     */
    Stream<IndexEntry> findByDictionaryEntryOrderByTimestampAsc(DictionaryEntry dictionaryEntry);

    Stream<IndexEntry> findByDictionaryEntryAndTimestampLessThanEqualOrderByTimestampAsc(DictionaryEntry dictionaryEntry, long timestamp);

    @Query(value = "SELECT i.* FROM INDEXES i " +
            "INNER JOIN (SELECT MAX(TIMESTAMP) AS TS, KEY_VALUE FROM INDEXES WHERE DICTIONARY_ENTRY_UUID = :dictUuid AND TIMESTAMP <= :pivot GROUP BY KEY_VALUE) ii ON i.TIMESTAMP = ii.TS AND i.KEY_VALUE = ii.KEY_VALUE " +
            "WHERE i.DICTIONARY_ENTRY_UUID = :dictUuid", nativeQuery = true)
    Stream<ProjectedIndexEntry> findAccumulableRegeneratedContentForDictionaryEntry(@Param("dictUuid") UUID dictionaryEntryUuid, @Param("pivot") long timestamp);

    @Query(value = "SELECT i.KEY_VALUE, i.PAYLOAD FROM INDEXES i " +
            "INNER JOIN (SELECT MAX(TIMESTAMP) AS TS, KEY_VALUE FROM INDEXES WHERE DICTIONARY_ENTRY_UUID = :dictUuid GROUP BY KEY_VALUE) ii ON i.TIMESTAMP = ii.TS AND i.KEY_VALUE = ii.KEY_VALUE " +
            "WHERE i.DICTIONARY_ENTRY_UUID = :dictUuid AND i.ACTION != 'REMOVE'", nativeQuery = true)
    Stream<Object[]> _internal_findRegeneratedContentForDictionaryEntry(@Param("dictUuid") UUID dictionaryEntryUuid);

    /**
     * For database based regenerate process on diff
     *
     * @param dictionaryEntryUuid
     * @return regenerated content for a dictionary Entry, with minimal steps
     */
    default Map<String, String> findRegeneratedContentForDictionaryEntry(UUID dictionaryEntryUuid) {
        Map<String, String> result = new HashMap<>(10000);
        _internal_findRegeneratedContentForDictionaryEntry(dictionaryEntryUuid).forEach(t -> result.put((String) t[0], (String) t[1]));
        return result;
    }

    /**
     * <b><font color="red">Query for internal use only</font></b>
     */
    @Query(value = "select i.* "
            + "from indexes i "
            + "inner join ("
            + "	select max(ii.id) as max_id, ii.key_value from indexes ii where ii.dictionary_entry_uuid = :uuid and ii.id not in (:excludeIds) group by ii.key_value"
            + ") mi on i.id = mi.max_id "
            + "where i.key_value in (:keys)", nativeQuery = true)
    List<IndexEntry> _internal_findAllPreviousIndexEntries(
            @Param("uuid") String dictionaryEntryUuid,
            @Param("keys") List<String> keyValues,
            @Param("excludeIds") List<Long> excludeIds);

    /**
     * Search previous entries, with support for large data volumes, ignoring existing entries
     *
     * @param dictionaryEntry current DictionaryEntry
     * @param index           current index which previous entries are required
     * @return previous index content, for HR generate
     */
    default Map<String, IndexEntry> findAllPreviousIndexEntriesExcludingExisting(
            DictionaryEntry dictionaryEntry,
            List<IndexEntry> index) {

        // Do not attempt to select with an empty "in"
        if (index == null || index.isEmpty()) {
            return new HashMap<>();
        }

        // If less than 1000 items, direct call
        if (index.size() < 1000) {
            return _internal_findAllPreviousIndexEntries(
                    dictionaryEntry.getUuid().toString(),
                    index.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()),
                    index.stream().map(IndexEntry::getId).collect(Collectors.toList())).stream()
                    .collect(Collectors.toMap(IndexEntry::getKeyValue, v -> v));
        }

        // Else need to split list partitions
        Map<String, IndexEntry> result = new HashMap<>();

        Lists.partition(index, 999).forEach(
                i -> result.putAll(_internal_findAllPreviousIndexEntries(
                        dictionaryEntry.getUuid().toString(),
                        i.stream().map(DiffLine::getKeyValue).collect(Collectors.toList()),
                        i.stream().map(IndexEntry::getId).collect(Collectors.toList())).stream()
                        .collect(Collectors.toMap(IndexEntry::getKeyValue, v -> v))));

        return result;
    }

    /**
     * A model compliant with IndexEntry, but for pur projection use
     */
    class ProjectedIndexEntry implements DiffLine {

        private final UUID dictionaryEntryUuid;
        private final IndexAction action;
        private final long timestamp;
        private final String previous;
        private final String keyValue;
        private final String payload;

        public ProjectedIndexEntry(UUID dictionaryEntryUuid, IndexAction action, long timestamp, String previous, String keyValue, String payload) {
            this.dictionaryEntryUuid = dictionaryEntryUuid;
            this.action = action;
            this.timestamp = timestamp;
            this.previous = previous;
            this.keyValue = keyValue;
            this.payload = payload;
        }

        @Override
        public UUID getDictionaryEntryUuid() {
            return dictionaryEntryUuid;
        }

        @Override
        public IndexAction getAction() {
            return action;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String getPrevious() {
            return previous;
        }

        @Override
        public String getKeyValue() {
            return keyValue;
        }

        @Override
        public String getPayload() {
            return payload;
        }
    }
}
