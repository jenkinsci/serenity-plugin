package com.ikokoon.serenity.hudson;

import static org.junit.Assert.assertTrue;
import hudson.FilePath;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.ikokoon.serenity.ATest;
import com.ikokoon.toolkit.Toolkit;

public class SerenityPublisherTest extends ATest {

	private File sourceDirectory;
	private SerenityPublisher serenityPublisher;

	@Before
	public void before() {
		sourceDirectory = new File("./serenity/source");
		Toolkit.createFile(sourceDirectory);
		serenityPublisher = new SerenityPublisher();
	}

	@After
	public void after() {
		Toolkit.deleteFile(sourceDirectory, 3);
	}

	@Test
	public void findFilesAndDirectories() throws Exception {
		FilePath filePath = new FilePath(new File("/usr/share/eclipse/workspace/ikube"));
		List<FilePath> filePaths = new ArrayList<FilePath>();
		Pattern pattern = Pattern.compile(SerenityPublisher.SERENITY_SOURCE_REGEX);
		PrintStream printStream = Mockito.mock(PrintStream.class);
		serenityPublisher.findFilesAndDirectories(filePath, filePaths, pattern, printStream);
		assertTrue(filePaths.size() > 0);
	}

}
