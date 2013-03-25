package ch.puzzle.doc.screencasts;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import ch.puzzle.annotations.TestCase;
import ch.puzzle.util.DocletPropertyUtils;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 * 
 */
public class ScreencastWatcher extends TestWatcher {

	/** Log4j Logger. */
	private static final Logger LOG = Logger.getLogger(ScreencastWatcher.class);

	/** Montemedia Screenrecorder to take screencast with. */
	private ScreenRecorder screenRecorder;

	/**
	 * @see org.junit.rules.TestWatcher#starting(org.junit.runner.Description)
	 */
	@Override
	protected void starting(final Description description) {
		if (!"on".equals(DocletPropertyUtils.getPropertyValue("screencasts"))) {
			return;
		}
		try {
			// Create a instance of GraphicsConfiguration to get the Graphics
			// configuration
			// of the Screen. This is needed for ScreenRecorder class.
			final GraphicsConfiguration gc = GraphicsEnvironment//
					.getLocalGraphicsEnvironment()//
					.getDefaultScreenDevice()//
					.getDefaultConfiguration();

			final Format fileFormat = new Format(FormatKeys.MediaTypeKey,
					MediaType.FILE, FormatKeys.MimeTypeKey, FormatKeys.MIME_AVI);

			final Format screenFormat = new Format(FormatKeys.MediaTypeKey,
					MediaType.VIDEO, FormatKeys.EncodingKey,
					VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
					VideoFormatKeys.CompressorNameKey,
					VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
					VideoFormatKeys.DepthKey, Integer.valueOf(24),
					FormatKeys.FrameRateKey, Rational.valueOf(15),
					VideoFormatKeys.QualityKey, Float.valueOf(1.0f),
					FormatKeys.KeyFrameIntervalKey, Integer.valueOf(15 * 60));

			final Format mouseFormat = new Format(FormatKeys.MediaTypeKey,
					MediaType.VIDEO, FormatKeys.EncodingKey, "black",
					FormatKeys.FrameRateKey, Rational.valueOf(30));

			final Format audioFormat = null;

			this.screenRecorder = new ScreenRecorder(gc, fileFormat,
					screenFormat, mouseFormat, audioFormat);

			// Call the start method of ScreenRecorder to begin recording
			this.screenRecorder.start();
		} catch (IOException | AWTException e) {
			LOG.error(e);
		}

		super.starting(description);
	}

	/**
	 * @see org.junit.rules.TestWatcher#finished(org.junit.runner.Description)
	 */
	@Override
	protected void finished(final Description description) {

		final String screencastProperty = DocletPropertyUtils
				.getPropertyValue("screencasts");
		if (!"on".equals(screencastProperty)) {
			return;
		}
		final String testcaseId = description.getAnnotation(TestCase.class)
				.id();
		// Call the stop method of ScreenRecorder to end the recording
		File destinationFile;
		try {
			this.screenRecorder.stop();
			final String destinationDir = DocletPropertyUtils
					.getPropertyValue("site.resources.output.screencasts");
			final File sourceFile = this.screenRecorder.getCreatedMovieFiles()
					.get(0);

			destinationFile = new File(destinationDir + testcaseId + ".avi");

			if (destinationFile.exists()) {
				destinationFile.delete();
			}
			FileUtils.moveFile(sourceFile, destinationFile);
		} catch (final IOException e) {
			LOG.error("could not move generated screencast.", e);
			return;
		}

		// convert to ogg
		try {
			Runtime.getRuntime().exec(
					"ffmpeg -i " + destinationFile.getAbsolutePath() + " "
							+ destinationFile.getParentFile().getAbsolutePath()
							+ "/" + testcaseId + ".ogg");

		} catch (final IOException e) {
			LOG.warn(
					"install ffmpeg if you want to include the screencast in your documentation as video.",
					e);
		}

		super.finished(description);
	}
}
