package in.neolab.configurable.cache.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * Configuration class
 * @author Nikita Parygin
 */
public class SystemHolder {
    private static File configFile;
    private static Config config;

    /**
     * Default empty constructor
     */
    private  SystemHolder() {

    }

    /**
     * Initialize System Holder object
     * @param path path to the config file
     */
    public static void init(String path) {
        configFile = new File(path);
        if (configFile.exists()) {
            config = ConfigFactory.parseFile(configFile).getConfig("configurable-cache");
        } else {
            config = null;
        }
    }

    /**
     * Returns {@link Config} instance
     * @return config
     */
    public static Config getConfig() {
        return config;
    }
}
