package repository.detailed;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

final class TestRepositoryXmlSupport {

    private static final String FIXTURE_RESOURCE = "repository/repository-test-data.xml";
    private static final Path FIXTURE_PATH = Path.of(
            "src", "test", "resources", "repository", "repository-test-data.xml"
    );

    private TestRepositoryXmlSupport(){
    }

    static Path copyFixtureXmlTo(Path targetPath){
        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = openFixtureXml()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return targetPath;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not copy repository XML fixture.", exception);
        }
    }

    private static InputStream openFixtureXml() throws IOException{
        InputStream inputStream = TestRepositoryXmlSupport.class
                .getClassLoader()
                .getResourceAsStream(FIXTURE_RESOURCE);
        if (inputStream != null) {
            return inputStream;
        }

        for (Path fixturePath : fixturePathCandidates()) {
            if (Files.exists(fixturePath)) {
                return Files.newInputStream(fixturePath);
            }
        }

        throw new IOException("Repository XML fixture not found in test resources.");
    }

    private static List<Path> fixturePathCandidates(){
        List<Path> candidates = new ArrayList<>();
        Path currentDirectory = Path.of(System.getProperty("user.dir")).toAbsolutePath();

        for (Path directory = currentDirectory; directory != null; directory = directory.getParent()) {
            candidates.add(directory.resolve(FIXTURE_PATH));
            candidates.add(directory.resolve("test").resolve("resources").resolve("repository").resolve("repository-test-data.xml"));
        }

        return candidates;
    }
}
