package repository.detailed;

import models.Subtask;
import models.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.XMLStorageConnector;
import repository.detailed.impl.SubtaskRepositoryImpl;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSubtaskRepository {

    @TempDir
    Path tempDir;

    @Test
    public void test_findAll_goodcase01_returnNestedAndStandaloneSubtasks(){
        SubtaskRepository repository = createSubtaskRepositoryWithFixture();

        List<Subtask> subtasks = repository.findAll();

        assertEquals(3, subtasks.size());
        assertEquals(10, subtasks.get(0).getId());
        assertEquals("Fixture Aufgabe", subtasks.get(0).getTitle());
        assertEquals(11, subtasks.get(1).getId());
        assertEquals("Fixture Aufgabe 2", subtasks.get(1).getTitle());
        assertEquals(20, subtasks.get(2).getId());
        assertEquals("Standalone Aufgabe", subtasks.get(2).getTitle());
    }

    @Test
    public void test_findById_goodcase01_returnSubtaskWithVariantsFromFixture(){
        SubtaskRepository repository = createSubtaskRepositoryWithFixture();

        Subtask actualSubtask = repository.findById(10).orElseThrow();

        assertEquals(10, actualSubtask.getId());
        assertEquals("Fixture Aufgabe", actualSubtask.getTitle());
        assertEquals(5, actualSubtask.getPoints());
        assertEquals(1, actualSubtask.getChapterId());
        assertEquals(List.of("java", "xml"), actualSubtask.getLabels());
        assertEquals(1, actualSubtask.getChildElements().size());
        assertEquals("Fixture Variante", actualSubtask.getChildElements().get(0).getTitle());
        assertEquals("Fixture Frage", actualSubtask.getChildElements().get(0).getQuestion());
    }

    @Test
    public void test_findAllChildren_goodcase01_returnNestedVariants(){
        SubtaskRepository repository = createSubtaskRepositoryWithFixture();

        List<Variant> variants = repository.findAllChildren(20);

        assertEquals(1, variants.size());
        assertEquals(200, variants.get(0).getId());
        assertEquals("Standalone Variante", variants.get(0).getTitle());
        assertEquals("Standalone Loesung", variants.get(0).getSolution());
    }

    @Test
    public void test_save_goodcase01_persistSubtaskAttributesAndVariants(){
        SubtaskRepository repository = createSubtaskRepository();
        Subtask subtask = Subtask.builder()
                .id(10)
                .title("Aufgabe 1")
                .points(5.5)
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
        assertEquals(5.5, actualSubtask.getPoints());
        assertEquals(1, actualSubtask.getChapterId());
        assertEquals(List.of("swe", "test"), actualSubtask.getLabels());
        assertEquals(1, actualSubtask.getChildElements().size());
        assertEquals("Frage A", actualSubtask.getChildElements().get(0).getQuestion());
        assertEquals("Loesung A", actualSubtask.getChildElements().get(0).getSolution());
    }

    @Test
    public void test_update_goodcase01_replaceSubtaskAttributesAndVariants(){
        SubtaskRepository repository = createSubtaskRepository();
        repository.save(Subtask.builder()
                .id(10)
                .title("Alt")
                .points(5)
                .chapterId(1)
                .labels(List.of("alt"))
                .childElements(List.of(Variant.builder()
                        .id(100)
                        .title("Alte Variante")
                        .question("Alte Frage")
                        .solution("Alte Loesung")
                        .build()))
                .build());
        Subtask updatedSubtask = Subtask.builder()
                .id(10)
                .title("Neu")
                .points(9)
                .chapterId(2)
                .labels(List.of("neu", "xml"))
                .childElements(List.of(Variant.builder()
                        .id(101)
                        .title("Neue Variante")
                        .question("Neue Frage")
                        .solution("Neue Loesung")
                        .build()))
                .build();

        repository.update(updatedSubtask);

        Subtask actualSubtask = repository.findById(10).orElseThrow();
        assertEquals("Neu", actualSubtask.getTitle());
        assertEquals(9, actualSubtask.getPoints());
        assertEquals(2, actualSubtask.getChapterId());
        assertEquals(List.of("neu", "xml"), actualSubtask.getLabels());
        assertEquals(1, actualSubtask.getChildElements().size());
        assertEquals(101, actualSubtask.getChildElements().get(0).getId());
        assertEquals("Neue Frage", actualSubtask.getChildElements().get(0).getQuestion());
    }

    private SubtaskRepository createSubtaskRepository(){
        XMLStorageConnector connector = createConnector();
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        return new SubtaskRepositoryImpl(connector, variantRepository);
    }

    private SubtaskRepository createSubtaskRepositoryWithFixture(){
        XMLStorageConnector connector = createConnectorWithFixture();
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        return new SubtaskRepositoryImpl(connector, variantRepository);
    }

    private XMLStorageConnector createConnector(){
        return new XMLStorageConnectorImpl(
                tempDir.resolve("subtask-repository.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

    private XMLStorageConnector createConnectorWithFixture(){
        return new XMLStorageConnectorImpl(
                TestRepositoryXmlSupport.copyFixtureXmlTo(tempDir.resolve("subtask-repository-fixture.xml")),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

}
