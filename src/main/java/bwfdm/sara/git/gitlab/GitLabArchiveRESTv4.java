package bwfdm.sara.git.gitlab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.springframework.core.ParameterizedTypeReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.auth.AuthenticatedREST;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.project.MetadataField;

public class GitLabArchiveRESTv4 implements ArchiveRepo {
	// "Name is too long (maximum is 255 characters)"
	// "Path is too long (maximum is 255 characters)"
	// → that length is actually characters, not bytes
	// note for length budget:
	// - 4 padding (3 spaces and 1 long-dash)
	// - up to 16 characters for the ID (name must be unique!)
	private static final int MAX_VERSION = 30;
	private static final int MAX_TITLE = 200;
	// name rules:
	// "Name can contain only letters, digits, emojis, '_', '.', dash,
	// space."
	// → nobody needs or uses the emoji
	// → "…" is apparently a letter? it is allowed!
	// the actual regex (as of 10.8.3) is (! is a backslash):
	// [!p{Alnum}!u{00A9}-!u{1f9c0}_][!p{Alnum}!p{Pd}!u{00A9}-!u{1f9c0}_!.]*
	// note that 1f9c0 is incorrect; there's a few emoji after it...
	// → permit only the stuff guaranteed unchangeable by Unicode consortium:
	private static final Pattern NAME_FIRST = Pattern.compile("^[\\p{Alnum}_]");
	private static final Pattern NAME_FORBIDDEN = Pattern
			.compile("[^\\p{Alnum}\\p{Pd}_\\. ]");
	private static final char ELLIPSIS = 0x2026; // "…"
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
	private final String projectNamespace, darkNamespace, tempNamespace;
	private final String sshPrivateKey, sshPublicKey, knownHostsFile;

	/**
	 * @param root
	 *            root URL of GitLab webserver, without trailing slash
	 * @param apiToken
	 *            private token used to access the GitLab API
	 * @param tempNamespace
	 *            GitLab namespace used for temporarily-archived data
	 * @param projectNamespace
	 *            GitLab namespace for the final archived data
	 * @param darkNamespace
	 *            GitLab namespace for the dark archive
	 * @param sshPrivateKey
	 *            full contents of SSH private key file (usually named
	 *            {@code id_ecdsa})
	 * @param sshPublicKey
	 *            full contents of SSH public key file (usually named
	 *            {@code id_ecdsa.pub})
	 * @param knownHostsFile
	 *            all {@code known_hosts} file entries for to the GitLab
	 *            server's SSH host key, separated by newlines
	 */
	@JsonCreator
	public GitLabArchiveRESTv4(@JsonProperty("url") final String root,
			@JsonProperty("token") final String apiToken,
			@JsonProperty("temp-namespace") final String tempNamespace,
			@JsonProperty("main-namespace") final String projectNamespace,
			@JsonProperty("dark-namespace") final String darkNamespace,
			@JsonProperty("private-key") final String sshPrivateKey,
			@JsonProperty("public-key") final String sshPublicKey,
			@JsonProperty("known-hosts") final String knownHostsFile) {
		if (root.endsWith("/"))
			throw new IllegalArgumentException(
					"root URL must not end with slash: " + root);

		this.projectNamespace = projectNamespace;
		this.darkNamespace = darkNamespace;
		this.tempNamespace = tempNamespace;

		this.sshPrivateKey = sshPrivateKey;
		this.sshPublicKey = sshPublicKey;
		this.knownHostsFile = knownHostsFile;
		authRest = new PrivateTokenREST(root + API_PREFIX, apiToken);
		rest = new RESTHelper(authRest, "");
	}

	@Override
	public ArchiveProject createProject(final String id, final boolean visible,
			final Map<MetadataField, String> meta)
			throws ProjectExistsException {
		final String version = filter(meta, MetadataField.VERSION, MAX_VERSION);
		final String title = filter(meta, MetadataField.TITLE, MAX_TITLE);
		String name = title + " " + version + " \u2015 " + id;
		// if name doesn't start with a permitted character, prepend something
		if (!NAME_FIRST.matcher(name).find())
			name = "\uD83D\uDCBE" + name;

		final Map<String, String> args = new HashMap<>();
		args.put("path", "p" + id);
		args.put("name", name);
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
		return new GitLabArchiveProject(authRest, project, sshPrivateKey,
				sshPublicKey, knownHostsFile, true);
	}

	private String filter(final Map<MetadataField, String> meta,
			MetadataField field, int maxLength) {
		// remove invalid characters
		String value = NAME_FORBIDDEN.matcher(meta.get(field)).replaceAll("");
		// shorten to maximum length
		if (value.length() > maxLength)
			value = value.substring(0, maxLength - 1) + ELLIPSIS;
		return value;
	}

	private int getNamespaceID(final String namespace) {
		final List<Namespace> namespaces = rest.getList(
				rest.uri("/namespaces").queryParam("search", namespace),
				new ParameterizedTypeReference<List<Namespace>>() {
				});
		for (final Namespace ns : namespaces)
			if (ns.path.equals(namespace))
				return ns.id;
		throw new NoSuchElementException(
				"namespace " + namespace + " not found on server");
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
				sshPrivateKey, sshPublicKey, knownHostsFile, false);
	}

	private GLProjectInfo getProjectInfo(final String id) {
		final List<GLProjectInfo> projects = rest.getList(
				rest.uri("/projects").queryParam("search", id),
				new ParameterizedTypeReference<List<GLProjectInfo>>() {
				});
		for (final GLProjectInfo p : projects)
			if (p.name.equals(id))
				return p;
		throw new NoSuchElementException(
				"namespace " + projectNamespace + " not found on server");
	}
}
