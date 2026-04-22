package repository.detailed;

import models.Chapter;
import models.Subtask;
import models.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.XMLStorageConnector;
import repository.detailed.impl.ChapterRepositoryImpl;
import repository.detailed.impl.SubtaskRepositoryImpl;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestChapterRepository {

    @TempDir
    Path tempDir;

    @Test
    public void test_findAll_goodcase01_returnChaptersFromFixture(){
        ChapterRepository repository = createChapterRepositoryWithFixture();

        List<Chapter> chapters = repository.findAll();

        assertEquals(1, chapters.size());
        assertEquals(1, chapters.get(0).getId());
        assertEquals("Fixture Kapitel", chapters.get(0).getTitle());
        assertEquals(2, chapters.get(0).getChildElements().size());
    }

    @Test
    public void test_findById_goodcase01_returnChapterWithSubtasksAndVariantsFromFixture(){
        ChapterRepository repository = createChapterRepositoryWithFixture();

        Chapter actualChapter = repository.findById(1).orElseThrow();

        assertEquals(1, actualChapter.getId());
        assertEquals("Fixture Kapitel", actualChapter.getTitle());
        assertEquals(2, actualChapter.getChildElements().size());
        assertEquals("Fixture Aufgabe", actualChapter.getChildElements().get(0).getTitle());
        assertEquals(5, actualChapter.getChildElements().get(0).getPoints());
        assertEquals(List.of("java", "xml"), actualChapter.getChildElements().get(0).getLabels());
        assertEquals(1, actualChapter.getChildElements().get(0).getChildElements().size());
        assertEquals("Fixture Frage", actualChapter.getChildElements().get(0).getChildElements().get(0).getQuestion());
        assertEquals("Fixture Aufgabe 2", actualChapter.getChildElements().get(1).getTitle());
    }

    @Test
    public void test_findAllChildren_goodcase01_returnNestedSubtasks(){
        ChapterRepository repository = createChapterRepositoryWithFixture();

        List<Subtask> subtasks = repository.findAllChildren(1);

        assertEquals(2, subtasks.size());
        assertEquals(10, subtasks.get(0).getId());
        assertEquals("Fixture Aufgabe", subtasks.get(0).getTitle());
        assertEquals(11, subtasks.get(1).getId());
        assertEquals("Fixture Aufgabe 2", subtasks.get(1).getTitle());
    }

    @Test
    public void test_save_goodcase01_persistChapterWithNestedSubtasksAndVariants(){
        ChapterRepository repository = createChapterRepository();
        Chapter chapter = Chapter.builder()
                .id(1)
                .title("Kapitel 1")
                .childElements(List.of(Subtask.builder()
                        .id(10)
                        .title("Aufgabe 1")
                        .points(5)
                        .chapterId(1)
                        .labels(List.of("swe", "test"))
                        .childElements(List.of(Variant.builder()
                                .id(100)
                                .title("Variante A")
                                .question("Frage A")
                                .solution("Loesung A")
                                .build()))
                        .build()))
                .build();

        repository.save(chapter);

        Chapter actualChapter = repository.findById(1).orElseThrow();
        assertEquals("Kapitel 1", actualChapter.getTitle());
        assertEquals(1, actualChapter.getChildElements().size());
        assertEquals("Aufgabe 1", actualChapter.getChildElements().get(0).getTitle());
        assertEquals(5, actualChapter.getChildElements().get(0).getPoints());
        assertEquals(List.of("swe", "test"), actualChapter.getChildElements().get(0).getLabels());
        assertEquals(1, actualChapter.getChildElements().get(0).getChildElements().size());
        assertEquals("Frage A", actualChapter.getChildElements().get(0).getChildElements().get(0).getQuestion());
    }

    @Test
    public void test_update_goodcase01_replaceChapterSubtasksAndNestedVariants(){
        ChapterRepository repository = createChapterRepository();
        repository.save(Chapter.builder()
                .id(1)
                .title("Kapitel alt")
                .childElements(List.of(Subtask.builder()
                        .id(10)
                        .title("Aufgabe alt")
                        .points(5)
                        .chapterId(1)
                        .labels(List.of("alt"))
                        .childElements(List.of(Variant.builder()
                                .id(100)
                                .title("Variante alt")
                                .question("Frage alt")
                                .solution("Loesung alt")
                                .build()))
                        .build()))
                .build());
        Chapter updatedChapter = Chapter.builder()
                .id(1)
                .title("Kapitel neu")
                .childElements(List.of(Subtask.builder()
                        .id(11)
                        .title("Aufgabe neu")
                        .points(9)
                        .chapterId(1)
                        .labels(List.of("neu"))
                        .childElements(List.of(Variant.builder()
                                .id(101)
                                .title("Variante neu")
                                .question("Frage neu")
                                .solution("Loesung neu")
                                .build()))
                        .build()))
                .build();

        repository.update(updatedChapter);

        Chapter actualChapter = repository.findById(1).orElseThrow();
        assertEquals("Kapitel neu", actualChapter.getTitle());
        assertEquals(1, actualChapter.getChildElements().size());
        assertEquals(11, actualChapter.getChildElements().get(0).getId());
        assertEquals("Aufgabe neu", actualChapter.getChildElements().get(0).getTitle());
        assertEquals(1, actualChapter.getChildElements().get(0).getChildElements().size());
        assertEquals(101, actualChapter.getChildElements().get(0).getChildElements().get(0).getId());
        assertEquals("Loesung neu", actualChapter.getChildElements().get(0).getChildElements().get(0).getSolution());
    }

    private ChapterRepository createChapterRepository(){
        XMLStorageConnector connector = createConnector();
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        SubtaskRepositoryImpl subtaskRepository = new SubtaskRepositoryImpl(connector, variantRepository);
        return new ChapterRepositoryImpl(connector, subtaskRepository);
    }

    private ChapterRepository createChapterRepositoryWithFixture(){
        XMLStorageConnector connector = createConnectorWithFixture();
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        SubtaskRepositoryImpl subtaskRepository = new SubtaskRepositoryImpl(connector, variantRepository);
        return new ChapterRepositoryImpl(connector, subtaskRepository);
    }

    private XMLStorageConnector createConnector(){
        return new XMLStorageConnectorImpl(
                tempDir.resolve("chapter-repository.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

    private XMLStorageConnector createConnectorWithFixture(){
        return new XMLStorageConnectorImpl(
                TestRepositoryXmlSupport.copyFixtureXmlTo(tempDir.resolve("chapter-repository-fixture.xml")),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

}
