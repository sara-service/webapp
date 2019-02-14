package bwfdm.sara.git.gitlab;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jgit.lib.PersonIdent;
import org.springframework.core.ParameterizedTypeReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.auth.AuthenticatedREST;
import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.project.ArchiveMetadata;

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
	// Alnum is ASCII-range only – no accented letters. thus we need a bit more,
	// but hardcoding the limit feels wrong.
	// → permit all letters and numbers as defined by Unicode consortium. GitLab
	// will always have to support basically all of these, or people will start
	// complaining pretty quickly...
	private static final Pattern NAME_FIRST = Pattern
			.compile("^[\\p{L}\\p{N}_]");
	private static final Pattern NAME_FORBIDDEN = Pattern
			.compile("[^\\p{L}\\p{N}\\p{Pd}_\\. ]");
	private static final Pattern SPACES = Pattern.compile("\\p{Z}+");
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
	final String finalNamespace;
	final String sshPrivateKey, sshPublicKey, sshKnownHosts;
	private final String committerName, committerEmail;
	final String root;

	/**
	 * @param root
	 *            root URL of GitLab webserver, without trailing slash
	 * @param apiToken
	 *            private token used to access the GitLab API
	 * @param archiveNamespace
	 *            GitLab namespace for the final archived data
	 * @param sshPrivateKey
	 *            full contents of SSH private key file (usually named
	 *            {@code id_ecdsa})
	 * @param sshPublicKey
	 *            full contents of SSH public key file (usually named
	 *            {@code id_ecdsa.pub})
	 * @param sshKnownHosts
	 *            all {@code known_hosts} file entries for to the GitLab
	 *            server's SSH host key, separated by newlines
	 * @param committerName
	 *            name to use for commits by SARA. if <code>null</code>, uses
	 *            {@link ArchiveRepo#DEFAULT_COMMITTER_NAME}
	 * @param committerEmail
	 *            email address to use for commits by SARA
	 */
	@JsonCreator
	public GitLabArchiveRESTv4(@JsonProperty("url") final String root,
			@JsonProperty("token") final String apiToken,
			@JsonProperty("namespace") final String archiveNamespace,
			@JsonProperty("private-key") final String sshPrivateKey,
			@JsonProperty("public-key") final String sshPublicKey,
			@JsonProperty("known-hosts") final String sshKnownHosts,
			@JsonProperty(value = "committer-name", required = false) final String committerName,
			@JsonProperty("committer-email") final String committerEmail) {
		if (root.endsWith("/"))
			throw new IllegalArgumentException(
					"root URL must not end with slash: " + root);

		this.committerName = committerName != null ? committerName
				: DEFAULT_COMMITTER_NAME;
		this.committerEmail = committerEmail;
		this.finalNamespace = archiveNamespace;

		this.sshPrivateKey = sshPrivateKey;
		this.sshPublicKey = sshPublicKey;
		this.sshKnownHosts = sshKnownHosts;
		authRest = new PrivateTokenREST(root + API_PREFIX, apiToken);
		rest = new RESTHelper(authRest, "");
		this.root = root;
	}

	@Override
	public ArchiveProject createProject(final String id, final boolean visible,
			final ArchiveMetadata meta)
			throws ProjectExistsException {
		final String version = filter(meta.version, MAX_VERSION);
		final String title = filter(meta.title, MAX_TITLE);
		String name = title + " " + version + " \u2015 " + id;
		// if name doesn't start with a permitted character, prepend something
		if (!NAME_FIRST.matcher(name).find())
			name = "\uD83D\uDCBE" + name;

		final Map<String, String> args = new HashMap<>();
		args.put("path", "p" + id);
		args.put("name", name);
		args.put("description", meta.description);
		for (final String feature : UNUSED_FEATURES)
			args.put(feature, "false");
		// always create as private project in the sara user's namespace. it
		// will then be moved and made public (if necessary) in commit().
		args.put("visibility", "private");
		final GLProjectInfo project = rest.post(rest.uri("/projects"), args,
				new ParameterizedTypeReference<GLProjectInfo>() {
				});
		return new GitLabArchiveProject(this, authRest, project,
				visible);
	}

	private String filter(final String raw, final int maxLength) {
		// canonicalize whitespace. it's definitely NOT a good idea to just
		// remove spaces like all other invalid characters.
		String value = SPACES.matcher(raw).replaceAll(" ");
		// remove invalid characters
		value = NAME_FORBIDDEN.matcher(value).replaceAll("");
		// shorten to maximum length
		if (value.length() > maxLength)
			value = value.substring(0, maxLength - 1) + ELLIPSIS;
		return value;
	}

	@Override
	public PersonIdent getMetadataCommitter() {
		return new PersonIdent(committerName, committerEmail);
	}
}
