package fr.uem.efluid.tools;

import java.util.List;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TransformerSetEntry;
import fr.uem.efluid.model.entities.TransformerSource;
import fr.uem.efluid.services.types.Value;

/**
 * <p>
 * A <tt>SourcedTransformer</tt> instantiated at first use, which reuse then the provided
 * source, config, and source extract for faster processing
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public abstract class PreloadedSourcedTransformer implements SourcedTransformer {

	private boolean initialized = false;

	private TransformerSource currentSource;

	private List<TransformerSetEntry> entries;

	/**
	 * @param dic
	 * @param payloadValues
	 * @param source
	 * @return
	 * @see fr.uem.efluid.tools.SourcedTransformer#extractRequiredSourceEntries(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      java.util.List, fr.uem.efluid.model.entities.TransformerSource)
	 */
	@Override
	public final List<TransformerSetEntry> extractRequiredSourceEntries(DictionaryEntry dic, List<Value> payloadValues,
			TransformerSource source) {

		if (!this.initialized) {
			applyConfiguration(true, source);
			this.currentSource = source;
			this.initialized = true;
		}

		return extractRequiredSourceEntries(dic, payloadValues);
	}

	/**
	 * @param dict
	 * @param payload
	 * @param source
	 * @param extractedSource
	 * @return
	 * @see fr.uem.efluid.tools.SourcedTransformer#transformPayload(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      java.lang.String, fr.uem.efluid.model.entities.TransformerSource,
	 *      java.util.List)
	 */
	@Override
	public final String transformPayload(DictionaryEntry dict, String payload, TransformerSource source,
			List<TransformerSetEntry> extractedSource) {

		if (!this.initialized) {
			applyConfiguration(false, source);
			this.currentSource = source;
			this.initialized = true;
		}

		return transformPayload(dict, payload);
	}

	/**
	 * @return
	 */
	protected TransformerSource getSource() {
		return this.currentSource;
	}

	/**
	 * @return
	 */
	protected List<TransformerSetEntry> getEntries() {
		return this.entries;
	}

	/**
	 * @param source
	 */
	protected abstract void applyConfiguration(boolean forExtraction, TransformerSource source);

	/**
	 * @param dict
	 * @param value
	 * @return
	 */
	protected abstract String transformPayload(DictionaryEntry dict, String payload);

	/**
	 * @param dic
	 * @param payloadValues
	 * @return
	 */
	protected abstract List<TransformerSetEntry> extractRequiredSourceEntries(DictionaryEntry dic, List<Value> payloadValues);
}
