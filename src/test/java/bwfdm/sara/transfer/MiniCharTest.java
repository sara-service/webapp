package bwfdm.sara.transfer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Testcase for {@link MiniCharDet}. */
public class MiniCharTest {
	private MiniCharDet detector;

	public MiniCharTest() {
		detector = new MiniCharDet();
	}

	private void test(String result, int... bytes) {
		byte[] data = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++)
			data[i] = (byte) bytes[i];
		assertEquals(result, detector.detect(data));
	}

	@Test
	public void utf8() {
		// pure ASCII, correctly treated as UTF-8
		test("test", 0x74, 0x65, 0x73, 0x74);
		// basic UTF-8 with "weird" characters from the BMP
		test("\u0163\u00EB\u0161\u02A6", 0xc5, 0xa3, 0xc3, 0xab, 0xc5, 0xa1,
				0xca, 0xa6);
		// UTF-8 with codepoint beyond the BMP (an emoji)
		test("\uD83E\uDD84", 0xF0, 0x9F, 0xA6, 0x84);
		// UTF-8 with a BOM
		test("test", 0xEF, 0xBB, 0xBF, 0x74, 0x65, 0x73, 0x74);
		// a bare UTF-8 BOM
		test("", 0xEF, 0xBB, 0xBF);
		// control character orgy
		test("\33[31m\t\7x\r\177\32\0\14\n", 0x1b, 0x5b, 0x33, 0x31, 0x6d, 0x09,
				0x07, 0x78, 0x0d, 0x7f, 0x1a, 0x00, 0x0c, 0x0a);
		// the last valid UTF-8 code point
		test("\uDBFF\uDFFF", 0xf4, 0x8f, 0xbf, 0xbf);
	}

	@Test
	public void utf16() {
		// ASCII-only, little-endian
		test("test", 0xff, 0xfe, 0x74, 0x00, 0x65, 0x00, 0x73, 0x00, 0x74,
				0x00);
		// ASCII-only, big-endian
		test("test", 0xfe, 0xff, 0x00, 0x74, 0x00, 0x65, 0x00, 0x73, 0x00,
				0x74);
		// basic test with characters from the BMP
		test("\u0163\u00EB\u0161\u02A6", 0xfe, 0xff, 0x01, 0x63, 0x00, 0xeb,
				0x01, 0x61, 0x02, 0xa6);
		// UTF-16 using surrogate pairs
		test("\uD83E\uDD84", 0xff, 0xfe, 0x3e, 0xd8, 0x84, 0xdd);
		// a bare BOM
		test("", 0xff, 0xfe);
		test("", 0xfe, 0xff);
	}

	@Test
	public void cp1252() {
		// ISO-8859-1 characters only (these map straight to Unicode)
		test("t\u00EA\u00DFt", 0x74, 0xea, 0xdf, 0x74);
		// control characters remapped by Windows-1252
		test("\u203A\u2020\u00EB\u0161\u2020\u2039", 0x9b, 0x86, 0xeb, 0x9a,
				0x86, 0x8b);
	}

	@Test
	public void boundaryCases() {
		// U+FFFF, which is guaranteed to stay unassigned forever. shouldn't be
		// replaced though.
		test("\uFFFF", 0xEF, 0xBF, 0xBF); // UTF-8
		test("\uFFFF", 0xff, 0xfe, 0xff, 0xff); // UTF-16

		// UTF-8 "BOM" plus invalid UTF-8 (U+FFFD is the replacement character).
		// might legitimately be rejected as UTF-8.
		test("\uFFFD", 0xEF, 0xBB, 0xBF, 0xFF);

		// UTF-16 BOM and a lone surrogate
		test("\uFFFD", 0xfe, 0xff, 0xd8, 0x3d);
		test("\uFFFD", 0xfe, 0xff, 0xdc, 0x6c);

		// lone surrogates, encoded as UTF-8. technically not permitted, so they
		// end up being decoded as CP1252.
		test("\u00ed\u00a0\u00bd", 0xED, 0xA0, 0xBD);
		test("\u00ed\u00b1\u00ac", 0xED, 0xB1, 0xAC);

		// UTF-16 BOM but an odd number of bytes, decoded as CP1252
		test("\u00fe\u00ff\u0161", 0xfe, 0xff, 0x9a);

		// undefined codepoints in Windows-1252. might legitimately be mapped to
		// a control characters instead.
		test("\ufffd\ufffd", 0x81, 0x90);

		// // various invalid UTF-8 cases decoded as CP1252 // //
		// overlong UTF-8, defined to be invalid
		test("\u00f0\u201a\u201a\u00ac", 0xF0, 0x82, 0x82, 0xAC);
		// codepoint beyond U+10FFFF, which is forbidden because UTF-16 (and
		// thus eg. Java) cannot represent it
		test("\u00f4\ufffd\u20ac\u20ac", 0xf4, 0x90, 0x80, 0x80);
		// 5-byte sequence, max permitted is 4 bytes
		test("\u00f8\u02c6\u20ac\u20ac\u20ac", 0xf8, 0x88, 0x80, 0x80, 0x80);
		// overlong encoding of NUL. accepted by some decoders because it avoids
		// null terminators in strings, but Java doesn't seem to like it.
		test("\u00C0\u20ac", 0xc0, 0x80);
	}
}
