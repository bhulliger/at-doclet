package ch.puzzle.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public final class DocletPropertyUtils {

	/**
	 * Empty default constructor.
	 */
	private DocletPropertyUtils() {
		// empty default constructor
	}

	/** Log4j Logger. */
	private static final Logger LOG = Logger.getLogger(DocletPropertyUtils.class);

	/** Site-Generation-Properties to read from. */
	private static Properties docletProperties;

	/**
	 * @param property
	 *            the property key to get the according value for.
	 * @return the value to the provided property
	 */
	public static String getPropertyValue(final String property) {
		return DocletPropertyUtils.getProperties().getProperty(property);
	}

	/**
	 * Initializes the properties if not done yet.
	 * 
	 * @return the (initialized) properties.
	 */
	private static Properties getProperties() {
		if (docletProperties == null) {
			try (final InputStream resourceAsStream = DocletPropertyUtils.class
					.getResourceAsStream("/doclet.properties")) {
				docletProperties = new Properties();
				docletProperties.load(resourceAsStream);
				resourceAsStream.close();
			}
			catch (final IOException e) {
				DocletPropertyUtils.LOG.error("could not read property file [doclet.properties]", e);
			}
		}
		return docletProperties;
	}
}
