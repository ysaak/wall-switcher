package ysaak.wallswitcher.ui.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalization class
 */
public class I18n {
    private static final Logger LOGGER = LoggerFactory.getLogger(I18n.class);

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("lang/messages", new UTF8Control());

    private I18n() { /* Empty constructor */ }

    /**
     * Find a translation using key
     * @param key translation key
     * @return Translated text
     */
    public static String get(String key) {
        try {
            return BUNDLE.getString(key);
        }
        catch (MissingResourceException e) {
            LOGGER.warn("Translation key not found : " + key);
            return "??" + key + "??";
        }

    }

    /**
     * Find a translation using key
     * @param key translation key
     * @param args arguments
     * @return Translated text
     */
    public static String get(String key, Object... args) {
        final String text = get(key);
        return MessageFormat.format(text, (Object[]) args);
    }

    /**
     * Find a translation for an enum
     * @param enumValue Enum to translate
     * @param <T> Enum type
     * @return Translated enum
     */
    public static <T extends Enum<T>> String get(T enumValue) {

        if (enumValue == null) {
            return "";
        }

        final String key = enumValue.getClass().getName() + "." + enumValue.toString();

        return get(key);
    }

    /**
     * Returns the resource BUNDLE used by the facade
     * @return A resource BUNDLE
     */
    public static ResourceBundle getBundle() {
        return BUNDLE;
    }
}
