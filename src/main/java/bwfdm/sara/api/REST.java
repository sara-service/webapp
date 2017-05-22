package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.gitlab.Branch;
import bwfdm.sara.gitlab.GitLab;
import bwfdm.sara.gitlab.ProjectInfo;
import bwfdm.sara.gitlab.Tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api")
public class REST {
	@GetMapping("refs")
	public List<Ref> getBranches(@RequestParam("project") final String project,
			final HttpSession session) {
		final String token = (String) session.getAttribute("gitlab_token");
		final GitLab gl = new GitLab(Temp.GITLAB, project, token);
		final List<Ref> refs = getAllRefs(gl);
		final ProjectInfo projectInfo = gl.getProjectInfo();
		sortRefs(refs, projectInfo.master);
		return refs;
	}

	private void sortRefs(final List<Ref> refs, final String master) {
		refs.sort(new Comparator<Ref>() {
			@Override
			public int compare(final Ref a, final Ref b) {
				// put default branch first so that it's the one selected by
				// default
				if (a.name.equals(master))
					return -1;
				if (b.name.equals(master))
					return +1;
				// branches before tags
				if (a.type.equals("branch") && !b.type.equals("branch"))
					return -1;
				if (b.type.equals("branch") && !a.type.equals("branch"))
					return +1;
				// protected branches are more likely to be the "main" branches,
				// so put them before unprotected (likely "side") branches
				if (a.prot && !b.prot)
					return -1;
				if (b.prot && !a.prot)
					return +1;
				// tiebreaker within those groups: lexicographic ordering
				return a.name.compareTo(b.name);
			}
		});
	}

	private List<Ref> getAllRefs(final GitLab gl) {
		final List<Ref> refs = new ArrayList<Ref>();
		for (final Branch b : gl.getBranches())
			refs.add(new Ref(b));
		for (final Tag t : gl.getTags())
			refs.add(new Ref(t));
		return refs;
	}

	@GetMapping("actions")
	public Map<String, String> getActions(
			@RequestParam("project") final String project,
			final HttpSession session) {
		// FIXME load from database instead
		@SuppressWarnings("unchecked")
		final Map<String, String> actions = (Map<String, String>) session
				.getAttribute("branch_actions");
		if (actions == null)
			return Collections.emptyMap();
		return actions;
	}

	@PutMapping("actions")
	public void setActions(@RequestParam("project") final String project,
			@RequestBody final Map<String, String> actions,
			final HttpSession session) {
		// FIXME store in database instead
		session.setAttribute("branch_actions", actions);
	}

	@JsonInclude(Include.NON_NULL)
	private static class Ref {
		@JsonProperty("name")
		private final String name;
		@JsonProperty("type")
		private final String type;
		@JsonProperty("protected")
		private final boolean prot;

		private Ref(final Branch b) {
			type = "branch";
			name = b.name;
			prot = b.prot;
		}

		private Ref(final Tag t) {
			type = "tag";
			name = t.name;
			// branches CAN be protected, but the API doesn't return that field
			prot = false;
		}
	}
}
