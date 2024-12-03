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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import massbank.RecordParser;
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
import org.petitparser.context.Result;

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
	List<Path> recordfiles = new ArrayList<>();
	
	
	public SimpleFileRepository() throws ConfigurationException {
        logger.info("Opening DataRootPath {} and iterate over content.", Config.get().DataRootPath());
		Path dataRootPath = Path.of(Config.get().DataRootPath());
		// get version and timestamp
		Configurations configs = new Configurations();
		Configuration versionconfig = configs.properties(new File(dataRootPath.toFile(), "VERSION"));
		
		version = versionconfig.getString("version");
        logger.info("Repo version: {}", version);
		
		timestamp = ZonedDateTime.parse(versionconfig.getString("timestamp"), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        logger.info("Repo timestamp: {}", timestamp);

		try (Stream<Path> paths = Files.walk(dataRootPath)) {
			recordfiles = paths
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".txt"))
				.toList();
		} catch (IOException e) {
			logger.error("Error while listing files", e);
		}

        logger.info("Found {} records in repo.", recordfiles.size());
	}
	
	public Stream<Record> getRecords() {
		RecordParser recordparser = new RecordParser(new HashSet<>());
		return recordfiles.parallelStream()
            .map(Validator::readFile)
            .filter(Objects::nonNull)
            .map(recordString -> {
                return Validator.parseRecord(recordString, recordparser);
            })
			.filter(Objects::nonNull)
			.peek(record -> {record.setTimestamp(timestamp);});
	}
	 
	public String getRepoVersion() {
		return version;
	}
	
	public int getSize()
	{
		return recordfiles.size();
	}
}
