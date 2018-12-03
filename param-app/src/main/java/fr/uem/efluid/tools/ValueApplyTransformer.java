package fr.uem.efluid.tools;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.Value;

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
public abstract class ValueApplyTransformer implements ApplyTransformer {

	@Autowired
	private ManagedValueConverter converter;

	@Override
	public String transformPayload(DictionaryEntry dict, String payload) {

		// Expand payload and transform values
		return this.converter.convertToExtractedValue(
				this.converter.expandInternalValue(payload).stream()
						.collect(Collectors.toMap(v -> v.getName(),
								v -> transformValue(dict, v), // Transform value
								(a, b) -> a,
								() -> new LinkedHashMap<>())));
	}

	/**
	 * @param dict
	 * @param value
	 * @return
	 */
	abstract String transformValue(DictionaryEntry dict, Value value);

}
