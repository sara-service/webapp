package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bwfdm.sara.Config;
import bwfdm.sara.git.GitRepoFactory;
import bwfdm.sara.git.ProjectInfo;
import bwfdm.sara.project.Project;

import bwfdm.sara.publication.Repository;
import bwfdm.sara.publication.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	
	@GetMapping("check-user-exists")
	public boolean checkUserExists(
			@RequestParam("repo_uuid") final String repo_uuid,
			@RequestParam("user_email") final String user_email) {
		return user_email.equals("stefan.kombrink@uni-ulm.de") && !repo_uuid.equals("");
	}
	
	@GetMapping("collection-list")
	public List<Collection> getCollections(
			@RequestParam("repo_uuid") final String repo_uuid, 
			@RequestParam("user_email") final String user_email) {
		
		if (!checkUserExists(repo_uuid, user_email)) {
			return new ArrayList<Collection>();
		}
		
		List<Collection> allColls = config.getPublicationDatabase().getList(Collection.class);
		List<Collection> colls = new ArrayList<Collection>();
		for (final Collection c: allColls) {
			if (UUID.fromString(repo_uuid).equals(c.id)) {
				colls.add(c);
			}
		}
		if (colls.isEmpty()) {
			// query ALL collections via REST
		}
		return colls;
	}

	@GetMapping("project-list")
	public List<ProjectInfo> getProjectList(final HttpSession session) {
		return Project.getInstance(session).getGitRepo().getProjects();
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
