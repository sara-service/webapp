package bwfdm.sara.extractor.licensee;

/**
 * Internal data class for Licensee. It is public only because JRuby cannot
 * access package-private classes.
 */
public interface LicenseeFile {
	/**
	 * @return name of the file which contains the license, never
	 *         <code>null</code>
	 */
	public String getFile();

	/**
	 * @return {@link SPDX License ID https://spdx.org/licenses/}, never
	 *         <code>null</code>
	 */
	public String getID();

	/**
	 * @return license display name, or <code>null</code> iff {@link #getID()}
	 *         is <code>null</code>
	 */
	public String getName();

	/** @return confidence score (0.0 … 1.0) */
	public float getScore();
}
