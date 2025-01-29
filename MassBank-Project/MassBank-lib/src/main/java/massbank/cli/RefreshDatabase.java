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

import massbank.ProjectPropertiesLoader;
import massbank.db.DatabaseManager;
import massbank.repository.RepositoryInterface;
import massbank.repository.SimpleFileRepository;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is called from command line. It clears all tables in the database, reads all records
 * and sends them to the database. Configuration is taken from class Config.
 *
 * Command line usage (no options):
 * RefreshDatabase
 *
 * @author rmeier
 * @version 04-12-2024
 */
public class RefreshDatabase {
	private static final Logger logger = LogManager.getLogger(RefreshDatabase.class);


	public static void main(String[] args) throws SQLException, IOException, ConfigurationException {
		// load version and print
		Properties properties = ProjectPropertiesLoader.loadProperties();
		System.out.println("RefreshDatabase version: " + properties.getProperty("version"));
		
		logger.trace("Remove all entries from database.");
		DatabaseManager.emptyTables();

		RepositoryInterface repo = new SimpleFileRepository();
		AtomicInteger progressCounter = new AtomicInteger(0);
		int totalRecords = repo.getSize();
		System.out.printf("%d records to process.%n", totalRecords);

		repo.getRecords().forEach((r) -> {
			try {
				DatabaseManager.persistAccessionFile(r);
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(1);
			}
			int progress = progressCounter.incrementAndGet();
			if (progress % (totalRecords / 100) == 0) {
				System.out.printf("\rProgress: %d/%d %.0f%%", progress, totalRecords, (progress * 100.0 / totalRecords));
			}
		});
		System.out.println();

        logger.info("Setting version of database to: {}.", repo.getRepoVersion());
		DatabaseManager.setRepoVersion(repo.getRepoVersion());
					
		DatabaseManager.close();
	}
}