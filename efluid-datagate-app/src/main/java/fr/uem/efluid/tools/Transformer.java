package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public interface Transformer {

	/**
	 * @param dict
	 * @return
	 */
	boolean isApplyOnDictionaryEntry(DictionaryEntry dict);
}
