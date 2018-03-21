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
			root.addChild("Psychopathie");
			/*
			root.addChildren(Arrays.asList(
			        new Hierarchy("Informatik"),
			        new Hierarchy("Elektrotechnik"),
			        new Hierarchy("Psychopathie")
			));*/
			return root;
		}
	}
	
	public class Hierarchy{
		@JsonProperty
	    private String data = null;
		@JsonManagedReference
	    private List<Hierarchy> children = new ArrayList<Hierarchy>();
		@JsonBackReference
		private Hierarchy parent = null;
		
	    public Hierarchy(String data) {
	        this.data = data;
	    }

	    public void addChild(Hierarchy child) {
	        child.setParent(this);
	        this.children.add(child);
	    }

	    public void addChild(String data) {
	        Hierarchy newChild = new Hierarchy(data);
	        newChild.setParent(this);
	        children.add(newChild);
	    }
	    
	    public void addChildren(List<Hierarchy> children) {
	        for(Hierarchy t : children) {
	            t.setParent(this);
	        }
	        this.children.addAll(children);
	    }
	    public String getData() {
	        return data;
	    }

	    public void setData(String data) {
	        this.data = data;
	    }

	    private void setParent(Hierarchy parent) {
	        this.parent = parent;
	    }

	    public Hierarchy getParent() {
	        return parent;
	    }
	}
/*
	public class TreeNode<T>{
		@JsonProperty
	    private T data = null;
		@JsonProperty
	    private List<TreeNode<T>> children = new ArrayList<>();
		@JsonProperty
		private TreeNode<T> parent = null;

	    public TreeNode(T data) {
	        this.data = data;
	    }

	    public void addChild(TreeNode<T> child) {
	        child.setParent(this);
	        this.children.add(child);
	    }

	    public void addChild(T data) {
	        TreeNode<T> newChild = new TreeNode<>(data);
	        newChild.setParent(this);
	        children.add(newChild);
	    }

	    public void addChildren(List<TreeNode<T>> children) {
	        for(TreeNode<T> t : children) {
	            t.setParent(this);
	        }
	        this.children.addAll(children);
	    }

	    public List<TreeNode<T>> getChildren() {
	        return children;
	    }
	    
	    public int getChildrenCount() {
	    	return children.size();
	    }
	    
	    public boolean isLeaf() {
	    	return getChildrenCount() == 0;
	    }

	    public T getData() {
	        return data;
	    }

	    public void setData(T data) {
	        this.data = data;
	    }

	    private void setParent(TreeNode<T> parent) {
	        this.parent = parent;
	    }

	    public TreeNode<T> getParent() {
	        return parent;
	    }
	}
	
	private class CommunityOrCollection {
		@JsonProperty
		private final String alot = "alot";
	}
	*/

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
