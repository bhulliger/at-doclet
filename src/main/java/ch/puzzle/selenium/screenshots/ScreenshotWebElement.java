package ch.puzzle.selenium.screenshots;

import static org.apache.commons.io.FileUtils.copyFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * A wrapper around a Selenium {@link WebElement}. Used to take Screenshots. A
 * AOP Pointcut is added around certain actions (@see {@link ScreenshotAspect}).
 * 
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
public class ScreenshotWebElement implements WebElement {

	/** Log4j Logger. */
	private static final Logger LOG = Logger
			.getLogger(ScreenshotWebElement.class);

	/** the selenium {@link WebElement} that get wrapped. */
	private final WebElement element;

	/** the driver used. */
	private final WebDriver driver;

	/**
	 * Constructor.
	 * 
	 * @param element
	 *            the {@link WebElement} to wrap.
	 * @param driver
	 *            the {@link WebDriver} used to take screenshots.
	 */
	public ScreenshotWebElement(final WebElement element, final WebDriver driver) {
		this.element = element;
		this.driver = driver;
	}

	/**
	 * @see whatever.util.screenshots.Screenshotable#takeScreenshot(java.lang.String)
	 */
	public void takeScreenshot(final String destinationFile) {
		try {
			final File srcFile = ((TakesScreenshot) this.driver)
					.getScreenshotAs(OutputType.FILE);

			copyFile(srcFile, new File(destinationFile));

		} catch (final IOException e) {
			LOG.error("could not copy file. ", e);
		}
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#click()
	 */
	@Override
	public void click() {
		this.element.click();

	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#submit()
	 */
	@Override
	public void submit() {
		this.element.submit();

	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#sendKeys(java.lang.CharSequence[])
	 */
	@Override
	public void sendKeys(final CharSequence... keysToSend) {
		this.element.sendKeys(keysToSend);

	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#clear()
	 */
	@Override
	public void clear() {
		this.element.clear();

	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#getTagName()
	 */
	@Override
	public String getTagName() {
		return this.element.getTagName();
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(final String name) {
		return this.element.getAttribute(name);
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#isSelected()
	 */
	@Override
	public boolean isSelected() {
		return this.element.isSelected();
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return this.element.isEnabled();
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#getText()
	 */
	@Override
	public String getText() {
		return this.element.getText();
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#findElements(org.openqa.selenium.By)
	 */
	@Override
	public List<WebElement> findElements(final By by) {
		return this.element.findElements(by);
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#findElement(org.openqa.selenium.By)
	 */
	@Override
	public WebElement findElement(final By by) {
		return this.element.findElement(by);
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#isDisplayed()
	 */
	@Override
	public boolean isDisplayed() {
		return this.element.isDisplayed();
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#getLocation()
	 */
	@Override
	public Point getLocation() {
		return this.element.getLocation();
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#getSize()
	 */
	@Override
	public Dimension getSize() {
		return this.element.getSize();
	}

	/**
	 * Overridden to add AOP-Pointcut around it.
	 * 
	 * @see org.openqa.selenium.WebElement#getCssValue(java.lang.String)
	 */
	@Override
	public String getCssValue(final String propertyName) {
		return this.element.getCssValue(propertyName);
	}

}
