package service.impl.elements;

import models.Chapter;
import models.Subtask;
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
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestParentServiceImpl {

    @TempDir
    Path tempDir;

    @Test
    public void test_countChildren_goodcase01_countNestedSubtasks() {
        ChapterRepositoryImpl repository = createChapterRepository();
        ChapterServiceImpl service = new ChapterServiceImpl(repository);
        Chapter chapter = Chapter.builder()
                .id(1)
                .title("Chapter with children")
                .childElements(List.of(
                        Subtask.builder().id(10).title("Task 1").points(2).chapterId(1).build(),
                        Subtask.builder().id(11).title("Task 2").points(3).chapterId(1).build()
                ))
                .build();
        repository.save(chapter);

        assertEquals(2, service.countChildren(1));
    }

    @Test
    public void test_countChildren_goodcase02_returnZeroForParentWithoutChildren() {
        ChapterServiceImpl service = createChapterService();
        Chapter chapter = service.create(command("Empty chapter"));

        assertEquals(0, service.countChildren(chapter.getId()));
    }

    @Test
    public void test_countChildren_badcase01_rejectMissingParent() {
        ChapterServiceImpl service = createChapterService();

        assertThrows(NoSuchElementException.class, () -> service.countChildren(404));
    }

    private ChapterServiceImpl createChapterService() {
        return new ChapterServiceImpl(createChapterRepository());
    }

    private ChapterRepositoryImpl createChapterRepository() {
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                tempDir.resolve("parent-service.xml"),
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
        VariantRepositoryImpl variantRepository = new VariantRepositoryImpl(connector);
        SubtaskRepositoryImpl subtaskRepository = new SubtaskRepositoryImpl(connector, variantRepository);
        return new ChapterRepositoryImpl(connector, subtaskRepository);
    }

    private ChapterServiceImpl.ChapterCommand command(String title) {
        return new ChapterServiceImpl.ChapterCommand() {
            @Override
            public String title() {
                return title;
            }

            @Override
            public Integer parentId() {
                return null;
            }
        };
    }
}
