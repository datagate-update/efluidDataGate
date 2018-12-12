package fr.uem.efluid.tools;

import java.util.List;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TransformerSetEntry;
import fr.uem.efluid.model.entities.TransformerSource;
import fr.uem.efluid.services.types.Value;

/**
 * <p>
 * A model of transformer where the applied data can be specified from
 * <tt>TransformerSource</tt>, and which can select the source content to associate to a
 * commit and then export with the commit data
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public interface SourcedTransformer extends Transformer {

	/**
	 * <p>
	 * For a given dictionary entry / payload (extracted as list of <tt>Value</tt>), get
	 * the corresponding entries from given <tt>TransformerSource</tt> which may be
	 * 
	 * @param dic
	 * @param payloadValues
	 * @return
	 */
	List<TransformerSetEntry> extractRequiredSourceEntries(DictionaryEntry dic, List<Value> payloadValues, TransformerSource source);

	/**
	 * @param dict
	 * @param payload
	 * @param source
	 * @param extractedSource
	 * @return
	 */
	String transformPayload(DictionaryEntry dict, String payload, TransformerSource source, List<TransformerSetEntry> extractedSource);
}
