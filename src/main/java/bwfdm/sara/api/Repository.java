package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.db.FrontendDatabase;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.GitProject;
import bwfdm.sara.git.ProjectInfo;
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
		return project.getFrontendDatabase().getRefActions().values();
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

		Map<Ref, RefAction> actionMap = db.getRefActions();
		if (actionMap.isEmpty()) {
			// default to publishing protected branches (assumed to be the main
			// branches). other branches are ignored by default to avoid
			// overloading the user when there are many branches.
			for (final RefInfo r : refs)
				if (r.isProtected || r.isDefault) {
					db.setRefAction(r.ref, PublicationMethod.PUBLISH_FULL,
							RefAction.HEAD_COMMIT);
				}
			// fetch the list again to get one with the modifications we just
			// did. (the list is immutable once obtained.)
			actionMap = db.getRefActions();
			project.getTransferRepo().invalidate();
		}

		for (final RefInfo r : refs)
			r.action = actionMap.get(r.ref);
	}

	@PostMapping("actions")
	public void setActions(@RequestParam("ref") final String refPath,
			@RequestParam("publish") final PublicationMethod action,
			@RequestParam("firstCommit") final String start,
			final HttpSession session) {
		final Project project = Project.getInstance(session);
		project.getFrontendDatabase()
				.setRefAction(Ref.fromPath(refPath), action, start);
		project.getTransferRepo().invalidate();
	}

	@GetMapping("commits")
	public List<? extends Commit> getCommits(
			@RequestParam("ref") final String ref,
			@RequestParam(name = "limit", defaultValue = "20") final int limit,
			final HttpSession session) {
		return Project.getGitProject(session).getCommits(ref, limit);
	}

	@GetMapping("project-info")
	public ProjectInfo getProjectInfo(final HttpSession session) {
		return Project.getGitProject(session).getProjectInfo();
	}

	@PostMapping("project-info")
	public void setProjectInfo(
			@RequestParam(name = "name", required = false) final String name,
			@RequestParam(name = "description", required = false) final String description,
			final HttpSession session) {
		Project.getGitProject(session).updateProjectInfo(name, description);
	}

	@GetMapping("return")
	public RedirectView getReturnURL(final HttpSession session) {
		final Project project = Project.getInstance(session);
		if (project.getProjectPath() == null)
			return new RedirectView(project.getGitRepo().getHomePageURL());
		return new RedirectView(project.getGitProject().getProjectViewURL());
	}

	@GetMapping("edit-file")
	public RedirectView getEditURL(@RequestParam("branch") final String branch,
			@RequestParam("path") final String path, final HttpSession session) {
		final GitProject repo = Project.getGitProject(session);
		if (repo.getBlob("heads/" + branch, path) != null)
			return new RedirectView(repo.getEditURL(branch, path));
		return new RedirectView(repo.getCreateURL(branch, path));
	}
}
