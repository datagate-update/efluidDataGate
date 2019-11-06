package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.uem.efluid.model.shared.ExportAwareTransformerSource;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * Dictionary element where the substitution values can be extracted for transformer
 * process
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
@Entity
@Table(name = "transformer_sources")
public class TransformerSource extends ExportAwareTransformerSource<Project> {

	@Id
	private UUID uuid;

	@NotNull
	private String tableName;

	@NotNull
	private String transformerClassName;

	private String transformerConfig;

	@ManyToOne(optional = false)
	private Project project;

	@NotNull
	private LocalDateTime createdTime;

	private LocalDateTime updatedTime;

	private LocalDateTime importedTime;

	/**
	 * 
	 */
	public TransformerSource() {
		super();
	}

	/**
	 * @param uuid
	 */
	public TransformerSource(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#getUuid()
	 */
	@Override
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @param uuid
	 */
	/**
	 * @param uuid
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.shared.ExportAwareTransformerSource#getTableName()
	 */
	@Override
	public String getTableName() {
		return this.tableName;
	}

	/**
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.shared.ExportAwareTransformerSource#getTransformerClassName()
	 */
	@Override
	public String getTransformerClassName() {
		return this.transformerClassName;
	}

	/**
	 * @param transformerClassName
	 */
	public void setTransformerClassName(String transformerClassName) {
		this.transformerClassName = transformerClassName;
	}

	/**
	 * @return the transformerConfig
	 */
	@Override
	public String getTransformerConfig() {
		return this.transformerConfig;
	}

	/**
	 * @param transformerConfig
	 *            the transformerConfig to set
	 */
	public void setTransformerConfig(String transformerConfig) {
		this.transformerConfig = transformerConfig;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.shared.ExportAwareTransformerSource#getCreatedTime()
	 */
	@Override
	public LocalDateTime getCreatedTime() {
		return this.createdTime;
	}

	/**
	 * @param createdTime
	 */
	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#getImportedTime()
	 */
	@Override
	public LocalDateTime getImportedTime() {
		return this.importedTime;
	}

	/**
	 * @param importedTime
	 */
	public void setImportedTime(LocalDateTime importedTime) {
		this.importedTime = importedTime;
	}

	/**
	 * @return the project
	 */
	@Override
	public Project getProject() {
		return this.project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * @return the updatedTime
	 */
	@Override
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @param updatedTime
	 *            the updatedTime to set
	 */
	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {

		SharedOutputInputUtils.fromJson(raw)
				.applyUUID("uid", this::setUuid)
				.applyLdt("cre", this::setCreatedTime)
				.applyLdt("upd", this::setUpdatedTime)
				.applyString("tab", this::setTableName)
				.applyString("cla", this::setTransformerClassName)
				.applyB64String("cfg", this::setTransformerConfig)
				.applyUUID("pro", v -> setProject(new Project(v)));
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.uuid == null) ? 0 : this.uuid.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransformerSource other = (TransformerSource) obj;
		if (this.uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!this.uuid.equals(other.uuid))
			return false;
		return true;
	}
}