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
package massbank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import massbank.db.DatabaseManager;

/**
 * This class is called from command line to create a new temporary
 * database <i>tmpdbName</i>, fill it with all records found in <i>DataRootPath</i>
 * and move the new database to <i>dbName</i>.
 *
 * @author rmeier
 * @version 10-06-2020
 */
public class RefreshDatabase {
	private static final Logger logger = LogManager.getLogger(RefreshDatabase.class);

	public static void main(String[] args) throws FileNotFoundException, SQLException, ConfigurationException, IOException {
		logger.trace("Creating a new database \""+ Config.get().tmpdbName() +"\" and initialize a MassBank database scheme.");
		DatabaseManager.init_db(Config.get().tmpdbName());
		
		logger.trace("Creating a DatabaseManager for \"" + Config.get().tmpdbName() + "\".");
		final DatabaseManager db = new DatabaseManager(Config.get().tmpdbName());
		
		logger.trace("Get version of data source.");
		String version	= FileUtils.readFileToString(new File(Config.get().DataRootPath()+"/VERSION"), StandardCharsets.UTF_8);
		
		logger.info("Opening DataRootPath \"" + Config.get().DataRootPath() + "\" and iterate over content.");
		File dataRootPath = new File(Config.get().DataRootPath());
		List<File> recordfiles = new ArrayList<>();
		for (String file : dataRootPath.list(DirectoryFileFilter.INSTANCE)) {
			if (file.equals(".scripts")) continue;
			if (file.equals(".figure")) continue;
			recordfiles.addAll(FileUtils.listFiles(new File(dataRootPath, file), new String[] {"txt"}, true));
		}
		
		List<Record> accessions = recordfiles.parallelStream().map(filename -> {
			Record record=null;
			logger.info("Validating \"" + filename + "\".");
			String contributor = filename.getParentFile().getName();
			try {
				String recordAsString = FileUtils.readFileToString(filename, StandardCharsets.UTF_8);
				record = Validator.validate(recordAsString, contributor);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (record == null) {
				logger.error("Error reading/validating record \"" + filename.toString() + "\".");
			}
			return record;
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
		
		System.out.println(accessions.size());
		
		for (Record accession : accessions) {
			logger.trace("Writing record \"" + accession.ACCESSION() + "\" to database.");
			db.persistAccessionFile(accession);
		}
		
		logger.trace("Setting Timestamp in database");
		PreparedStatement stmnt = db.getConnection().prepareStatement("INSERT INTO LAST_UPDATE (TIME,VERSION) VALUES (CURRENT_TIMESTAMP,?);");
		stmnt.setString(1, version);
		stmnt.executeUpdate();
		db.getConnection().commit();
		db.closeConnection();
					
		logger.trace("Moving new database to MassBank database.");
		DatabaseManager.move_temp_db_to_main_massbank();
		
	}
}