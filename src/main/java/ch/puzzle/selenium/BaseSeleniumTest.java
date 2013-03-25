package ch.puzzle.selenium;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import ch.puzzle.annotations.TestCase;
import ch.puzzle.doc.reports.ReportWatcher;
import ch.puzzle.doc.reports.TestResultSaver;
import ch.puzzle.doc.screencasts.ScreencastWatcher;
import ch.puzzle.doc.screenshots.ScreenshotWatcher;
import ch.puzzle.selenium.screenshots.ScreenshotDriver;
import ch.puzzle.util.DocletPropertyUtils;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public abstract class BaseSeleniumTest {

	/** Log4J Logger. */
	private static final Logger LOG = Logger.getLogger(BaseSeleniumTest.class);

	/** static driver instance. */
	private static WebDriver driverInstance;

	private static Properties seleniumProperties;

	public static TestCase currentTestCase = null;

	@Rule
	public TestWatcher currentTest = new TestWatcher() {

		@Override
		protected void starting(final Description description) {
			BaseSeleniumTest.currentTestCase = description.getAnnotation(TestCase.class);
			super.starting(description);
		}
	};

	@Rule
	public ReportWatcher reportWatcher = new ReportWatcher() {

		@Override
		public String getCurrentReleaseBuild() {
			return new SimpleDateFormat("dd.MM.yyyy").format(new Date());
		}
	};

	@Rule
	public ScreenshotWatcher screenshotWatcher = new ScreenshotWatcher();

	@Rule
	public ScreencastWatcher screencastWatcher = new ScreencastWatcher();

	/**
	 * close all drivers and windows after the test class has finished.
	 */
	@AfterClass
	public static void closeBrowser() {
		if (driverInstance != null) {
			driverInstance.close();
			driverInstance.quit();
			driverInstance = null;
		}
	}

	/**
	 * Update the testreport file (as configured in the config.properties file).
	 */
	@AfterClass
	public static void updateReports() {
		if ("on".equals(DocletPropertyUtils.getPropertyValue("reports"))) {
			TestResultSaver.getInstance().updateAndSaveTestReport();
		}
	}

	/**
	 * @return the initialized (singleton) driver for RE7
	 */
	protected static WebDriver getDriver() {
		if (driverInstance == null) {
			driverInstance = initDriver();
		}
		return driverInstance;
	}

	/**
	 * initialize the driver as defined in the properties file.
	 * 
	 * @return the initialized driver.
	 */
	protected static WebDriver initDriver() {
		WebDriver driver = null;

		if ("on".equals(DocletPropertyUtils.getPropertyValue("screenshots"))) {
			driver = new ScreenshotDriver();
			return driver;
		}

		if ("on".equals(getPropertyValue("headless"))) {

			final String xPort = System.getProperty("limportal.xvfb.id", ":99");
			final File firefoxPath = new File(System.getProperty("limportal.deploy.firefox.path", "/usr/bin/firefox"));
			final FirefoxBinary firefoxBinary = new FirefoxBinary(firefoxPath);
			firefoxBinary.setEnvironmentProperty("DISPLAY", xPort);

			driver = new FirefoxDriver(firefoxBinary, null);

			return driver;
		}

		// headless firefox driver
		if ("org.openqa.selenium.chrome.ChromeDriver".equals(getPropertyValue("driverClass"))) {
			final DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability("chrome.binary", getPropertyValue("chrome.binary"));
			final String webdriverLocation = getPropertyValue("chrome.webdriver.binary");
			if (webdriverLocation != null) {
				System.setProperty("webdriver.chrome.driver", webdriverLocation);
			}
			driver = new ChromeDriver(capabilities);
		}
		else {
			try {
				driver = (WebDriver) Class.forName(getPropertyValue("driverClass")).newInstance();
				driver.manage().window().maximize();
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
			}
			catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				LOG.error(e);
			}
		}

		// enable javascript for htmlUntidriver
		if (driver instanceof HtmlUnitDriver) {
			final HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
			htmlUnitDriver.setJavascriptEnabled(true);
		}

		return driver;
	}

	/**
	 * @param propertyKey
	 * @return propertyValue
	 */
	private static String getPropertyValue(final String propertyKey) {

		if (seleniumProperties == null) {
			seleniumProperties = new Properties();
			try (final InputStream resourceAsStream = DocletPropertyUtils.class
					.getResourceAsStream("/selenium.properties")) {
				seleniumProperties.load(resourceAsStream);
				resourceAsStream.close();
			}
			catch (final IOException e) {
				LOG.error("could not read property file [selenium.properties]", e);
			}
		}

		return seleniumProperties.getProperty(propertyKey);
	}

	/**
	 * @return the currently running test case
	 */
	public static TestCase getCurrentTestCase() {
		return BaseSeleniumTest.currentTestCase;
	}

}
