package massbank;

import java.io.File;
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
	
	public static synchronized Config getInstance () throws ConfigurationException {
		if (Config.instance == null) {
			Config.instance = new Config();
		}
		return Config.instance;
	}
	
	// the name of the main MassBank database
	public String get_dbName() {
		return config.getString("dbName");
	}
	
	// the name of the temporary database for import of a new MassBank database
	public String get_tmpdbName() {
			return config.getString("tmpdbName");
		}
	
	
	// the password of the database user
	public String get_dbPassword() {
		return config.getString("dbPassword");
	}
	
	// the hostname of the mariadb server
	public String get_dbHostName() {
		return config.getString("dbHostName");
	}
	
	// the storage directory of the GIT repo
	public String get_DataRootPath() {
		return config.getString("DataRootPath");
	}
	
	
	public String get_LinkNum() {
		return config.getString("LinkNum");
	}
	
	public String get_NodeNum() {
		return config.getString("NodeNum");
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println(Config.getInstance().get_dbName());
		System.out.println(Config.getInstance().get_tmpdbName());
		System.out.println(Config.getInstance().get_dbPassword());
		System.out.println(Config.getInstance().get_dbHostName());
		System.out.println(Config.getInstance().get_DataRootPath());
	}

}
