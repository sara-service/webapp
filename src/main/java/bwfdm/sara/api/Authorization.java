package bwfdm.sara.api;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.Config;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.project.Project;

@RestController
@ControllerAdvice
@RequestMapping("/api/auth")
public class Authorization {
	@Autowired
	private Config config;

	@GetMapping("login")
	public RedirectView triggerAuth(
			@RequestParam("repo") final String gitRepo,
			@RequestParam(name = "project", required = false) final String projectPath,
			final RedirectAttributes redir, final HttpSession session) {
		// check whether the user is pushy and tries to archive two projects at
		// once. if so, make sure he knows it won't work.
		if (Project.hasInstance(session)) {
			final Project project = Project.getInstance(session);
			final String prevProject = project.getProjectPath();
			if (prevProject != null)
				// user is in the middle of the publication workflow for some
				// project and is about to start a new one. bad user; show
				// warning.
				if (!project.getRepoID().equals(gitRepo) // different repo
						|| projectPath == null // any selection on index.html
						|| !prevProject.equals(projectPath)) { // diff. project
					redir.addAttribute("repo", gitRepo);
					redir.addAttribute("project", projectPath);
					return new RedirectView("/pushy.html");
			}
		}

		// everything else is just the same whether the user is returning from
		// the "pushy" warning or not
		return forceAuth(gitRepo, projectPath, redir, session);
	}

	@GetMapping("new")
	public RedirectView forceAuth(
			@RequestParam("repo") final String gitRepo,
			@RequestParam(name = "project", required = false) final String projectPath,
			final RedirectAttributes redir, final HttpSession session) {
		// if the user does not have a session for this repo, create one
		if (!Project.hasInstance(session)
				|| !Project.getInstance(session).getRepoID().equals(gitRepo))
			Project.createInstance(session, gitRepo, projectPath, config);

		final Project project = Project.getInstance(session);
		final GitRepo repo = project.getGitRepo();
		// if project selected, remember for later
		if (projectPath != null)
			project.setProjectPath(projectPath);
		// if the user already has a session and the token still works, we're
		// done; no need to bother the user with authorization again.
		if (repo.hasWorkingToken())
			return authFinished(project);

		// user doesn't have a token or it has expired. trigger authorization
		// again.
		return repo.triggerAuth(config.getWebRoot() + "/api/auth/redirect",
				redir, session);
	}

	@GetMapping("redirect")
	public RedirectView getOAuthToken(
			@RequestParam final Map<String, String> args,
			final RedirectAttributes redir, final HttpSession session) {
		if (!Project.hasInstance(session))
			// session has timed out before user returned. this should never
			// happen.
			return new RedirectView("/autherror.html");

		final Project project = Project.getInstance(session);
		if (project.getGitRepo().parseAuthResponse(args, session))
			return authFinished(project);

		// authorization failed. there isn't much we can do here; retrying
		// probably won't help.
		return new RedirectView("/autherror.html");
	}

	private RedirectView authFinished(final Project project) {
		// if no project selected yet, go to project selection
		if (project.getProjectPath() == null)
			return new RedirectView("/projects.html");
		// else go to branch selection
		project.initializeProject();
		return new RedirectView("/branches.html");
	}
}
