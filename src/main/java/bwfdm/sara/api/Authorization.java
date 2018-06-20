package bwfdm.sara.api;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
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
import bwfdm.sara.auth.AuthProvider;
import bwfdm.sara.auth.ShibAuth;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.PublicationSession;
import bwfdm.sara.publication.Item;

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
		// if project selected, remember for later. if no project selected,
		// remember to show the selection screen instead.
		project.setProjectPath(projectPath);
		// if the user already has a session and the token still works, we're
		// done; no need to bother the user with authorization again.
		if (repo.hasWorkingToken())
			return authFinished(project);

		// user doesn't have a token or it has expired. trigger authorization
		// again.
		return triggerAuth(repo, redir, session);
	}

	@GetMapping("publish")
	public RedirectView authForPublication(
			@RequestParam("item") final String itemID,
			final RedirectAttributes redir, final HttpSession session) {
		final UUID itemUUID = UUID.fromString(itemID);
		final Item item = config.getPublicationDatabase()
					.updateFromDB(new Item(itemUUID));

		final GitRepo repo;
		if (Project.hasInstance(session)
				&& UUID.fromString(Project.getInstance(session).getRepoID())
						.equals(item.source_uuid))
			// if user already has a Project for this GitRepo, recycle its
			// authorization
			repo = Project.getInstance(session).getGitRepo();
		else
			repo = config.getConfigDatabase()
					.newGitRepo(item.source_uuid.toString());

		// if the user doesn't yet have a session for this item, create one
		if (!PublicationSession.hasInstance(session)
				|| !PublicationSession.getInstance(session).getItemUUID()
						.equals(itemUUID))
			PublicationSession.createInstance(session, item.source_uuid, repo,
					itemUUID, config);
		final PublicationSession publish = PublicationSession
				.getInstance(session);

		final AuthProvider auth = publish.getAuth();
		if (auth.hasWorkingToken())
			// if the user already has a session and the token still works,
			// we're done; no need to bother the user with authorization again.
			return authFinished(publish);

		// user doesn't have a token or it has expired. trigger authorization
		// again.
		return triggerAuth(auth, redir, session);
	}

	private RedirectView triggerAuth(final AuthProvider auth,
			final RedirectAttributes redir, final HttpSession session) {
		final String ep = auth.getShibAuth() != null ? "shibboleth"
				: "redirect";
		return auth.triggerAuth(config.getWebRoot() + "/api/auth/" + ep, redir,
				session);
	}

	@GetMapping("redirect")
	public RedirectView parseOAuthResponseWithoutShib(
			@RequestParam final Map<String, String> args,
			final RedirectAttributes redir, final HttpSession session) {
		return parseOAuthResponse(args, redir, session, null);
	}

	@GetMapping("shibboleth")
	public RedirectView parseOAuthResponse(
			@RequestParam final Map<String, String> args,
			final RedirectAttributes redir, final HttpSession session,
			final HttpServletRequest request) {
		final AuthProvider auth;
		if (PublicationSession.hasInstance(session))
			auth = PublicationSession.getInstance(session).getAuth();
		else if (Project.hasInstance(session))
			auth = Project.getInstance(session).getGitRepo();
		else
			// session has timed out before user returned. this should never
			// happen (but it just did).
			return new RedirectView("/autherror.html");

		// if we need Shib, make it parse the request first. this guarantees
		// that auth.parseAuthResponse() won't put anything valid into the
		// request if Shib fails.
		final ShibAuth shib = auth.getShibAuth();
		if (shib != null)
			// FIXME maybe show an error instead of an exception?
			// then again, not too much can go wrong with shib, and stuff that
			// does tends to be un-fixable to the user anyway...
			shib.parseAuthResponse(request);

		if (!auth.parseAuthResponse(args, session))
			// authorization failed. there isn't much we can do here;
			// retrying probably won't help.
			return new RedirectView("/autherror.html");

		if (PublicationSession.hasInstance(session))
			return authFinished(PublicationSession.getInstance(session));
		else if (Project.hasInstance(session))
			return authFinished(Project.getInstance(session));
		else
			// huh? we did have one of those beforehand!!
			throw new IllegalStateException(
					"session has suddenly disappeared?!");
	}

	private RedirectView authFinished(final PublicationSession publish) {
		publish.initialize();
		return new RedirectView("/publish.html");
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
