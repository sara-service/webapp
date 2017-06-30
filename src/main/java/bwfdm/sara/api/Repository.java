package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import bwfdm.sara.ProjectFactory;
import bwfdm.sara.api.RefInfo.Action;
import bwfdm.sara.git.Branch;
import bwfdm.sara.git.Commit;
import bwfdm.sara.git.GitRepo;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.git.Tag;

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

	@GetMapping("selected-refs")
	public List<RefInfo> getSelectedBranches(final HttpSession session) {
		final List<RefInfo> refs = getBranches(session);
		for (final Iterator<RefInfo> i = refs.iterator(); i.hasNext();)
			if (i.next().action == null)
				i.remove();
		return refs;
	}

	private List<RefInfo> getAllRefs(final HttpSession session) {
		final GitRepo gl = ProjectFactory.getInstance(session).getGitRepo();
		final List<RefInfo> refs = new ArrayList<RefInfo>();
		for (final Branch b : gl.getBranches())
			refs.add(new RefInfo(b));
		for (final Tag t : gl.getTags())
			refs.add(new RefInfo(t));
		return refs;
	}

	private void loadActions(final List<RefInfo> refs, final HttpSession session) {
		// FIXME load from database instead
		if (session.getAttribute("branch_actions") == null) {
			final HashMap<String, Action> map = new HashMap<String, Action>();
			// default to publishing protected branches (assumed to be the main
			// branches), while only archiving everything else.
			for (final RefInfo r : refs) {
				if (r.isProtected || r.isDefault)
					r.action = Action.PUBLISH_FULL;
				map.put(r.ref, r.action);
			}
			session.setAttribute("branch_actions", map);
		}

		@SuppressWarnings("unchecked")
		final Map<String, Action> actions = (Map<String, Action>) session
				.getAttribute("branch_actions");
		for (final RefInfo r : refs)
			r.action = actions.get(r.ref);

		@SuppressWarnings("unchecked")
		final Map<String, String> starts = (Map<String, String>) session
				.getAttribute("branch_starts");
		if (starts != null)
			for (final RefInfo r : refs)
				r.start = starts.get(r.ref);
	}

	@PostMapping("refs")
	public void setActions(@RequestParam("ref") final String ref,
			@RequestParam("action") final Action action,
			@RequestParam("start") final String start, final HttpSession session) {
		// FIXME store in database instead
		if (session.getAttribute("branch_actions") == null)
			session.setAttribute("branch_actions",
					new HashMap<String, Action>());
		@SuppressWarnings("unchecked")
		final Map<String, Action> actions = (Map<String, Action>) session
				.getAttribute("branch_actions");
		actions.put(ref, action);

		if (session.getAttribute("branch_starts") == null)
			session.setAttribute("branch_starts", new HashMap<String, String>());
		@SuppressWarnings("unchecked")
		final Map<String, String> starts = (Map<String, String>) session
				.getAttribute("branch_starts");
		starts.put(ref, start);
	}

	@GetMapping("commits")
	public List<? extends Commit> getCommits(
			@RequestParam("ref") final String ref,
			@RequestParam(name = "limit", defaultValue = "20") final int limit,
			final HttpSession session) {
		return GitRepoFactory.getInstance(session).getCommits(ref, limit);
	}

	@GetMapping("project-info")
	public ProjectInfo getProjectInfo(final HttpSession session) {
		return GitRepoFactory.getInstance(session).getProjectInfo();
	}

	@PostMapping("project-info")
	public void setProjectInfo(
			@RequestParam(name = "name", required = false) final String name,
			@RequestParam(name = "description", required = false) final String description,
			final HttpSession session) {
		GitRepoFactory.getInstance(session)
				.updateProjectInfo(name, description);
	}

	@GetMapping("return")
	public RedirectView getReturnURL(final HttpSession session) {
		return new RedirectView(GitRepoFactory.getInstance(session)
				.getProjectViewURL());
	}

	@GetMapping("edit-file")
	public RedirectView getEditURL(@RequestParam("branch") final String branch,
			@RequestParam("path") final String path, final HttpSession session) {
		final GitRepo repo = GitRepoFactory.getInstance(session);
		if (repo.getBlob("heads/" + branch, path) != null)
			return new RedirectView(repo.getEditURL(branch, path));
		return new RedirectView(repo.getCreateURL(branch, path));
	}
}
