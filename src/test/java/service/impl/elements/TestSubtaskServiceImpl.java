package service.impl.elements;

import models.Subtask;
import models.SubtaskDifficulty;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSubtaskServiceImpl {

    @TempDir
    Path tempDir;

    @Test
    public void test_create_goodcase01_storeParentIdAsChapterId() {
        SubtaskServiceImpl service = createSubtaskService();

        Subtask subtask = service.create(command("Task", 5, 1));

        assertEquals(1, subtask.getChapterId());
    }

    @Test
    public void test_create_badcase01_rejectMissingParentId() {
        SubtaskServiceImpl service = createSubtaskService();

        assertThrows(IllegalStateException.class, () -> service.create(command("Task", 5, null)));
    }

    private SubtaskServiceImpl createSubtaskService() {
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                tempDir.resolve("subtask-service.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        return new SubtaskServiceImpl(new SubtaskRepositoryImpl(connector, variantRepository));
    }

    private SubtaskServiceImpl.SubtaskCommand command(String title, double points, Integer parentId) {
        return new SubtaskServiceImpl.SubtaskCommand() {
            @Override
            public String title() {
                return title;
            }

            @Override
            public double points() {
                return points;
            }

            @Override
            public SubtaskDifficulty difficulty() {
                return SubtaskDifficulty.MEDIUM;
            }

            @Override
            public List<String> labels() {
                return List.of();
            }

            @Override
            public Integer parentId() {
                return parentId;
            }
        };
    }
}
