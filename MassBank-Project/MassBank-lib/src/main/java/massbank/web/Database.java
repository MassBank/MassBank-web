package massbank.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {

	private final static String driver = "org.mariadb.jdbc.Driver";
	private final static String user = "bird";
	private final static String password = "bird2006";
	private final static String databaseName ="MassBank";
	private final static String dbHostName = getDbHostName();
	private final static String connectUrl = "jdbc:mariadb://" + dbHostName + "/" + databaseName + "?rewriteBatchedStatements=true&pool";
	private final Connection connection;
	
	public Database() {
		this.connection = this.openConnection();
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
	
	private static String getDbHostName() {
		String dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
		if ( !MassBankEnv.get(MassBankEnv.KEY_DB_MASTER_NAME).equals("") ) {
			dbHostName = MassBankEnv.get(MassBankEnv.KEY_DB_MASTER_NAME);
		}
		return dbHostName;
	}
}
