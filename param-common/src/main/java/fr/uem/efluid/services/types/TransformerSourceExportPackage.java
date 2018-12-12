package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareProject;
import fr.uem.efluid.model.shared.ExportAwareTransformerSource;

/**
 * <p>
 * Export / import impackage for transformer-sources
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public abstract class TransformerSourceExportPackage<D extends ExportAwareTransformerSource<? extends ExportAwareProject>>
		extends SharedPackage<D> {

	public static final String TRANSFORMER_SOURCES_EXPORT = "full-transformer-sources";
	public static final String PARTIAL_TRANSFORMER_SOURCES_EXPORT = "partial-transformer-sources";

	/**
	 * @param name
	 * @param exportDate
	 */
	public TransformerSourceExportPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#getVersion()
	 */
	@Override
	public final String getVersion() {
		return "1";
	}

}
