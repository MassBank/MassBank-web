package massbank;

import jakarta.servlet.ServletContext;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// implement a singleton config class
public class ConfigWeb {
    private static final Logger logger = LogManager.getLogger(ConfigWeb.class);
    private static ConfigWeb instance;
    private final Configuration config;

    private ConfigWeb() {
        config = new BaseConfiguration();
        config.addProperty("LongName", getEnv("MASSBANK_LONG_NAME", "Personal MassBank"));
        config.addProperty("SitemapBaseURL", getEnv("MASSBANK_SITEMAP_BASE_URL", "https://msbi.ipb-halle.de/MassBank/"));
    }

    private String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public static synchronized ConfigWeb get() {
        if (ConfigWeb.instance == null) {
            ConfigWeb.instance = new ConfigWeb();
        }
        return ConfigWeb.instance;
    }

    public String LongName() {
        return config.getString("LongName");
    }

    // the final URL for sitemap.xml
    public String SitemapBaseURL() {
        return config.getString("SitemapBaseURL");
    }

    public String TOMCAT_TEMP_PATH(ServletContext context) {
        return context.getRealPath("/") + "temp/";
    }

    public String TOMCAT_TEMP_URL() {
        return "/MassBank/temp/";
    }

}
