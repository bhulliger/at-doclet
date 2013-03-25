package ch.puzzle.doc.reports;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public class TestResultSaver {

	/**
	 * The Testresult matrix as a map where the key of the outer map is the
	 * Release, the Key of the inner map is the id of the testcase.
	 */
	private final Map<String, Map<String, TestResult>> result = new HashMap<>();

	/** singleton instance of the testresult saver. */
	private static TestResultSaver instance;

	/**
	 * Constructor.
	 */
	private TestResultSaver() {
		// do nothing
	}

	/**
	 * @return singleton instance of a {@link TestResultSaver}
	 */
	public static TestResultSaver getInstance() {
		if (instance == null) {
			instance = new TestResultSaver();
		}
		return instance;
	}

	/**
	 * Adds the given Result to the result map for later savings.
	 * 
	 * @param build
	 *            the build name under which to save the testresult for.
	 * @param testCase
	 *            the test case to save the result for
	 * @param result
	 *            the {@link TestResult}
	 */
	private void setResult(final String build, final String testCase,
			final TestResult result) {
		if (this.result.get(build) == null) {
			this.result.put(build, new HashMap<String, TestResult>());
		}
		this.result.get(build).put(testCase, result);
	}

	/**
	 * Updates the result of the given TestCase in the given build ID to
	 * {@link TestResult#FAILED}.
	 * 
	 * @param build
	 *            the build for which to update the testresult
	 * @param testCase
	 *            the testcase to set the result
	 */
	public void failed(final String build, final String testCase) {
		this.setResult(build, testCase, TestResult.FAILED);
	}

	/**
	 * Updates the result of the given TestCase in the given build ID to
	 * {@link TestResult#SUCCEEDED}.
	 * 
	 * @param build
	 *            the build for which to update the testresult
	 * @param testCase
	 *            the testcase to set the result
	 */
	public void succeeded(final String build, final String testCase) {
		this.setResult(build, testCase, TestResult.SUCCEEDED);
	}

	/**
	 * Reads the persisted testreport (configured CSV File) and updates it with
	 * the new result set.
	 * 
	 * @return the updated testresult.
	 */
	public Map<String, Map<String, TestResult>> updateTestReport() {
		final Map<String, Map<String, TestResult>> testReport = TestReportParser
				.parseExistingTestReport();

		// append new results
		for (final Entry<String, Map<String, TestResult>> entry : this.result
				.entrySet()) {
			final String key = entry.getKey();
			final Map<String, TestResult> value = entry.getValue();
			if (testReport.containsKey(key)) {
				for (final Entry<String, TestResult> toOverride : value
						.entrySet()) {
					testReport.get(key).put(toOverride.getKey(),
							toOverride.getValue());
				}
			} else {
				testReport.put(key, value);
			}
		}

		return testReport;
	}

	/**
	 * Updates the testreport file and persists it again.
	 */
	public void updateAndSaveTestReport() {
		TestReportParser.saveTestReport(this.updateTestReport());
	}

}