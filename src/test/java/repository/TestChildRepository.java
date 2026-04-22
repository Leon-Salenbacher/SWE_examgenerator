package repository;

import models.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class TestChildRepository {

    @TempDir
    Path tempDir;

    @Test
    public void test_childRepository_goodcase01_useVariantRepositoryAsChildRepository(){
        ChildRepository<Variant> repository = new VariantRepositoryImpl(createConnector());
        Variant variant = createVariant(1, "Variante", "Was ist ein Test?", "Ein Sicherheitsnetz");

        repository.save(variant);

        Variant actualVariant = repository.findById(1).orElseThrow();
        assertEquals("Variante", actualVariant.getTitle());
        assertEquals("Was ist ein Test?", actualVariant.getQuestion());
        assertEquals("Ein Sicherheitsnetz", actualVariant.getSolution());
    }

    @Test
    public void test_getTitle_goodcase01_useQuestionAsFallback(){
        ChildRepository<Variant> repository = new VariantRepositoryImpl(createConnector());
        Variant variant = createVariant(2, "", "Fallback Frage", "Fallback Loesung");

        repository.save(variant);

        assertEquals("Fallback Frage", repository.findById(2).orElseThrow().getTitle());
    }

    private XMLStorageConnector createConnector(){
        return new XMLStorageConnectorImpl(
                tempDir.resolve("child-repository.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

    private Variant createVariant(int id, String title, String question, String solution){
        Variant variant = new Variant();
        variant.setId(id);
        variant.setTitle(title);
        variant.setQuestion(question);
        variant.setSolution(solution);
        return variant;
    }
}
