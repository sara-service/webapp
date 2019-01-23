package bwfdm.sara.transfer;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Minimal replacement for juniversalchardet, which is basically abandoned,
 * large, complex and has somewhat unclear licensing terms. Fortunately,
 * non-UTF-8 is becoming incredibly rare, so we can more or less just always
 * "detect" UTF-8, regardless of input. Therefore this minimalist detector is
 * designed to always treat the input as UTF-8 if it's valid UTF-8; the chances
 * of that happening by accident are basically zero. It does bother to handle
 * the two most common exceptions, though:
 * <ul>
 * <li>UTF-16: This is an easy mistake to make on Windows. In text files, it has
 * a byte-order mark (BOM) so it's trivial to detect. The BOM also always makes
 * it invalid UTF-8 so there is no risk of breaking anything important.
 * <li>Windows-1252: Still the most common non-Unicode encoding for websites,
 * almost universally mislabeled as ISO-8859-1. In practice, Windows-1252 is a
 * subset of ISO-8859-1 because nobody uses control characters, so we do what
 * browsers do and treat both as Windows-1252. Detected by simply giving up and
 * using it for everything that is neither UTF-8 nor UTF-16.
 */
public class MiniCharDet {
	private static final Log logger = LogFactory.getLog(MiniCharDet.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final Charset UTF16 = Charset.forName("UTF-16");
	private static final Charset CP1252 = getWindows1252();

	private static Charset getWindows1252() {
		try {
			return Charset.forName("Windows-1252");
		} catch (UnsupportedCharsetException ex) {
			logger.warn("Windows-1252 unavailable; falling back to ISO-8859-1",
					ex);
			return Charset.forName("ISO-8859-1");
		}
	}

	public String detect(final byte[] data) {
		if (data.length >= 3 && data[0] == (byte) 0xEF && data[1] == (byte) 0xBB
				&& data[2] == (byte) 0xBF)
			// UTF-8 BOM. there's no such thing, but Windows likes to write it
			// anyway. it does however positively identify the input as UTF-8,
			// so we just decode it as such, making it fit if necessary. also,
			// we might as well strip that stupid BOM while we're at it.
			return decode(UTF8, data, 3, CodingErrorAction.REPLACE);
		if (data.length >= 2 && (data.length & 1) == 0
				&& ((data[0] == (byte) 0xFE && data[1] == (byte) 0xFF)
						|| (data[0] == (byte) 0xFF && data[1] == (byte) 0xFE)))
			// UTF-16 BOM, and a multiple of 16 bits. Java will use the BOM to
			// detect endianness and then strip it.
			return decode(UTF16, data, 0, CodingErrorAction.REPLACE);

		// at this point, it's probably UTF-8, so we can just decode it as such.
		// try decoding it as such to be
		// sure.
		final String utf = decode(UTF8, data, 0, CodingErrorAction.REPORT);
		if (utf != null)
			return utf;
		// one of the "legacy" encodings. only Windows-1252 is still in common
		// use, so we decode it as that. also neatly handles those people who
		// use the characters added by Windows-1252 but call it ISO-8859-1
		// anyway...
		return decode(CP1252, data, 0, CodingErrorAction.REPLACE);
	}

	private String decode(final Charset charset, final byte[] data,
			final int start, final CodingErrorAction errorAction) {
		final CharsetDecoder decoder = charset.newDecoder()
				.onMalformedInput(errorAction)
				.onUnmappableCharacter(errorAction);
		final ByteBuffer bytes = ByteBuffer.wrap(data, start,
				data.length - start);
		try {
			return decoder.decode(bytes).toString();
		} catch (CharacterCodingException e) {
			return null;
		}
	}
}
