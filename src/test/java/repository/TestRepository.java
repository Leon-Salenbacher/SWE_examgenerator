package repository;

import exceptions.XmlStorageException;
import models.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestRepository {

    @TempDir
    Path tempDir;

    @Test
    public void test_findById_goodcase01_returnEmptyForMissingId(){
        Repository<Variant> repository = createVariantRepositoryWithFixture();

        Optional<Variant> actualVariant = repository.findById(42);

        assertTrue(actualVariant.isEmpty());
    }

    @Test
    public void test_findAll_goodcase01_returnAllVariants(){
        Repository<Variant> repository = createVariantRepositoryWithFixture();

        List<Variant> variants = repository.findAll();

        assertEquals(4, variants.size());
        assertEquals(100, variants.get(0).getId());
        assertEquals(200, variants.get(1).getId());
        assertEquals(2, variants.get(2).getId());
        assertEquals("Variante C", variants.get(3).getTitle());
    }

    @Test
    public void test_findById_goodcase02_returnVariantFromFixture(){
        Repository<Variant> repository = createVariantRepositoryWithFixture();

        Variant actualVariant = repository.findById(2).orElseThrow();

        assertEquals(2, actualVariant.getId());
        assertEquals("Variante B", actualVariant.getTitle());
        assertEquals("Frage B", actualVariant.getQuestion());
        assertEquals("Loesung B", actualVariant.getSolution());
    }

    @Test
    public void test_save_goodcase01_persistVariant(){
        Repository<Variant> repository = createVariantRepository();
        Variant variant = Variant.builder()
                .id(1)
                .title("Variante A")
                .question("Frage A")
                .solution("Loesung A")
                .build();

        Variant savedVariant = repository.save(variant);

        assertSame(variant, savedVariant);
        Optional<Variant> actualVariant = repository.findById(1);
        assertTrue(actualVariant.isPresent());
        assertEquals("Variante A", actualVariant.get().getTitle());
        assertEquals("Frage A", actualVariant.get().getQuestion());
        assertEquals("Loesung A", actualVariant.get().getSolution());
    }

    @Test
    public void test_save_badcase01_missingRootThrows(){
        XMLStorageConnector connector = createConnector();
        connector.getDocument().removeChild(connector.getDocument().getDocumentElement());
        Repository<Variant> brokenRepository = new VariantRepositoryImpl(connector);

        assertThrows(XmlStorageException.class,
                () -> brokenRepository.save(Variant.builder()
                        .id(1)
                        .title("Titel")
                        .question("Frage")
                        .solution("Loesung")
                        .build()));
    }

    @Test
    public void test_update_goodcase01_replaceVariantAttributes(){
        Repository<Variant> repository = createVariantRepository();
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

        Variant actualVariant = repository.update(updatedVariant);

        assertSame(updatedVariant, actualVariant);
        Variant persistedVariant = repository.findById(1).orElseThrow();
        assertEquals("Neu", persistedVariant.getTitle());
        assertEquals("Neue Frage", persistedVariant.getQuestion());
        assertEquals("Neue Loesung", persistedVariant.getSolution());
    }

    @Test
    public void test_update_badcase01_missingIdThrows(){
        Repository<Variant> repository = createVariantRepository();

        assertThrows(XmlStorageException.class,
                () -> repository.update(Variant.builder()
                        .id(99)
                        .title("Fehlt")
                        .question("Frage")
                        .solution("Loesung")
                        .build()));
    }

    @Test
    public void test_deleteById_goodcase01_removeVariant(){
        Repository<Variant> repository = createVariantRepository();
        repository.save(Variant.builder()
                .id(1)
                .title("Variante A")
                .question("Frage A")
                .solution("Loesung A")
                .build());

        repository.deleteById(1);

        assertTrue(repository.findById(1).isEmpty());
        assertEquals(0, repository.findAll().size());
    }

    @Test
    public void test_deleteById_badcase01_missingIdThrows(){
        Repository<Variant> repository = createVariantRepository();

        assertThrows(XmlStorageException.class, () -> repository.deleteById(404));
    }

    private Repository<Variant> createVariantRepository(){
        return new VariantRepositoryImpl(createConnector());
    }

    private Repository<Variant> createVariantRepositoryWithFixture(){
        return new VariantRepositoryImpl(createConnectorWithFixture());
    }

    private XMLStorageConnector createConnector(){
        return new XMLStorageConnectorImpl(
                tempDir.resolve("repository.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

    private XMLStorageConnector createConnectorWithFixture(){
        return new XMLStorageConnectorImpl(
                TestRepositoryXmlSupport.copyFixtureXmlTo(tempDir.resolve("repository-fixture.xml")),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
    }

}
