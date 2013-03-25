package ch.puzzle.selenium.screenshots;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import ch.puzzle.annotations.TestCase;
import ch.puzzle.selenium.BaseSeleniumTest;
import ch.puzzle.util.DocletPropertyUtils;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
@Aspect
public class ScreenshotAspect {

	@Pointcut("execution(public * org.openqa.selenium.WebElement.submit())	|| execution(public * org.openqa.selenium.WebElement.click())")
	public void submitOrClick() {
		// pointcut method.
	}

	@After("submitOrClick()")
	public void submitOrClickAfter(final JoinPoint thisJoinPoint)
			throws Throwable {
		((ScreenshotWebElement) thisJoinPoint.getThis()).takeScreenshot(this
				.getFilePath());
	}

	@Before("submitOrClick()")
	public void submitOrClickBefore(final JoinPoint thisJoinPoint)
			throws Throwable {
		((ScreenshotWebElement) thisJoinPoint.getThis()).takeScreenshot(this
				.getFilePath());
	}

	/**
	 * @param testCase
	 * @return the path where to put the screenshot
	 */
	private String getFilePath() {
		final TestCase testCase = BaseSeleniumTest.getCurrentTestCase();
		final String screenshotDirectory = DocletPropertyUtils
				.getPropertyValue("site.resources.output.screenshots");
		final StringBuilder sb = new StringBuilder(screenshotDirectory);
		if (testCase != null) {
			// FIXME: windows paths
			sb.append(testCase.id()).append("/");
			sb.append(testCase.useCase().getSimpleName());
			sb.append("_");
			sb.append(testCase.id());
			sb.append("-");
		}
		sb.append(new SimpleDateFormat("yyyyMMdd-HHmmss_S").format(new Date()));

		sb.append(".png");
		return sb.toString();
	}
}
