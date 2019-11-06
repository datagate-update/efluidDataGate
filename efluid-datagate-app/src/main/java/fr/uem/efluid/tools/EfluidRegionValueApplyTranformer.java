package fr.uem.efluid.tools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.Associate;

/**
 * <p>
 * Example of substitute process for Efluid when a transformer need to change values in
 * regionalized context
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class EfluidRegionValueApplyTranformer extends SwitchedValueApplyTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(EfluidRegionValueApplyTranformer.class);

	private final String fromCol;
	private final String toCol;
	private final String selTable;

	private static final String REGION_CHANGE_QUERY = "select %s, %s from %s";

	@Autowired
	private JdbcTemplate jdbc;

	private Map<String, String> substitute;

	public EfluidRegionValueApplyTranformer(
			String fromCol,
			String toCol,
			String selTable) {

		super();

		this.fromCol = fromCol;
		this.toCol = toCol;
		this.selTable = selTable;
	}

	@Override
	protected String transformCompliantValue(DictionaryEntry dict, Value value) {
		String valueToProcess = value.getValueAsString();

		for (String code : this.substitute.keySet()) {
			if (valueToProcess.contains(code)) {
				valueToProcess = valueToProcess.replaceAll(code, this.substitute.get(code));
			}
		}

		return valueToProcess;
	}

	@PostConstruct
	public void preloadRegionChanges() {

		String query = String.format(REGION_CHANGE_QUERY, this.fromCol, this.toCol, this.selTable);

		// Extract all and keep as this for further needs
		this.substitute = this.jdbc.query(query, new CodeChangeRowMapper(this.fromCol, this.toCol)).stream()
				.collect(Collectors.toMap(Associate::getOne, Associate::getTwo));

		LOGGER.info("Value transformer for efluid initialized with {} code substitution to apply on "
				+ "{} tables and a total of {} column rules.",
				Integer.valueOf(this.substitute.size()), Integer.valueOf(this.tableApplies.size()),
				Long.valueOf(this.valueApplies.values().stream().flatMap(Collection::stream).count()));
	}

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	private static class CodeChangeRowMapper implements RowMapper<Associate<String, String>> {

		private final String fromCol;
		private final String toCol;

		public CodeChangeRowMapper(String fromCol, String toCol) {
			super();
			this.fromCol = fromCol;
			this.toCol = toCol;
		}

		@Override
		public Associate<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
			return Associate.of(rs.getString(this.fromCol), rs.getString(this.toCol));
		}
	}
}