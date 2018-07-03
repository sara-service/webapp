package bwfdm.sara.publication;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class NameRegexTest {
	private static final String REGEX = "^([^,]*)\\p{Z}+([^\\p{Z},]+)$";
	// alternative pattern that handles European nobles better:
	// "^(.*?)\\p{Z}+([^\\p{Lu}\\p{Lt}]*[\\p{Lu}\\p{Lt}][^\\p{Z}]+)$");
	// (probably not worth the complexity, and might break for other languages)

	private static String rewrite(String string) {
		return string.replaceAll(REGEX, "$2, $1");
	}

	@Test
	public void testEuropeanNames() {
		// simple cases of european names that must work
		assertEquals(rewrite("Matthias Fratz"), "Fratz, Matthias");
		assertEquals(rewrite("Stefan von Kombrink"), "Kombrink, Stefan von");
		assertEquals(rewrite("Angela Dorothea Merkel"),
				"Merkel, Angela Dorothea");
	}

	@Test
	public void testSpecialNames() {
		// abbreviated names and uppercase test. these are all American names.
		assertEquals(rewrite("Donald J. TRUMP"), "TRUMP, Donald J.");
		assertEquals(rewrite("E. Howard Hunt"), "Hunt, E. Howard");
		assertEquals(rewrite("JB Johnson"), "Johnson, JB");
		// unusual characters in name
		assertEquals(rewrite("Tim O'Reilly"), "O'Reilly, Tim");
		assertEquals(rewrite("Karl-Theodor zu Copy&Paste"),
				"Copy&Paste, Karl-Theodor zu");
	}

	@Test
	public void testNoRewrite() {
		// name already in correct format
		assertEquals(rewrite("Trump, Donald"), "Trump, Donald");
		// no family name, so we cannot rewrite anything
		assertEquals(rewrite("Jesus"), "Jesus");
	}

	@Test
	public void testUnicodeNames() {
		// some unusual scripts
		assertEquals(rewrite("Влади́мир Влади́мирович Пу́тин"),
				"Пу́тин, Влади́мир Влади́мирович");
		assertEquals(rewrite("Αλέξης Τσίπρας"), "Τσίπρας, Αλέξης");
		assertEquals(rewrite("عبد الفتاح سعيد حسين خليل السيسي"),
				"السيسي, عبد الفتاح سعيد حسين خليل");
		// non-latin space (currently the only one!)
		assertEquals(rewrite("ᚑᚌᚐᚋ ᚍᚒ"), "ᚍᚒ, ᚑᚌᚐᚋ");
		// non-breaking space
		assertEquals(rewrite("Jesus Christus"), "Christus, Jesus");
		// not a name; only tests non-letters in name
		assertEquals(rewrite("〩〿　〶〾"), "〶〾, 〩〿");
	}

	@Test
	@Ignore("almost impossible to get right")
	// it might be possible to get the asian ones right by disallowing certain
	// scripts in the lastname. for Hungarian, that approach is hopeless.
	public void testLastnameFirstname() {
		// Chinese names have family name first, like most Asian names: 习 is the
		// family name. fortunately they are written without spaces.
		assertEquals(rewrite("习近平"), "习近平");
		// ...and so do Japanese names: 安倍 is the family name.
		assertEquals(rewrite("安倍 晋三"), "安倍 晋三");
		// hungarian names are already in the right format, but without the
		// comma. naively rewriting this yields "Mihály, Orbán Viktor" which is
		// complete bullshit; Mihály is his middle name!
		assertEquals(rewrite("Orbán Viktor Mihály"), "Orbán, Viktor Mihály");
	}
}
