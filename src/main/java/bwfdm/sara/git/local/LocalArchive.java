package bwfdm.sara.git.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import bwfdm.sara.git.ArchiveProject;
import bwfdm.sara.git.ArchiveRepo;
import bwfdm.sara.project.ArchiveMetadata;

public class LocalArchive implements ArchiveRepo {
	private final String committerName, committerEmail;
	private final File tempRoot, publicRoot, privateRoot;
	final String webBase;

	/**
	 * @param publicRoot
	 *            path to local directory storing <b>public</b> archive
	 *            repositories. to prevent accidental or malicious deletion,
	 *            this directory should be {@code rwxrwxrwt} like {@code /tmp},
	 *            and should have a cron job that periodically makes its
	 *            subdirectories root owned. that way, SARA can create new
	 *            projects but cannot delete (or mess up) existing ones.
	 * @param privateRoot
	 *            path to local directory storing <b>private</b> (dark) archive
	 *            repositories. the same comment about permissions and
	 *            owenership applies.
	 * @param tempRoot
	 *            path to local staging directory. must be on the same
	 *            filesystem as publicRoot / privateRoot because repository
	 *            directories will be semi-atomically moved after the push
	 *            completes – and moving directories across filesystems isn't
	 *            semi-atomic, or anywhere close to.
	 * @param webBase
	 *            base URL of repository viewer. note that the viewer is
	 *            expected to strip the {@code .git} suffix from directory
	 *            names, as eg. Gitiles does.
	 * @param committerName
	 *            name to use for commits by SARA. if <code>null</code>, uses
	 *            {@link ArchiveRepo#DEFAULT_COMMITTER_NAME}
	 * @param committerEmail
	 *            email address to use for commits by SARA
	 */
	@JsonCreator
	public LocalArchive(@JsonProperty("public-root") final String publicRoot,
			@JsonProperty("private-root") final String privateRoot,
			@JsonProperty("temp-root") final String tempRoot,
			@JsonProperty("web-base") final String webBase,
			@JsonProperty(value = "committer-name", required = false) final String committerName,
			@JsonProperty("committer-email") final String committerEmail) {
		if (webBase.endsWith("/"))
			throw new IllegalArgumentException(
					"root URL must not end with slash: " + webBase);

		this.committerName = committerName != null ? committerName
				: DEFAULT_COMMITTER_NAME;
		this.committerEmail = committerEmail;
		this.tempRoot = new File(tempRoot);
		this.publicRoot = new File(publicRoot);
		this.privateRoot = new File(privateRoot);
		this.webBase = webBase;
	}

	@Override
	public ArchiveProject createProject(final String id, final boolean visible,
			final ArchiveMetadata meta) throws GitAPIException, IOException {
		final String description = meta.title + " " + meta.version
				+ "\n\n\uD83D\uDCA1 " + meta.description;
		final String projectName = "p" + id, dirName = projectName + ".git";
		final File tempDir = new File(tempRoot, dirName);
		final File targetDir = new File(visible ? publicRoot : privateRoot,
				dirName);

		Git.init().setDirectory(tempDir).setBare(true).call();
		try (final OutputStream desc = new FileOutputStream(
				new File(tempDir, "description"))) {
			desc.write(description.getBytes(StandardCharsets.UTF_8));
		}
		return new LocalArchiveProject(webBase + "/" + projectName, tempDir,
				targetDir);
	}

	@Override
	public PersonIdent getMetadataCommitter() {
		return new PersonIdent(committerName, committerEmail);
	}
}
