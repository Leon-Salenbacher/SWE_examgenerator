package service.impl;

import exceptions.XmlStorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class ProjectDataInitializer {

    private static final String EMPTY_DOCUMENT = "<ExamGenerator/>" + System.lineSeparator();

    public Path ensureDataFile(Path dataPath, Path sampleDataPath) {
        Objects.requireNonNull(dataPath, "dataPath must not be null.");
        if (Files.exists(dataPath)) {
            return dataPath;
        }

        try {
            Path parent = dataPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            copySampleOrCreateEmpty(dataPath, sampleDataPath);
            return dataPath;
        } catch (IOException exception) {
            throw new XmlStorageException("Could not initialize project data at " + dataPath + '.', exception);
        }
    }

    private void copySampleOrCreateEmpty(Path dataPath, Path sampleDataPath) throws IOException {
        if (sampleDataPath != null && Files.exists(sampleDataPath)) {
            Files.copy(sampleDataPath, dataPath, StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        Files.writeString(dataPath, EMPTY_DOCUMENT);
    }
}
