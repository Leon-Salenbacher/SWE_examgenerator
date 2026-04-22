package repository;

import models.Chapter;
import models.Subtask;
import models.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.detailed.impl.ChapterRepositoryImpl;
import repository.detailed.impl.SubtaskRepositoryImpl;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class TestParentRepository {

    @TempDir
    Path tempDir;

    @Test
    public void test_save_goodcase01_persistChapterWithSubtasks(){
        ParentRepository<Chapter, Subtask> repository = createChapterRepository();
        Chapter chapter = Chapter.builder()
                .id(1)
                .title("Kapitel 1")
                .childElements(List.of(Subtask.builder()
                        .id(10)
                        .title("Aufgabe 1")
                        .points(5)
                        .chapterId(1)
                        .labels(List.of("swe", "test"))
                        .build()))
                .build();

        repository.save(chapter);

        Chapter actualChapter = repository.findById(1).orElseThrow();
        assertEquals("Kapitel 1", actualChapter.getTitle());
        assertEquals(1, actualChapter.getChildElements().size());
        assertEquals("Aufgabe 1", actualChapter.getChildElements().get(0).getTitle());
        assertEquals(5, actualChapter.getChildElements().get(0).getPoints());
    }

    @Test
    public void test_findAllChildren_goodcase01_returnNestedSubtasks(){
        ParentRepository<Chapter, Subtask> repository = createChapterRepositoryWithFixture();

        List<Subtask> subtasks = repository.findAllChildren(1);

        assertEquals(2, subtasks.size());
        assertEquals("Fixture Aufgabe", subtasks.get(0).getTitle());
        assertEquals("Fixture Aufgabe 2", subtasks.get(1).getTitle());
    }

    @Test
    public void test_findAllChildren_goodcase02_returnEmptyListForMissingParent(){
        ParentRepository<Chapter, Subtask> repository = createChapterRepositoryWithFixture();

        List<Subtask> subtasks = repository.findAllChildren(404);

        assertTrue(subtasks.isEmpty());
    }

    @Test
    public void test_update_goodcase01_replaceNestedChildren(){
        ParentRepository<Chapter, Subtask> repository = createChapterRepository();
        repository.save(Chapter.builder()
                .id(1)
                .title("Kapitel 1")
                .childElements(List.of(Subtask.builder()
                        .id(10)
                        .title("Alte Aufgabe")
                        .points(5)
                        .chapterId(1)
                        .labels(List.of("swe", "test"))
                        .build()))
                .build());
        Chapter updatedChapter = Chapter.builder()
                .id(1)
                .title("Kapitel 1 neu")
                .childElements(List.of(Subtask.builder()
                        .id(11)
                        .title("Neue Aufgabe")
                        .points(9)
                        .chapterId(1)
                        .labels(List.of("swe", "test"))
                        .build()))
                .build();

        repository.update(updatedChapter);

        Chapter actualChapter = repository.findById(1).orElseThrow();
        assertEquals("Kapitel 1 neu", actualChapter.getTitle());
        assertEquals(1, actualChapter.getChildElements().size());
        assertEquals(11, actualChapter.getChildElements().get(0).getId());
        assertEquals("Neue Aufgabe", actualChapter.getChildElements().get(0).getTitle());
    }

    @Test
    public void test_save_goodcase02_persistSubtaskWithVariants(){
        ParentRepository<Subtask, Variant> repository = createSubtaskRepository();
        Subtask subtask = Subtask.builder()
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
                .build();

        repository.save(subtask);

        Subtask actualSubtask = repository.findById(10).orElseThrow();
        assertEquals("Aufgabe 1", actualSubtask.getTitle());
        assertEquals(1, actualSubtask.getChildElements().size());
        assertEquals("Frage A", actualSubtask.getChildElements().get(0).getQuestion());
        assertEquals("Loesung A", actualSubtask.getChildElements().get(0).getSolution());
    }

    private ParentRepository<Chapter, Subtask> createChapterRepository(){
        XMLStorageConnector connector = createConnector();
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        SubtaskRepositoryImpl subtaskRepository = new SubtaskRepositoryImpl(connector, variantRepository);
        return new ChapterRepositoryImpl(connector, subtaskRepository);
    }

    private ParentRepository<Chapter, Subtask> createChapterRepositoryWithFixture(){
        XMLStorageConnector connector = createConnectorWithFixture();
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        SubtaskRepositoryImpl subtaskRepository = new SubtaskRepositoryImpl(connector, variantRepository);
        return new ChapterRepositoryImpl(connector, subtaskRepository);
    }

    private ParentRepository<Subtask, Variant> createSubtaskRepository(){
        XMLStorageConnector connector = createConnector();
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        return new SubtaskRepositoryImpl(connector, variantRepository);
    }

    private XMLStorageConnector createConnector(){
        return new XMLStorageConnectorImpl(
                tempDir.resolve("parent-repository.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

    private XMLStorageConnector createConnectorWithFixture(){
        return new XMLStorageConnectorImpl(
                TestRepositoryXmlSupport.copyFixtureXmlTo(tempDir.resolve("parent-repository-fixture.xml")),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

}
