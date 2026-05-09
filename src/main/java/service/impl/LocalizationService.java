package service.impl;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.*;

public class LocalizationService {
    private static final String BUNDLE_BASE = "i18n.messages";
    private static final LocalizationService INSTANCE = new LocalizationService();

    private final SettingsService settingsService;
    private final ObjectProperty<Locale> localeProperty;
    private ResourceBundle resourceBundle;

    private LocalizationService() {
        this.settingsService = SettingsService.getInstance();
        Locale initialLocale = supportedLocaleOrDefault(settingsService.getLocale());
        this.localeProperty = new SimpleObjectProperty<>(initialLocale);
        this.resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE, initialLocale);
        this.localeProperty.addListener((obs, oldLocale, newLocale) -> {
            Locale supportedLocale = supportedLocaleOrDefault(newLocale);
            resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE, supportedLocale);
            settingsService.setLocale(supportedLocale);
        });
    }

    public static LocalizationService getInstance() {
        return INSTANCE;
    }

    public String get(String key, Object... args) {
        try {
            String value = resourceBundle.getString(key);
            return args == null || args.length == 0 ? value : MessageFormat.format(value, args);
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    public void setLocale(Locale locale) {
        Locale supportedLocale = supportedLocaleOrDefault(locale);
        if (!supportedLocale.equals(localeProperty.get())) {
            localeProperty.set(supportedLocale);
        } else {
            settingsService.setLocale(supportedLocale);
        }
    }

    public Locale getLocale() {
        return localeProperty.get();
    }

    public ReadOnlyObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    public List<Locale> getSupportedLocales() {
        List<Locale> supported = new ArrayList<>();
        supported.add(Locale.ENGLISH);
        supported.add(Locale.GERMAN);
        return supported;
    }

    private Locale supportedLocaleOrDefault(Locale locale) {
        if (locale != null && Locale.GERMAN.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
            return Locale.GERMAN;
        }
        return SettingsService.DEFAULT_LOCALE;
    }
}
