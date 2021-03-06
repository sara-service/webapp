package bwfdm.sara.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bwfdm.sara.project.Name;

/**
 * Tries to split a single display name into surname and given name. This is
 * impossible to get right all the time, but reasonably easy to get right most
 * of the time if you know what culture your usres are from.
 */
public class DisplayNameSplitter {
	// Note: These regexes deliberately don't restrict what characters are
	// permitted in names. Unicode character class {@code Z} is spaces, so
	// anything non-space-y is accepted as part of a name.
	/**
	 * Librarian / bureaucratic name format, which has surname first but
	 * <b>separated with a comma</b>. Ugly, but at least pretty unambiguous. The
	 * obvious counterexample is the ", Jr." in "Henry Ford, Jr.", which
	 * analogous to {@link https://nwalsh.com/tex/texhelp/bibtx-23.html BibTeX}
	 * is supported if written as "Ford, Jr., Henry".
	 */
	private static final Pattern LIBRARIAN = Pattern
			.compile("^(?<surname>.+),\\p{Z}+(?<given>[^,]*?)$");

	/**
	 * "Western" name order, which has surname <b>last</b>. For names like
	 * "Ludwig van Beethoven", this yields "Beethoven" / "Ludwig van", which
	 * isn't legally correct ("van" is part of the surname) but very common.
	 */
	// alternative pattern that handles European nobles better:
	// "^(?<given>.*?)\\p{Z}+(?<surname>[^\\p{Lu}\\p{Lt}]*[\\p{Lu}\\p{Lt}][^\\p{Z}]+)$"
	// (probably not worth the complexity, and might break for other languages)
	private static final Pattern WESTERN = Pattern
			.compile("^(?<given>.*)\\p{Z}+(?<surname>[^\\p{Z}]+)$");
	/**
	 * "Eastern" name order, which has surname <b>first</b>. Despite the name,
	 * it's quite common in western Europe as well. Also, Chinese (and sometimes
	 * Japanese) names don't contain spaces, so this doesn't actually work for
	 * many "real" east Asian names.
	 */
	private static final Pattern EASTERN = Pattern
			.compile("^(?<surname>[^\\p{Z}]+)\\p{Z}+(?<given>.*)$");

	private final Pattern pattern;

	/**
	 * @param format
	 *            either a fixed pattern name ("eastern" or "western"), or a
	 *            regex with named capture groups for {@code given} and
	 *            {@code surname}
	 */
	public DisplayNameSplitter(final String format) {
		if (format.equalsIgnoreCase("western"))
			pattern = WESTERN;
		else if (format.equalsIgnoreCase("eastern"))
			pattern = EASTERN;
		else
			pattern = Pattern.compile(format);
	}

	public Name split(final String name) {
		// the librarian format is pretty much unambiguous, so always allow it
		final Matcher librarian = LIBRARIAN.matcher(name);
		if (librarian.find())
			return toName(librarian);

		final Matcher m = pattern.matcher(name);
		if (m.find())
			return toName(m);

		// fallback: stick the entire string into the surname
		return new Name(name, "");
	}

	private Name toName(final Matcher m) {
		return new Name(m.group("surname"), m.group("given"));
	}
}
