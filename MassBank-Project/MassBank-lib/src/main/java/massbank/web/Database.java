package massbank.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.configuration2.ex.ConfigurationException;

import massbank.Config;

public class Database {

	private final static String driver = "org.mariadb.jdbc.Driver";
	private static String user;
	private static String password;
	private static String databaseName;
	private static String dbHostName;
	private static String connectUrl;
	private Connection connection;
	
	public Database() {
		this.connection = this.openConnection();
		Config config = null;
		try {
			config = Config.get();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Database.user = "";
		Database.password = config.dbPassword();
		Database.databaseName = config.dbName();
		Database.dbHostName = config.dbHostName();
		Database.connectUrl = "jdbc:mariadb://" + dbHostName + "/" + databaseName + "?rewriteBatchedStatements=true&pool";
	}

	private Connection openConnection() {
		Connection connection = null;
		try {
			Class.forName(Database.driver);
			connection = DriverManager.getConnection(Database.connectUrl, Database.user, Database.password);
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;
	}
	
	public void closeConnection() {
		try {
			if (this.connection != null) {
				this.connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		return this.connection;
	}
	
}
