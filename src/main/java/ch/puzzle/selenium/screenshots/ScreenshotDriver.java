package ch.puzzle.selenium.screenshots;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Driver used to take screenshots. It wrappes a {@link FirefoxDriver} and adds
 * a pointcut around (@see {@link ScreenshotAspect}).
 * 
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public class ScreenshotDriver extends FirefoxDriver {

	/**
	 * must be overridden to enable aspects.
	 * 
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElement(org.openqa.selenium.By)
	 */
	@Override
	public WebElement findElement(final By by) {
		return new ScreenshotWebElement(super.findElement(by), this);
	}

}