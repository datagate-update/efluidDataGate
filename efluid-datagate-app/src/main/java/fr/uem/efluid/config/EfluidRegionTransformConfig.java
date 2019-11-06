package fr.uem.efluid.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.uem.efluid.config.EfluidRegionTransformConfig.EfluidRegionTransformProperties;
import fr.uem.efluid.tools.EfluidRegionValueApplyTranformer;
import fr.uem.efluid.utils.Associate;

@Configuration
@EnableConfigurationProperties(EfluidRegionTransformProperties.class)
public class EfluidRegionTransformConfig {

	@Autowired
	private EfluidRegionTransformProperties properties;

	/**
	 * <p>
	 * For efluid transform
	 * </p>
	 * 
	 * @return
	 */
	@Bean
	public EfluidRegionValueApplyTranformer efluidRegionTransformer() {

		// Init transform with code substitute source
		EfluidRegionValueApplyTranformer transformer = new EfluidRegionValueApplyTranformer(
				this.properties.getCodeFromColumn(),
				this.properties.getCodeToColumn(),
				this.properties.getCodeTable());

		// Apply where transform is valid
		this.properties.getApplyTo().stream()
				.map(v -> Associate.ofSplited(v, ":"))
				.forEach(v -> transformer.addSupportOn(v.getOne(), v.getTwo()));

		// Will be post completed in context
		return transformer;
	}

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	@ConfigurationProperties(prefix = "datagate-efluid.efluid-region-transform")
	public static class EfluidRegionTransformProperties {

		private String codeTable;

		private String codeFromColumn;

		private String codeToColumn;

		private List<String> applyTo;

		public EfluidRegionTransformProperties() {
			super();
		}

		public String getCodeTable() {
			return this.codeTable;
		}

		public void setCodeTable(String codeTable) {
			this.codeTable = codeTable;
		}

		public String getCodeFromColumn() {
			return this.codeFromColumn;
		}

		public void setCodeFromColumn(String codeFromColumn) {
			this.codeFromColumn = codeFromColumn;
		}

		public String getCodeToColumn() {
			return this.codeToColumn;
		}

		public void setCodeToColumn(String codeToColumn) {
			this.codeToColumn = codeToColumn;
		}

		public List<String> getApplyTo() {
			return this.applyTo;
		}

		public void setApplyTo(List<String> applyTo) {
			this.applyTo = applyTo;
		}
	}
}
