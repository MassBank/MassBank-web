package massbank;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseTimestamp {
	private static final Logger logger = LogManager.getLogger(DatabaseTimestamp.class);
	private Date timestamp;
	
	private DatabaseTimestamp() {
	}
	
	public static DatabaseTimestamp getTimestamp() {
		// get timestamp of last db change from database
		DatabaseTimestamp ts = new DatabaseTimestamp(); 
		DatabaseManager databaseManager;
		try {
			databaseManager = new DatabaseManager("MassBank");
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement("SELECT MAX(TIME) FROM LAST_UPDATE");			
			ResultSet res = stmnt.executeQuery();
			res.next();
			ts.timestamp = res.getTimestamp(1);
			logger.trace("Create DatabaseTimestamp with: " + ts.timestamp.toString());
		} catch (SQLException | ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ts;
	}
	
	public boolean isOutdated() {
		// check if this.timestamp is older than timestamp from database
		DatabaseManager databaseManager;
		Date database_timestamp = null;
		try {
			databaseManager = new DatabaseManager("MassBank");
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement("SELECT MAX(TIME) FROM LAST_UPDATE");			
			ResultSet res = stmnt.executeQuery();
			res.next();
			database_timestamp = res.getTimestamp(1);
		} catch (SQLException | ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.trace("Found DatabaseTimestamp: " + database_timestamp.toString());
		logger.trace("Own Timestamp: " + timestamp.toString());
		logger.trace("isOutdated(): " + timestamp.before(database_timestamp));
		return timestamp.before(database_timestamp);
	}
	
}
