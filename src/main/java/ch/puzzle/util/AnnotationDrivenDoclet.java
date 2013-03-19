package ch.puzzle.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import lombok.extern.log4j.Log4j;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
@Log4j
public class AnnotationDrivenDoclet {

	private static final String[] SUPPORTED_VIDEO_TYPES = { "ogg", "mp4",
			"webm" };

	/**
	 * The base output directory where the apt-files are stored. The value is
	 * passed by the commandline parameter '-output' for the javadoc command.
	 * 
	 * e.g. javadoc -output ~/docs
	 */
	protected static String baseOutputDir;

	/**
	 * The resource directory of the project. The parameter "-resourceDir" has
	 * to be set as parameter if does not match the maven default of
	 * src/site/resources.
	 */
	protected static String resourcesDir = "src/site/resources/";

	/**
	 * Map of configured annotations. stores which template to use for which
	 * annotation. The values are passed by at least one commandline parameter
	 * '-annotation'. One parameter for each used annotation is required.
	 * 
	 * e.g. javadoc -annotation ch.puzzle.example.Page
	 * ${basedir}/src/site/apt/templates/pages.apt.template
	 */
	protected static Map<String, String> configuredAnnotations = new HashMap<>();

	private static final Pattern KEY_DELIMITER = Pattern
			.compile("\\$\\{\\w+\\}");

	private static final String TABLE_START_DELIMITER = "~~{table}";

	private static final String TABLE_END_DELIMITER = "~~{/table}";

	private static final Pattern IMAGES_DELIMITER = Pattern
			.compile("~~\\{images:(\\/*[\\w+\\$\\{\\}]\\/*)+\\}");

	private static final Pattern SCREENCAST_MATCHER = Pattern
			.compile("~~\\{screencast:(\\/*[\\w+\\_\\-\\/])*\\$\\{\\w+\\}");

	/**
	 * @param root
	 * @return boolean value whether the generation was successful or not.
	 * @throws IOException
	 * @throws UnsupportedCommandLineParameterException
	 * @throws MissingCommandLineParameterException
	 */
	public static boolean start(final RootDoc root) throws IOException,
			UnsupportedCommandLineParameterException,
			MissingCommandLineParameterException {

		/** process commandline-parameters and save them to class variables */
		processOptions(root.options());

		for (final ClassDoc classDoc : root.classes()) {
			processAnnotations(classDoc.tags(), classDoc.annotations(),
					evaluatePath(classDoc));

			final MethodDoc[] methods = classDoc.methods();
			for (final MethodDoc methodDoc : methods) {

				processAnnotations(methodDoc.tags(), methodDoc.annotations(),
						evaluatePath(classDoc));
			}
		}

		return true;
	}

	/**
	 * @param tags
	 * @param annotations
	 * @param destinationFolder
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected static void processAnnotations(final Tag[] tags,
			final AnnotationDesc[] annotations, final String destinationFolder)
			throws IOException, FileNotFoundException {
		for (final AnnotationDesc annotationDesc : annotations) {

			final String qualifiedAnnotationName = annotationDesc
					.annotationType().qualifiedName();

			if (!configuredAnnotations.containsKey(qualifiedAnnotationName)) {
				continue;
			}

			final Map<String, String> replacements = new HashMap<>();

			for (final Tag tag : tags) {
				String text = tag.text();
				final Matcher matcher = Pattern.compile("\n\\s*\n\\s+")
						.matcher(text);
				if (matcher.find()) {
					text = matcher.replaceAll("\n\n  ");
				}
				replacements.put(tag.name().replace("@", ""), text);
			}

			final File file = new File(destinationFolder);
			if (!file.exists()) {
				file.mkdirs();
			}

			for (ElementValuePair annotationElement : annotationDesc
					.elementValues()) {
				String key = annotationElement.element().name();
				String value = annotationElement.value().toString()
						.replaceAll("\"", "");
				replacements.put(key, value);
			}

			final String templatePath = configuredAnnotations
					.get(qualifiedAnnotationName);
			try (final BufferedReader reader = new BufferedReader(
					new FileReader(templatePath));

					final PrintWriter writer = new PrintWriter(new FileWriter(
							destinationFolder + "/" + replacements.get("id")
									+ ".apt"))) {

				boolean insideTable = false;
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.contains(TABLE_START_DELIMITER)) {
						insideTable = true;
					} else if (line.contains(TABLE_END_DELIMITER)) {
						insideTable = false;
					}
					line = processLine(line, insideTable, replacements);
					writer.println(line);
				}
				writer.close();
				reader.close();
			}
		}
	}

	protected static String processLine(final String line,
			final boolean insideTable, final Map<String, String> replacements) {
		String toPrint = line;

		// process screencast tags
		final Matcher screencastMatcher = SCREENCAST_MATCHER.matcher(line);
		if (screencastMatcher.find()) {
			return processScreencast(screencastMatcher.group(), replacements);
		}

		// process images tags
		final Matcher imageMatcher = IMAGES_DELIMITER.matcher(line);
		if (imageMatcher.find()) {
			return processImages(imageMatcher.group(), replacements);
		}

		// process "default" lines. replace placeholders.
		final Matcher matcher = KEY_DELIMITER.matcher(line);
		while (matcher.find()) {
			final String placeholder = matcher.group();
			final Matcher keyMatcher = Pattern.compile("\\w+").matcher(
					placeholder);
			final String key = keyMatcher.find() ? keyMatcher.group() : "";
			String replacement = replacements.containsKey(key) ? replacements
					.get(key) : "";

			// TODO: replace javadoc links

			// format text for tablecells.
			if (insideTable) {
				replacement = formatTextForTableCell(replacement);
			}
			// replace the line to print
			toPrint = toPrint.replace(placeholder, replacement);
		}

		// print the updated line
		return toPrint;
	}

	/**
	 * @param line
	 *            the inputline from the template
	 * @param replacements
	 *            the replacement parameters from the javadoc
	 * @return the replacement string for the screencasts
	 */
	protected static String processScreencast(final String line,
			final Map<String, String> replacements) {
		// 1. replace placeholders
		final Matcher matcher = KEY_DELIMITER.matcher(line);

		log.debug(line);

		String fileIdentifier = "";

		while (matcher.find()) {
			final String placeholder = matcher.group();
			final Matcher keyMatcher = Pattern.compile("\\w+").matcher(
					placeholder);
			final String key = keyMatcher.find() ? keyMatcher.group() : "";
			fileIdentifier = replacements.get(key);
		}

		// check if screencast is available
		boolean screencastExists = false;
		for (String supportedVideoType : SUPPORTED_VIDEO_TYPES) {
			if (new File(resourcesDir + "screencasts/" + fileIdentifier + "."
					+ supportedVideoType).exists()) {
				screencastExists = true;
				break;
			}
		}

		StringBuilder toPrint = new StringBuilder();

		if (screencastExists) {
			final StringBuilder snippet = new StringBuilder();
			snippet.append("<video width=\"800\" controls>");
			snippet.append("<source src=\"/screencasts/")
					.append(fileIdentifier)
					.append(".ogg\" type=\"video/ogg\">");
			snippet.append("<source src=\"/screencasts/")
					.append(fileIdentifier)
					.append(".webm\" type=\"video/webm\">");
			snippet.append("<source src=\"/screencasts/")
					.append(fileIdentifier)
					.append(".mp4\" type=\"video/mp4\">");
			snippet.append("Your Browser does not support the video tag.");
			snippet.append("</video>");

			final String snippetFileName = resourcesDir
					+ "generated/snippets/screencast_snippet_" + fileIdentifier
					+ ".txt";
			final File snippetFile = new File(snippetFileName);

			try {
				if (!snippetFile.exists()) {
					snippetFile.getParentFile().mkdirs();
					snippetFile.createNewFile();
				}

				try (final BufferedWriter bw = new BufferedWriter(
						new FileWriter(snippetFile.getAbsolutePath()))) {
					bw.write(snippet.toString());
				}
			} catch (final IOException e) {
				log.error(e);
			}

			toPrint.append("\n\n%{snippet|verbatim=false|file="
					+ snippetFileName + "}");
		} else {
			toPrint.append("\n\n  n/a"); // Print "n/a" in apt file.
		}
		return toPrint.toString();
	}

	/**
	 * @param line
	 *            the input line containing the folder to process
	 * @param replacements
	 *            the replacement parameters from the javadoc
	 * @return the replacement string for the images
	 */
	protected static String processImages(final String line,
			final Map<String, String> replacements) {

		// 1. replace placeholders
		String toPrint = line;

		final Matcher matcher = KEY_DELIMITER.matcher(line);

		while (matcher.find()) {
			final String placeholder = matcher.group();
			final Matcher keyMatcher = Pattern.compile("\\w+").matcher(
					placeholder);
			final String key = keyMatcher.find() ? keyMatcher.group() : "";
			final String replacement = replacements.containsKey(key) ? replacements
					.get(key) : "";

			toPrint = toPrint.replace(placeholder, replacement);
		}
		final Matcher pathMatcher = Pattern.compile(
				":(\\/*[\\w\\-\\_\\s]+\\/*)+").matcher(toPrint);
		String directoryPath = "";
		if (pathMatcher.find()) {
			directoryPath = pathMatcher.group();
		}

		// 3. load images
		directoryPath = directoryPath.replaceAll(":", "");
		final File imagesDir = new File(resourcesDir + directoryPath);

		final StringBuilder sb = new StringBuilder();
		if (imagesDir.exists()) {

			final List<File> listFiles = Arrays.asList(imagesDir.listFiles());
			Collections.sort(listFiles);
			sb.append("\n\n");
			for (final File file : listFiles) {

				final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
				mimetypesFileTypeMap.addMimeTypes("image png tif jpg jpeg bmp");

				final String type = mimetypesFileTypeMap.getContentType(file)
						.split("/")[0];

				if ("image".equals(type)) {
					sb.append("\n\n[")
							.append(file.getPath().replace(resourcesDir, "/"))
							.append("]");
				} else {
					log.debug(file.getName() + " has an unsupported filetype ["
							+ type + "]. skipping.");
				}
			}
		}

		if (sb.length() == 0) {
			sb.append("\n\n n/a"); // if no images are printed, add "n/a" to
									// stringbuilder.
		}

		// // 4. return apt-content
		return sb.toString();
	}

	/**
	 * @param replacement
	 * @return the formatted string usable in table cells of the apt-format
	 */
	private static String formatTextForTableCell(final String replacement) {
		final Matcher matcher = Pattern.compile("\\s+|\t|\n").matcher(
				replacement);

		if (matcher.find()) {
			return matcher.replaceAll(" ");
		}

		return replacement;
	}

	/**
	 * @param options
	 *            the command line parameter options provided with the javadoc
	 *            call
	 * @throws UnsupportedCommandLineParameterException
	 *             thrown if the provided commandline parameters do not fit the
	 *             requirements (too many or to few options per command)
	 * @throws MissingCommandLineParameterException
	 *             thrown if a parameter is missing.
	 */
	protected static void processOptions(final String[][] options)
			throws UnsupportedCommandLineParameterException,
			MissingCommandLineParameterException {
		for (final String[] strings : options) {
			final String tag = strings[0];
			switch (tag) {
			case "-output":
				validateNumberOfParameters(strings, 1);
				baseOutputDir = strings[1];
				break;
			case "-annotation":
				validateNumberOfParameters(strings, 2);
				configuredAnnotations.put(strings[1], strings[2]);
				break;
			case "-siteResources":
				validateNumberOfParameters(strings, 1);
				resourcesDir = strings[1];
				break;
			default:
				break;
			}
		}

		// validate if all parameters were provided
		if (baseOutputDir == null) {
			throw new MissingCommandLineParameterException(
					"-output parameter is required but not provided.");
		}
		if (configuredAnnotations.isEmpty()) {
			throw new MissingCommandLineParameterException(
					"at least one '-annotation' configuration is required but none is provided.");
		}

	}

	/**
	 * @param strings
	 * @throws UnsupportedCommandLineParameterException
	 */
	private static void validateNumberOfParameters(final String[] strings,
			final int expected) throws UnsupportedCommandLineParameterException {
		if (strings.length != expected + 1) {
			throw new UnsupportedCommandLineParameterException(
					"invalid number of arguments provided for output option. allowed is 1 parameter. found "
							+ expected + ".");
		}
	}

	/**
	 * @param classDoc
	 * @return the path where to save the generated files.
	 */
	private static String evaluatePath(final ClassDoc classDoc) {
		return baseOutputDir
				+ classDoc.containingPackage().name().replaceAll("\\.", "/");

	}

	/**
	 * @param option
	 * @return the number of parameters.
	 */
	public static int optionLength(final String option) {
		if ("-output".equals(option)) {
			return 2;
		}
		if ("-annotation".equals(option)) {
			return 3;
		}
		if ("-siteResources".equals(option)) {
			return 2;
		}
		return 0;
	}
}