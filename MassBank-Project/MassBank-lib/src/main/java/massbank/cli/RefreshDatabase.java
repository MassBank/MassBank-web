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
package massbank.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import massbank.Config;
import massbank.Record;
import massbank.db.DatabaseManager;
import massbank.repository.RepositoryInterface;
import massbank.repository.SimpleFileRepository;

/**
 * This class is called from command line to create a new temporary
 * database <i>tmpdbName</i>, fill it with all records found in <i>DataRootPath</i>
 * and move the new database to <i>dbName</i>.
 *
 * @author rmeier
 * @version 24-01-2023
 */
public class RefreshDatabase {
	private static final Logger logger = LogManager.getLogger(RefreshDatabase.class);

	public static void main(String[] args) throws FileNotFoundException, SQLException, ConfigurationException, IOException {
		// load version and print
		final Properties properties = new Properties();
		try {
			properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("RefreshDatabase version: " + properties.getProperty("version"));
		
		logger.info("Creating a new database \""+ Config.get().tmpdbName() +"\" and initialize a MassBank database scheme.");
		DatabaseManager.init_db(Config.get().tmpdbName());
		
		logger.trace("Creating a DatabaseManager for \"" + Config.get().tmpdbName() + "\".");
		final DatabaseManager db = new DatabaseManager(Config.get().tmpdbName());
		
		RepositoryInterface repo = new SimpleFileRepository();
		List<Record> records = repo.getRecords();
		
		logger.info(records.size() + " records ready to be send to database.");
		AtomicInteger currentIndex = new AtomicInteger(1);
		int numRecordsOnePercent = records.size()/100+1;
		System.out.print(records.size() + " records to send to database. 0% Done.");
		records.stream().forEach((r) -> {
			db.persistAccessionFile(r);
			int index=currentIndex.getAndIncrement();
			if (index%numRecordsOnePercent == 0) {
				System.out.print("\r" + records.size() + " records to send to database. " + 100*index/records.size() + "% Done.");
			}
		});
		System.out.println("\r" + records.size() + " records to send to database. 100% Done");
		
		logger.info("Setting version of database to: " + repo.getRepoVersion() + ".");
		PreparedStatement stmnt = db.getConnection().prepareStatement("INSERT INTO LAST_UPDATE (LAST_UPDATE,VERSION) VALUES (CURRENT_TIMESTAMP,?);");
		stmnt.setString(1, repo.getRepoVersion());
		stmnt.executeUpdate();
		db.getConnection().commit();
		db.closeConnection();
					
		logger.trace("Moving new database to MassBank database.");
		DatabaseManager.move_temp_db_to_main_massbank();
		
	}
}