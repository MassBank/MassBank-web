package massbank;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// implement a singleton config class
public class Config {
    private static final Logger logger = LogManager.getLogger(Config.class);
    private static Config instance;
    private final Configuration config;

    private Config() {
        config = new BaseConfiguration();
        config.addProperty("dbName", getEnv("MASSBANK_DB_NAME", "MassBank"));
        config.addProperty("dbPassword", getEnv("MASSBANK_DB_PASSWORD", "123blah321"));
        config.addProperty("dbHostName", getEnv("MASSBANK_DB_HOSTNAME", "127.0.0.1"));
        config.addProperty("DataRootPath", getEnv("MASSBANK_DATA_ROOT_PATH", "../../MassBank-data"));
    }

    private String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public static synchronized Config get() {
        if (Config.instance == null) {
            Config.instance = new Config();
        }
        return Config.instance;
    }

    // the name of the main MassBank database
    public String dbName() {
        return config.getString("dbName");
    }

    // the password of the database user
    public String dbPassword() {
        return config.getString("dbPassword");
    }

    // the hostname of the mariadb server
    public String dbHostName() {
        return config.getString("dbHostName");
    }

    // the storage directory of the GIT repo
    public String DataRootPath() {
        return config.getString("DataRootPath");
    }

}
