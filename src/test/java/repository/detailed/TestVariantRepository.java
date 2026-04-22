package repository.detailed;

import models.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.XMLStorageConnector;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestVariantRepository {

    @TempDir
    Path tempDir;

    @Test
    public void test_findAll_goodcase01_returnNestedAndStandaloneVariants(){
        VariantRepository repository = createVariantRepositoryWithFixture();

        List<Variant> variants = repository.findAll();

        assertEquals(4, variants.size());
        assertEquals(100, variants.get(0).getId());
        assertEquals("Fixture Variante", variants.get(0).getTitle());
        assertEquals("Fixture Frage", variants.get(0).getQuestion());
        assertEquals("Fixture Loesung", variants.get(0).getSolution());
        assertEquals(200, variants.get(1).getId());
        assertEquals(2, variants.get(2).getId());
        assertEquals(3, variants.get(3).getId());
    }

    @Test
    public void test_findById_goodcase01_returnVariantFromFixture(){
        VariantRepository repository = createVariantRepositoryWithFixture();

        Variant actualVariant = repository.findById(200).orElseThrow();

        assertEquals(200, actualVariant.getId());
        assertEquals("Standalone Variante", actualVariant.getTitle());
        assertEquals("Standalone Frage", actualVariant.getQuestion());
        assertEquals("Standalone Loesung", actualVariant.getSolution());
    }

    @Test
    public void test_save_goodcase01_persistVariantAttributes(){
        VariantRepository repository = createVariantRepository();
        Variant variant = Variant.builder()
                .id(1)
                .title("Variante A")
                .question("Frage A")
                .solution("Loesung A")
                .build();

        repository.save(variant);

        Variant actualVariant = repository.findById(1).orElseThrow();
        assertEquals(1, actualVariant.getId());
        assertEquals("Variante A", actualVariant.getTitle());
        assertEquals("Frage A", actualVariant.getQuestion());
        assertEquals("Loesung A", actualVariant.getSolution());
    }

    @Test
    public void test_update_goodcase01_replaceVariantAttributes(){
        VariantRepository repository = createVariantRepository();
        repository.save(Variant.builder()
                .id(1)
                .title("Alt")
                .question("Alte Frage")
                .solution("Alte Loesung")
                .build());
        Variant updatedVariant = Variant.builder()
                .id(1)
                .title("Neu")
                .question("Neue Frage")
                .solution("Neue Loesung")
                .build();

        repository.update(updatedVariant);

        Variant actualVariant = repository.findById(1).orElseThrow();
        assertEquals("Neu", actualVariant.getTitle());
        assertEquals("Neue Frage", actualVariant.getQuestion());
        assertEquals("Neue Loesung", actualVariant.getSolution());
    }

    private VariantRepository createVariantRepository(){
        return new VariantRepositoryImpl(createConnector());
    }

    private VariantRepository createVariantRepositoryWithFixture(){
        return new VariantRepositoryImpl(createConnectorWithFixture());
    }

    private XMLStorageConnector createConnector(){
        return new XMLStorageConnectorImpl(
                tempDir.resolve("variant-repository.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

    private XMLStorageConnector createConnectorWithFixture(){
        return new XMLStorageConnectorImpl(
                TestRepositoryXmlSupport.copyFixtureXmlTo(tempDir.resolve("variant-repository-fixture.xml")),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

}
