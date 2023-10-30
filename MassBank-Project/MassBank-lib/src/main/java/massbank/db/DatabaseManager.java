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

import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import massbank.Config;
import massbank.Record;
import massbank.ScriptRunner;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * This class provides the code for storage and retrieval of records in SQL databases. 
 * 
 * @author rmeier
 * @version 26-10-2023
 *
 */
public class DatabaseManager {
	private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
	
	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;
	
	static {
		config.setDriverClassName("org.mariadb.jdbc.Driver");
		config.setJdbcUrl("jdbc:mariadb://" + Config.get().dbHostName() + ":3306/" + Config.get().dbName());
		config.setUsername("root");
		config.setPassword(Config.get().dbPassword());
		ds = new HikariDataSource(config);
	}

	private DatabaseManager() {
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
	
	public static void close() {
		ds.close();
	}
	
	public static void emptyTables() throws SQLException, IOException {
		try (Connection con = DatabaseManager.getConnection()) {
			con.setAutoCommit(false);
			try (PreparedStatement pst = con.prepareStatement("DROP VIEW msms_spectrum, msms_spectrum_peak, ms_compound, synonym")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE COMMENT")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE SP_SAMPLE")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE SP_LINK")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE AC_MASS_SPECTROMETRY")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE AC_CHROMATOGRAPHY")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE MS_FOCUSED_ION")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE MS_DATA_PROCESSING")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE ANNOTATION_HEADER")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE ANNOTATION")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE PEAK")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE RECORD")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE COMPOUND_COMPOUND_CLASS")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE CONTRIBUTOR")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE COMPOUND_NAME")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE CH_LINK")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE COMPOUND")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE COMPOUND_CLASS")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE NAME")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE SAMPLE")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE INSTRUMENT")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE DEPRECATED_RECORD")) {
				pst.executeUpdate();
			}
			try (PreparedStatement pst = con.prepareStatement("DROP TABLE LAST_UPDATE")) {
				pst.executeUpdate();
			}
			
			ScriptRunner runner = new ScriptRunner(con, false);
			runner.runScript(new InputStreamReader(DatabaseManager.class.getClassLoader().getResourceAsStream("create_massbank_scheme.sql")));
			
			con.commit();
			con.setAutoCommit(true);
		}
	}
	
	// TABLE CONTRIBUTOR
	private final static String insertCONTRIBUTOR = "INSERT INTO CONTRIBUTOR(ACRONYM, SHORT_NAME, FULL_NAME) VALUES (?,?,?) ON DUPLICATE KEY UPDATE ACRONYM=ACRONYM";
	private final static String selectCONTRIBUTORIdByACRONYM = "SELECT * FROM CONTRIBUTOR WHERE ACRONYM = ?";
	// TABLE COMPOUND
	private final static String insertCOMPOUND = "INSERT INTO COMPOUND(CH_FORMULA, CH_EXACT_MASS, CH_EXACT_MASS_SIGNIFICANT," +
		"CH_SMILES, CH_IUPAC) " +
		"VALUES(?,?,?,?,?)";
	private final static String selectCOMPOUND = "SELECT * FROM COMPOUND WHERE ID = ?";
	// TABLE COMPOUND_CLASS
	private final static String insertCOMPOUND_CLASS = "INSERT INTO COMPOUND_CLASS(DATABASE_NAME, DATABASE_ID, CH_COMPOUND_CLASS) VALUES(?,?,?)";
	private final static String selectCOMPOUND_CLASS = "SELECT * FROM COMPOUND_CLASS WHERE ID = ?";
	// TABLE COMPOUND_COMPOUND_CLASS
	private final static String insertCOMPOUND_COMPOUND_CLASS = "INSERT INTO COMPOUND_COMPOUND_CLASS(COMPOUND, CLASS) VALUES(?,?)";
	private final static String selectCOMPOUND_COMPOUND_CLASS = "SELECT * FROM COMPOUND_COMPOUND_CLASS WHERE COMPOUND = ?";
	// TABLE NAME
	private final static String insertNAME = "INSERT INTO NAME(CH_NAME) VALUES(?)";
	private final static String selectNAME = "SELECT * FROM NAME WHERE ID = ?";
	// TABLE COMPOUND_NAME
	private final static String insertCOMPOUND_NAME = "INSERT IGNORE INTO COMPOUND_NAME VALUES(?,?)";
	private final static String selectCOMPOUND_NAME = "SELECT * FROM COMPOUND_NAME WHERE COMPOUND = ? ORDER BY NAME";
	// TABLE CH_LINK
	private final static String insertCH_LINK = "INSERT INTO CH_LINK(COMPOUND, DATABASE_NAME, DATABASE_ID) VALUES(?,?,?)";
	private final static String selectCH_LINK = "SELECT * FROM CH_LINK WHERE COMPOUND = ? ORDER BY ID";
	// TABLE SAMPLE
	private final static String insertSAMPLE = "INSERT INTO SAMPLE(SP_SCIENTIFIC_NAME, SP_LINEAGE) VALUES(?,?)";
	private final static String selectSAMPLE = "SELECT * FROM SAMPLE WHERE ID = ?";
	// TABLE INSTRUMENT
	private final static String insertINSTRUMENT = "INSERT INTO INSTRUMENT(AC_INSTRUMENT, AC_INSTRUMENT_TYPE) VALUES(?,?)";
	private final static String selectINSTRUMENT = "SELECT * FROM INSTRUMENT WHERE ID = ?";
	// TABLE RECORD
	private final static String insertRECORD = "INSERT INTO RECORD(ACCESSION, RECORD_TIMESTAMP, RECORD_TITLE, DATE, AUTHORS, LICENSE, COPYRIGHT, PUBLICATION, PROJECT," + 
			"CH, SP, AC_INSTRUMENT, AC_MASS_SPECTROMETRY_MS_TYPE, AC_MASS_SPECTROMETRY_ION_MODE, PK_SPLASH, CONTRIBUTOR)" +
			"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private final static String selectRECORD = "SELECT * FROM RECORD WHERE ACCESSION = ?";
	// TABLE RECORD
	private final static String insertDEPRECATED_RECORD = "INSERT INTO DEPRECATED_RECORD(ACCESSION, CONTRIBUTOR, CONTENT) VALUES (?,?,?)"; 
	private final static String selectDEPRECATED_RECORD = "SELECT * FROM DEPRECATED_RECORD WHERE ACCESSION = ?";
	// TABLE COMMENT
	private final static String insertCOMMENT = "INSERT INTO COMMENT(RECORD, COMMENT) VALUES(?,?)";
	private final static String selectCOMMENT = "SELECT * FROM COMMENT WHERE RECORD = ? ORDER BY ID";
	// TABLE SP_SAMPLE
	private final static String insertSP_SAMPLE = "INSERT INTO SP_SAMPLE(RECORD, SP_SAMPLE) VALUES(?,?)";
	private final static String selectSP_SAMPLE = "SELECT * FROM SP_SAMPLE WHERE RECORD = ? ORDER BY ID";
	// TABLE SP_LINK
	private final static String insertSP_LINK = "INSERT INTO SP_LINK(RECORD, SP_LINK) VALUES(?,?)";
	private final static String selectSP_LINK = "SELECT * FROM SP_LINK WHERE RECORD = ? ORDER BY ID";
	// TABLE AC_MASS_SPECTROMETRY
	private final static String insertAC_MASS_SPECTROMETRY = "INSERT INTO AC_MASS_SPECTROMETRY(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final static String selectAC_MASS_SPECTROMETRY = "SELECT * FROM AC_MASS_SPECTROMETRY WHERE RECORD = ? ORDER BY ID";
	// TABLE AC_CHROMATOGRAPHY
	private final static String insertAC_CHROMATOGRAPHY = "INSERT INTO AC_CHROMATOGRAPHY(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final static String selectAC_CHROMATOGRAPHY = "SELECT * FROM AC_CHROMATOGRAPHY WHERE RECORD = ? ORDER BY ID";
	// TABLE MS_FOCUSED_ION
	private final static String insertMS_FOCUSED_ION = "INSERT INTO MS_FOCUSED_ION(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final static String selectMS_FOCUSED_ION = "SELECT * FROM MS_FOCUSED_ION WHERE RECORD = ? ORDER BY ID";
	// TABLE MS_DATA_PROCESSING
	private final static String insertMS_DATA_PROCESSING = "INSERT INTO MS_DATA_PROCESSING(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final static String selectMS_DATA_PROCESSING = "SELECT * FROM MS_DATA_PROCESSING WHERE RECORD = ? ORDER BY ID";
	// TABLE PEAK
	private final static String insertPEAK = "INSERT INTO PEAK(RECORD, PK_PEAK_MZ, PK_PEAK_MZ_SIGNIFICANT, PK_PEAK_INTENSITY, " +
			"PK_PEAK_INTENSITY_SIGNIFICANT, PK_PEAK_RELATIVE) VALUES(?,?,?,?,?,?)";
	private final static String selectPEAK = "SELECT * FROM PEAK WHERE RECORD = ? ORDER BY PK_PEAK_MZ";
	// TABLE ANNOTATION_HEADER
	private final static String insertANNOTATION_HEADER = "INSERT INTO ANNOTATION_HEADER(RECORD, HEADER) VALUES(?,?)";
	private final static String selectANNOTATION_HEADER = "SELECT * FROM ANNOTATION_HEADER WHERE RECORD = ?";
	// TABLE ANNOTATION
	private final static String insertANNOTATION = "INSERT INTO ANNOTATION VALUES(?,?,?,?)";
	private final static String selectANNOTATION = "SELECT * FROM ANNOTATION WHERE RECORD = ?";
	
	private final static String sqlGetContributorFromAccession = 
			"SELECT ACRONYM, SHORT_NAME, FULL_NAME FROM CONTRIBUTOR WHERE ID =" +
			"(" +
			"	SELECT CONTRIBUTOR" + 
			"	FROM" +
			"	(" +
			"		(SELECT ACCESSION, CONTRIBUTOR FROM RECORD) UNION" + 
			"		(SELECT ACCESSION, CONTRIBUTOR FROM DEPRECATED_RECORD)" +
			"	) MERGED_CONTRIBUTOR" +
			"	WHERE ACCESSION = ?" +
			");";
	private final static String sqlGetAccessions = 
			"SELECT ACCESSION " + 
			"FROM RECORD;";
	
//	/**
//	 * Create a database with the MassBank database scheme.
//	 * @param dbName the name of the new database
//	 */
//	public static void init_db(String dbName) throws SQLException, ConfigurationException, FileNotFoundException, IOException {
//		String link="jdbc:mariadb://" 
//				+ Config.get().dbHostName() + ":3306/" 
//				+ "?user=root" 
//				+ "&password=" + Config.get().dbPassword();
//		
//		logger.trace("Opening database connection with url\"" + link + "\".");
//		Connection connection = DriverManager.getConnection(link);
//		Statement stmt = connection.createStatement();
//		
//		logger.trace("Executing sql statements to create empty database \"" + dbName + "\".");
//		stmt.executeUpdate("DROP DATABASE IF EXISTS " + dbName + ";");
//		stmt.executeUpdate("CREATE DATABASE " + dbName + " CHARACTER SET = 'utf8';");
//		stmt.executeUpdate("USE " + dbName + ";");
//				
//		logger.trace("Executing sql statements in file at: \"" + DatabaseManager.class.getClassLoader().getResource("create_massbank_scheme.sql") + "\".");
//		ScriptRunner runner = new ScriptRunner(connection, false);
//		runner.runScript(new InputStreamReader(DatabaseManager.class.getClassLoader().getResourceAsStream("create_massbank_scheme.sql")));
//		
//		stmt.close();
//		logger.trace("Closing connection with url\"" + link + "\".");
//		connection.commit();
//		connection.close();
//	}
	
	/**
	 * Store the content of the given record in the database
	 * 
	 * @param  rec the record to store
	 */
	public static void persistAccessionFile(Record rec) throws SQLException {
		// get contributor ID or create
		Integer contributorId = -1;
		try (Connection con = ds.getConnection()) {
			con.setAutoCommit(false);
			// add contributor if not already there
			try (PreparedStatement pst = con.prepareStatement(insertCONTRIBUTOR)) {
				pst.setString(1, rec.CONTRIBUTOR());
				pst.setString(2, rec.CONTRIBUTOR());
				pst.setString(3, rec.CONTRIBUTOR());
				pst.executeUpdate();
			}
			// get the ID
			try (PreparedStatement pst = con.prepareStatement(selectCONTRIBUTORIdByACRONYM)) {
				pst.setString(1, rec.CONTRIBUTOR());
				try (ResultSet set = pst.executeQuery()) {
					set.next();
					contributorId = set.getInt(1);
				}
			}
			
			// deprecated record goes to separate table
			if (rec.DEPRECATED()) {
				try (PreparedStatement pst = con.prepareStatement(insertDEPRECATED_RECORD)) {
					pst.setString(1, rec.ACCESSION());
					pst.setInt(2, contributorId);
					pst.setBlob(3, new ByteArrayInputStream(rec.DEPRECATED_CONTENT().getBytes()));
					pst.executeUpdate();
					return;
				}
			}
			
			// add to COMPOUND table
			Integer compoundId = -1;
			try (PreparedStatement pst = con.prepareStatement(insertCOMPOUND, Statement.RETURN_GENERATED_KEYS)) {
				pst.setString(1, rec.CH_FORMULA());
				pst.setDouble(2, rec.CH_EXACT_MASS().doubleValue());
				pst.setInt(3, rec.CH_EXACT_MASS().scale());
				pst.setString(4, rec.CH_SMILES());
				pst.setString(5, rec.CH_IUPAC());
				pst.executeUpdate();
				try (ResultSet set = pst.getGeneratedKeys()) {
					set.next();
					compoundId = set.getInt(1);
				}
			}
			// add to COMPOUND_CLASS and connect with compoundId
			for (String compound : rec.CH_COMPOUND_CLASS()) {
				Integer compoundClassId = -1;
				try (PreparedStatement pst = con.prepareStatement(insertCOMPOUND_CLASS, Statement.RETURN_GENERATED_KEYS)) {
					pst.setString(1, null);
					pst.setString(2, null);
					pst.setString(3, compound);
					pst.executeUpdate();
					try (ResultSet set = pst.getGeneratedKeys()) {
						set.next();
						compoundClassId = set.getInt(1);
					}
				}
				try (PreparedStatement pst = con.prepareStatement(insertCOMPOUND_COMPOUND_CLASS)) {
					pst.setInt(1, compoundId);
					pst.setInt(2, compoundClassId);
					pst.executeUpdate();
				}
			}
			
			// add to NAME and connect with compoundId
			for (String name : rec.CH_NAME()) {
				Integer nameId;
				try (PreparedStatement pst = con.prepareStatement(insertNAME, Statement.RETURN_GENERATED_KEYS)) {
					pst.setString(1, name);
					pst.executeUpdate();
					try (ResultSet set = pst.getGeneratedKeys()) {
						set.next();
						nameId = set.getInt(1);
					}			
				}
				try (PreparedStatement pst = con.prepareStatement(insertCOMPOUND_NAME)) {
					pst.setInt(1, compoundId);
					pst.setInt(2, nameId);
					pst.executeUpdate();
				}
			}
			
			// add to CH_LINK and connect with compoundId
			try (PreparedStatement pst = con.prepareStatement(insertCH_LINK)) {
				Iterator<Entry<String, String>> itr = rec.CH_LINK().entrySet().iterator();
				while (itr.hasNext()) {
					Entry<String,String> entry = itr.next();
					pst.setInt(1,compoundId);
					pst.setString(2, entry.getKey());
					pst.setString(3, entry.getValue());
					pst.addBatch();		
				}
				pst.executeBatch();
			}
			
			// add to SAMPLE
			Integer sampleId = -1;
			try (PreparedStatement pst = con.prepareStatement(insertSAMPLE, Statement.RETURN_GENERATED_KEYS)) {
				if (rec.SP_SCIENTIFIC_NAME() != null) {
					pst.setString(1, rec.SP_SCIENTIFIC_NAME());
				} else {
					pst.setNull(1, java.sql.Types.VARCHAR);
				}
				if (rec.SP_LINEAGE() != null) {
					pst.setString(2, rec.SP_LINEAGE());
				} else {
					pst.setNull(2, java.sql.Types.VARCHAR);
				}
				if (rec.SP_SCIENTIFIC_NAME() != null || rec.SP_LINEAGE() != null) {
					pst.executeUpdate();
					try (ResultSet set = pst.getGeneratedKeys()) {
						set.next();
						sampleId = set.getInt(1);
					}
				}
			}
			
			// add to INSTRUMENT
			Integer instrumentId = -1;
			try (PreparedStatement pst = con.prepareStatement(insertINSTRUMENT, Statement.RETURN_GENERATED_KEYS)) {
				pst.setString(1, rec.AC_INSTRUMENT());
				pst.setString(2, rec.AC_INSTRUMENT_TYPE());
				pst.executeUpdate();
				try (ResultSet set = pst.getGeneratedKeys()) {
					set.next();
					instrumentId = set.getInt(1);
				}
			}
			
			// add to RECORD
			try (PreparedStatement pst = con.prepareStatement(insertRECORD)) {
				pst.setString(1, rec.ACCESSION());
				pst.setTimestamp(2, Timestamp.from(rec.getTimestamp()));
				pst.setString(3, rec.RECORD_TITLE1());
				pst.setString(4, rec.DATE());
				pst.setString(5, rec.AUTHORS());
				pst.setString(6, rec.LICENSE());			
				if (rec.COPYRIGHT() != null) {
					pst.setString(7, rec.COPYRIGHT());			
				} else {
					pst.setNull(7, java.sql.Types.VARCHAR);
				}
				if (rec.PUBLICATION() != null) {
					pst.setString(8, rec.PUBLICATION());			
				} else {
					pst.setNull(8, java.sql.Types.VARCHAR);
				}
				if (rec.PROJECT() != null) {
					pst.setString(9, rec.PROJECT());			
				} else {
					pst.setNull(9, java.sql.Types.VARCHAR);
				}
				pst.setInt(10, compoundId);
				if (sampleId > 0) {
					pst.setInt(11, sampleId);
				} else {
					pst.setNull(11, java.sql.Types.INTEGER);
				}
				pst.setInt(12, instrumentId);
				pst.setString(13, rec.AC_MASS_SPECTROMETRY_MS_TYPE());
				pst.setString(14, rec.AC_MASS_SPECTROMETRY_ION_MODE());
				pst.setString(15, rec.PK_SPLASH());
				pst.setInt(16, contributorId);
				pst.executeUpdate();
			}
			
			// add to SP_SAMPLE
			try (PreparedStatement pst = con.prepareStatement(insertSP_SAMPLE, Statement.RETURN_GENERATED_KEYS)) {
				for (String sample : rec.SP_SAMPLE()) {
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, sample);
					pst.addBatch();
				}
				pst.executeBatch();
			}
				
			// add to SP_LINK and connect with ACCESSION
			try (PreparedStatement pst = con.prepareStatement(insertSP_LINK)) {
				Iterator<Entry<String, String>> itr = rec.SP_LINK().entrySet().iterator();
				while (itr.hasNext()) {
					Entry<String, String> entry = itr.next();
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, entry.getKey() + " " + entry.getValue());
					pst.addBatch();
				}
				pst.executeBatch();
			}
			
			// add to COMMENT and connect with ACCESSION
			try (PreparedStatement pst = con.prepareStatement(insertCOMMENT)) {
				for (String comment : rec.COMMENT()) {
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, comment);
					pst.addBatch();
				}
				pst.executeBatch();
			}
	
			// add to AC_MASS_SPECTROMETRY and connect with ACCESSION
			try (PreparedStatement pst = con.prepareStatement(insertAC_MASS_SPECTROMETRY)) {
				for (Pair<String, String> massspectrometry : rec.AC_MASS_SPECTROMETRY()) {
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, massspectrometry.getLeft());
					pst.setString(3, massspectrometry.getRight());
					pst.addBatch();
				}
				pst.executeBatch();
			}

			// add to AC_CHROMATOGRAPHY and connect with ACCESSION
			try (PreparedStatement pst = con.prepareStatement(insertAC_CHROMATOGRAPHY)) {
				for (Pair<String, String> chromatography : rec.AC_CHROMATOGRAPHY()) {
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, chromatography.getLeft());
					pst.setString(3, chromatography.getRight());
					pst.addBatch();
				}
				pst.executeBatch();
			}
			
			// add to AC_CHROMATOGRAPHY and connect with ACCESSION
			try (PreparedStatement pst = con.prepareStatement(insertMS_FOCUSED_ION)) {
				for (Pair<String, String> focusedion : rec.MS_FOCUSED_ION()) {
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, focusedion.getLeft());
					pst.setString(3, focusedion.getRight());
					pst.addBatch();
				}
				pst.executeBatch();
			}
			
			// add to MS_DATA_PROCESSING and connect with ACCESSION
			try (PreparedStatement pst = con.prepareStatement(insertMS_DATA_PROCESSING)) {
				for (Pair<String, String> dataprocessing : rec.MS_DATA_PROCESSING()) {
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, dataprocessing.getLeft());
					pst.setString(3, dataprocessing.getRight());
					pst.addBatch();
				}
				pst.executeBatch();
			}

			// add to PEAK and connect with ACCESSION
			try (PreparedStatement pst = con.prepareStatement(insertPEAK)) {
				for (Triple<BigDecimal,BigDecimal,Integer> peak : rec.PK_PEAK()) {
					pst.setString(1, rec.ACCESSION());
					pst.setDouble(2, peak.getLeft().doubleValue());
					pst.setInt(3, peak.getLeft().scale());
					
					pst.setDouble(4, peak.getMiddle().doubleValue());
					pst.setInt(5, peak.getMiddle().scale());
					
					pst.setInt(6, peak.getRight().intValue());
					pst.addBatch();
				}
				pst.executeBatch();
			}
			
			// add to ANNOTATION_HEADER and ANNOTATIOM and connect with ACCESSION
			if (!rec.PK_ANNOTATION_HEADER().isEmpty()) {
				try (PreparedStatement pst = con.prepareStatement(insertANNOTATION_HEADER)) {
					pst.setString(1, rec.ACCESSION());
					pst.setString(2, String.join(" ", rec.PK_ANNOTATION_HEADER()));
					pst.executeUpdate();
				}
				try (PreparedStatement pst = con.prepareStatement(insertANNOTATION)) {
					for (Pair<BigDecimal, List<String>> annotation : rec.PK_ANNOTATION()) {
						pst.setString(1, rec.ACCESSION());
						pst.setDouble(2, annotation.getLeft().doubleValue());
						pst.setInt(3, annotation.getLeft().scale());
						pst.setString(4, String.join(" ",annotation.getRight()));
						pst.addBatch();
					}
					pst.executeBatch();
				}
			}
			con.commit();
			con.setAutoCommit(true);
		}
	}
	
	public static void setRepoVersion(String version) throws SQLException {
		try (Connection con = ds.getConnection()) {
			PreparedStatement stmnt = con.prepareStatement("INSERT INTO LAST_UPDATE (LAST_UPDATE,VERSION) VALUES (CURRENT_TIMESTAMP,?)");
			stmnt.setString(1, version);
			stmnt.executeUpdate();
		}
		
	}
	
	/**
	 * Returns the complete record TODO solve 1:1 relations by a single sql
	 * statement with joins (PK_ANNOTATION_HEADER, acc.PK_NUM_PEAK, Compound stuff,
	 * SP_SCIENTIFIC_NAME, SP_LINEAGE, AC_INSTRUMENT, AC_INSTRUMENT_TYPE)
	 * 
	 * @param accessionId
	 * @return Record
	 */
	public static Record getAccessionData(String accessionId) {
		Record acc = new Record();		
		try (Connection con = ds.getConnection()) {
			PreparedStatement statementSelectCOMPOUND = con.prepareStatement(selectCOMPOUND);
			PreparedStatement statementSelectCOMPOUND_CLASS = con.prepareStatement(selectCOMPOUND_CLASS);
			PreparedStatement statementSelectCOMPOUND_COMPOUND_CLASS = con.prepareStatement(selectCOMPOUND_COMPOUND_CLASS);
			PreparedStatement statementSelectNAME = con.prepareStatement(selectNAME);
			PreparedStatement statementSelectCOMPOUND_NAME = con.prepareStatement(selectCOMPOUND_NAME);
			PreparedStatement statementSelectCH_LINK = con.prepareStatement(selectCH_LINK);
			PreparedStatement statementSelectSAMPLE = con.prepareStatement(selectSAMPLE);
			PreparedStatement statementSelectINSTRUMENT = con.prepareStatement(selectINSTRUMENT);
			PreparedStatement statementSelectRECORD = con.prepareStatement(selectRECORD);
			PreparedStatement statementSelectDEPRECATED_RECORD = con.prepareStatement(selectDEPRECATED_RECORD);
			PreparedStatement statementSelectCOMMENT = con.prepareStatement(selectCOMMENT);
			PreparedStatement statementSelectSP_SAMPLE = con.prepareStatement(selectSP_SAMPLE);
			PreparedStatement statementSelectSP_LINK = con.prepareStatement(selectSP_LINK);
			PreparedStatement statementSelectAC_MASS_SPECTROMETRY = con.prepareStatement(selectAC_MASS_SPECTROMETRY);
			PreparedStatement statementSelectAC_CHROMATOGRAPHY = con.prepareStatement(selectAC_CHROMATOGRAPHY);
			PreparedStatement statementSelectMS_FOCUSED_ION = con.prepareStatement(selectMS_FOCUSED_ION);
			PreparedStatement statementSelectMS_DATA_PROCESSING = con.prepareStatement(selectMS_DATA_PROCESSING);
			PreparedStatement statementSelectPEAK = con.prepareStatement(selectPEAK);
			PreparedStatement statementSelectANNOTATION_HEADER = con.prepareStatement(selectANNOTATION_HEADER);
			PreparedStatement statementSelectANNOTATION = con.prepareStatement(selectANNOTATION);
			
			statementSelectRECORD.setString(1, accessionId);
			ResultSet set = statementSelectRECORD.executeQuery();
			int compoundID = -1;
			int sampleID = -1;
			int instrumentID = -1;
			if (set.next()) {
				acc.ACCESSION(set.getString("ACCESSION"));
				acc.setTimestamp(set.getTimestamp("RECORD_TIMESTAMP").toInstant());
				acc.RECORD_TITLE1(set.getString("RECORD_TITLE"));
				acc.DATE(set.getString("DATE"));
				acc.AUTHORS(set.getString("AUTHORS"));
				acc.LICENSE(set.getString("LICENSE"));
				acc.COPYRIGHT(set.getString("COPYRIGHT"));
				acc.PUBLICATION(set.getString("PUBLICATION"));
				acc.PROJECT(set.getString("PROJECT"));
				compoundID = set.getInt("CH");
				sampleID = set.getInt("SP");
				instrumentID = set.getInt("AC_INSTRUMENT");
				acc.AC_MASS_SPECTROMETRY_MS_TYPE(set.getString("AC_MASS_SPECTROMETRY_MS_TYPE"));
				acc.AC_MASS_SPECTROMETRY_ION_MODE(set.getString("AC_MASS_SPECTROMETRY_ION_MODE"));
				acc.PK_SPLASH(set.getString("PK_SPLASH"));
				statementSelectAC_CHROMATOGRAPHY.setString(1, set.getString("ACCESSION"));
				statementSelectAC_MASS_SPECTROMETRY.setString(1, set.getString("ACCESSION"));
				statementSelectMS_DATA_PROCESSING.setString(1, set.getString("ACCESSION"));
				statementSelectMS_FOCUSED_ION.setString(1, set.getString("ACCESSION"));
				statementSelectCOMMENT.setString(1, set.getString("ACCESSION"));
				statementSelectPEAK.setString(1, set.getString("ACCESSION"));
				// statementPK_NUM_PEAK.setString(1, set.getString("ACCESSION"));
				statementSelectANNOTATION_HEADER.setString(1, accessionId);
				
				ResultSet tmp = statementSelectAC_CHROMATOGRAPHY.executeQuery();
				List<Pair<String, String>> tmpList	= new ArrayList<Pair<String, String>>();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.AC_CHROMATOGRAPHY(tmpList);
				
				tmp = statementSelectAC_MASS_SPECTROMETRY.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.AC_MASS_SPECTROMETRY(tmpList);
				
				tmp = statementSelectMS_DATA_PROCESSING.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.MS_DATA_PROCESSING(tmpList);
				
				tmp = statementSelectMS_FOCUSED_ION.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.MS_FOCUSED_ION(tmpList);
				
				tmp = statementSelectCOMMENT.executeQuery();
				List<String> tmpList2	= new ArrayList<String>();
				while (tmp.next())
					tmpList2.add(tmp.getString("COMMENT"));
				acc.COMMENT(tmpList2);
				
				tmp = statementSelectANNOTATION_HEADER.executeQuery();
//				int PK_ANNOTATION_HEADER_numberOfTokens	= -1;
				if (tmp.next()) {
					String PK_ANNOTATION_HEADER	= tmp.getString("HEADER");
					String[] PK_ANNOTATION_HEADER_tokens	= PK_ANNOTATION_HEADER.split(" ");
					acc.PK_ANNOTATION_HEADER(Arrays.asList(PK_ANNOTATION_HEADER_tokens));
//					PK_ANNOTATION_HEADER_numberOfTokens	= PK_ANNOTATION_HEADER_tokens.length;
					
					statementSelectANNOTATION.setString(1, set.getString("ACCESSION"));
					tmp = statementSelectANNOTATION.executeQuery();
					while (tmp.next()) {
						BigDecimal mz = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_MZ")))).setScale(tmp.getInt("PK_PEAK_MZ_SIGNIFICANT"));
						List<String> annotation = Arrays.asList(tmp.getString("PK_ANNOTATION").split(" "));
						acc.PK_ANNOTATION_ADD_LINE(Pair.of(mz, annotation));
					}
					
				}
				
				tmp = statementSelectPEAK.executeQuery();
				while (tmp.next()) {
					BigDecimal mz = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_MZ")))).setScale(tmp.getInt("PK_PEAK_MZ_SIGNIFICANT"));
					BigDecimal intensity = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_INTENSITY")))).setScale(tmp.getInt("PK_PEAK_INTENSITY_SIGNIFICANT"));
					acc.PK_PEAK_ADD_LINE(Triple.of(mz, intensity, tmp.getInt("PK_PEAK_RELATIVE")));
				}
			} else {
				// try to find the ACCESSION in DEPRECATED_RECORD
				statementSelectDEPRECATED_RECORD.setString(1, accessionId);
				set = statementSelectDEPRECATED_RECORD.executeQuery();
				if (set.next()) {
					acc.ACCESSION(set.getString("ACCESSION"));
					acc.DEPRECATED(true);
					acc.DEPRECATED_CONTENT(set.getString("CONTENT"));
					return acc;
				} else return null;
			}
			set.close();
			
			
			if (compoundID == -1)
				throw new IllegalStateException("compoundID is not set");
			statementSelectCOMPOUND.setInt(1, compoundID);
			set = statementSelectCOMPOUND.executeQuery();
			while (set.next()) {
				acc.CH_FORMULA(set.getString("CH_FORMULA"));
				BigDecimal exactMass = (new BigDecimal(String.valueOf(set.getDouble("CH_EXACT_MASS")))).setScale(set.getInt("CH_EXACT_MASS_SIGNIFICANT"));
				acc.CH_EXACT_MASS(exactMass);
				acc.CH_SMILES(set.getString("CH_SMILES"));
				acc.CH_IUPAC(set.getString("CH_IUPAC"));
			}
			set.close();
			
			statementSelectCH_LINK.setInt(1, compoundID);
			set = statementSelectCH_LINK.executeQuery();
			LinkedHashMap<String, String> tmpMap = new LinkedHashMap<String, String>();
			while (set.next()) {
				tmpMap.put(set.getString("DATABASE_NAME"), set.getString("DATABASE_ID"));
			}
			acc.CH_LINK(tmpMap);
			
			statementSelectCOMPOUND_COMPOUND_CLASS.setInt(1, compoundID);
			set = statementSelectCOMPOUND_COMPOUND_CLASS.executeQuery();
			List<String> tmpList2	= new ArrayList<String>();
			while (set.next()) {
				statementSelectCOMPOUND_CLASS.setInt(1, set.getInt("CLASS"));
				ResultSet tmp = statementSelectCOMPOUND_CLASS.executeQuery();
				while (tmp.next()) {
					tmpList2.add(tmp.getString("CH_COMPOUND_CLASS"));
				}
			}
			acc.CH_COMPOUND_CLASS(tmpList2);
			
			statementSelectCOMPOUND_NAME.setInt(1, compoundID);
			set = statementSelectCOMPOUND_NAME.executeQuery();
			tmpList2.clear();
			while (set.next()) {
				int name = set.getInt("NAME") ;
				statementSelectNAME.setInt(1, name);	
				//statementSelectNAME.setInt(1, set.getInt("NAME"));
				ResultSet tmp = statementSelectNAME.executeQuery();
				while (tmp.next()) {
					tmpList2.add(tmp.getString("CH_NAME"));
				}
			}
			acc.CH_NAME(tmpList2);
			
			statementSelectSAMPLE.setInt(1,sampleID);
			set = statementSelectSAMPLE.executeQuery();
			if (set.next()) {
				acc.SP_SCIENTIFIC_NAME(set.getString("SP_SCIENTIFIC_NAME"));
				acc.SP_LINEAGE(set.getString("SP_LINEAGE"));
			}
			
			statementSelectSP_LINK.setString(1,acc.ACCESSION());
			set = statementSelectSP_LINK.executeQuery();
			
			
			LinkedHashMap<String, String> tmpList = new LinkedHashMap<String, String>();
			while (set.next()) {
				String spLink	= set.getString("SP_LINK");
				String[] tokens	= spLink.split(" ");
				tmpList.put(tokens[0], String.join(" ", Arrays.asList(tokens).subList(1, tokens.length)));
			}
			acc.SP_LINK(tmpList);
				
			statementSelectSP_SAMPLE.setString(1,acc.ACCESSION());
			set = statementSelectSP_SAMPLE.executeQuery();
			tmpList2.clear();
			while (set.next()) {
				tmpList2.add(set.getString("SP_SAMPLE"));
			}
			acc.SP_SAMPLE(tmpList2);
			
			if (instrumentID == -1)	throw new IllegalStateException("instrumentID is not set");
			statementSelectINSTRUMENT.setInt(1, instrumentID);
			set = statementSelectINSTRUMENT.executeQuery();
			if (set.next()) {
				acc.AC_INSTRUMENT(set.getString("AC_INSTRUMENT"));
				acc.AC_INSTRUMENT_TYPE(set.getString("AC_INSTRUMENT_TYPE"));
			} else	throw new IllegalStateException("instrumentID is not in database");
		} catch (Exception e) {
			System.out.println("error: " + accessionId);
			e.printStackTrace();
			return null;
		}
		return acc;
	}
	
	
	public static Record.Structure getStructureOfAccession(String accessionId) {
		String CH_SMILES	= null;
		String CH_IUPAC		= null;
		
		try (Connection con = ds.getConnection()) {
			PreparedStatement statementSelectRECORD = con.prepareStatement(selectRECORD);
			PreparedStatement statementSelectCOMPOUND = con.prepareStatement(selectCOMPOUND);
			
			statementSelectRECORD.setString(1, accessionId);
			ResultSet set = statementSelectRECORD.executeQuery();
			int compoundID = -1;
			if (set.next()) {
				compoundID = set.getInt("CH");
			} else throw new IllegalStateException("accessionId '" + accessionId + "' is not in database");
			
			if (compoundID == -1)
				throw new IllegalStateException("compoundID is not set");
			statementSelectCOMPOUND.setInt(1, compoundID);
			set = statementSelectCOMPOUND.executeQuery();
			while (set.next()) {
				String smilesString	= set.getString("CH_SMILES");
				if (!smilesString.equals("N/A")) CH_SMILES	= smilesString;
				
				String iupacString	= set.getString("CH_IUPAC");
				if (!iupacString.equals("N/A")) CH_IUPAC	= iupacString;
			}
		} catch (Exception e) {
			System.out.println("error: " + accessionId);
			e.printStackTrace();
			return null;
		}
		
		return new Record.Structure(CH_SMILES, CH_IUPAC);
	}
	



	
	public static Record.Contributor getContributorFromAccession(String accessionId) {
//		String accessionId	= "OUF01001";
		Record.Contributor contributor	= null;
		try (Connection con = ds.getConnection()) {
			PreparedStatement statementGetContributorFromAccession = con.prepareStatement(sqlGetContributorFromAccession);
			statementGetContributorFromAccession.setString(1, accessionId);
			
			ResultSet tmp = statementGetContributorFromAccession.executeQuery();
			
			//if (!tmp.next()) throw new IllegalStateException("Accession '" + accessionId + "' is not in database");
			if (!tmp.next()) return null;
			
			// CONTRIBUTOR.ACRONYM, CONTRIBUTOR.SHORT_NAME, CONTRIBUTOR.FULL_NAME
//			System.out.println(tmp.getString("CONTRIBUTOR.ACRONYM"));
//			System.out.println(tmp.getString("CONTRIBUTOR.SHORT_NAME"));
//			System.out.println(tmp.getString("CONTRIBUTOR.FULL_NAME"));
			contributor	= new Record.Contributor(
					tmp.getString("CONTRIBUTOR.ACRONYM"), 
					tmp.getString("CONTRIBUTOR.SHORT_NAME"), 
					tmp.getString("CONTRIBUTOR.FULL_NAME")
			);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return contributor;
	}
	/**
	 * Get all Accessions stored in MassBank
	 * @return
	 */
	public String[] getAccessions() {
		List<String> accessions	= new ArrayList<String>();
		try (Connection con = ds.getConnection()) {
			PreparedStatement statementGetAccessions = con.prepareStatement(sqlGetAccessions);
			ResultSet tmp = statementGetAccessions.executeQuery();
			while(tmp.next())
				accessions.add(tmp.getString("ACCESSION"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return accessions.toArray(new String[accessions.size()]);
	}
}
