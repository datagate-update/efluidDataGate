package fr.uem.efluid.services;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import org.junit.Assert;
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
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.services.types.LocalPreparedDiff;
import fr.uem.efluid.services.types.PreparedIndexEntry;
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
public class PrepareDiffServiceIntegrationTest {

	@Autowired
	private PrepareIndexService service;

	@Autowired
	private TestDataLoader loader;

	@Autowired
	private DictionaryRepository dictionary;

	private UUID dictionaryEntryUuid;
	private UUID projectUuid;

	public void setupDatabase(String diff) {
		DataLoadResult res = this.loader.setupDatabaseForDiff(diff);
		this.dictionaryEntryUuid = res.getDicUuid();
		this.projectUuid = res.getProjectUuid();
	}

	@Test
	public void testProcessDiffNoIndex() {

		setupDatabase("diff7");
		LocalPreparedDiff diffToComplete = new LocalPreparedDiff(this.dictionaryEntryUuid);
		this.service.completeCurrentContentDiff(
				diffToComplete,
				this.dictionary.getOne(this.dictionaryEntryUuid),
				new HashMap<>(),
				new Project(this.projectUuid), new PilotedCommitPreparation<>(CommitState.LOCAL));
		Assert.assertEquals(0, diffToComplete.getDiff().size());
	}

	@Test
	public void testProcessDiffLargeIndex() {

		setupDatabase("diff8");
		LocalPreparedDiff diffToComplete = new LocalPreparedDiff(this.dictionaryEntryUuid);
		this.service.completeCurrentContentDiff(
				diffToComplete,
				this.dictionary.getOne(this.dictionaryEntryUuid),
				new HashMap<>(),
				new Project(this.projectUuid), new PilotedCommitPreparation<>(CommitState.LOCAL));
		Assert.assertEquals(80 + 100 + 85, diffToComplete.getDiff().size());
		List<PreparedIndexEntry> adds = diffToComplete.getDiff().stream()
				.filter(i -> i.getAction() == IndexAction.ADD)
				.collect(Collectors.toList());
		List<PreparedIndexEntry> removes = diffToComplete.getDiff().stream()
				.filter(i -> i.getAction() == IndexAction.REMOVE)
				.collect(Collectors.toList());
		List<PreparedIndexEntry> updates = diffToComplete.getDiff().stream()
				.filter(i -> i.getAction() == IndexAction.UPDATE)
				.collect(Collectors.toList());
		Assert.assertEquals(85, adds.size());
		Assert.assertEquals(100, removes.size());
		Assert.assertEquals(80, updates.size());
	}
}