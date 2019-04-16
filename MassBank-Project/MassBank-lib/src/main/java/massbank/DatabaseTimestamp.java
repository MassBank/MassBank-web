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
		DatabaseManager databaseManager;
//		try {
//			databaseManager = new DatabaseManager("MassBank");
//			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement("SELECT ACCESSION FROM RECORD");			
//			ResultSet res = stmnt.executeQuery();
//		} catch (SQLException | ConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		DatabaseTimestamp ts = new DatabaseTimestamp(); 
		ts.timestamp = new Date();
		return ts;
	}
	
	public boolean isOutdated() {
		// check if this.timestamp is older than timestamp from database
		Date now = new Date(); // database timestamp is required here
		logger.trace("Timestamp date is: " + timestamp.toString());
		logger.trace("Database timestamp is: " + now.toString());
		logger.trace("isOutdated(): " + timestamp.before(now));
		return timestamp.before(now);
	}
	
}
