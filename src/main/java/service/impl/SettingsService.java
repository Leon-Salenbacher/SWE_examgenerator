package service.impl;

import exceptions.XmlStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Reads and writes project-local user settings such as language and data path.
 */
public final class SettingsService {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final Path DEFAULT_SETTINGS_PATH = Path.of("data", "settings.properties");
    public static final Path DEFAULT_DATA_PATH = Path.of("data", "exam-generator-data.xml");

    private static final String LANGUAGE_KEY = "locale.language";
    private static final String DATA_PATH_KEY = "data.path";
    private static SettingsService instance;

    private final Path settingsPath;
    private final Properties properties = new Properties();

    /**
     * Creates a settings service for a specific properties file.
     *
     * @param settingsPath path to the properties file
     */
    public SettingsService(Path settingsPath) {
        this.settingsPath = Objects.requireNonNull(settingsPath, "settingsPath must not be null.");
        loadOrReset();
        applyDefaults();
        save();
    }

    /**
     * @return shared settings service using the default project settings path
     */
    public static synchronized SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService(DEFAULT_SETTINGS_PATH);
        }
        return instance;
    }

    /**
     * @return persisted locale, defaulting to English for missing or unsupported values
     */
    public synchronized Locale getLocale() {
        return localeFromLanguage(properties.getProperty(LANGUAGE_KEY));
    }

    /**
     * Persists the supported equivalent of the requested locale.
     *
     * @param locale requested locale
     */
    public synchronized void setLocale(Locale locale) {
        Locale supportedLocale = localeFromLanguage(locale == null ? null : locale.getLanguage());
        properties.setProperty(LANGUAGE_KEY, languageOf(supportedLocale));
        save();
    }

    /**
     * @return configured XML data path
     */
    public synchronized Path getDataPath() {
        return dataPathFromValue(properties.getProperty(DATA_PATH_KEY));
    }

    /**
     * Persists the XML data path used by the repositories.
     *
     * @param dataPath new data path, or the default path when {@code null}
     */
    public synchronized void setDataPath(Path dataPath) {
        properties.setProperty(DATA_PATH_KEY, (dataPath == null ? DEFAULT_DATA_PATH : dataPath).toString());
        save();
    }

    /**
     * @return path of the settings properties file
     */
    public Path getSettingsPath() {
        return settingsPath;
    }

    private void loadOrReset() {
        if (!Files.exists(settingsPath)) {
            return;
        }

        try (InputStream inputStream = Files.newInputStream(settingsPath)) {
            properties.load(inputStream);
        } catch (IOException | IllegalArgumentException exception) {
            // Invalid settings should not block startup; defaults are restored below.
            properties.clear();
        }
    }

    private void applyDefaults() {
        properties.setProperty(LANGUAGE_KEY, languageOf(getLocale()));
        properties.setProperty(DATA_PATH_KEY, getDataPath().toString());
    }

    private void save() {
        try {
            Path parent = settingsPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream outputStream = Files.newOutputStream(settingsPath)) {
                properties.store(outputStream, "SWE Exam Generator settings");
            }
        } catch (IOException exception) {
            throw new XmlStorageException("Could not save application settings at " + settingsPath + '.', exception);
        }
    }

    private Locale localeFromLanguage(String language) {
        if (Locale.GERMAN.getLanguage().equalsIgnoreCase(language)) {
            return Locale.GERMAN;
        }
        return DEFAULT_LOCALE;
    }

    private String languageOf(Locale locale) {
        return localeFromLanguage(locale == null ? null : locale.getLanguage()).getLanguage();
    }

    private Path dataPathFromValue(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_DATA_PATH;
        }

        try {
            return Path.of(value.trim());
        } catch (InvalidPathException exception) {
            return DEFAULT_DATA_PATH;
        }
    }
}
