package config;

import lombok.Getter;
import repository.XMLStorageConnector;
import repository.detailed.impl.ChapterRepositoryImpl;
import repository.detailed.impl.SubtaskRepositoryImpl;
import repository.detailed.impl.VariantRepositoryImpl;
import repository.impl.XMLStorageConnectorImpl;
import service.impl.ProjectDataInitializer;
import service.impl.SettingsService;
import service.impl.elements.ChapterServiceImpl;
import service.impl.elements.SubtaskServiceImpl;
import service.impl.elements.VariantServiceImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Path;

/**
 * Central application wiring point for services, repositories and XML storage.
 *
 * <p>The JavaFX controllers use this singleton instead of creating repositories
 * themselves, which keeps one shared XML document and one shared settings service
 * active throughout the application.</p>
 */
@Getter
public final class ApplicationContext {

    private static final Path SAMPLE_DATA_PATH = Path.of("src/main/resources/data/sample-data.xml");
    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final SettingsService settingsService;
    private final XMLStorageConnector xmlStorageConnector;
    private final VariantRepositoryImpl variantRepository;
    private final SubtaskRepositoryImpl subtaskRepository;
    private final ChapterRepositoryImpl chapterRepository;

    private final VariantServiceImpl variantService;
    private final SubtaskServiceImpl subtaskService;
    private final ChapterServiceImpl chapterService;

    private ApplicationContext(){
        this.settingsService = SettingsService.getInstance();
        Path dataPath = new ProjectDataInitializer().ensureDataFile(settingsService.getDataPath(), SAMPLE_DATA_PATH);
        settingsService.setDataPath(dataPath);

        this.xmlStorageConnector = new XMLStorageConnectorImpl(
                dataPath,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
        // Repositories share the same XML connector so nested data stays consistent.
        this.variantRepository = new VariantRepositoryImpl(xmlStorageConnector);
        this.subtaskRepository = new SubtaskRepositoryImpl(xmlStorageConnector, variantRepository);
        this.chapterRepository = new ChapterRepositoryImpl(xmlStorageConnector, subtaskRepository);

        // Services wrap repository access for the controller layer.
        this.variantService = new VariantServiceImpl(variantRepository);
        this.subtaskService = new SubtaskServiceImpl(subtaskRepository);
        this.chapterService = new ChapterServiceImpl(chapterRepository);
    }

    /**
     * Returns the shared application context.
     *
     * @return singleton context containing application services and repositories
     */
    public static ApplicationContext getInstance() {
        return INSTANCE;
    }
}
