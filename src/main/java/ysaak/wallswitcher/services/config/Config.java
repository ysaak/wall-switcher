package ysaak.wallswitcher.services.config;

/**
 * Config accessor
 */
public abstract class Config {

    private static IConfig INSTANCE = null;

    private static IConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PropertiesConfig();
        }
        return INSTANCE;
    }

    public static String get(String key) {
        return getInstance().get(key);
    }

    public static void set(String key, String value) {
        getInstance().set(key, value);
    }
}
