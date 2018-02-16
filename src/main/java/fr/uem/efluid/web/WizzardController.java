package fr.uem.efluid.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import fr.uem.efluid.services.ApplicationDetailsService;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.PilotableCommitPreparationService;
import fr.uem.efluid.services.SecurityService;
import fr.uem.efluid.utils.WebUtils;

/**
 * <p>
 * For all wizzard features : allows to specify minimal config + prepare the 1st mandatory
 * initial commit
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Controller
public class WizzardController {

	@Autowired
	private DictionaryManagementService dictionaryManagementService;

	@Autowired
	private PilotableCommitPreparationService pilotableCommitService;

	@Autowired
	private SecurityService security;

	@Autowired
	private ApplicationDetailsService detailsService;
	
	/**
	 * @return
	 */
	@RequestMapping(path = "/wizzard/", method = GET)
	public String welcome() {

		return "wizzard/welcome";
	}

	/**
	 * @return
	 */
	@RequestMapping(path = "/wizzard/1", method = GET)
	public String userPage() {

		return "wizzard/user";
	}

	/**
	 * @param login
	 * @param password
	 * @param email
	 * @return
	 */
	@RequestMapping(path = "/wizzard/1", method = POST)
	public String userSave(
			@RequestParam("login") String login,
			@RequestParam("password") String password,
			@RequestParam("email") String email) {

		this.security.addSimpleUser(login, email, password);

		return "wizzard/initial_dictionary";
	}

	/**
	 * @param model
	 * @param domainName
	 * @return
	 */
	@RequestMapping(value = "/wizzard/2", method = GET)
	public String initialCommitPage(Model model) {

		model.addAttribute("dictionary", this.dictionaryManagementService.getDictionnaryEntrySummaries());

		return "wizzard/initial_commit";
	}

	/**
	 * @param model
	 * @param domainName
	 * @return
	 */
	@RequestMapping(value = "/wizzard/2/create", method = POST)
	public String addFunctionalDomainData(Model model, @RequestParam("domainName") String domainName) {

		this.dictionaryManagementService.createNewFunctionalDomain(domainName);

		return initialCommitPage(model);
	}

	/**
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/wizzard/2/upload", method = POST)
	public String uploadDictionary(Model model, MultipartHttpServletRequest request) {

		this.dictionaryManagementService.importAll(WebUtils.inputExportImportFile(request));

		return initialCommitPage(model);
	}

	/**
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/wizzard/2/init", method = GET)
	public String startInitialCommit(Model model) {

		model.addAttribute("preparation", this.pilotableCommitService.startLocalCommitPreparation(true));

		return initialCommitPage(model);
	}

	/**
	 * @param model
	 * @param commitName
	 * @return
	 */
	@RequestMapping(value = "/wizzard/2/commit", method = GET)
	public String completedInitialCommit(Model model, @RequestParam("commitName") String commitName) {

		// Force initial commit
		this.pilotableCommitService.getCurrentCommitPreparation().getCommitData().setComment(commitName);
		model.addAttribute("preparation", this.pilotableCommitService.getCurrentCommitPreparation());
		this.pilotableCommitService.saveCommitPreparation();

		return initialCommitPage(model);
	}

	/**
	 * @return
	 */
	@RequestMapping(value = "/wizzard/3", method = GET)
	public String completedWizzard() {

		this.detailsService.completeWizzard();
		
		return "wizzard/completed";
	}

}
