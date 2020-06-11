/*******************************************************************************
 * Copyright (C) 2017 MassBank consortium
 * 
 * This file is part of MassBank.
 * 
 * MassBank is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 ******************************************************************************/
package massbank.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * DatabaseTimestamp checks COLUMN 'TIME' in TABLE 'LAST_UPDATE' in database 
 * 'MassBank' for the latest update time. DatabaseTimestamp is constructed with the
 * current timestamp in the database and can be used to label external resources
 * with a timestamp. {@code isOutdated()} is used to compare this timestamp to the 
 * current on in the database.
 * 
 * @author rmeier
 * @version 23-08-2019
 *
 */
public class DatabaseTimestamp {
	private static final Logger logger = LogManager.getLogger(DatabaseTimestamp.class);
	private Date timestamp = new Date();
	private String version = "unknown";
	
	public DatabaseTimestamp() throws SQLException, ConfigurationException {
		// get timestamp of last db change from database
		DatabaseManager databaseManager;
		databaseManager = new DatabaseManager("MassBank");
		PreparedStatement stmnt = databaseManager.getConnection().prepareStatement("SELECT MAX(TIME) FROM LAST_UPDATE;");			
		ResultSet res = stmnt.executeQuery();
		res.next();
		Date db_timestamp = res.getTimestamp(1);
		if ( db_timestamp == null) {
			logger.error("Timestamp from database is 'null'; using defaults.");
		}
		else {
			timestamp = db_timestamp;
		}
		stmnt = databaseManager.getConnection().prepareStatement("SELECT MAX(VERSION) FROM LAST_UPDATE;");			
		res = stmnt.executeQuery();
		res.next();
		String db_version = res.getString(1);
		if ( db_version == null) {
			logger.error("Version from database is 'null'; using defaults.");
		}
		else {
			version = db_version;
		}
		
		databaseManager.closeConnection();
		logger.trace("Create DatabaseTimestamp with: " + timestamp);
	}
	
	/**
	 * Check if this timestamp is outdated.
	 * 
	 * @return Return true if the current database timestamp is more recent than this one.
	 *
	 */
	public boolean isOutdated() throws SQLException, ConfigurationException {
		// check if this.timestamp is older than timestamp from database
		DatabaseManager databaseManager;
		databaseManager = new DatabaseManager("MassBank");
		PreparedStatement stmnt = databaseManager.getConnection().prepareStatement("SELECT MAX(TIME) FROM LAST_UPDATE");			
		ResultSet res = stmnt.executeQuery();
		res.next();
		Date db_timestamp = res.getTimestamp(1);
		if ( db_timestamp== null) {
			db_timestamp = new Date();
			logger.error("Timestamp from database is 'null'; using defaults.");
		}
		databaseManager.closeConnection();
		logger.trace("Found DatabaseTimestamp: " + db_timestamp);
		logger.trace("Own Timestamp: " + timestamp);
		logger.trace("isOutdated(): " + timestamp.before(db_timestamp));
		return timestamp.before(db_timestamp);
	}
	
	/**
	 * Return the String of the 'VERSION' file from the data repo at the time of database creation.
	 * 
	 * @return Return the version.
	 *
	 */
	public String getVersion() {
		return version;
	}
}
