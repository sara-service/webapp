package bwfdm.sara.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import bwfdm.sara.project.Name;

public class TestNameSplitter {
	private DisplayNameSplitter western, eastern;

	@Before
	public void init() {
		western = new DisplayNameSplitter("western");
		eastern = new DisplayNameSplitter("eastern");
	}

	private static void test(DisplayNameSplitter splitter,
			final String string, final String surname, final String given) {
		final Name name = splitter.split(string);
		assertEquals(name.surname, surname);
		assertEquals(name.givenname, given);
	}

	@Test
	public void testEuropeanNames() {
		// simple cases of european names that must work
		test(western, "Matthias Fratz", "Fratz", "Matthias");
		test(western, "Stefan von Kombrink", "Kombrink", "Stefan von");
		test(western, "Angela Dorothea Merkel", "Merkel", "Angela Dorothea");
	}

	@Test
	public void testSpecialNames() {
		// abbreviated names and uppercase test. these are all American names.
		test(western, "Donald J. TRUMP", "TRUMP", "Donald J.");
		test(western, "E. Howard Hunt", "Hunt", "E. Howard");
		test(western, "JB Johnson", "Johnson", "JB");
		// unusual characters in name
		test(western, "Tim O'Reilly", "O'Reilly", "Tim");
		test(western, "Karl-Theodor zu Copy&Paste", "Copy&Paste",
				"Karl-Theodor zu");
	}

	@Test
	public void testLibrarian() {
		// basic librarian-format name
		test(western, "Trump, Donald J.", "Trump", "Donald J.");
		// TeX-compatible "Junior" suffix
		test(western, "Ford, Jr., Henry", "Ford, Jr.", "Henry");
	}

	@Test
	public void testNoRewrite() {
		// name already in correct format
		test(western, "Trump, Donald", "Trump", "Donald");
		// no surname, so we cannot rewrite anything
		test(western, "Jesus", "Jesus", "");
		// kewl people
		test(western, "dal33t", "dal33t", "");
	}

	@Test
	public void testUnicodeNames() {
		// some unusual scripts
		test(western, "Влади́мир Влади́мирович Пу́тин", "Пу́тин",
				"Влади́мир Влади́мирович");
		test(western, "Αλέξης Τσίπρας", "Τσίπρας", "Αλέξης");
		test(western, "عبد الفتاح سعيد حسين خليل السيسي", "السيسي",
				"عبد الفتاح سعيد حسين خليل");
		// non-latin space (currently the only one there is!)
		test(western, "ᚑᚌᚐᚋ ᚍᚒ", "ᚍᚒ", "ᚑᚌᚐᚋ");
		// non-breaking space
		test(western, "Jesus Christus", "Christus", "Jesus");
		// not a name; only tests non-letters in name
		test(western, "〩〿　〶〾", "〶〾", "〩〿");
	}

	@Test
	public void testLastnameFirstname() {
		// Chinese names have surname first, like most Asian names: 习 is the
		// surname. however they are written without spaces, so the "eastern"
		// rewriter doesn't actually handle the most common eastern language
		// correctly....
		test(eastern, "习近平", "习近平", "");
		// ...and so do Japanese names: 安倍 is the surname.
		test(eastern, "安倍 晋三", "安倍", "晋三");
		// hungarian names are already in the right format, but without the
		// comma. rewriting this with the "western" rule yields "Mihály, Orbán
		// Viktor" which is complete bullshit; Mihály is his middle name!
		test(eastern, "Orbán Viktor Mihály", "Orbán", "Viktor Mihály");
	}

	@Test
 	public void testCustomRegex() {
		final DisplayNameSplitter noble = new DisplayNameSplitter(
				"^(?<given>.*?)\\p{Z}+(?<surname>[^\\p{Lu}\\p{Lt}]*[\\p{Lu}\\p{Lt}][^\\p{Z}]+)$");
		test(noble, "Stefan von Kombrink", "von Kombrink", "Stefan");
		test(noble, "Karl-Theodor zu Copy&Paste", "zu Copy&Paste",
				"Karl-Theodor");
		test(noble, "Angela Dorothea Merkel", "Merkel", "Angela Dorothea");
	}
}
