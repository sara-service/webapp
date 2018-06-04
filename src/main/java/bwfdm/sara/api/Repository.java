package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.Tag;
import bwfdm.sara.project.Project;
import bwfdm.sara.project.Ref;
import bwfdm.sara.project.RefAction;
import bwfdm.sara.project.RefAction.PublicationMethod;

@RestController
@RequestMapping("/api/repo")
public class Repository {
	@GetMapping("refs")
	public List<RefInfo> getBranches(final HttpSession session) {
		final List<RefInfo> refs = getAllRefs(session);
		Collections.sort(refs);
		loadActions(refs, session);
		return refs;
	}

	@GetMapping("actions")
	public Collection<RefAction> getRefActions(final HttpSession session) {
		final Project project = Project.getInstance(session);
		return project.getFrontendDatabase().getRefActions();
	}

	private List<RefInfo> getAllRefs(final HttpSession session) {
		final GitProject gl = Project.getInstance(session).getGitProject();
		final List<RefInfo> refs = new ArrayList<RefInfo>();
		for (final Branch b : gl.getBranches())
			refs.add(new RefInfo(b));
		for (final Tag t : gl.getTags())
			refs.add(new RefInfo(t));
		return refs;
	}

	private void loadActions(final List<RefInfo> refs, final HttpSession session) {
		final Project project = Project.getInstance(session);
		final FrontendDatabase db = project.getFrontendDatabase();

		List<RefAction> actionList = db.getRefActions();
		if (actionList.isEmpty()) {
			// default to publishing protected branches (assumed to be the main
			// branches). other branches are ignored by default to avoid
			// overloading the user when there are many branches.
			for (final RefInfo r : refs)
				if (r.isProtected || r.isDefault)
					actionList.add(
							new RefAction(r.ref, PublicationMethod.PUBLISH_FULL,
									RefAction.HEAD_COMMIT));
			db.setRefActions(actionList);
			// the user didn't change the list of branches, but we did. he will
			// still have to clone this again
			project.invalidateTransferRepo();
		}

		final Map<Ref, RefAction> actions = new HashMap<Ref, RefAction>();
		for (final RefAction a : actionList)
			actions.put(a.ref, a);
		for (final RefInfo r : refs)
			r.action = actions.get(r.ref);
	}

	@PutMapping("actions")
	public void setActions(@RequestBody final List<RefAction> actions,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getFrontendDatabase().setRefActions(actions);
		project.invalidateTransferRepo();
	}

	@GetMapping("commits")
	public List<? extends Commit> getCommits(
			@RequestParam("ref") final String ref,
			@RequestParam(name = "limit", defaultValue = "20") final int limit,
			final HttpSession session) {
		return Project.getGitProject(session).getCommits(ref, limit);
	}

	@GetMapping("return")
	public RedirectView getReturnURL(final HttpSession session) {
		final Project project = Project.getInstance(session);
		if (project.getProjectPath() == null)
			return new RedirectView(project.getGitRepo().getHomePageURL());
		return new RedirectView(project.getGitProject().getProjectViewURL());
	}

	@Deprecated
	@GetMapping("edit-file")
	public RedirectView getEditURL(@RequestParam("branch") final String branch,
			@RequestParam("path") final String path, final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.invalidateTransferRepo();
		final GitProject repo = project.getGitProject();
		if (repo.getBlob("heads/" + branch, path) != null)
			return new RedirectView(repo.getEditURL(branch, path));
		return new RedirectView(repo.getCreateURL(branch, path));
	}
}
