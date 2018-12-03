package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.Value;

/**
 * <p>
 * Example which does a simple uppercase on a defined table / column
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class UpperCaseValueApplyTransformer extends ValueApplyTransformer {

	private final String tablename;
	private final String colname;

	public UpperCaseValueApplyTransformer(String tablename, String colname) {
		super();
		this.tablename = tablename;
		this.colname = colname;
	}

	/**
	 * @param dict
	 * @param value
	 * @return
	 * @see fr.uem.efluid.tools.ValueApplyTransformer#transformValue(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      fr.uem.efluid.services.types.Value)
	 */

	public String transformValue(DictionaryEntry dict, Value value) {

		if (this.tablename.equals(dict.getTableName()) && this.colname.equals(value.getName())) {
			return value.getValueAsString().toUpperCase();
		}

		return value.getValueAsString();
	}

	@Override
	public boolean isApplyOnDictionaryEntry(DictionaryEntry dict) {
		return this.tablename.equals(dict.getTableName());
	}

}
