package fr.uem.efluid.model.repositories;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;

import javax.annotation.Nullable;

/**
 * <p>
 * Access to raw data of parameters : can read from Parameter source table or regenerate
 * from existing index
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface ManagedRegenerateRepository {

    /**
     * <p>
     * Produces the knew content for specified table, from recorded index
     * </p>
     * <p>
     * The knew content cannot be :
     *     <ul>
     *         	<li>null value for existing key</li>
     *     		<li>Empty string value for existing key</li>
     *     </ul>
     * </p>
     */
    Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry);

    /**
     * <p>
     * Produces the knew content for specified table, from sorted recorded index on parameter
     * until specified ldt, then with specified index (adapted for merge process)
     * </p>
     *
     * @param specifiedIndex      found index stream access. Sorted. The process will keep the index order
     * @param eachLineAccumulator an optional accumulator processing each line of specified index, to allow to combine some processes in one stream iteration
     */
    <D extends DiffLine> Map<String, String> regenerateKnewContent(
            /* sorted */final Stream<D> specifiedIndex,
            /* nullable */ final Consumer<D> eachLineAccumulator);

    /**
     * Refresh cached data if any
     */
    void refreshAll();
}
