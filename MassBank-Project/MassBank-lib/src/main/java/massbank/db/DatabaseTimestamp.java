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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * On construction, DatabaseTimestamp retrieves COLUMN 'LAST_UPDATE' in TABLE 'LAST_UPDATE' from
 * database 'MassBank' for the latest update time of the database. function {@code isOutdated()}
 * is used to compare this.timestamp with the latest state in the database.
 * 
 * @author rmeier
 * @version 25-10-2023
 *
 */
public class DatabaseTimestamp {
	private static final Logger logger = LogManager.getLogger(DatabaseTimestamp.class);
	private Instant timestamp = Instant.ofEpochSecond(0);
	private String version = "unknown";

	public DatabaseTimestamp() {
		// get timestamp of last db change from database
		try (Connection con = DatabaseManager.getConnection()) {
			try (PreparedStatement stmnt = con.prepareStatement("SELECT MAX(LAST_UPDATE),VERSION FROM LAST_UPDATE")) {
				ResultSet res = stmnt.executeQuery();
				Instant db_timestamp = null;
				String db_version = null;
				if (res.next()) {
					db_timestamp = res.getTimestamp(1) == null ? null : res.getTimestamp(1).toInstant();
					db_version = res.getString(2);
				}
				if (db_timestamp == null) {
					logger.error("Timestamp from database is \"null\". Using \"unix epoch time 0\".");
				} else {
					timestamp = db_timestamp;
				}
				if (db_version == null) {
					logger.error("Version from database is \"null\". Using \"unknown\".");
				} else {
					version = db_version;
				}
				logger.trace("Construct DatabaseTimestamp with timestamp \"" + timestamp + "\" and version \"" + version + "\"");
			}
		} catch (SQLException e) {
			logger.error("Database error.");
			logger.error(e.getMessage());
			logger.trace("Construct DatabaseTimestamp with timestamp \"" + timestamp + "\" and version \"" + version + "\"");
		}
	}

	/**
	 * Check if this.timestamp is outdated.
	 * 
	 * @return Return true if the database timestamp is more recent than
	 * the time at construction. This means the database has changed.
	 *
	 */
	public boolean isOutdated() {
		// check if this.timestamp is older than timestamp from database
		try (Connection con = DatabaseManager.getConnection()) {
			try (PreparedStatement stmnt = con.prepareStatement("SELECT MAX(LAST_UPDATE) FROM LAST_UPDATE")) {
				ResultSet res = stmnt.executeQuery();
				Instant db_timestamp = null;
				if (res.next()) {
					db_timestamp = res.getTimestamp(1) == null ? null : res.getTimestamp(1).toInstant();
				}
				if (db_timestamp == null) {
					logger.error("Timestamp from database is \"null\". Return \"false\".");
					return false;
				}
				logger.trace("Found DatabaseTimestamp: " + db_timestamp);
				logger.trace("Own Timestamp: " + timestamp);
				logger.trace("isOutdated(): " + timestamp.isBefore(db_timestamp));
				return timestamp.isBefore(db_timestamp);
			}
		} catch (SQLException e) {
			logger.error("Database error.");
			logger.error(e.getMessage());
			logger.error("Return \"false\"");
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
