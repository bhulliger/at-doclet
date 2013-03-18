/**
 * 
 */
package ch.puzzle.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * @author Brigitte Hulliger, <hulliger@puzzle.ch>
 */
public class AnnotationDrivenDocletTest {

	@Test
	public void testname() throws Exception {
		String line = "~~{screencast:screencasts/${id}}";

		Map<String, String> replacements = new HashMap<>();
		replacements.put("id", "TC-000-1");

		String output = AnnotationDrivenDoclet.processScreencast(line,
				replacements);

		System.out.println(output);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
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
		RootDoc root = Mockito.mock(RootDoc.class);
		final String[][] providedCommandLineParameters = {
				{ "-output", "anywhere" },
				{ "-annotation", "class1", "template1" },
				{ "-siteResources", "anywhere/else" } };

		Mockito.when(root.options()).thenReturn(providedCommandLineParameters);
		Mockito.when(root.classes()).thenReturn(new ClassDoc[] {});

		// WHEN
		AnnotationDrivenDoclet.start(root);

		// THEN
		assertEquals("anywhere", AnnotationDrivenDoclet.baseOutputDir);
		assertEquals("anywhere/else", AnnotationDrivenDoclet.resourcesDir);
		assertTrue(AnnotationDrivenDoclet.configuredAnnotations
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
		fail("Not yet implemented");
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
		String output = AnnotationDrivenDoclet.processLine(line, false,
				replacements);

		// THEN
		assertEquals(
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
		assertEquals(
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
		assertEquals("n/a", outputLine.trim());
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
		assertEquals("[/images/test/duke.png]", outputLine.trim());
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
		assertEquals("n/a", outputLine.trim());
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
		assertTrue(outputLine.contains("duke.png"));
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
		assertTrue(outputLine.contains("duke.png"));
		assertFalse(outputLine.contains("not_an_image.txt"));
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
		assertEquals(2, optionLength);
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
		assertEquals(3, optionLength);
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
		assertEquals(2, optionLength);
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
		assertEquals(0, optionLength);
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
		fail("should have thrown an UnsupportedCommandLineParameterException.");
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
		fail("should have thrown an UnsupportedCommandLineParameterException.");
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
		assertEquals("anywhere", AnnotationDrivenDoclet.baseOutputDir);
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
		assertEquals("anywhere/else", AnnotationDrivenDoclet.resourcesDir);
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
		assertEquals("src/site/resources/", AnnotationDrivenDoclet.resourcesDir);
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
		assertEquals(2, AnnotationDrivenDoclet.configuredAnnotations.size());
		assertTrue(AnnotationDrivenDoclet.configuredAnnotations
				.containsKey("class1"));
		assertTrue(AnnotationDrivenDoclet.configuredAnnotations
				.containsKey("class2"));
		assertEquals("template1",
				AnnotationDrivenDoclet.configuredAnnotations.get("class1"));
		assertEquals("template2",
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
		fail("should have thrown a MissingParameterException.");
	}

}
