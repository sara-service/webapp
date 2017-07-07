package bwfdm.sara.api;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.project.Project;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api")
public class Misc {
	@GetMapping("project-list")
	public List<ProjectInfo> getProjectList(final HttpSession session) {
		return Project.getInstance(session).getGitRepoWithoutProject()
				.getProjects();
	}

	@GetMapping("session-info")
	public SessionInfo getSessionInfo(final HttpSession session) {
		return new SessionInfo(Project.getInstance(session));
	}

	private class SessionInfo {
		@JsonProperty
		private final String repo;
		@JsonProperty("project")
		private final String projectPath;
		@JsonProperty
		private final IRMeta ir;

		public SessionInfo(final Project project) {
			repo = project.getRepoID();
			projectPath = project.getProjectPath();
			// TODO read this from the session as well
			ir = new IRMeta("https://kops.uni-konstanz.de/", "kops.svg");
		}
	}

	private class IRMeta {
		@JsonProperty
		private final String url;
		@JsonProperty
		private final String logo;

		public IRMeta(final String url, final String logo) {
			this.url = url;
			this.logo = logo;
		}
	}
}
