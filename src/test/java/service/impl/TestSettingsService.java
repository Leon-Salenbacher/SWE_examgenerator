package service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSettingsService {

    @TempDir
    Path tempDir;

    @Test
    public void test_constructor_goodcase01_createMissingSettingsWithDefaults() {
        Path settingsPath = tempDir.resolve("data").resolve("settings.properties");

        SettingsService settingsService = new SettingsService(settingsPath);

        assertTrue(Files.exists(settingsPath));
        assertEquals(Locale.ENGLISH, settingsService.getLocale());
        assertEquals(SettingsService.DEFAULT_DATA_PATH, settingsService.getDataPath());
    }

    @Test
    public void test_setLocale_goodcase01_persistSelectedLocale() {
        Path settingsPath = tempDir.resolve("settings.properties");
        SettingsService settingsService = new SettingsService(settingsPath);

        settingsService.setLocale(Locale.GERMAN);

        SettingsService reloadedSettingsService = new SettingsService(settingsPath);
        assertEquals(Locale.GERMAN, reloadedSettingsService.getLocale());
    }

    @Test
    public void test_setDataPath_goodcase01_persistSelectedDataPath() {
        Path settingsPath = tempDir.resolve("settings.properties");
        Path dataPath = tempDir.resolve("project-data").resolve("exam-data.xml");
        SettingsService settingsService = new SettingsService(settingsPath);

        settingsService.setDataPath(dataPath);

        SettingsService reloadedSettingsService = new SettingsService(settingsPath);
        assertEquals(dataPath, reloadedSettingsService.getDataPath());
    }

    @Test
    public void test_constructor_goodcase02_fallbackForUnsupportedLocaleAndMissingDataPath() throws Exception {
        Path settingsPath = tempDir.resolve("settings.properties");
        Files.writeString(settingsPath, "locale.language=fr" + System.lineSeparator() + "data.path=");

        SettingsService settingsService = new SettingsService(settingsPath);

        assertEquals(Locale.ENGLISH, settingsService.getLocale());
        assertEquals(SettingsService.DEFAULT_DATA_PATH, settingsService.getDataPath());
    }

    @Test
    public void test_constructor_goodcase03_fallbackForBrokenSettingsFile() throws Exception {
        Path settingsPath = tempDir.resolve("settings.properties");
        Files.writeString(settingsPath, "locale.language=de" + System.lineSeparator() + "broken=\\u12");

        SettingsService settingsService = new SettingsService(settingsPath);

        assertEquals(Locale.ENGLISH, settingsService.getLocale());
        assertEquals(SettingsService.DEFAULT_DATA_PATH, settingsService.getDataPath());
    }
}
