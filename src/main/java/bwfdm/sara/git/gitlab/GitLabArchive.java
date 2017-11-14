package bwfdm.sara.git.gitlab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.ParameterizedTypeReference;

import bwfdm.sara.auth.AuthenticatedREST;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.project.MetadataField;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitLabArchive implements ArchiveRepo {
	/**
	 * URL prefix for accessing the API. also defines which API version will be
	 * used.
	 * <p>
	 * note that this is deliberately a separate constant from
	 * {@link GitLabRESTv4#API_PREFIX}. the two GitLabs do not have to use the
	 * same API version.
	 */
	private static final String API_PREFIX = "/api/v4";
	/**
	 * List of features to disable on newly-created projects. As GitLab evolves
	 * to include more stuff that doesn't make sense for an archive, extend this
	 * list to disable those features to avoid confusing the user.
	 */
	private static final String[] UNUSED_FEATURES = { "issues_enabled",
			"merge_requests_enabled", "jobs_enabled", "wiki_enabled",
			"snippets_enabled", "container_registry_enabled",
			"shared_runners_enabled", "lfs_enabled", "request_access_enabled" };

	private final RESTHelper rest;
	private final AuthenticatedREST authRest;
	private final String projectNamespace;
	private final String sshKeyFile;
	private final String darkNamespace;
	private final String knownHostsFile;

	/**
	 * @param root
	 *            root URL of GitLab webserver, without trailing slash
	 * @param apiToken
	 *            private token used to access the GitLab API
	 * @param projectNamespace
	 *            namespace in which to create projects
	 * @param sshKeyFile
	 *            path to SSH private key file; the public key file has
	 *            {@code .pub} appended
	 */
	@JsonCreator
	public GitLabArchive(@JsonProperty("url") final String root,
			@JsonProperty("token") final String apiToken,
			@JsonProperty("namespace") final String projectNamespace,
			@JsonProperty("dark-namespace") final String darkNamespace,
			@JsonProperty("key") final String sshKeyFile,
			@JsonProperty("known-hosts") final String knownHostsFile) {
		if (root.endsWith("/"))
			throw new IllegalArgumentException(
					"root URL must not end with slash: " + root);

		authRest = new PrivateTokenREST(root + API_PREFIX, apiToken);
		this.projectNamespace = projectNamespace;
		this.sshKeyFile = sshKeyFile;
		this.darkNamespace = darkNamespace;
		this.knownHostsFile = knownHostsFile;
		rest = new RESTHelper(authRest, "");
	}

	@Override
	public ArchiveProject createProject(final String id, final boolean visible,
			final Map<MetadataField, String> meta)
			throws ProjectExistsException {
		final Map<String, String> args = new HashMap<>();
		args.put("path", "p" + id);
		// FIXME filter name:
		// "Name can contain only letters, digits, emojis, '_', '.', dash, space."
		// it's probably ok if we don't support the emojis...
		// FIXME check whether there is a length limit!
		final String name = meta.get(MetadataField.TITLE) + " "
				+ meta.get(MetadataField.VERSION);
		args.put("name", name + " _" + id + "_");
		args.put("description", meta.get(MetadataField.DESCRIPTION));
		for (final String feature : UNUSED_FEATURES)
			args.put(feature, "false");
		args.put("visibility", visible ? "public" : "private");
		// TODO create in own namespace first; then move on success!
		args.put("namespace_id",
				Integer.toString(getNamespaceID(projectNamespace)));
		final GLProjectInfo project = rest.post(rest.uri("/projects"), args,
				new ParameterizedTypeReference<GLProjectInfo>() {
				});
		return new GitLabArchiveProject(authRest, project, sshKeyFile,
				knownHostsFile, true);
	}

	private int getNamespaceID(final String namespace) {
		final List<Namespace> namespaces = rest.getList(rest.uri("/namespaces")
				.queryParam("search", namespace),
				new ParameterizedTypeReference<List<Namespace>>() {
				});
		for (final Namespace ns : namespaces)
			if (ns.path.equals(namespace))
				return ns.id;
		throw new NoSuchElementException("namespace " + namespace
				+ " not found on server");
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Namespace {
		@JsonProperty("full_path")
		private String path;
		@JsonProperty
		private int id;
	}

	@Override
	public ArchiveProject getProject(final String id)
			throws NoSuchElementException {
		return new GitLabArchiveProject(authRest, getProjectInfo(id),
				sshKeyFile, knownHostsFile, false);
	}

	private GLProjectInfo getProjectInfo(final String id) {
		final List<GLProjectInfo> projects = rest.getList(rest.uri("/projects")
				.queryParam("search", id),
				new ParameterizedTypeReference<List<GLProjectInfo>>() {
				});
		for (final GLProjectInfo p : projects)
			if (p.name.equals(id))
				return p;
		throw new NoSuchElementException("namespace " + projectNamespace
				+ " not found on server");
	}
}
