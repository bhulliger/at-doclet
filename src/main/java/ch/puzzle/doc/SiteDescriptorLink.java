package ch.puzzle.doc;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
public class SiteDescriptorLink {

	/**
	 * the class to generate the link for. used to evaluate the path to the
	 * generated file.
	 */
	private final Class<?> clazz;

	/** the id (name) of the file. used in the link as filename. */
	private final String id;

	/** the name of the link to display. */
	private final String linkName;

	/** Set of sublinks of this link. */
	private final Set<SiteDescriptorLink> subLinks;

	/**
	 * @param clazz
	 *            the {@link Class} to create the link for.
	 * @param id
	 *            the id of the file.
	 * @param linkName
	 *            the name to display
	 * @param subLinks
	 *            {@link Set} of sublinks to add.
	 */
	public SiteDescriptorLink(final Class<?> clazz, final String id,
			final String linkName, final Set<SiteDescriptorLink> subLinks) {
		this.clazz = clazz;
		this.id = id;
		this.linkName = linkName;
		this.subLinks = subLinks;
	}

	/**
	 * @param clazz
	 *            the {@link Class} to create the link for.
	 * @param id
	 *            the id of the file.
	 * @param linkName
	 *            the name to display
	 */
	public SiteDescriptorLink(final Class<?> clazz, final String id,
			final String linkName) {
		this.clazz = clazz;
		this.id = id;
		this.linkName = linkName;
		this.subLinks = new HashSet<>();
	}

	/**
	 * generates a http-link to add to the site.xml (maven site descriptor). Do
	 * not override this method.
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * @return code snippet for the html link(s).
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder();

		final String filePath = SiteDescriptorLink.evaluatePath(this.clazz);

		sb.append("\n<item name='").append(this.linkName).append("' href='")
				.append("/generated/").append(filePath).append("/")
				.append(this.id).append(".html'>");

		for (final SiteDescriptorLink subLink : this.subLinks) {
			sb.append(subLink.toString());
		}

		sb.append("</item>");

		return sb.toString();
	}

	/**
	 * generates the output path from the package declaration.
	 * 
	 * @param clazz
	 *            the class to evaluate the path from.
	 * @return path
	 */
	private static String evaluatePath(final Class<?> clazz) {
		return clazz.getPackage().toString().replace("package ", "")
				.replaceAll("\\.", "/");
	}
}
