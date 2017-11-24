package bwfdm.sara.git;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

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
	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final String privateKey, publicKey;
	private final String knownHosts;

	/**
	 * @param privateKey
	 *            full contents of the private key file (usually called
	 *            {@code id_rsa} or {@code id_ecdsa})
	 * @param publicKey
	 *            full contents of the public key file (usually called
	 *            {@code id_rsa.pub} or {@code id_ecdsa.pub})
	 * @param knownHosts
	 *            full contents of the known-hosts file (almost universally
	 *            called {@code known_hosts})
	 */
	public SSHKeySessionFactory(final String privateKey,
			final String publicKey, final String knownHosts) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.knownHosts = knownHosts;
	}

	@Override
	protected void configure(final Host hc, final Session session) {
		return; // defaults are fine
	}

	@Override
	protected JSch createDefaultJSch(final FS fs) throws JSchException {
		final JSch jsch = super.createDefaultJSch(fs);
		jsch.addIdentity("default", privateKey.getBytes(UTF8),
				publicKey.getBytes(UTF8), null);
		jsch.setKnownHosts(new ByteArrayInputStream(knownHosts.getBytes(UTF8)));
		return jsch;
	}

	@Override
	public void configure(final Transport transport) {
		((SshTransport) transport).setSshSessionFactory(this);
	}
}
