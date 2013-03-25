package ch.puzzle.doclet;

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

import org.apache.commons.io.FileUtils;

import ch.puzzle.annotations.Page;
import ch.puzzle.annotations.TestCase;
import ch.puzzle.annotations.UseCase;
import ch.puzzle.doclet.exceptions.MissingCommandLineParameterException;
import ch.puzzle.doclet.exceptions.UnsupportedCommandLineParameterException;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
public class AnnotationDrivenDoclet {

	/** Log4j Logger. */
	// private static final Logger LOG = Logger
	// .getLogger(AnnotationDrivenDoclet.class);

	/** File formats that are supported for embedding videos in documentation. */
	private static final String[] SUPPORTED_VIDEO_TYPES = { "ogg", "mp4", "webm" };

	/**
	 * The base output directory where the apt-files are stored. The value is
	 * passed by the commandline parameter '-output' for the javadoc command.
	 * 
	 * e.g. javadoc -output ~/docs
	 */
	static String baseOutputDir;

	/**
	 * The resource directory of the project. The parameter "-resourceDir" has
	 * to be set as parameter if does not match the maven default of
	 * src/site/resources.
	 */
	static String resourcesDir = "src/site/resources/";

	/**
	 * Map of configured annotations. stores which template to use for which
	 * annotation. The values are passed by at least one commandline parameter
	 * '-annotation'. One parameter for each used annotation is required.
	 * 
	 * e.g. javadoc -annotation ch.puzzle.example.Page
	 * ${basedir}/src/site/apt/templates/pages.apt.template
	 */
	static Map<String, String> configuredAnnotations = new HashMap<>();
	static {
		configuredAnnotations.put(TestCase.class.getName(), "testcase.apt.template");
		configuredAnnotations.put(UseCase.class.getName(), "usecase.apt.template");
		configuredAnnotations.put(Page.class.getName(), "page.apt.template");
	}

	/** Regex pattern to find placeholders in template file. */
	private static final Pattern KEY_DELIMITER = Pattern.compile("\\$\\{\\w+\\}");

	/** Delimiter to find tables inside the template file. */
	private static final String TABLE_START_DELIMITER = "~~{table}";

	/** Delimiter to find table ends inside the template file. */
	private static final String TABLE_END_DELIMITER = "~~{/table}";

	/** Regex pattern to find image placeholders inside the template file. */
	private static final Pattern IMAGES_DELIMITER = Pattern.compile("~~\\{images:(\\/*[\\w+\\$\\{\\}]\\/*)+\\}");

	/** Regex pattern to find screencast placeholders inside the template file. */
	private static final Pattern SCREENCAST_MATCHER = Pattern
			.compile("~~\\{screencast:(\\/*[\\w+\\_\\-\\/])*\\$\\{\\w+\\}");

	/**
	 * @param root
	 *            {@link RootDoc} document to start doclet generation from.
	 * @return boolean value whether the generation was successful or not.
	 * @throws IOException
	 * @throws UnsupportedCommandLineParameterException
	 * @throws MissingCommandLineParameterException
	 * @throws FileNotFoundException
	 */
	public static boolean start(final RootDoc root) throws UnsupportedCommandLineParameterException,
			MissingCommandLineParameterException, FileNotFoundException, IOException {

		/** process commandline-parameters and save them to class variables */
		processOptions(root.options());

		for (final ClassDoc classDoc : root.classes()) {
			processAnnotations(classDoc.tags(), classDoc.annotations(), evaluatePath(classDoc));

			final MethodDoc[] methods = classDoc.methods();
			for (final MethodDoc methodDoc : methods) {

				processAnnotations(methodDoc.tags(), methodDoc.annotations(), evaluatePath(classDoc));
			}
		}

		return true;
	}

	/**
	 * @param tags
	 *            the Tags to process.
	 * @param annotations
	 *            the annotations to process.
	 * @param destinationFolder
	 *            the directory where to put the generated files.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	static void processAnnotations(final Tag[] tags, final AnnotationDesc[] annotations, final String destinationFolder)
			throws IOException, FileNotFoundException {
		for (final AnnotationDesc annotationDesc : annotations) {

			final String qualifiedAnnotationName = annotationDesc.annotationType().qualifiedName();

			if (!configuredAnnotations.containsKey(qualifiedAnnotationName)) {
				continue;
			}

			final Map<String, String> replacements = new HashMap<>();

			for (final Tag tag : tags) {
				String text = tag.text();
				final Matcher matcher = Pattern.compile("\n\\s*\n\\s+").matcher(text);
				if (matcher.find()) {
					text = matcher.replaceAll("\n\n  ");
				}
				replacements.put(tag.name().replace("@", ""), text);
			}

			final File file = new File(destinationFolder);
			if (!file.exists()) {
				file.mkdirs();
			}

			for (final ElementValuePair annotationElement : annotationDesc.elementValues()) {
				final String key = annotationElement.element().name();
				final String value = annotationElement.value().toString().replaceAll("\"", "");
				replacements.put(key, value);
			}

			final String templatePath = configuredAnnotations.get(qualifiedAnnotationName);
			final String destinationFile = destinationFolder + "/" + replacements.get("id") + ".apt";
			try (final BufferedReader reader = new BufferedReader(new FileReader(templatePath));

			final PrintWriter writer = new PrintWriter(new FileWriter(destinationFile))) {

				boolean insideTable = false;
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.contains(TABLE_START_DELIMITER)) {
						insideTable = true;
					}
					else if (line.contains(TABLE_END_DELIMITER)) {
						insideTable = false;
					}
					line = processLine(line, insideTable, replacements);
					writer.println(line);
				}
			}
			catch (final FileNotFoundException e) {
				final StringBuilder sb = new StringBuilder();
				sb.append(replacements.get("id"));

				for (final Tag tag : tags) {
					sb.append("\n\n* ").append(tag.name());
					sb.append("\n\n  ").append(tag.text());
				}

				FileUtils.writeStringToFile(new File(destinationFile), sb.toString());

			}
		}
	}

	/**
	 * @param line
	 *            the input line to process.
	 * @param insideTable
	 *            boolean value whether the current line is between a
	 *            TABLE_START_DELIMITER and a TABLE_END_DELIMITER.
	 * @param replacements
	 *            map with the replacement keys found in javadoc.
	 * @return the processed line with all replacements done.
	 */
	static String processLine(final String line, final boolean insideTable, final Map<String, String> replacements) {
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
			final Matcher keyMatcher = Pattern.compile("\\w+").matcher(placeholder);
			final String key = keyMatcher.find() ? keyMatcher.group() : "";
			String replacement = replacements.containsKey(key) ? replacements.get(key) : "";

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
	static String processScreencast(final String line, final Map<String, String> replacements) {
		// 1. replace placeholders
		final Matcher matcher = KEY_DELIMITER.matcher(line);

		// LOG.debug(line);

		String fileIdentifier = "";

		while (matcher.find()) {
			final String placeholder = matcher.group();
			final Matcher keyMatcher = Pattern.compile("\\w+").matcher(placeholder);
			final String key = keyMatcher.find() ? keyMatcher.group() : "";
			fileIdentifier = replacements.get(key);
		}

		// check if screencast is available
		boolean screencastExists = false;
		for (final String supportedVideoType : SUPPORTED_VIDEO_TYPES) {
			if (new File(resourcesDir + "screencasts/" + fileIdentifier + "." + supportedVideoType).exists()) {
				screencastExists = true;
				break;
			}
		}

		final StringBuilder toPrint = new StringBuilder();

		if (screencastExists) {
			final StringBuilder snippet = new StringBuilder();
			snippet.append("<video width=\"800\" controls>");

			for (final String supportedVideoFormat : SUPPORTED_VIDEO_TYPES) {
				snippet.append("<source src=\"/screencasts/").append(fileIdentifier).append(".")
						.append(supportedVideoFormat).append("\" type=\"video/").append(supportedVideoFormat)
						.append("\">");
			}

			snippet.append("Your Browser does not support the video tag.");
			snippet.append("</video>");

			final String snippetFileName = resourcesDir + "generated/snippets/screencast_snippet_" + fileIdentifier
					+ ".txt";
			final File snippetFile = new File(snippetFileName);

			try {
				if (!snippetFile.exists()) {
					snippetFile.getParentFile().mkdirs();
					snippetFile.createNewFile();
				}

				try (final BufferedWriter bw = new BufferedWriter(new FileWriter(snippetFile.getAbsolutePath()))) {
					bw.write(snippet.toString());
				}
			}
			catch (final IOException e) {
				e.printStackTrace();
				// FIXME
				// LOG.error(e);
			}

			toPrint.append("\n\n%{snippet|verbatim=false|file=" + snippetFileName + "}");
		}
		else {
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
	static String processImages(final String line, final Map<String, String> replacements) {

		// 1. replace placeholders
		String toPrint = line;

		final Matcher matcher = KEY_DELIMITER.matcher(line);

		while (matcher.find()) {
			final String placeholder = matcher.group();
			final Matcher keyMatcher = Pattern.compile("\\w+").matcher(placeholder);
			final String key = keyMatcher.find() ? keyMatcher.group() : "";
			final String replacement = replacements.containsKey(key) ? replacements.get(key) : "";

			toPrint = toPrint.replace(placeholder, replacement);
		}
		final Matcher pathMatcher = Pattern.compile(":(\\/*[\\w\\-\\_\\s]+\\/*)+").matcher(toPrint);
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

				final String type = mimetypesFileTypeMap.getContentType(file).split("/")[0];

				if ("image".equals(type)) {
					sb.append("\n\n[").append(file.getPath().replace(resourcesDir, "/")).append("]");
				}
				else {

					// LOG.debug(file.getName() +
					// " has an unsupported filetype ["
					// + type + "]. skipping.");
					System.out.println(file.getName() + " has an unsupported filetype [" + type + "]. skipping.");
				}
			}
		}

		if (sb.length() == 0) {
			// if no images are printed, add "n/a" to stringbuilder.
			sb.append("\n\n n/a");
		}

		// // 4. return apt-content
		return sb.toString();
	}

	/**
	 * Removes newlines from the input String. This is used, because the
	 * apt-format from maven brings some limitations to multiline cells inside
	 * tables.
	 * 
	 * Replaces new Lines (\n), Tabs (\t) and multi whitespaces (\\s+) with a
	 * single whitespace.
	 * 
	 * @param inputString
	 *            the inputString to replace newlines
	 * @return the formatted string usable in table cells of the apt-format
	 */
	private static String formatTextForTableCell(final String inputString) {
		final Matcher matcher = Pattern.compile("\\s+|\t|\n").matcher(inputString);

		if (matcher.find()) {
			return matcher.replaceAll(" ");
		}

		return inputString;
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
	static void processOptions(final String[][] options) throws UnsupportedCommandLineParameterException,
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
			throw new MissingCommandLineParameterException("-output parameter is required but not provided.");
		}
		if (configuredAnnotations.isEmpty()) {
			throw new MissingCommandLineParameterException(
					"at least one '-annotation' configuration is required but none is provided.");
		}

	}

	/**
	 * @param strings
	 *            the provided parameters as string array.
	 * @param expected
	 *            the number of expected parameter values
	 * @throws UnsupportedCommandLineParameterException
	 *             if the number of provided parameters do not match the given
	 *             parameters.
	 */
	private static void validateNumberOfParameters(final String[] strings, final int expected)
			throws UnsupportedCommandLineParameterException {
		if (strings.length != expected + 1) {
			throw new UnsupportedCommandLineParameterException(
					"invalid number of arguments provided for output option. allowed is 1 parameter. found " + expected
							+ ".");
		}
	}

	/**
	 * @param classDoc
	 *            the classDoc to evaluate the destination path from.
	 * @return the path where to save the generated files.
	 */
	private static String evaluatePath(final ClassDoc classDoc) {
		// FIXME: windows paths.
		return baseOutputDir + classDoc.containingPackage().name().replaceAll("\\.", "/");

	}

	/**
	 * This method is required from the {@link Doclet} to enable commandline
	 * parameter.
	 * 
	 * @param option
	 *            the provided commandline parameter option.
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