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
package massbank.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import massbank.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.exception.CDKException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Convert Record structure to json+ld structured data.
 * 
 * @author rmeier
 * @version 07-06-2023
 */
public class RecordToJsonLD {
	private static final Logger logger = LogManager.getLogger(RecordToJsonLD.class);

	public static String convert(List<Record> records) {
		JsonArray allRecords = new JsonArray();
		for (Record r : records) {
			allRecords.addAll(r.createStructuredDataJsonArray());
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(allRecords);
	}

	/**
	 * A wrapper to convert multiple Records and write to file.
	 * 
	 * @param file    to write
	 * @param records to convert
	 * @throws CDKException
	 */
	public static void recordsToJson(File file, List<Record> records) {
		// collect data
		String recordJson = convert(records);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(recordJson);
		} catch (IOException e) {
			logger.error("Error writing JSON to file", e);
		}
	}
}
