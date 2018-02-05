package ysaak.screenchanger.utils.config;

/**
 * Configuration interface
 */
public interface IConfig {
    String get(String key);

    void set(String key, String value);
}
