package massbank.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import massbank.Config;


public class Database {

	private final static String driver = "org.mariadb.jdbc.Driver";
	private final static String user = "bird";
	private final static String password = "bird2006";
	private final static String databaseName ="MassBank";
	private final Connection connection;
	
	public Database() {
		this.connection = this.openConnection();
	}

	private Connection openConnection() {
		Connection connection = null;
		try {
			String link="jdbc:mariadb://" 
					+ Config.get().dbHostName() + ":3306/"
					+ Config.get().dbName() +
					"?rewriteBatchedStatements=true&pool";
			
			Class.forName(Database.driver);
			connection = DriverManager.getConnection(link, "root", Config.get().dbPassword());
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
