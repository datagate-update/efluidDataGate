package fr.uem.efluid.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.Value;

/**
 * <p>
 * A <tt>ValueApplyTransformer</tt> where multiple table / columns have value to
 * transform. Define a transformation on value with
 * {@link #transformCompliantValue(DictionaryEntry, Value)} and then record the values to
 * transform with {@link #addSupportOn(String, String)}
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public abstract class SwitchedValueApplyTransformer extends ValueApplyTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SwitchedValueApplyTransformer.class);

	public static final String ALL = "*";

	protected Map<String, Set<String>> valueApplies = new HashMap<>();

	protected Set<String> tableApplies = new HashSet<>();

	/**
	 * @param tableName
	 * @param valueName
	 */
	public void addSupportOn(String tableName, String valueName) {

		this.tableApplies.add(tableName);

		// No details on value apply = apply on all
		if (valueName.equals(ALL)) {
			this.valueApplies.remove(tableName);
			LOGGER.debug("Add transformer support for table {}, and all values", tableName);
		}

		else {
			Set<String> valueAppliesForTable = this.valueApplies.get(tableName);

			if (valueAppliesForTable == null) {
				valueAppliesForTable = new HashSet<>();
				this.valueApplies.put(tableName, valueAppliesForTable);
			}

			valueAppliesForTable.add(valueName);
			LOGGER.debug("Add transformer support for table {}, for value on column {}", tableName, valueName);
		}
	}

	/**
	 * @param tableName
	 * @param valueNames
	 */
	public void addSupportOn(String tableName, Collection<String> valueNames) {
		valueNames.forEach(v -> addSupportOn(tableName, v));
	}

	/**
	 * @param dict
	 * @param value
	 * @return
	 * @see fr.uem.efluid.tools.ValueApplyTransformer#transformValue(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      fr.uem.efluid.services.types.Value)
	 */
	@Override
	public String transformValue(DictionaryEntry dict, Value value) {

		if (isApplyOnDictionaryEntry(dict)) {

			Set<String> valueAppliesForTable = this.valueApplies.get(dict.getTableName());

			// If null = apply on all, else must have value name
			if (valueAppliesForTable == null || valueAppliesForTable.contains(value.getName())) {
				LOGGER.debug("Transformer can apply for table {} and value {}", dict.getTableName(), value.getName());
				return transformCompliantValue(dict, value);
			}
		}

		return value.getValueAsString();
	}

	/**
	 * <p>
	 * Get transformed value when compliant. No need to check if value need transform :
	 * already done at SwitchedValueApplyTransformer level
	 * </p>
	 * 
	 * @param dict
	 * @param value
	 * @return
	 */
	abstract protected String transformCompliantValue(DictionaryEntry dict, Value value);

	@Override
	public boolean isApplyOnDictionaryEntry(DictionaryEntry dict) {
		return this.tableApplies.contains(dict.getTableName());
	}
}
