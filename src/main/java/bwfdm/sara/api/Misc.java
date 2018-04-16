package bwfdm.sara.api;

import java.util.ArrayList;
import java.util.Arrays;
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
import bwfdm.sara.publication.Repository;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonSerializer;

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
	
	@GetMapping("query-hierarchy")
	public Hierarchy queryHierarchy(
			@RequestParam("user_email")final String user_email,
			@RequestParam("repo_uuid")final String repo_uuid) {
		if (!user_email.equals("stefan.kombrink@uni-ulm.de")) {
			return null;
		} else {
			Hierarchy root = new Hierarchy("Bibliography");
			
			Hierarchy inf = new Hierarchy("Informatik");
			Hierarchy ele = new Hierarchy("Elektrotechnik");
			Hierarchy psy = new Hierarchy("Psychopathie");
			
			inf.addChild("Forschungsdaten"); inf.addChild("Publikationen");
			ele.addChild("Forschungsdaten"); ele.addChild("Publikationen");
			psy.addChild("Amokläufe");
			
			root.addChildren(Arrays.asList(inf,ele,psy));
			String path = "";
			root.dump(path);
			return root;
		}
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
