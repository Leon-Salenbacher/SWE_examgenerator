package service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.*;

public class LocalizationService {
    private static final String BUNDLE_BASE = "i18n.messages";
    private static final LocalizationService INSTANCE = new LocalizationService();

    private final ObjectProperty<Locale> localeProperty = new SimpleObjectProperty<>(Locale.ENGLISH);
    private ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE, Locale.ENGLISH);

    private LocalizationService() {
        this.localeProperty.addListener((obs, oldLocale, newLocale) -> resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE, newLocale));
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
        if (locale != null && !locale.equals(localeProperty.get())) {
            localeProperty.set(locale);
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
}