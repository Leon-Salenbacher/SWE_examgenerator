package config;

import lombok.Getter;
import repository.XMLStorageConnector;
import repository.detailed.impl.ChapterRepositoryImpl;
import repository.detailed.impl.SubtaskRepositoryImpl;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;
import service.impl.elements.ChapterServiceImpl;
import service.impl.elements.SubtaskServiceImpl;
import service.impl.elements.VariantServiceImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;

@Getter
public final class ApplicationContext {

    private static final Path DATA_PATH = Path.of("src/main/resources/data/sample-data.xml");
    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final XMLStorageConnector xmlStorageConnector;
    private final VariantRepositoryImpl variantRepository;
    private final SubtaskRepositoryImpl subtaskRepository;
    private final ChapterRepositoryImpl chapterRepository;

    private final VariantServiceImpl variantService;
    private final SubtaskServiceImpl subtaskService;
    private final ChapterServiceImpl chapterService;

    private ApplicationContext(){
        this.xmlStorageConnector = new XMLStorageConnectorImpl(
                DATA_PATH,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
        //repository
        this.variantRepository = new VariantRepositoryImpl(xmlStorageConnector);
        this.subtaskRepository = new SubtaskRepositoryImpl(xmlStorageConnector, variantRepository);
        this.chapterRepository = new ChapterRepositoryImpl(xmlStorageConnector, subtaskRepository);

        //service
        this.variantService = new VariantServiceImpl(variantRepository);
        this.subtaskService = new SubtaskServiceImpl(subtaskRepository);
        this.chapterService = new ChapterServiceImpl(chapterRepository);
    }

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }
}
