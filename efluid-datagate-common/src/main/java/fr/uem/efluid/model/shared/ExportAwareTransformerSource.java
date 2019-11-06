package fr.uem.efluid.model.shared;

import java.time.LocalDateTime;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * Export Aware properties for a transformer source. The transformer source is associated
 * to the dictionary content, but it is linked directly to the <code>project</code>
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public abstract class ExportAwareTransformerSource<D extends ExportAwareProject> implements Shared {

	/**
	 * @return
	 */
	public abstract String getTableName();

	/**
	 * @return
	 */
	public abstract String getTransformerClassName();

	/**
	 * @return
	 */
	public abstract String getTransformerConfig();

	/**
	 * @return associated project
	 */
	public abstract D getProject();

	/**
	 * @param createdTime
	 *            the createdTime to set
	 */
	public abstract LocalDateTime getCreatedTime();

	/**
	 * @param updatedTime
	 *            the updatedTime to set
	 */
	public abstract LocalDateTime getUpdatedTime();

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public final String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("uid", getUuid())
				.with("cre", getCreatedTime())
				.with("upd", getUpdatedTime())
				.with("tab", getTableName())
				.with("cla", getTransformerClassName())
				.withB64("cfg", getTransformerConfig()) // B64 for fixed content
				.with("pro", getProject().getUuid())
				.toString();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExportAwareTransformerSource<?> other = (ExportAwareTransformerSource<?>) obj;
		if (this.getUuid() == null) {
			if (other.getUuid() != null)
				return false;
		} else if (!getUuid().equals(other.getUuid()))
			return false;
		return true;
	}
}