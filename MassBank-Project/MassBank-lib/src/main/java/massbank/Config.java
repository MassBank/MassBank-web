package massbank;

import java.io.File;

import jakarta.servlet.ServletContext;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// implement a singleton config class
public class Config {
	private static final Logger logger = LogManager.getLogger(Config.class);
	private static Config instance;
	private Configuration config;
	private Config () {
		Configurations configs = new Configurations();
		try {
			config = configs.properties(new File("/etc/massbank.conf"));
		} catch (ConfigurationException e) {
			logger.error("Can not read config file. Using defaults.");
			config = new BaseConfiguration();
			config.addProperty("Name", "Personal MassBank");
			config.addProperty("LongName", "Personal MassBank");
			config.addProperty("dbName", "MassBank");
			config.addProperty("dbPassword", "123blah321");
			config.addProperty("dbHostName", "127.0.0.1");
			config.addProperty("DataRootPath", "/GIT/MassBank-data");
			config.addProperty("SitemapBaseURL", "https://msbi.ipb-halle.de/MassBank/");
		}
	}
	
	public static synchronized Config get() {
		if (Config.instance == null) {
			Config.instance = new Config();
		}
		return Config.instance;
	}
	
	public String Name() {
		return config.getString("Name");
	}
	
	public String LongName() {
		return config.getString("LongName");
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
	
	public static void main(String[] args) {
		System.out.println(Config.get().dbName());
		System.out.println(Config.get().dbPassword());
		System.out.println(Config.get().dbHostName());
		System.out.println(Config.get().DataRootPath());
	}

}
