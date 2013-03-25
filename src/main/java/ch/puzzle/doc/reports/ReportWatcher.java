package ch.puzzle.doc.reports;

import static ch.puzzle.util.DocletPropertyUtils.getPropertyValue;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import ch.puzzle.annotations.TestCase;

/**
 * Saves the TestResult to the testresult.csv file.
 * 
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public abstract class ReportWatcher extends TestWatcher {

	/**
	 * @see org.junit.rules.TestWatcher#succeeded(org.junit.runner.Description)
	 */
	@Override
	protected void succeeded(final Description description) {
		if ("on".equals(getPropertyValue("reports"))) {
			TestResultSaver.getInstance().succeeded(
					this.getCurrentReleaseBuild(),
					description.getAnnotation(TestCase.class).id());
		}
		super.succeeded(description);
	}

	/**
	 * @see org.junit.rules.TestWatcher#failed(java.lang.Throwable,
	 *      org.junit.runner.Description)
	 */
	@Override
	protected void failed(final Throwable e, final Description description) {
		if ("on".equals(getPropertyValue("reports"))) {
			TestResultSaver.getInstance().failed(this.getCurrentReleaseBuild(),
					description.getAnnotation(TestCase.class).id());
		}
		super.failed(e, description);
	}

	/**
	 * Identfier for the testreport columns. Ususally the build identifier.
	 * 
	 * @return build Identifier as String.
	 */
	public abstract String getCurrentReleaseBuild();

}
