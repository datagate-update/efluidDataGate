package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * <p>
 * When the parameter value need to be udpated regarding a substitution rule when applied
 * after a merge insert, a customer process can be used to define and validate the new
 * value to apply. This transformer can be specified for any "destination" application
 * instance, and will be then used in every merge process, when processing the value to
 * merge.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public interface ApplyTransformer {

	/**
	 * @param dict
	 * @param value
	 * @return
	 */
	String transformPayload(DictionaryEntry dict, String payload);

	/**
	 * @param dict
	 * @return
	 */
	boolean isApplyOnDictionaryEntry(DictionaryEntry dict);
}
