package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareTableLink;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class TableLinkExportPackage<D extends ExportAwareTableLink<?>> extends SharedPackage<D> {

	public static final String LINKS_EXPORT = "full-links";
	public static final String PARTIAL_LINKS_EXPORT = "partial-links";

	/**
	 * @param name
	 * @param exportDate
	 */
	public TableLinkExportPackage(String name, LocalDateTime exportDate) {
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