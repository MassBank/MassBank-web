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
import java.time.Instant;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * DatabaseTimestamp retrieves COLUMN 'LAST_UPDATE' in TABLE 'LAST_UPDATE' from
 * database 'MassBank' for the latest update time of the database.
 * {@code isOutdated()} is used to compare this.timestamp with the latest state
 * in the database.
 * 
 * @author rmeier
 * @version 01-02-2023
 *
 */
public class DatabaseTimestamp {
	private static final Logger logger = LogManager.getLogger(DatabaseTimestamp.class);
	private Instant timestamp = Instant.ofEpochSecond(0);
	private String version = "unknown";

	public DatabaseTimestamp() {
		// get timestamp of last db change from database
		try {
			DatabaseManager databaseManager = new DatabaseManager("MassBank");
			PreparedStatement stmnt = databaseManager.getConnection()
					.prepareStatement("SELECT MAX(LAST_UPDATE),VERSION FROM LAST_UPDATE;");
			ResultSet res = stmnt.executeQuery();
			res.next();
			Instant db_timestamp = res.getTimestamp(1).toInstant();
			String db_version = res.getString(2);
			if (db_timestamp == null) {
				logger.error("Timestamp from database is \"null\". Using defaults.");
			} else {
				timestamp = db_timestamp;
			}
			if (db_version == null) {
				logger.error("Version from database is \"null\".");
			} else {
				version = db_version;
			}
			databaseManager.closeConnection();
			logger.trace("Construct DatabaseTimestamp with timestamp \"" + timestamp + "\" and version \"" + db_version
					+ "\"");
		} catch (SQLException | ConfigurationException e) {
			logger.error("Error constructing DatabaseTimestamp from database value.", e);// TODO Auto-generated catch
																							// block
		}

	}

	/**
	 * Check if this.timestamp is outdated.
	 * 
	 * @return Return true if the current database timestamp is more recent than
	 *         this.timestamp.
	 *
	 */
	public boolean isOutdated() {
		// check if this.timestamp is older than timestamp from database
		try {
			DatabaseManager databaseManager;
			databaseManager = new DatabaseManager("MassBank");
			PreparedStatement stmnt = databaseManager.getConnection()
					.prepareStatement("SELECT MAX(LAST_UPDATE) FROM LAST_UPDATE");
			ResultSet res = stmnt.executeQuery();
			res.next();
			Instant db_timestamp = res.getTimestamp(1).toInstant();
			if (db_timestamp == null) {
				logger.error("Timestamp from database is \"null\". Return \"false\".");
				return false;
			}
			databaseManager.closeConnection();
			logger.trace("Found DatabaseTimestamp: " + db_timestamp);
			logger.trace("Own Timestamp: " + timestamp);
			logger.trace("isOutdated(): " + timestamp.isBefore(db_timestamp));
			return timestamp.isBefore(db_timestamp);
		} catch (SQLException | ConfigurationException e) {
			logger.error("Error retrieving timestamp from database. Return \"false\"", e);
			return false;
		}
	}

	/**
	 * Return the String of the 'VERSION' file from the data repo at the time of
	 * database creation.
	 * 
	 * @return Return the version.
	 *
	 */
	public String getVersion() {
		return version;
	}
}
