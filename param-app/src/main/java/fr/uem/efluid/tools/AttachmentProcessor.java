package fr.uem.efluid.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.uem.efluid.model.entities.AttachmentType;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.utils.FormatUtils;

/**
 * <p>
 * For processing of attachment files : tools for edit display, execute display and
 * execute process. Depends on associated type
 * </p>
 * 
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public abstract class AttachmentProcessor {

	/**
	 * 
	 */
	protected AttachmentProcessor() {
		super();
	}

	/**
	 * @param att
	 * @return
	 */
	protected String formatForDisplay(Compliant att) {
		return FormatUtils.toString(att.getData());
	}

	/**
	 * @param att
	 * @return
	 */
	public byte[] display(Compliant att) {
		return FormatUtils.toBytes(formatForDisplay(att));
	}

	/**
	 * @param att
	 */
	@SuppressWarnings("unused")
	public void execute(User user, Compliant att) {
		// Default does nothing
	}

	/**
	 * <p>
	 * Entry point for easy access to supported AttachmentProcessors
	 * </p>
	 * 
	 * @author elecomte
	 * @since v2.0.0
	 * @version 1
	 */
	public static class Provider {

		private static final AttachmentProcessor DEFAULT = new AttachmentProcessor() {
			// Keep standard
		};

		private final boolean displaySupport;

		private final Map<AttachmentType, AttachmentProcessor> byType = new HashMap<>();

		/**
		 * @param displaySupport
		 */
		public Provider(boolean displaySupport) {
			super();
			this.displaySupport = displaySupport;
		}

		/**
		 * <p>
		 * Provides a default one if none found
		 * </p>
		 * 
		 * @param compliant
		 * @return
		 */
		public byte[] display(Compliant compliant) {
			return getFor(compliant).display(compliant);
		}

		/**
		 * <p>
		 * Provides a default one if none found
		 * </p>
		 * 
		 * @param compliant
		 * @return
		 */
		public AttachmentProcessor getFor(Compliant compliant) {
			AttachmentProcessor spec = this.byType.get(compliant.getType());
			return spec == null ? DEFAULT : spec;
		}

		/**
		 * @return the displaySupport
		 */
		public boolean isDisplaySupport() {
			return this.displaySupport;
		}

		/**
		 * @param proc
		 * @param type
		 */
		public void addProcessor(AttachmentProcessor proc, AttachmentType type) {
			this.byType.put(type, proc);
		}
	}

	/**
	 * <p>
	 * Common model for attachment processing input
	 * </p>
	 * 
	 * @author elecomte
	 * @since v2.0.0
	 * @version 1
	 */
	public static interface Compliant {

		UUID getUuid();

		String getName();

		byte[] getData();

		AttachmentType getType();
	}
}
