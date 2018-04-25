package massbank;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

// implement a singleton config class
public class Config {
	private static Config instance;
	private Configuration config;
	private Config () throws ConfigurationException {
		Configurations configs = new Configurations();
		config = configs.properties(new File("/etc/massbank.conf"));
	}
	
	public static synchronized Config get() throws ConfigurationException {
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
	
	// the name of the temporary database for import of a new MassBank database
	public String tmpdbName() {
			return config.getString("tmpdbName");
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
	
	
	public String LinkNum() {
		return config.getString("LinkNum");
	}
	
	public String NodeNum() {
		return config.getString("NodeNum");
	}
	
	public String TOMCAT_APPPSERV_PATH() {
		return config.getString("TOMCAT_APPPSERV_PATH");
	}
	
	public String BASE_URL() {
		return config.getString("BASE_URL");
	}
	
	public String ADMIN_CONF_PATH() {
		return config.getString("ADMIN_CONF_PATH");
	}
	
	public String TOMCAT_TEMP_PATH(ServletContext context) {
		return context.getRealPath("/") + "temp/";
	}
	
	public String TOMCAT_TEMP_URL() {
		return config.getString("BASE_URL")+ "temp/";
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(Config.get().dbName());
		System.out.println(Config.get().tmpdbName());
		System.out.println(Config.get().dbPassword());
		System.out.println(Config.get().dbHostName());
		System.out.println(Config.get().DataRootPath());
	}

}
