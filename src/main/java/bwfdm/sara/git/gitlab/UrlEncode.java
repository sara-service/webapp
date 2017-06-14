package bwfdm.sara.git.gitlab;

import java.io.UnsupportedEncodingException;

import org.springframework.web.util.UriUtils;

public class UrlEncode {
	private static final String ENCODING = "UTF-8";

	public static String encodeQueryParam(final String param) {
		try {
			return UriUtils.encodeQueryParam(param, ENCODING);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(ENCODING + " unsupported?!", e);
		}
	}

	public static String encodePathSegment(final String seg) {
		try {
			return UriUtils.encodePathSegment(seg, ENCODING);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(ENCODING + " unsupported?!", e);
		}
	}

	public static String decode(final String component) {
		try {
			return UriUtils.decode(component, ENCODING);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(ENCODING + " unsupported?!", e);
		}
	}
}
