package bwfdm.sara.project;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.util.DigestUtils;

/**
 * Calculates a hash over some arbitrary input data, such that minimal changes
 * in the input result in a completely different hash.
 * <p>
 * <b>Not cryptographically secure!</b> Always assume this hash is guessable and
 * duplicates are easy to generate if an attacker tries.
 */
public class Hash {
	private static final Encoder BASE64 = Base64.getUrlEncoder()
			.withoutPadding();
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	public void add(String data) {
		add(data.getBytes(UTF8));
	}

	public void add(byte[] bytes) {
		byte[] digest = DigestUtils.md5Digest(bytes);
		buffer.write(digest, 0, digest.length); // doesn't throw exceptions
	}

	public String getHash() {
		byte[] digest = DigestUtils.md5Digest(buffer.toByteArray());
		return BASE64.encodeToString(digest);
	}
}
