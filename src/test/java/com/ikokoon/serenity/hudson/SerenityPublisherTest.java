package com.ikokoon.serenity.hudson;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.toolkit.Toolkit;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SerenityPublisherTest extends ATest {

    private File sourceDirectory;
    private PrintStream printStream;
    private SerenityPublisher serenityPublisher;

    @Before
    public void before() {
        printStream = Mockito.mock(PrintStream.class);
        sourceDirectory = new File("./" + IConstants.SERENITY_SOURCE_DIRECTORY);
        Toolkit.getOrCreateDirectory(sourceDirectory);
        serenityPublisher = new SerenityPublisher();
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                LOGGER.severe(Arrays.deepToString(invocation.getArguments()));
                return null;
            }
        }).when(printStream).println(Mockito.anyString());
    }

    @After
    public void after() {
        Toolkit.deleteFile(sourceDirectory, 3);
    }

    @Test
    public void findFilesAndDirectories() throws Exception {
        FilePath filePath = new FilePath(new File("."));
        List<FilePath> filePaths = new ArrayList<>();
        Pattern pattern = Pattern.compile(SerenityPublisher.SERENITY_SOURCE_REGEX);
        serenityPublisher.findFilesAndDirectories(filePath, filePaths, pattern);
        assertTrue(filePaths.size() > 0);
        for (final FilePath path : filePaths) {
            if (path.toURI().toString().contains("serenity/source")) {
                return;
            }
        }
        fail();
    }

    @Test
    public void findDatabaseFiles() throws Exception {
        Toolkit.createFile(new File("./" + IConstants.SERENITY_SOURCE_DIRECTORY, "serenity.odb"));
        FilePath filePath = new FilePath(new File("."));
        List<FilePath> filePaths = new ArrayList<>();
        Pattern pattern = Pattern.compile(SerenityPublisher.SERENITY_ODB_REGEX);
        serenityPublisher.findFilesAndDirectories(filePath, filePaths, pattern);
        assertTrue(filePaths.size() > 0);
    }

    @Test
    public void writeCoveredSourceForClasses() throws InterruptedException, IOException {
        File rootDirectory = new File("src/test/resources");

        final AbstractBuild<?, ?> build = Mockito.mock(AbstractBuild.class);
        final BuildListener buildListener = Mockito.mock(BuildListener.class);

        Mockito.when(build.getRootDir()).thenReturn(rootDirectory);
        Mockito.when(buildListener.getLogger()).thenReturn(printStream);

        serenityPublisher.writeCoveredSourceForClasses(build, buildListener);
    }

}
