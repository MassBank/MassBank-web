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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import massbank.db.DatabaseManager;
import massbank.repository.RepositoryInterface;
import massbank.repository.SimpleFileRepository;

/**
 * This class is called from command line. It clears all tables and sends all 
 * records from the repo to the database.
 *
 * @author rmeier
 * @version 26-10-2023
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
		
		logger.trace("Remove all entries from database.");
		DatabaseManager.emptyTables();
		
		RepositoryInterface repo = new SimpleFileRepository();
		AtomicInteger currentIndex = new AtomicInteger(1);
		int repoOnePercent = (repo.getSize()/100)+1;
		System.out.print(repo.getSize() + " records to read. 0% Done.");
		
		repo.getRecords().forEach((r) -> {
			try {
				DatabaseManager.persistAccessionFile(r);
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(1);
			}
			int index=currentIndex.getAndIncrement();
			if (index%repoOnePercent == 0) {
				System.out.print("\r" + repo.getSize() + " records to send to database. " + 100*index/repo.getSize() + "% Done.");
			}
		});
		System.out.println("\r" + repo.getSize() + " records to send to database. 100% Done");
		
		logger.info("Setting version of database to: " + repo.getRepoVersion() + ".");
		DatabaseManager.setRepoVersion(repo.getRepoVersion());
					
		DatabaseManager.close();
	}
}