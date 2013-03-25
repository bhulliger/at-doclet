package ch.puzzle.doc.reports;

/**
 * Enum for the testresult in the report.
 * 
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public enum TestResult {

	/** test run successfully. */
	SUCCEEDED("OK"),

	/** test run failed. */
	FAILED("NOK"),

	/**
	 * no test result available. this can have two reasons: The test did not
	 * exist at the time the run was made or the test did not run for the
	 * release.
	 */
	NOT_AVAILABLE("n/a");

	/** the printed text. */
	private String text;

	/**
	 * @param txt
	 *            the printed text.
	 */
	private TestResult(final String txt) {
		this.text = txt;
	}

	/**
	 * @param value
	 *            the text to search for.
	 * @return the {@link TestResult} with the given value as text.
	 * @throws IllegalArgumentException
	 *             if no {@link TestResult} with the given value exists.
	 */
	public static TestResult getInstanceByValue(final String value) {
		for (final TestResult testResult : values()) {
			if (value.equals(testResult.text)) {
				return testResult;
			}
		}
		throw new IllegalArgumentException("unsupported value: " + value);
	}

	/**
	 * @return text
	 */
	public String getText() {
		return this.text;
	}

}
