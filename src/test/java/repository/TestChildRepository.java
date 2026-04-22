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
        Variant variant = Variant.builder()
                .id(1)
                .title("Variante")
                .question("Was ist ein Test?")
                .solution("Ein Sicherheitsnetz")
                .build();

        repository.save(variant);

        Variant actualVariant = repository.findById(1).orElseThrow();
        assertEquals("Variante", actualVariant.getTitle());
        assertEquals("Was ist ein Test?", actualVariant.getQuestion());
        assertEquals("Ein Sicherheitsnetz", actualVariant.getSolution());
    }

    @Test
    public void test_getTitle_goodcase01_useQuestionAsFallback(){
        ChildRepository<Variant> repository = new VariantRepositoryImpl(createConnector());
        Variant variant = Variant.builder()
                .id(2)
                .title("")
                .question("Fallback Frage")
                .solution("Fallback Loesung")
                .build();

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

}
