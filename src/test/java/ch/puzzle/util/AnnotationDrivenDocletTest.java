package ch.puzzle.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
public class AnnotationDrivenDocletTest {

	/**
	 * reset static fields
	 */
	@After
	public void tearDown() {
		AnnotationDrivenDoclet.configuredAnnotations = new HashMap<>();
		AnnotationDrivenDoclet.baseOutputDir = null;
		AnnotationDrivenDoclet.resourcesDir = "src/site/resources/";
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#start(com.sun.javadoc.RootDoc)}
	 * .
	 */
	@Test
	public void shouldProcessCommandLineParametersInDoclet() throws Exception {
		// GIVEN
		final RootDoc root = Mockito.mock(RootDoc.class);
		final String[][] providedCommandLineParameters = {
				{ "-output", "anywhere" },
				{ "-annotation", "class1", "template1" },
				{ "-siteResources", "anywhere/else" } };

		Mockito.when(root.options()).thenReturn(providedCommandLineParameters);
		Mockito.when(root.classes()).thenReturn(new ClassDoc[] {});

		// WHEN
		AnnotationDrivenDoclet.start(root);

		// THEN
		Assert.assertEquals("anywhere", AnnotationDrivenDoclet.baseOutputDir);
		Assert.assertEquals("anywhere/else",
				AnnotationDrivenDoclet.resourcesDir);
		Assert.assertTrue(AnnotationDrivenDoclet.configuredAnnotations
				.containsKey("class1"));
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#processAnnotations(com.sun.javadoc.Tag[], com.sun.javadoc.AnnotationDesc[], java.lang.String)}
	 * .
	 */
	@Test
	@Ignore(value = "not yet implemented")
	public void testProcessAnnotations() {
		// TODO
		Assert.fail("Not yet implemented");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void shouldProcessScreencastLine() throws Exception {
		// GIVEN
		final String line = "anything before placeholder ~~{screencast:screencasts/${id}} anything after placeholder";

		final HashMap<String, String> replacements = new HashMap<>();
		replacements.put("id", "demo");

		// WHEN
		final String output = AnnotationDrivenDoclet.processLine(line, false,
				replacements);

		// THEN
		Assert.assertEquals(
				"%{snippet|verbatim=false|file=src/site/resources/generated/snippets/screencast_snippet_demo.txt}",
				output.trim());
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#processScreencast(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void shouldProcessScreencastsAndReplaceLineForTagWithParameters() {
		// GIVEN
		final String line = "anything before placeholder ~~{screencast:screencasts/${id}} anything after placeholder";

		final HashMap<String, String> replacements = new HashMap<>();
		replacements.put("id", "demo");

		// WHEN
		final String outputLine = AnnotationDrivenDoclet.processScreencast(
				line, replacements);

		// THEN
		Assert.assertEquals(
				"%{snippet|verbatim=false|file=src/site/resources/generated/snippets/screencast_snippet_demo.txt}",
				outputLine.trim());
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#processScreencast(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void shouldPrintNAifNoScreencastWithGivenIdentifierCouldBeFound() {
		// GIVEN
		final String line = "~~{screencast:screencasts/${id}}";

		final HashMap<String, String> replacements = new HashMap<>();
		replacements.put("id", "nothing");

		// WHEN
		final String outputLine = AnnotationDrivenDoclet.processScreencast(
				line, replacements);

		// THEN
		Assert.assertEquals("n/a", outputLine.trim());
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#processImages(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void shouldProcessImagesAndReplaceLineForTagWithoutParameters() {
		// GIVEN
		final String line = "anything before placeholder ~~{images:/images/test} anything after placeholder";

		// WHEN
		final String outputLine = AnnotationDrivenDoclet.processImages(line,
				new HashMap<String, String>());

		// THEN
		Assert.assertEquals("[/images/test/duke.png]", outputLine.trim());
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#processImages(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void shouldPrintNAifNoScreenshotsCouldBeFoundInDirectory() {
		// GIVEN
		final String line = "~~{images:/images/${param}}";

		final HashMap<String, String> replacements = new HashMap<>();
		replacements.put("param", "empty");

		// WHEN
		final String outputLine = AnnotationDrivenDoclet.processImages(line,
				replacements);

		// THEN
		Assert.assertEquals("n/a", outputLine.trim());
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#processImages(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void shouldProcessImagesReplaceTagsInPath() {
		// GIVEN
		final String line = "anything before placeholder ~~{images:/images/${param}} anything after placeholder";

		// WHEN
		final Map<String, String> replacements = new HashMap<>();
		replacements.put("param", "test");

		final String outputLine = AnnotationDrivenDoclet.processImages(line,
				replacements);

		// THEN
		Assert.assertTrue(outputLine.contains("duke.png"));
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#processImages(java.lang.String, java.util.Map)}
	 * .
	 */
	@Test
	public void shouldOnlyProcessImages() {
		// GIVEN
		// directory contains a 'not_an_image.txt' file which should not be
		// processed.
		final String line = "anything before placeholder ~~{images:/images/test} anything after placeholder";

		// WHEN
		final String outputLine = AnnotationDrivenDoclet.processImages(line,
				new HashMap<String, String>());

		// THEN
		Assert.assertTrue(outputLine.contains("duke.png"));
		Assert.assertFalse(outputLine.contains("not_an_image.txt"));
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldContainOptionLength2ForOutput() {
		// GIVEN

		// WHEN
		final int optionLength = AnnotationDrivenDoclet.optionLength("-output");

		// THEN
		Assert.assertEquals(2, optionLength);
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldContainOptionLength3ForAnnotations() {
		// GIVEN

		// WHEN
		final int optionLength = AnnotationDrivenDoclet
				.optionLength("-annotation");

		// THEN
		Assert.assertEquals(3, optionLength);
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldContainOptionLength2ForResourcesDir() {
		// GIVEN

		// WHEN
		final int optionLength = AnnotationDrivenDoclet
				.optionLength("-siteResources");

		// THEN
		Assert.assertEquals(2, optionLength);
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldContainOptionLength0ForDefault() {
		// GIVEN

		// WHEN
		final int optionLength = AnnotationDrivenDoclet
				.optionLength("-anythingElse");

		// THEN
		Assert.assertEquals(0, optionLength);
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test(expected = UnsupportedCommandLineParameterException.class)
	public void shouldThrowExceptionIfTooManyArgumentsAreProvided()
			throws Exception {
		// GIVEN
		final String[][] providedCommandLineParameters = { { "-annotation",
				"to", "many", "parameters" } };

		// WHEN
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters);

		// THEN
		Assert.fail("should have thrown an UnsupportedCommandLineParameterException.");
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test(expected = UnsupportedCommandLineParameterException.class)
	public void shouldThrowExceptionIfTooFewArgumentsAreProvided()
			throws Exception {
		// GIVEN
		final String[][] providedCommandLineParameters = { { "-annotation" } };

		// WHEN
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters);

		// THEN
		Assert.fail("should have thrown an UnsupportedCommandLineParameterException.");
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldProcessOutputParameter() throws Exception {
		// GIVEN
		final String[][] providedCommandLineParameters = {
				{ "-output", "anywhere" },
				{ "-annotation", "class1", "template1" } };

		// WHEN
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters);

		// THEN
		Assert.assertEquals("anywhere", AnnotationDrivenDoclet.baseOutputDir);
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldProcessResourcesDirParameter() throws Exception {
		// GIVEN
		final String[][] providedCommandLineParameters = {
				{ "-output", "anywhere" },
				{ "-annotation", "class1", "template1" },
				{ "-siteResources", "anywhere/else" } };

		// WHEN
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters);

		// THEN
		Assert.assertEquals("anywhere/else",
				AnnotationDrivenDoclet.resourcesDir);
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldTakeDefaultMavenResourcesDirIfNoneProvided()
			throws Exception {
		// GIVEN
		final String[][] providedCommandLineParameters = {
				{ "-output", "anywhere" },
				{ "-annotation", "class1", "template1" } };

		// WHEN
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters);

		// THEN
		Assert.assertEquals("src/site/resources/",
				AnnotationDrivenDoclet.resourcesDir);
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test
	public void shouldProcessAnnotationParameter() throws Exception {
		// GIVEN
		final String[][] providedCommandLineParameters = {
				{ "-annotation", "class1", "template1" },
				{ "-annotation", "class2", "template2" },
				{ "-output", "anywhere" } };

		// WHEN
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters);

		// THEN
		Assert.assertEquals(2,
				AnnotationDrivenDoclet.configuredAnnotations.size());
		Assert.assertTrue(AnnotationDrivenDoclet.configuredAnnotations
				.containsKey("class1"));
		Assert.assertTrue(AnnotationDrivenDoclet.configuredAnnotations
				.containsKey("class2"));
		Assert.assertEquals("template1",
				AnnotationDrivenDoclet.configuredAnnotations.get("class1"));
		Assert.assertEquals("template2",
				AnnotationDrivenDoclet.configuredAnnotations.get("class2"));
	}

	/**
	 * Test method for
	 * {@link ch.puzzle.util.AnnotationDrivenDoclet#optionLength(java.lang.String)}
	 * .
	 */
	@Test(expected = MissingCommandLineParameterException.class)
	public void shouldThrowExceptionIfParameterIsMissing() throws Exception {
		// GIVEN
		final String[][] providedCommandLineParameters1 = { { "-output",
				"anywhere" } };
		final String[][] providedCommandLineParameters2 = { { "-annotation",
				"any", "any" } };

		// WHEN
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters1);
		AnnotationDrivenDoclet.processOptions(providedCommandLineParameters2);

		// THEN
		Assert.fail("should have thrown a MissingParameterException.");
	}

}
