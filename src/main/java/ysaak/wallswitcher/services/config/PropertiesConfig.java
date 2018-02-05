package ysaak.wallswitcher.services.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ysaak.wallswitcher.App;

import java.io.*;
import java.util.Properties;

/**
 * Property based configuration store
 */
public class PropertiesConfig implements IConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesConfig.class);

    private final Properties properties = new Properties();

    PropertiesConfig() {
        loadConfiguration();
    }

    @Override
    public String get(String key) {
        return properties.getProperty(key);
    }

    @Override
    public void set(String key, String value) {
        properties.put(key, value);
        storeConfiguration();
    }

    private File getConfigFile() {
        return App.getAppDirectory().resolve("config.properties").toFile();
    }

    private void storeConfiguration() {
        try (OutputStream output = new FileOutputStream(getConfigFile())) {
            // save properties to project root folder
            properties.store(output, null);
        }
        catch (IOException e) {
            LOGGER.error("Error while writing configuration", e);
        }
    }

    private void loadConfiguration() {
        if (getConfigFile().exists()) {
            try (InputStream input = new FileInputStream(getConfigFile())) {
                // load a properties file
                properties.clear();
                properties.load(input);

            } catch (IOException e) {
                LOGGER.error("Error while loading configuration", e);
            }
        }
    }
}
