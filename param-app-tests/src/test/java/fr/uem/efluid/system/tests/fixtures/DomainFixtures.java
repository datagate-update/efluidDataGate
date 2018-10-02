package fr.uem.efluid.system.tests.fixtures;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;

import cucumber.api.Delimiter;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.uem.efluid.system.common.SystemTest;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class DomainFixtures extends SystemTest {

	private static List<String> specifiedDomainNames;

	@Before
	public void resetFixture() {
		specifiedDomainNames = null;
	}

	@Given("^the existing functional domains (.*)$")
	public void the_following_functional_domains_exist(@Delimiter(",") List<String> domainNames) throws Throwable {

		// Implicit init with domains
		initMinimalWizzardDataWithDomains(domainNames);

		// Implicit authentication and on page
		implicitlyAuthenticatedAndOnPage("functional domain edit page");

		// Keep domains
		specifiedDomainNames = domainNames;
	}

	@When("^the user add functional domain (.*)$")
	public void the_user_add_functional_domain(String name) throws Throwable {

		post("/ui/domains/add/" + URLEncoder.encode(name, "UTF-8"));

		// List can be unmodifiable, reset it
		List<String> updatedDomainNames = new ArrayList<>();

		updatedDomainNames.addAll(specifiedDomainNames);
		updatedDomainNames.add(name);

		specifiedDomainNames = updatedDomainNames;

		// Update is REST only, update page
		get(getCorrespondingLinkForPageName("functional domain edit page"));
	}

	@SuppressWarnings("unchecked")
	@Then("^the (\\d+) \\w+ functional domains are displayed$")
	public void the_x_functional_domains_are_displayed(int nbr) throws Throwable {

		currentAction = currentAction.andExpect(model().attribute("domains", allOf(
				hasSize(nbr), // List size ok
				hasItems(hasProperty("name", isIn(specifiedDomainNames)))// Values
		)));
	}
}