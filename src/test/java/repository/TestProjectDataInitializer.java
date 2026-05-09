package repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.impl.ProjectDataInitializer;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestProjectDataInitializer {

    @TempDir
    Path tempDir;

    @Test
    public void test_ensureDataFile_goodcase01_copySampleToProjectDataFile() throws Exception {
        Path samplePath = tempDir.resolve("sample-data.xml");
        Path dataPath = tempDir.resolve("data").resolve("exam-generator-data.xml");
        String sampleXml = "<ExamGenerator><Chapter id=\"1\" title=\"Sample\"/></ExamGenerator>";
        Files.writeString(samplePath, sampleXml);

        new ProjectDataInitializer().ensureDataFile(dataPath, samplePath);

        assertTrue(Files.exists(dataPath));
        assertEquals(sampleXml, Files.readString(dataPath));
        assertEquals(sampleXml, Files.readString(samplePath));
    }

    @Test
    public void test_ensureDataFile_goodcase02_createEmptyDocumentWhenSampleIsMissing() throws Exception {
        Path dataPath = tempDir.resolve("data").resolve("exam-generator-data.xml");
        Path missingSamplePath = tempDir.resolve("missing-sample.xml");

        new ProjectDataInitializer().ensureDataFile(dataPath, missingSamplePath);

        assertTrue(Files.exists(dataPath));
        assertEquals("<ExamGenerator/>", Files.readString(dataPath).trim());
    }

    @Test
    public void test_ensureDataFile_goodcase03_keepExistingProjectDataFile() throws Exception {
        Path samplePath = tempDir.resolve("sample-data.xml");
        Path dataPath = tempDir.resolve("data").resolve("exam-generator-data.xml");
        String existingXml = "<ExamGenerator><Chapter id=\"7\" title=\"Existing\"/></ExamGenerator>";
        Files.createDirectories(dataPath.getParent());
        Files.writeString(samplePath, "<ExamGenerator><Chapter id=\"1\" title=\"Sample\"/></ExamGenerator>");
        Files.writeString(dataPath, existingXml);

        new ProjectDataInitializer().ensureDataFile(dataPath, samplePath);

        assertEquals(existingXml, Files.readString(dataPath));
    }
}
