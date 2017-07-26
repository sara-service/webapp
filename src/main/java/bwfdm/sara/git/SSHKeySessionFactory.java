package bwfdm.sara.git;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHKeySessionFactory extends JschConfigSessionFactory implements
		TransportConfigCallback {
	private final String keyFile;
	private final String knownHosts;

	/**
	 * @param keyFile
	 *            full path to the private key file (usually called
	 *            {@code id_rsa} or {@code id_ecdsa})
	 * @param knownHosts
	 *            full path to the known-hosts file (almost universally called
	 *            {@code known_hosts})
	 */
	public SSHKeySessionFactory(final String keyFile, final String knownHosts) {
		this.knownHosts = knownHosts;
		this.keyFile = keyFile;
	}

	@Override
	protected void configure(final Host hc, final Session session) {
		return; // defaults are fine
	}

	@Override
	protected JSch createDefaultJSch(final FS fs) throws JSchException {
		final JSch jsch = super.createDefaultJSch(fs);
		jsch.addIdentity(keyFile);
		jsch.setKnownHosts(knownHosts);
		return jsch;
	}

	@Override
	public void configure(final Transport transport) {
		((SshTransport) transport).setSshSessionFactory(this);
	}
}