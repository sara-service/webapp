package bwfdm.sara.api;

import java.util.List;

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
import bwfdm.sara.publication.Hierarchy;
import bwfdm.sara.publication.PublicationRepository;
import bwfdm.sara.publication.Repository;

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

	@GetMapping("get-pubrepo-cfg")
	public String getPubRepoCfg(@RequestParam("field")final String field, final HttpSession session) {
		Project project = Project.getInstance(session);
		return project.getFrontendDatabase().getPubRepoCfg(field);
	}

	@GetMapping("set-pubrepo-cfg")
	public void setPubRepoCfg(@RequestParam("field")final String field, @RequestParam("value")final String value, final HttpSession session) {
        final Project project = Project.getInstance(session);
		project.getFrontendDatabase().setPubRepoCfg(field,value);
	}
	
	@GetMapping("query-hierarchy")
	public Hierarchy queryHierarchy(
			@RequestParam("user_email")final String user_email,
			@RequestParam("repo_uuid")final String repo_uuid) {
		List<PublicationRepository> pubRepos = config.getPublicationDatabase().getPubRepos();
		PublicationRepository repo = null;
		for (PublicationRepository r: pubRepos) {
			if (r.getDAO().uuid.toString().equals(repo_uuid)) {
				repo = r;
			}
		}
		
		if (repo == null) {
			System.out.println("Error! No Publication Repository with given 'repo_uuid' found!");
			return null;
		}
		
		if (repo.isUserRegistered(user_email)) {
			System.out.println("OK! User is registered!");
		} else {
			System.out.println("ERROR! User is not registered!");
			return null;
		}
		
		if (repo.isUserAssigned(user_email)) {
			System.out.println("OK! User is registered and has submit rights to some collection!");
		} else {
			System.out.println("ERROR! User is registered but has no submit rights to anywhere!");
			return new Hierarchy("");
		}
		
		
		Hierarchy root = repo.getHierarchy(user_email);
		
		String path = "";
		root.dump(path);
		return root;

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
