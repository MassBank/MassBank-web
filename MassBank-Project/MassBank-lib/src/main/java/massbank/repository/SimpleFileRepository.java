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
package massbank.repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import massbank.Config;
import massbank.Record;
import massbank.cli.Validator;

/**
 * This class implements a classical MassBank repository with record files 
 * in directories, but no subdirectories as in MassBank-data. It reads a
 * file named 'VERSION' with a version String for the whole repo and a
 * timestamp, which is applied to all Records.
 * @author rmeier
 * @version 26-10-2023
 */
public class SimpleFileRepository implements RepositoryInterface {
	private static final Logger logger = LogManager.getLogger(SimpleFileRepository.class);
	private String version;
	Instant timestamp;
	List<File> recordfiles = new ArrayList<>();
	
	
	public SimpleFileRepository() throws ConfigurationException {
		logger.info("Opening DataRootPath \"" + Config.get().DataRootPath() + "\" and iterate over content.");
		File dataRootPath = new File(Config.get().DataRootPath());
		// get version and timestamp
		Configurations configs = new Configurations();
		Configuration versionconfig = configs.properties(new File(dataRootPath, "VERSION"));
		
		version = versionconfig.getString("version");
		logger.info("Repo version: " + version);
		
		timestamp = ZonedDateTime.parse(versionconfig.getString("timestamp"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
		logger.info("Repo timestamp: " + timestamp);
		
		for (String file : dataRootPath.list(DirectoryFileFilter.INSTANCE)) {
			if (file.startsWith(".")) continue;
			recordfiles.addAll(FileUtils.listFiles(new File(dataRootPath, file), new String[] {"txt"}, false));
		}
		logger.info("Found " + recordfiles.size() + " records in repo.");	
	}
	
	public Stream<Record> getRecords() {
		return recordfiles.parallelStream().map(filename -> {
			Record record = null;
			logger.trace("Working on \'" + filename + "\'.");
			try {
				String recordString = FileUtils.readFileToString(filename, StandardCharsets.UTF_8);
				record = Validator.validate(recordString, Set.of("legacy"));
				if (record == null) {
					logger.error("Error in \'" + filename + "\'.");
				}
				else {
					record.setTimestamp(timestamp);
				}
			} catch (IOException e) {
				logger.error("Error reading record \"" + filename.toString() + "\". File will be ignored.\n", e);
			}
			return record;
		})
		.filter(Objects::nonNull);
	}
	 
	public String getRepoVersion() {
		return version;
	}
	
	public int getSize()
	{
		return recordfiles.size();
	}
}
