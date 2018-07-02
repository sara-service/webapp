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
		if (Project.hasInstance(session) && isPushy(
				Project.getCompletedInstance(session), gitRepo, projectPath)) {
			// user is in the middle of the publication workflow for some
			// project and is about to start a new one. bad user; show warning.
			redir.addAttribute("repo", gitRepo);
			redir.addAttribute("project", projectPath);
			return new RedirectView("/pushy.html");
		}

		// everything else is just the same whether the user is returning from
		// the "pushy" warning or not
		return forceAuth(gitRepo, projectPath, redir, session);
	}

	private boolean isPushy(final Project existingProject, final String gitRepo,
			final String projectPath) {
		// existing project is done; don't show a message that it isn't!
		// FIXME should also check that publication done or disabled!
		if (existingProject.isDone())
			return false;

		// no project selected yet, so continuing doesn't make sense because
		// there's nothing to continue with. easy to trigger if the user selects
		// a git repo on index.html, goes back and selects a different git repo,
		// without ever selecting a project.
		final String prevProject = existingProject.getProjectPath();
		if (prevProject == null)
			return false;

		// different repo selected, so must be different project
		if (!existingProject.getRepoID().equals(gitRepo))
			return true;
		// user is coming from index.html and has selected something there
		if (projectPath == null)
			return true;
		// user explicitly selected a different project
		if (!prevProject.equals(projectPath))
			return true;

		// user starting to publish the same project again. there's nothing
		// wrong with that; in fact the warning could be silly because both
		// continuing the existing workflow and starting a new workflow would
		// archive exactly the same project.
		return false;
	}

	@GetMapping("new")
	public RedirectView forceAuth(
			@RequestParam("repo") final String gitRepo,
			@RequestParam(name = "project", required = false) final String projectPath,
			final RedirectAttributes redir, final HttpSession session) {
		// if the user does not have a session for this repo, create a new one.
		// deliberately reuses completed projects: setProjectPath() will restore
		// these to "virgin" condition, which allows reusing their
		// authorization.
		if (!Project.hasInstance(session) || !Project
				.getCompletedInstance(session).getRepoID().equals(gitRepo))
			Project.createInstance(session, gitRepo, projectPath, config);
		
		// note: at this point, the project may be completed, but the
		// setProjectPath() below will take care of that.
		final Project project = Project.getCompletedInstance(session);
		// if project selected, remember for later. if no project selected,
		// remember to show the selection screen instead.
		project.setProjectPath(projectPath);
		// if the user already has a session and the token still works, we're
		// done; no need to bother the user with authorization again.
		final GitRepo repo = project.getGitRepo();
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
		final Item item = config.getPublicationDatabase()
				.updateFromDB(new Item(UUID.fromString(itemID)));

		final GitRepo repo;
		if (Project.hasInstance(session) && item.source_uuid.equals(UUID
				.fromString(Project.getCompletedInstance(session).getRepoID())))
			// if user already has a Project for this GitRepo, recycle its
			// authorization
			repo = Project.getCompletedInstance(session).getGitRepo();
		else
			repo = config.getConfigDatabase()
					.newGitRepo(item.source_uuid.toString());

		// if the user doesn't yet have a session for this item, create one
		if (!PublicationSession.hasInstance(session)
				|| !canReuse(PublicationSession.getInstance(session), item))
			PublicationSession.createInstance(session, item.source_uuid, repo,
					item.uuid, config);

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

	private boolean canReuse(final PublicationSession existingSession,
			final Item item) {
		if (!existingSession.hasItem())
			// existing session still uninitialized; just create a new one
			return false;

		// we can only reuse a session if it's for the same item. in that case,
		// we obviously want to reuse it.
		if (!existingSession.getItemUUID().equals(item.uuid))
			return false;

		// sanity check: only allow reuse when source and user ID match. they
		// always should, but if we just return true here, the user cannot
		// recover if wrong values ever end up in there.
		return existingSession.getSourceUserID().equals(item.source_user_id)
				&& existingSession.getSourceUUID().equals(item.source_uuid);
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
		boolean hasActiveProject = Project.hasInstance(session)
				&& !Project.getCompletedInstance(session).isDone();
		final AuthProvider auth;
		if (hasActiveProject)
			auth = Project.getInstance(session).getGitRepo();
		else if (PublicationSession.hasInstance(session))
			auth = PublicationSession.getInstance(session).getAuth();
		else
			// session has timed out before user returned. this should never
			// happen (but it just did).
			throw new IllegalStateException(
					"session expired while waiting for authorization"
							+ " (overzealous cookie cleaning extension?)");

		// if we need Shib, make it parse the request first. this guarantees
		// that auth.parseAuthResponse() won't put anything valid into the
		// request if Shib fails (because Shib will just throw an exception if
		// anything goes wrong).
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

		if (hasActiveProject)
			return authFinished(Project.getInstance(session));
		else
			return authFinished(PublicationSession.getInstance(session));
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
