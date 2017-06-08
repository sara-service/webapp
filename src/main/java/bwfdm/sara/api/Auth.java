package bwfdm.sara.api;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;

@RestController
@RequestMapping("/api/auth")
public class Auth {
	@GetMapping("login")
	public RedirectView triggerLogin(
			@RequestParam("repo") final String gitRepo,
			@RequestParam("project") final String project,
			final RedirectAttributes redir, final HttpSession session) {
		// user not yet signed in, so create a new session
		if (!GitRepoFactory.hasInstance(session))
			return newLogin(gitRepo, project, redir, session);

		// check whether the user is pushy and tries to archive two projects at
		// once. if so, make sure he knows it won't work.
		final GitRepo repo = GitRepoFactory.getInstance(session);
		if (!repo.getID().equals(gitRepo)
				|| (repo.getProject() != null && !repo.getProject().equals(
						project))) {
			redir.addAttribute("repo", gitRepo);
			redir.addAttribute("project", project);
			return new RedirectView("/pushy.html");
		}

		if (repo.hasWorkingToken())
			// user already has a working session for that project; no need to
			// go through authorization again
			return redirectToBranches();

		// token has expired. trigger authorization again.
		return repo.triggerLogin(getLoginRedirectURI(session), redir, session);
	}

	@GetMapping("new")
	public RedirectView newLogin(@RequestParam("repo") final String gitRepo,
			@RequestParam("project") final String project,
			final RedirectAttributes redir, final HttpSession session) {
		final GitRepo repo;
		if (!GitRepoFactory.hasInstance(session)
				|| !GitRepoFactory.getInstance(session).getID().equals(gitRepo))
			// user has no session for this repo, so create one
			repo = GitRepoFactory.createInstance(session, gitRepo);
		else
			repo = GitRepoFactory.getInstance(session);

		repo.setProject(project);
		final RedirectView target = repo.triggerLogin(
				getLoginRedirectURI(session), redir, session);
		if (target == null)
			return redirectToBranches();
		return target;
	}

	@GetMapping("redirect")
	public RedirectView getOAuthToken(
			@RequestParam final Map<String, String> args,
			final RedirectAttributes redir, final HttpSession session) {
		if (!GitRepoFactory.hasInstance(session))
			// session has timed out before user returned. this should never
			// happen.
			return new RedirectView("/autherror.html");

		final GitRepo repo = GitRepoFactory.getInstance(session);
		if (repo.parseLoginResponse(args, session))
			return redirectToBranches();

		// authorization failed. there isn't much we can do here; we definitely
		// cannot restart it because we don't know the project or gitrepo.
		return new RedirectView("/autherror.html");
	}

	private RedirectView redirectToBranches() {
		return new RedirectView("/branches.html");
	}

	private static String getLoginRedirectURI(final HttpSession session) {
		return Config.getWebRoot(session) + "/api/auth/redirect";
	}
}
