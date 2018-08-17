package bwfdm.sara.api;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.Config;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.project.Project;
import bwfdm.sara.publication.Repository;

@RestController
@RequestMapping("/api")
public class Misc {
	@Autowired
	private Config config;
	
	@GetMapping("repo-list")
	public List<GitRepoFactory> getRepoList() {
		return config.getConfigDatabase().getGitRepos();
	}
	
	@GetMapping("pubrepo-list")
	public List<Repository> getPubRepoList() {
		return config.getPublicationDatabase().getList(Repository.class);
	}

	@GetMapping("project-list")
	public ProjectList getProjectList(final HttpSession session) {
		return new ProjectList(Project.getInstance(session));
	}

	private class ProjectList {
		@JsonProperty
		private final String repo;
		@JsonProperty
		private final List<ProjectInfo> projects;

		private ProjectList(final Project project) {
			repo = project.getRepoID();
			projects = project.getGitRepo().getProjects();
		}
	}

	@GetMapping("session-info")
	public SessionInfo getSessionInfo(final HttpSession session) {
		return new SessionInfo(Project.getCompletedInstance(session));
	}

	private class SessionInfo {
		@JsonProperty
		private final String repo;
		@JsonProperty("project")
		private final String projectPath;
		@JsonProperty("item")
		private UUID item;

		public SessionInfo(final Project project) {
			repo = project.getRepoID();
			projectPath = project.getProjectPath();
			if (project.isDone())
				item = project.getPushTask().getItemUUID();
			else
				item = null;
		}
	}
}
