package bwfdm.sara.git.gitlab;

import org.eclipse.jgit.api.TransportCommand;

import bwfdm.sara.auth.AuthenticatedREST;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.SSHKeySessionFactory;
import bwfdm.sara.utils.UrlEncode;

public class GitLabArchiveProject implements ArchiveProject {
	private final RESTHelper rest;
	private final GLProjectInfo project;
	private final String privateKey, publicKey, knownHosts;
	private final boolean isEmpty;

	public GitLabArchiveProject(final AuthenticatedREST authRest,
			final GLProjectInfo project, final String privateKey,
			final String publicKey, final String knownHosts,
			final boolean isEmpty) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.knownHosts = knownHosts;
		this.isEmpty = isEmpty;
		rest = new RESTHelper(authRest, "/projects/"
				+ UrlEncode.encodePathSegment(project.path));
		this.project = project;
	}

	@Override
	public String getPushURI() {
		return project.cloneURL;
	}

	@Override
	public String getDarkPushURI() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String getWebURL() {
		return project.webURL;
	}

	@Override
	public void setDefaultBranch(final String defaultBranch) {
		project.master = defaultBranch;
		rest.put(rest.uri("" /* the project itself */), project);
	}

	@Override
	public void setCredentials(final TransportCommand<?, ?> tx) {
		tx.setTransportConfigCallback(new SSHKeySessionFactory(privateKey,
				publicKey, knownHosts));
	}

	@Override
	public boolean isEmpty() {
		return isEmpty;
	}

	@Override
	public void deleteProject() {
		if (!isEmpty)
			throw new IllegalStateException("attempt to delete project "
					+ project.path + " we didn't create");
		rest.delete(rest.uri("" /* the project itself */));
	}
}
