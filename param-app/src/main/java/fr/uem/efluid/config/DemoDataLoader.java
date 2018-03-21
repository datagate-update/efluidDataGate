package fr.uem.efluid.config;

import static fr.uem.efluid.model.entities.IndexAction.ADD;
import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static fr.uem.efluid.model.entities.IndexAction.UPDATE;
import static fr.uem.efluid.utils.DataGenerationUtils.*;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.CommitRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.UserRepository;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Create some default values for demo mode
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Configuration
@Profile("demo")
@Transactional
public class DemoDataLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataLoader.class);

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private UserRepository users;

	@Autowired
	private IndexRepository index;

	@Autowired
	private CommitRepository commits;

	@Autowired
	private ManagedValueConverter converter;

	@PostConstruct
	public void initValues() {

		LOGGER.info("[DEMO] Init some demo values for testing");
		User dupont = this.users.save(user("dupont"));
		User testeur = this.users.save(user("testeur"));

		FunctionalDomain dom1 = this.domains.save(domain("Gestion du matériel"));
		FunctionalDomain dom2 = this.domains.save(domain("Processus utilisateurs"));
		this.domains.save(domain("Matrice des prix"));
		this.domains.save(domain("Editions"));

		DictionaryEntry cmat = this.dictionary
				.save(entry("Catégorie de matériel", dom1, null, "TCATEGORYMATERIEL", "1=1", "NOM", ColumnType.STRING));
		DictionaryEntry tmat = this.dictionary
				.save(entry("Type de matériel", dom1, null, "TTYPEMATERIEL", "1=1", "SERIE", ColumnType.STRING));
		DictionaryEntry mode = this.dictionary
				.save(entry("Modèle de compteur", dom1, "\"CODE_SERIE\", \"CREATE_DATE\", \"DESCRIPTION\", \"FABRICANT\", \"TYPEID\"",
						"TMODELE", "\"ACTIF\"=true", "CODE_SERIE", ColumnType.STRING));
		DictionaryEntry oth1 = this.dictionary
				.save(entry("Test sur le type de compteur", dom1, "\"COUNT\", \"TYPEID\"", "TTABLEOTHER", "1=1", "VALUE",
						ColumnType.STRING));
		DictionaryEntry oth2 = this.dictionary
				.save(entry("Autre table pour voir", dom2, null, "TTABLEOTHERTEST2", "1=1", "VALUE1", ColumnType.STRING));
		this.dictionary.save(entry("Fake table", dom2, "\"ATTR1\", \"ATTR2\"", "FAKETABLE", "1=1", "SERIAL", ColumnType.STRING));

		Commit com1 = this.commits.save(commit("Ajout du paramètrage de Catégorie de matériel", dupont, 5));
		Commit com2 = this.commits.save(commit("Ajout des Types de matériel", testeur, 3));
		Commit com3 = this.commits.save(commit("Ajout du reste", testeur, 1));

		this.index.save(
				update("something", ADD, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), cmat, com1));
		this.index.save(
				update("something", ADD, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), tmat, com2));
		this.index.save(
				update("something", REMOVE, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), tmat, com2));
		this.index.save(
				update("something", ADD, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), mode, com2));
		this.index.save(update("2355", ADD, content("Name=\"something\", Detail=\"other\", value=12345", this.converter), oth1, com2));

		this.index.save(update("234", ADD, content("Name=\"other\", Detail=\"something\", value=12345", this.converter), oth2, com2));
		this.index.save(update("236", ADD, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), oth2, com2));
		this.index.save(update("234", UPDATE, content("Name=\"other\", Detail=\"something\", value=12345", this.converter), oth2, com2));
		this.index.save(update("235", ADD, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), oth2, com2));
		this.index
				.save(update("235", REMOVE, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), oth2, com3));
		this.index.save(update("4", ADD, content("Name=\"something\", Detail=\"other\", value=33", this.converter), oth2, com3));
		this.index.save(update("234", UPDATE, content("Name=\"second\", Detail=\"something\", value=12345", this.converter), oth2, com3));
		this.index.save(update("7", ADD, content("Name=\"something\", Detail=\"something\", value=12345", this.converter), oth2, com3));
		this.index.save(update("11", ADD, content("Name=\"something\", Detail=\"something\", value=222", this.converter), oth2, com3));
		this.index.save(update("236", ADD, content("Name=\"something\", Detail=\"something\", value=222", this.converter), oth2, com3));

		LOGGER.info("[DEMO] Demo values init done");
	}

}