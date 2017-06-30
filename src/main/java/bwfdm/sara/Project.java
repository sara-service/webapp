package bwfdm.sara;

import bwfdm.sara.git.GitRepo;

public class Project {
	private final GitRepo repo;
	private final String gitRepo;

	public Project(final String gitRepo, final GitRepo repo) {
		this.gitRepo = gitRepo;
		this.repo = repo;
	}

	public GitRepo getGitRepo() {
		return repo;
	}

	public String getRepoID() {
		return gitRepo;
	}
}
