package fr.uem.efluid.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import fr.uem.efluid.stubs.DataLoadResult;
import fr.uem.efluid.stubs.TestDataLoader;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class ManagedParametersRepositoryIntegrationTest {

	@Autowired
	private ManagedExtractRepository extracted;

	@Autowired
	private ManagedRegenerateRepository regenerated;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dictionary;

	private UUID dictionaryEntryUuid;
	private UUID projectUuid;

	public void setupDatabase(String diff) {
		DataLoadResult res =  this.loader.setupDatabaseForDiff(diff);
		this.dictionaryEntryUuid = res.getDicUuid();
		this.projectUuid = res.getProjectUuid();
	}

	@Test
	public void testExtractCurrentContentLow() {
		setupDatabase("diff7");
		Map<String, String> raw = this.extracted.extractCurrentContent(this.dictionary.getOne(this.dictionaryEntryUuid), new HashMap<>(), new Project(this.projectUuid));
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff7/actual.csv");
	}

	@Test
	public void testRegenerateKnewContentLow() {
		setupDatabase("diff7");
		Map<String, String> raw = this.regenerated.regenerateKnewContent(this.dictionary.getOne(this.dictionaryEntryUuid));
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff7/knew.csv");
	}

	@Test
	public void testExtractCurrentContentHeavy() {
		setupDatabase("diff8");
		Map<String, String> raw = this.extracted.extractCurrentContent(this.dictionary.getOne(this.dictionaryEntryUuid), new HashMap<>(), new Project(this.projectUuid));
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff8/actual.csv");
	}

	@Test
	public void testRegenerateKnewContentHeavy() {
		setupDatabase("diff8");
		Map<String, String> raw = this.regenerated.regenerateKnewContent(this.dictionary.getOne(this.dictionaryEntryUuid));
		this.loader.assertDatasetEqualsRegardingConverter(raw, "diff8/knew.csv");
	}
}