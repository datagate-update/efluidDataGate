package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.uem.efluid.model.shared.ExportAwareTransformerSource;

/**
 * <p>
 * For processed transformer source
 * </p>
 * 
 * @author elecomte
 * @since v0.3.0
 * @version 1
 */
public class ParameterTransformerSource extends ExportAwareTransformerSource<ParameterProjectDefinition> {

	private UUID uuid;

	private String tableName;

	private String transformerClassName;

	private String transformerConfig;

	private ParameterProjectDefinition project;

	private LocalDateTime createdTime;

	private LocalDateTime updatedTime;

	/**
	 * 
	 */
	public ParameterTransformerSource() {
		super();
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
	 * @see fr.uem.efluid.model.shared.ExportAwareTransformerSource#getProject()
	 */
	@Override
	public ParameterProjectDefinition getProject() {
		return this.project;
	}

	/**
	 * @param project
	 */
	public void setProject(ParameterProjectDefinition project) {
		this.project = project;
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
	 * @see fr.uem.efluid.model.shared.ExportAwareTransformerSource#getUpdatedTime()
	 */
	@Override
	public LocalDateTime getUpdatedTime() {
		return this.updatedTime;
	}

	/**
	 * @param updatedTime
	 */
	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#getImportedTime()
	 */
	@Override
	public LocalDateTime getImportedTime() {
		return null;
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {
		// Not implemented
	}
}