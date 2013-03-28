package ch.puzzle.doc.reports;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import ch.puzzle.util.DocletPropertyUtils;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
public class TestReportParser {

	/**
	 * Empty default constructor.
	 */
	private TestReportParser() {
		// empty default constructor
	}

	private static final Logger LOG = Logger.getAnonymousLogger();

	/**
	 * Saves the given testReport Data to the Test Report File.
	 * 
	 * @param testReport
	 *            the inmemory testreport to persist.
	 */
	static void saveTestReport(
			final Map<String, Map<String, TestResult>> testReport) {

		final List<String> releases = new ArrayList<>(testReport.keySet());
		Collections.sort(releases);

		final Set<String> testCaseSet = new HashSet<>();

		// collect all testcases that ever existed.
		for (final Map<String, TestResult> testRun : testReport.values()) {
			testCaseSet.addAll(testRun.keySet());
		}

		final List<String> testCases = new ArrayList<>(testCaseSet);
		Collections.sort(testCases);

		// init csvwriter
		final File file = initFile();
		if (file == null) {
			LOG.log(Level.WARNING,
					"Could not create output file for testreport. aborting...");
			return;
		}

		try (final CSVWriter writer = new CSVWriter(new FileWriter(file))) {
			// final File file = initFile();

			// print header row
			final String[] headerRow = new String[releases.size() + 1];
			for (int i = 0; i < releases.size(); i++) {
				headerRow[i + 1] = releases.get(i);
			}
			writer.writeNext(headerRow);

			// print test cases

			for (int i = 0; i < testCases.size(); i++) {
				final String[] testCaseRow = new String[releases.size() + 1];
				final String currentTestCase = testCases.get(i);
				testCaseRow[0] = currentTestCase;

				for (int column = 1; column <= releases.size(); column++) {
					final String release = releases.get(column - 1);
					if (testReport.get(release).containsKey(currentTestCase)) {
						testCaseRow[column] = testReport.get(release)
								.get(currentTestCase).getText();
					} else {
						testCaseRow[column] = TestResult.NOT_AVAILABLE
								.getText();
					}
				}

				writer.writeNext(testCaseRow);
			}

			writer.close();

			LOG.log(Level.INFO, "successfully written testreport file to: "
					+ file.getAbsolutePath());

		} catch (final IOException e) {
			LOG.log(Level.WARNING, "Resulting Testreport cannot be saved.", e);
		}

	}

	/**
	 * Searches for a Test report File and reads it.
	 * 
	 * @return the parsed testresult.csv as a map
	 */
	public static Map<String, Map<String, TestResult>> parseExistingTestReport() {
		final Map<String, Map<String, TestResult>> testReport = new HashMap<>();
		final List<String> releases = new ArrayList<>();

		try {

			final File file = initFile();

			try (final CSVReader reader = new CSVReader(new FileReader(file))) {
				final List<String[]> allRows = reader.readAll();

				if (!allRows.isEmpty()) {
					// init releases
					for (int i = 1; i < allRows.get(0).length; i++) {
						releases.add(allRows.get(0)[i]);
					}

					for (int i = 1; i < allRows.size(); i++) {
						// parse result
						final String[] currentRow = allRows.get(i);
						if (currentRow.length < (releases.size() + 1)) {
							LOG.log(Level.WARNING,
									"Invalid format. Aborting...");
							break;
						}
						final String testCase = currentRow[0];
						// parse through columns
						for (int j = 1; j < currentRow.length; j++) {
							final String release = releases.get(j - 1);
							final TestResult testResult = TestResult
									.getInstanceByValue(currentRow[j]);

							if (testReport.get(release) == null) {
								testReport.put(release,
										new HashMap<String, TestResult>());
							}

							testReport.get(release).put(testCase, testResult);
						}
					}
				}
				reader.close();
			}

		} catch (final Exception e) {
			LOG.log(Level.WARNING,
					"Could not parse existing testreport. File could not be read.",
					e);
		}

		return testReport;
	}

	/**
	 * @return the existing file with the testreports or a new empty file, if it
	 *         doesn't exist yet.
	 * 
	 * @throws IOException
	 *             thrown if the file did not exist yet but cannot be created
	 *             either.
	 */
	private static File initFile() {
		File file = null;
		final String testreportfile = DocletPropertyUtils
				.getPropertyValue("site.report.testreport");
		try {
			file = new File(testreportfile);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
		} catch (final IOException e) {
			LOG.log(Level.WARNING, "Could not create file in " + testreportfile
					+ ". ", e);
		}
		// TODO: refactor. potential nullpointer
		return file;
	}
}
