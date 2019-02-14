package bwfdm.sara.git.gitlab;

import java.util.Collections;

import org.eclipse.jgit.api.TransportCommand;

import bwfdm.sara.auth.AuthenticatedREST;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.SSHKeySessionFactory;
import bwfdm.sara.utils.UrlEncode;

public class GitLabArchiveProject implements ArchiveProject {
	private final GitLabArchiveRESTv4 archive;
	private final RESTHelper rest;
	private final GLProjectInfo project;
	private final boolean visible;
	private boolean committed, deleted;
	private final String finalProjectPath;

	GitLabArchiveProject(final GitLabArchiveRESTv4 archive,
			final AuthenticatedREST authRest, final GLProjectInfo project,
			final boolean visible) {
		this.archive = archive;
		this.project = project;
		this.visible = visible;
		finalProjectPath = archive.finalNamespace + "/" + project.name;
		rest = new RESTHelper(authRest,
				"/projects/" + UrlEncode.encodePathSegment(project.path));
	}

	@Override
	public String getWebURL() {
		return archive.root + "/" + finalProjectPath;
	}

	@Override
	public String getPushURI() {
		return project.cloneURL;
	}

	@Override
	public void configureCredentials(final TransportCommand<?, ?> tx) {
		tx.setTransportConfigCallback(
				new SSHKeySessionFactory(archive.sshPrivateKey,
						archive.sshPublicKey, archive.sshKnownHosts));
	}

	@Override
	public void setDefaultBranch(final String defaultBranch) {
		rest.put(rest.uri("" /* the project itself */),
				Collections.singletonMap("default_branch", defaultBranch));
	}

	@Override
	public void rollback() {
		if (committed)
			throw new IllegalStateException(
					"attempt to delete committed project " + project);
		if (deleted)
			return;
		rest.delete(rest.uri("" /* the project itself */));
		deleted = true;
	}

	@Override
	public void commit() {
		if (deleted)
			throw new IllegalStateException(
					"attempt to commit deleted project " + project);
		if (committed)
			return;

		// if archiving publicly, make project public. this needs to be done
		// before moving the project because SARA needs Owner permission to
		// change visibility – and the whole point of the move is to elegantly
		// remove the Owner permission.
		// this also means that finding projects that failed to transfer are
		// rather easy to find: just go to the sara user's profile page and look
		// for public projects there...
		if (visible)
			rest.put(rest.uri("" /* the project itself */),
					Collections.singletonMap("visibility", "public"));
		// move to final archive namespace
		rest.put(rest.uri("/transfer"),
				Collections.singletonMap("namespace", archive.finalNamespace));
		committed = true;
	}

	@Override
	public boolean isCommitted() {
		return committed;
	}
}
