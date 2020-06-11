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
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;

import massbank.Config;
import massbank.Record;
import massbank.ScriptRunner;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * This class provides the code for storage and retrieval of records in SQL databases. 
 * 
 * @author rmeier
 * @version 09-06-2020
 *
 */
public class DatabaseManager {
	private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
	
	private String databaseName;
	private final String connectUrl;
	private Connection con;
	
	// TABLE CONTRIBUTOR
	private final static String insertCONTRIBUTOR = "INSERT INTO CONTRIBUTOR(ACRONYM, SHORT_NAME, FULL_NAME) VALUES (?,?,?)";
	private final PreparedStatement statementInsertCONTRIBUTOR;
	private final static String selectCONTRIBUTORIdByACRONYM = "SELECT * FROM CONTRIBUTOR WHERE ACRONYM = ?";
	private final PreparedStatement statementSelectCONTRIBUTORIdByACRONYM;
	// TABLE COMPOUND
	private final static String insertCOMPOUND = "INSERT INTO COMPOUND(CH_FORMULA, CH_EXACT_MASS, CH_EXACT_MASS_SIGNIFICANT," +
		"CH_SMILES, CH_IUPAC, CH_CDK_DEPICT_SMILES, CH_CDK_DEPICT_GENERIC_SMILES, CH_CDK_DEPICT_STRUCTURE_SMILES) " +
		"VALUES(?,?,?,?,?,?,?,?)";
	private final PreparedStatement statementInsertCOMPOUND;
	private final static String selectCOMPOUND = "SELECT * FROM COMPOUND WHERE ID = ?";
	private final PreparedStatement statementSelectCOMPOUND;
	// TABLE COMPOUND_CLASS
	private final static String insertCOMPOUND_CLASS = "INSERT INTO COMPOUND_CLASS(DATABASE_NAME, DATABASE_ID, CH_COMPOUND_CLASS) VALUES(?,?,?)";
	private final PreparedStatement statementInsertCOMPOUND_CLASS;
	private final static String selectCOMPOUND_CLASS = "SELECT * FROM COMPOUND_CLASS WHERE ID = ?";
	private final PreparedStatement statementSelectCOMPOUND_CLASS;
	// TABLE COMPOUND_COMPOUND_CLASS
	private final static String insertCOMPOUND_COMPOUND_CLASS = "INSERT INTO COMPOUND_COMPOUND_CLASS(COMPOUND, CLASS) VALUES(?,?)";
	private final PreparedStatement statementInsertCOMPOUND_COMPOUND_CLASS;
	private final static String selectCOMPOUND_COMPOUND_CLASS = "SELECT * FROM COMPOUND_COMPOUND_CLASS WHERE COMPOUND = ?";
	private final PreparedStatement statementSelectCOMPOUND_COMPOUND_CLASS;
	// TABLE NAME
	private final static String insertNAME = "INSERT INTO NAME(CH_NAME) VALUES(?)";
	private final PreparedStatement statementInsertNAME;
	private final static String selectNAME = "SELECT * FROM NAME WHERE ID = ?";
	private final PreparedStatement statementSelectNAME;
	// TABLE COMPOUND_NAME
	private final static String insertCOMPOUND_NAME = "INSERT IGNORE INTO COMPOUND_NAME VALUES(?,?)";
	private final PreparedStatement statementInsertCOMPOUND_NAME;
	private final static String selectCOMPOUND_NAME = "SELECT * FROM COMPOUND_NAME WHERE COMPOUND = ? ORDER BY NAME";
	private final PreparedStatement statementSelectCOMPOUND_NAME;
	// TABLE CH_LINK
	private final static String insertCH_LINK = "INSERT INTO CH_LINK(COMPOUND, DATABASE_NAME, DATABASE_ID) VALUES(?,?,?)";
	private final PreparedStatement statementInsertCH_LINK;
	private final static String selectCH_LINK = "SELECT * FROM CH_LINK WHERE COMPOUND = ? ORDER BY ID";
	private final PreparedStatement statementSelectCH_LINK;
	// TABLE SAMPLE
	private final static String insertSAMPLE = "INSERT INTO SAMPLE(SP_SCIENTIFIC_NAME, SP_LINEAGE) VALUES(?,?)";
	private final PreparedStatement statementInsertSAMPLE;
	private final static String selectSAMPLE = "SELECT * FROM SAMPLE WHERE ID = ?";
	private final PreparedStatement statementSelectSAMPLE;
	// TABLE INSTRUMENT
	private final static String insertINSTRUMENT = "INSERT INTO INSTRUMENT(AC_INSTRUMENT, AC_INSTRUMENT_TYPE) VALUES(?,?)";
	private final PreparedStatement statementInsertINSTRUMENT;
	private final static String selectINSTRUMENT = "SELECT * FROM INSTRUMENT WHERE ID = ?";
	private final PreparedStatement statementSelectINSTRUMENT;
	// TABLE RECORD
	private final static String insertRECORD = "INSERT INTO RECORD(ACCESSION, RECORD_TITLE, DATE, AUTHORS, LICENSE, COPYRIGHT, PUBLICATION," + 
			"CH, SP, AC_INSTRUMENT, AC_MASS_SPECTROMETRY_MS_TYPE, AC_MASS_SPECTROMETRY_ION_MODE, PK_SPLASH, CONTRIBUTOR)" +
			"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private final PreparedStatement statementInsertRECORD;
	private final static String selectRECORD = "SELECT * FROM RECORD WHERE ACCESSION = ?";
	private final PreparedStatement statementSelectRECORD;
	// TABLE RECORD
	private final static String insertDEPRECATED_RECORD = "INSERT INTO DEPRECATED_RECORD(ACCESSION, CONTRIBUTOR, CONTENT) VALUES (?,?,?)"; 
	private final PreparedStatement statementInsertDEPRECATED_RECORD;
	private final static String selectDEPRECATED_RECORD = "SELECT * FROM DEPRECATED_RECORD WHERE ACCESSION = ?";
	private final PreparedStatement statementSelectDEPRECATED_RECORD;
	// TABLE COMMENT
	private final static String insertCOMMENT = "INSERT INTO COMMENT(RECORD, COMMENT) VALUES(?,?)";
	private final PreparedStatement statementInsertCOMMENT;
	private final static String selectCOMMENT = "SELECT * FROM COMMENT WHERE RECORD = ? ORDER BY ID";
	private final PreparedStatement statementSelectCOMMENT;
	// TABLE SP_SAMPLE
	private final static String insertSP_SAMPLE = "INSERT INTO SP_SAMPLE(RECORD, SP_SAMPLE) VALUES(?,?)";
	private final PreparedStatement statementInsertSP_SAMPLE;
	private final static String selectSP_SAMPLE = "SELECT * FROM SP_SAMPLE WHERE RECORD = ? ORDER BY ID";
	private final PreparedStatement statementSelectSP_SAMPLE;
	// TABLE SP_LINK
	private final static String insertSP_LINK = "INSERT INTO SP_LINK(RECORD, SP_LINK) VALUES(?,?)";
	private final PreparedStatement statementInsertSP_LINK;
	private final static String selectSP_LINK = "SELECT * FROM SP_LINK WHERE RECORD = ? ORDER BY ID";
	private final PreparedStatement statementSelectSP_LINK;
	// TABLE AC_MASS_SPECTROMETRY
	private final static String insertAC_MASS_SPECTROMETRY = "INSERT INTO AC_MASS_SPECTROMETRY(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final PreparedStatement statementInsertAC_MASS_SPECTROMETRY;
	private final static String selectAC_MASS_SPECTROMETRY = "SELECT * FROM AC_MASS_SPECTROMETRY WHERE RECORD = ? ORDER BY ID";
	private final PreparedStatement statementSelectAC_MASS_SPECTROMETRY;
	// TABLE AC_CHROMATOGRAPHY
	private final static String insertAC_CHROMATOGRAPHY = "INSERT INTO AC_CHROMATOGRAPHY(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final PreparedStatement statementInsertAC_CHROMATOGRAPHY;
	private final static String selectAC_CHROMATOGRAPHY = "SELECT * FROM AC_CHROMATOGRAPHY WHERE RECORD = ? ORDER BY ID";
	private final PreparedStatement statementSelectAC_CHROMATOGRAPHY;
	// TABLE MS_FOCUSED_ION
	private final static String insertMS_FOCUSED_ION = "INSERT INTO MS_FOCUSED_ION(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final PreparedStatement statementInsertMS_FOCUSED_ION;
	private final static String selectMS_FOCUSED_ION = "SELECT * FROM MS_FOCUSED_ION WHERE RECORD = ? ORDER BY ID";
	private final PreparedStatement statementSelectMS_FOCUSED_ION;
	// TABLE MS_DATA_PROCESSING
	private final static String insertMS_DATA_PROCESSING = "INSERT INTO MS_DATA_PROCESSING(RECORD, SUBTAG, VALUE) VALUES(?,?,?)";
	private final PreparedStatement statementInsertMS_DATA_PROCESSING;
	private final static String selectMS_DATA_PROCESSING = "SELECT * FROM MS_DATA_PROCESSING WHERE RECORD = ? ORDER BY ID";
	private final PreparedStatement statementSelectMS_DATA_PROCESSING;
	// TABLE PEAK
	private final static String insertPEAK = "INSERT INTO PEAK(RECORD, PK_PEAK_MZ, PK_PEAK_MZ_SIGNIFICANT, PK_PEAK_INTENSITY, " +
			"PK_PEAK_INTENSITY_SIGNIFICANT, PK_PEAK_RELATIVE) VALUES(?,?,?,?,?,?)";
	private final PreparedStatement statementInsertPEAK;
	private final static String selectPEAK = "SELECT * FROM PEAK WHERE RECORD = ? ORDER BY PK_PEAK_MZ";
	private final PreparedStatement statementSelectPEAK;
	// TABLE ANNOTATION_HEADER
	private final static String insertANNOTATION_HEADER = "INSERT INTO ANNOTATION_HEADER(RECORD, HEADER) VALUES(?,?)";
	private final PreparedStatement statementInsertANNOTATION_HEADER;
	private final static String selectANNOTATION_HEADER = "SELECT * FROM ANNOTATION_HEADER WHERE RECORD = ?";
	private final PreparedStatement statementSelectANNOTATION_HEADER;
	// TABLE ANNOTATION
	private final static String insertANNOTATION = "INSERT INTO ANNOTATION VALUES(?,?,?,?)";
	private final PreparedStatement statementInsertANNOTATION;
	private final static String selectANNOTATION = "SELECT * FROM ANNOTATION WHERE RECORD = ?";
	private final PreparedStatement statementSelectANNOTATION;
	

	
	
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
	
	private final PreparedStatement statementGetContributorFromAccession;
	private final PreparedStatement statementGetAccessions;
	
	
	

	public DatabaseManager(String dbName) throws SQLException, ConfigurationException {
		this.databaseName = dbName;
		this.connectUrl = "jdbc:mariadb://" + Config.get().dbHostName() + ":3306/" 
			+ databaseName + "?rewriteBatchedStatements=true"
			+ "&user=root" 
			+ "&password=" + Config.get().dbPassword();
		this.openConnection();
		
		
		statementInsertCONTRIBUTOR = this.con.prepareStatement(insertCONTRIBUTOR, Statement.RETURN_GENERATED_KEYS);
		statementSelectCONTRIBUTORIdByACRONYM = this.con.prepareStatement(selectCONTRIBUTORIdByACRONYM);
				
		statementInsertCOMPOUND = this.con.prepareStatement(insertCOMPOUND, Statement.RETURN_GENERATED_KEYS);
		statementSelectCOMPOUND = this.con.prepareStatement(selectCOMPOUND);
		
		statementInsertCOMPOUND_CLASS = this.con.prepareStatement(insertCOMPOUND_CLASS, Statement.RETURN_GENERATED_KEYS);
		statementSelectCOMPOUND_CLASS = this.con.prepareStatement(selectCOMPOUND_CLASS);
		
		statementInsertCOMPOUND_COMPOUND_CLASS = this.con.prepareStatement(insertCOMPOUND_COMPOUND_CLASS);
		statementSelectCOMPOUND_COMPOUND_CLASS = this.con.prepareStatement(selectCOMPOUND_COMPOUND_CLASS);
		
		statementInsertNAME = this.con.prepareStatement(insertNAME, Statement.RETURN_GENERATED_KEYS);
		statementSelectNAME = this.con.prepareStatement(selectNAME);
		
		statementInsertCOMPOUND_NAME = this.con.prepareStatement(insertCOMPOUND_NAME);
		statementSelectCOMPOUND_NAME = this.con.prepareStatement(selectCOMPOUND_NAME);
		
		statementInsertCH_LINK = this.con.prepareStatement(insertCH_LINK);
		statementSelectCH_LINK = this.con.prepareStatement(selectCH_LINK);
		
		statementInsertSAMPLE = this.con.prepareStatement(insertSAMPLE, Statement.RETURN_GENERATED_KEYS);
		statementSelectSAMPLE = this.con.prepareStatement(selectSAMPLE);
		
		statementInsertINSTRUMENT = this.con.prepareStatement(insertINSTRUMENT, Statement.RETURN_GENERATED_KEYS);
		statementSelectINSTRUMENT = this.con.prepareStatement(selectINSTRUMENT);
		
		statementInsertRECORD = this.con.prepareStatement(insertRECORD);
		statementSelectRECORD = this.con.prepareStatement(selectRECORD);

		statementInsertDEPRECATED_RECORD = this.con.prepareStatement(insertDEPRECATED_RECORD);
		statementSelectDEPRECATED_RECORD = this.con.prepareStatement(selectDEPRECATED_RECORD);

		statementInsertCOMMENT = this.con.prepareStatement(insertCOMMENT);
		statementSelectCOMMENT = this.con.prepareStatement(selectCOMMENT);
		
		statementInsertSP_SAMPLE = this.con.prepareStatement(insertSP_SAMPLE);
		statementSelectSP_SAMPLE = this.con.prepareStatement(selectSP_SAMPLE);

		statementInsertSP_LINK = this.con.prepareStatement(insertSP_LINK);
		statementSelectSP_LINK = this.con.prepareStatement(selectSP_LINK);

		statementInsertAC_MASS_SPECTROMETRY = this.con.prepareStatement(insertAC_MASS_SPECTROMETRY);
		statementSelectAC_MASS_SPECTROMETRY = this.con.prepareStatement(selectAC_MASS_SPECTROMETRY);
		
		statementInsertAC_CHROMATOGRAPHY = this.con.prepareStatement(insertAC_CHROMATOGRAPHY);
		statementSelectAC_CHROMATOGRAPHY = this.con.prepareStatement(selectAC_CHROMATOGRAPHY);
		
		statementInsertMS_FOCUSED_ION = this.con.prepareStatement(insertMS_FOCUSED_ION);
		statementSelectMS_FOCUSED_ION = this.con.prepareStatement(selectMS_FOCUSED_ION);

		statementInsertMS_DATA_PROCESSING = this.con.prepareStatement(insertMS_DATA_PROCESSING);
		statementSelectMS_DATA_PROCESSING = this.con.prepareStatement(selectMS_DATA_PROCESSING);

		statementInsertPEAK = this.con.prepareStatement(insertPEAK);
		statementSelectPEAK = this.con.prepareStatement(selectPEAK);
		
		statementInsertANNOTATION_HEADER = this.con.prepareStatement(insertANNOTATION_HEADER);
		statementSelectANNOTATION_HEADER = this.con.prepareStatement(selectANNOTATION_HEADER);

		statementInsertANNOTATION = this.con.prepareStatement(insertANNOTATION);
		statementSelectANNOTATION = this.con.prepareStatement(selectANNOTATION);
		
		
		
		statementGetContributorFromAccession = this.con.prepareStatement(sqlGetContributorFromAccession);
		statementGetAccessions = this.con.prepareStatement(sqlGetAccessions);
	}
	
	
	/**
	 * Create a database with the MassBank database scheme.
	 * @param dbName the name of the new database
	 */
	public static void init_db(String dbName) throws SQLException, ConfigurationException, FileNotFoundException, IOException {
		String link="jdbc:mariadb://" 
				+ Config.get().dbHostName() + ":3306/" 
				+ "?user=root" 
				+ "&password=" + Config.get().dbPassword();
		logger.trace("Opening database connection with url\"" + link + "\".");
		Connection connection = DriverManager.getConnection(link);
		
		logger.trace("Executing sql statements to create empty database \"" + dbName + "\".");
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("DROP DATABASE IF EXISTS " + dbName + ";");
		stmt.executeUpdate("CREATE DATABASE " + dbName + " CHARACTER SET = 'utf8';");
		stmt.executeUpdate("USE " + dbName + ";");
		
		logger.trace("Executing sql statements in file at: \"" + DatabaseManager.class.getClassLoader().getResource("create_massbank_scheme.sql") + "\".");
		ScriptRunner runner = new ScriptRunner(connection, false, false);
		runner.runScript(new InputStreamReader(DatabaseManager.class.getClassLoader().getResourceAsStream("create_massbank_scheme.sql")));
		
		stmt.close();
		logger.trace("Closing connection with url\"" + link + "\".");
		connection.commit();
		connection.close();
	}
	
	/**
	 * Move all tables from MassBankNew to MassBank,
	 */
	public static void move_temp_db_to_main_massbank() throws ConfigurationException, SQLException, IOException {
		String link="jdbc:mariadb://" 
				+ Config.get().dbHostName() + ":3306/" 
				+ "?user=root" 
				+ "&password=" + Config.get().dbPassword();
		logger.trace("Opening database connection with url\"" + link + "\".");
		Connection connection = DriverManager.getConnection(link);
		
		StringBuilder sb = new StringBuilder();
		
		// get all tables
		Statement stmt = connection.createStatement();
		List<String> table_names = new ArrayList<String>();
		ResultSet result = stmt.executeQuery("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA ='MassBankNew'");
		while (result.next()) {
			table_names.add(result.getString(1));
        }
		stmt.close();
		
		// create MassBankBackup database
		sb.append("CREATE DATABASE MassBankBackup;\n");
		
		// move MassBankNew to MassBank and MassBank to MassBankBackup
		sb.append("RENAME TABLE");
		for(String table_name : table_names) {
			sb.append(" MassBank."+table_name+" to MassBankBackup."+table_name+",");
			sb.append(" MassBankNew."+table_name+" to MassBank."+table_name+",");
		}
		sb.setLength(sb.length() - 1);
		sb.append(";\n");
		
		// drop MassBankNew and MassBankBackup 
		sb.append("DROP DATABASE " + Config.get().tmpdbName() + ";\n");
		sb.append("DROP DATABASE MassBankBackup;\n");
		
		logger.trace("Running sql commands:\n" + sb.toString());
		ScriptRunner runner = new ScriptRunner(connection, false, false);
		runner.runScript(new StringReader(sb.toString()));
		connection.commit();
		connection.close();
	}
	

		
	private void openConnection() {
		Connection con	= null;
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			con = DriverManager.getConnection(connectUrl);
			con.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.con = con;
	}

	public void closeConnection() {
		try {
			if ( this.con != null ) this.con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		return this.con;
	}
	
	/**
	 * Store the content of the given record in the database
	 * 
	 * @param  acc the record to store
	 */
	public void persistAccessionFile(Record acc) {
		boolean bulk=false;
		
		// get contributor ID
		Integer contributorId = -1;
		try {
			statementSelectCONTRIBUTORIdByACRONYM.setString(1, acc.CONTRIBUTOR());
			try (ResultSet set = statementSelectCONTRIBUTORIdByACRONYM.executeQuery()) {
				if (set.next()) {
					contributorId = set.getInt(1);
				}
				else {
					statementInsertCONTRIBUTOR.setString(1, acc.CONTRIBUTOR());
					statementInsertCONTRIBUTOR.setString(2, acc.CONTRIBUTOR());
					statementInsertCONTRIBUTOR.setString(3, acc.CONTRIBUTOR());
					statementInsertCONTRIBUTOR.executeUpdate();
					try (ResultSet set2 = statementInsertCONTRIBUTOR.getGeneratedKeys()) {
						set2.next();
						contributorId = set2.getInt("ID");
					}
				}
			}
			if (acc.DEPRECATED()) {
				statementInsertDEPRECATED_RECORD.setString(1, acc.ACCESSION());
				statementInsertDEPRECATED_RECORD.setInt(2, contributorId);
				statementInsertDEPRECATED_RECORD.setBlob(3, new ByteArrayInputStream(acc.DEPRECATED_CONTENT().getBytes()));
				statementInsertDEPRECATED_RECORD.executeUpdate();
				return;
			}
			
			
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
				
		try {
			statementInsertCOMPOUND.setString(1, acc.CH_FORMULA());
			statementInsertCOMPOUND.setDouble(2, acc.CH_EXACT_MASS().doubleValue());
			statementInsertCOMPOUND.setInt(3, acc.CH_EXACT_MASS().scale());
			statementInsertCOMPOUND.setString(4, acc.CH_SMILES());
			statementInsertCOMPOUND.setString(5, acc.CH_IUPAC());
			// TODO support CH$CDK_DEPICT_SMILES
			// TODO support CH$CDK_DEPICT_GENERIC_SMILES
			// TODO support CH$CDK_DEPICT_STRUCTURE_SMILES
			statementInsertCOMPOUND.setNull(6, java.sql.Types.VARCHAR);
			statementInsertCOMPOUND.setNull(7, java.sql.Types.VARCHAR);
			statementInsertCOMPOUND.setNull(8, java.sql.Types.VARCHAR);
			statementInsertCOMPOUND.executeUpdate();
			int compoundId = -1;
			try (ResultSet set = statementInsertCOMPOUND.getGeneratedKeys()) {
				set.next();
				compoundId = set.getInt("ID");
			}
				
			int compoundClassId;
			for (String el : acc.CH_COMPOUND_CLASS()) {
				statementInsertCOMPOUND_CLASS.setString(1, null);
				statementInsertCOMPOUND_CLASS.setString(2, null);
				statementInsertCOMPOUND_CLASS.setString(3, el);
				statementInsertCOMPOUND_CLASS.executeUpdate();
				try (ResultSet set = statementInsertCOMPOUND_CLASS.getGeneratedKeys()) {
					set.next();
					compoundClassId = set.getInt("ID");
				}
				statementInsertCOMPOUND_COMPOUND_CLASS.setInt(1, compoundId);
				statementInsertCOMPOUND_COMPOUND_CLASS.setInt(2, compoundClassId);
				statementInsertCOMPOUND_COMPOUND_CLASS.executeUpdate();
			}
				
			int nameId;
	//		String insertName = "INSERT INTO NAME VALUES(?,?)";
	//		stmnt = con.prepareStatement(insertName);
			for (String el : acc.CH_NAME()) {
				statementInsertNAME.setString(1, el);
//				try {
				statementInsertNAME.executeUpdate();
					ResultSet set = statementInsertNAME.getGeneratedKeys();
					set.next();
					nameId = set.getInt("ID");
	
	//				String insertCompoundName = "INSERT INTO COMPOUND_NAME VALUES(?,?)";
	//				stmnt = con.prepareStatement(insertCompoundName);
					statementInsertCOMPOUND_NAME.setInt(1, compoundId);
					statementInsertCOMPOUND_NAME.setInt(2, nameId);
					statementInsertCOMPOUND_NAME.executeUpdate();
			}
				
				//System.out.println(System.nanoTime());
		//		String insertChLink = "INSERT INTO CH_LINK VALUES(?,?,?)";
		//		stmnt = con.prepareStatement(insertChLink);
				for (Pair<String, String> el : acc.CH_LINK()) {
					statementInsertCH_LINK.setInt(1,compoundId);
					statementInsertCH_LINK.setString(2, el.getLeft());
					statementInsertCH_LINK.setString(3, el.getRight());
		//			statementInsertCH_LINK.executeUpdate();
					statementInsertCH_LINK.addBatch();
				}
				if (!bulk) {
					statementInsertCH_LINK.executeBatch();
				}
				
				//System.out.println(System.nanoTime());
				int sampleId = -1;
				if (acc.SP_SCIENTIFIC_NAME() != null) {
					statementInsertSAMPLE.setString(1, acc.SP_SCIENTIFIC_NAME());
				} else {
					statementInsertSAMPLE.setNull(1, java.sql.Types.VARCHAR);
				}
				if (acc.SP_LINEAGE() != null) {
					statementInsertSAMPLE.setString(2, acc.SP_LINEAGE());
				} else {
					statementInsertSAMPLE.setNull(2, java.sql.Types.VARCHAR);
				}
				if (acc.SP_SCIENTIFIC_NAME() != null || acc.SP_LINEAGE() != null) {
					statementInsertSAMPLE.executeUpdate();
					ResultSet set = statementInsertSAMPLE.getGeneratedKeys();
					set.next();
					sampleId = set.getInt("ID");
				}
				
				//System.out.println(System.nanoTime());
				statementInsertINSTRUMENT.setString(1, acc.AC_INSTRUMENT());
				statementInsertINSTRUMENT.setString(2, acc.AC_INSTRUMENT_TYPE());
				statementInsertINSTRUMENT.executeUpdate();
				ResultSet set = statementInsertINSTRUMENT.getGeneratedKeys();
				set.next();
				int instrumentId = set.getInt("ID");
				
				//System.out.println(System.nanoTime());
				statementInsertRECORD.setString(1, acc.ACCESSION());
				statementInsertRECORD.setString(2, acc.RECORD_TITLE1());
				statementInsertRECORD.setString(3, acc.DATE());
				statementInsertRECORD.setString(4, acc.AUTHORS());
		//		if (acc.get("LICENSE").size() != 0) {
					statementInsertRECORD.setString(5, acc.LICENSE());			
		//		} else {
		//			statementInsertRECORD.setNull(5, java.sql.Types.VARCHAR);
		//		}
				if (acc.COPYRIGHT() != null) {
					statementInsertRECORD.setString(6, acc.COPYRIGHT());			
				} else {
					statementInsertRECORD.setNull(6, java.sql.Types.VARCHAR);
				}
				if (acc.PUBLICATION() != null) {
					statementInsertRECORD.setString(7, acc.PUBLICATION());			
				} else {
					statementInsertRECORD.setNull(7, java.sql.Types.VARCHAR);
				}
				statementInsertRECORD.setInt(8, compoundId);
				if (sampleId > 0) {
					statementInsertRECORD.setInt(9, sampleId);
				} else {
					statementInsertRECORD.setNull(9, java.sql.Types.INTEGER);
				}
				statementInsertRECORD.setInt(10, instrumentId);
				statementInsertRECORD.setString(11, acc.AC_MASS_SPECTROMETRY_MS_TYPE());
				statementInsertRECORD.setString(12, acc.AC_MASS_SPECTROMETRY_ION_MODE());
				statementInsertRECORD.setString(13, acc.PK_SPLASH());
				statementInsertRECORD.setInt(14, contributorId);
				statementInsertRECORD.executeUpdate();
				
				//System.out.println(System.nanoTime());
				for (String el : acc.SP_SAMPLE()) {
					statementInsertSP_SAMPLE.setString(1, acc.ACCESSION());
					statementInsertSP_SAMPLE.setString(2, el);
		//			statementInsertSP_SAMPLE.executeUpdate();
					statementInsertSP_SAMPLE.addBatch();
				}
				if (!bulk) {
					statementInsertSP_SAMPLE.executeBatch();
				}
				//System.out.println(System.nanoTime());
				for (Pair<String, String> el : acc.SP_LINK()) {
					statementInsertSP_LINK.setString(1, acc.ACCESSION());
					statementInsertSP_LINK.setString(2, el.getLeft() + " " + el.getRight());
		//			statementInsertSP_LINK.executeUpdate();Select
					statementInsertSP_LINK.addBatch();
				}
				if (!bulk) {
					statementInsertSP_LINK.executeBatch();
				}
				
		//		set = statementInsertRECORD.getGeneratedKeys();
		//		set.next();
				String accession = acc.ACCESSION();
				
				//System.out.println(System.nanoTime());
				for (String el : acc.COMMENT()) {
					statementInsertCOMMENT.setString(1, accession);
					statementInsertCOMMENT.setString(2, el);
		//			statementInsertCOMMENT.executeUpdate();
					statementInsertCOMMENT.addBatch();
				}
				if (!bulk) {
					statementInsertCOMMENT.executeBatch();
				}
				
				//System.out.println(System.nanoTime());
				for (Pair<String, String> el : acc.AC_MASS_SPECTROMETRY()) {
					statementInsertAC_MASS_SPECTROMETRY.setString(1, accession);
					statementInsertAC_MASS_SPECTROMETRY.setString(2, el.getLeft());
					statementInsertAC_MASS_SPECTROMETRY.setString(3, el.getRight());
		//			statementInsertAC_MASS_SPECTROMETRY.executeUpdate();
					statementInsertAC_MASS_SPECTROMETRY.addBatch();
				}
				if (!bulk) {
					statementInsertAC_MASS_SPECTROMETRY.executeBatch();
				}
				
				//System.out.println(System.nanoTime());
				for (Pair<String, String> el : acc.AC_CHROMATOGRAPHY()) {
					statementInsertAC_CHROMATOGRAPHY.setString(1, accession);
					statementInsertAC_CHROMATOGRAPHY.setString(2, el.getLeft());
					statementInsertAC_CHROMATOGRAPHY.setString(3, el.getRight());
		//			statementInsertAC_CHROMATOGRAPHY.executeUpdate();
					statementInsertAC_CHROMATOGRAPHY.addBatch();
				}
				if (!bulk) {
					statementInsertAC_CHROMATOGRAPHY.executeBatch();
				}
				
				//System.out.println(System.nanoTime());
				for (Pair<String, String> el : acc.MS_FOCUSED_ION()) {
					statementInsertMS_FOCUSED_ION.setString(1, accession);
					statementInsertMS_FOCUSED_ION.setString(2, el.getLeft());
					statementInsertMS_FOCUSED_ION.setString(3, el.getRight());
		//			statementInsertMS_FOCUSED_ION.executeUpdate();
					statementInsertMS_FOCUSED_ION.addBatch();
				}
				if (!bulk) {
					statementInsertMS_FOCUSED_ION.executeBatch();
				}
				
				//System.out.println(System.nanoTime());
				for (Pair<String, String> el : acc.MS_DATA_PROCESSING()) {
					statementInsertMS_DATA_PROCESSING.setString(1, accession);
					statementInsertMS_DATA_PROCESSING.setString(2, el.getLeft());
					statementInsertMS_DATA_PROCESSING.setString(3, el.getRight());
		//			statementInsertMS_DATA_PROCESSING.executeUpdate();
					statementInsertMS_DATA_PROCESSING.addBatch();
				}
				if (!bulk) {
					statementInsertMS_DATA_PROCESSING.executeBatch();
				}
		
				//System.out.println(System.nanoTime());
				
				for (Triple<BigDecimal,BigDecimal,Integer> peak : acc.PK_PEAK()) {
					statementInsertPEAK.setString(1, accession);
					statementInsertPEAK.setDouble(2, peak.getLeft().doubleValue());
					statementInsertPEAK.setInt(3, peak.getLeft().scale());
					
					statementInsertPEAK.setDouble(4, peak.getMiddle().doubleValue());
					statementInsertPEAK.setInt(5, peak.getMiddle().scale());
					
					statementInsertPEAK.setInt(6, peak.getRight().intValue());
		//			statementInsertPEAK.executeUpdate();
					statementInsertPEAK.addBatch();
				}
				if (!bulk) {
					statementInsertPEAK.executeBatch();
				}
				
				
				
				List<String> annotationHeader = acc.PK_ANNOTATION_HEADER();
				if (!annotationHeader.isEmpty()) {
					statementInsertANNOTATION_HEADER.setString(1, accession);
					statementInsertANNOTATION_HEADER.setString(2, String.join(" ", annotationHeader));
					statementInsertANNOTATION_HEADER.executeUpdate();
				
					for (Pair<BigDecimal, List<String>> annotation : acc.PK_ANNOTATION()) {
						statementInsertANNOTATION.setString(1, accession);
						statementInsertANNOTATION.setDouble(2, annotation.getLeft().doubleValue());
						statementInsertANNOTATION.setInt(3, annotation.getLeft().scale());
						statementInsertANNOTATION.setString(4, String.join(" ",annotation.getRight()));
						statementInsertANNOTATION.addBatch();
					}
				}
				
				if (!bulk) {
					statementInsertANNOTATION.executeBatch();
				}
			
		
		
		
		con.commit();
		
		} catch (SQLException e) {
			StringBuilder tmp = new StringBuilder();
			tmp.append(e.getMessage());
			tmp.append("\n");
			for (StackTraceElement el : e.getStackTrace()) {
				tmp.append(el.toString());
				tmp.append("\n");
			}
			System.out.println("DB ERROR " + tmp + " for accession: " + acc.ACCESSION());
//			try {
//				e.printStackTrace(new PrintStream(new FileOutputStream("/Users/laptop/Desktop/errors/" + acc.ACCESSION() + ".txt")));
//			} catch (FileNotFoundException e1) {
//				//e1.printStackTrace();
//			}
			this.closeConnection();
		} catch (IndexOutOfBoundsException e) {
			StringBuilder tmp = new StringBuilder();
			tmp.append(e.getMessage());
			tmp.append("\n");
			for (StackTraceElement el : e.getStackTrace()) {
				tmp.append(el.toString());
				tmp.append("\n");
			}
			System.out.println("DB ERROR " + tmp + " for accession: " + acc.ACCESSION());
//			System.out.println(acc.ACCESSION());
//			System.out.println(acc.get("PK$PEAK").size());
//			System.out.println(acc.get("PK$ANNOTATION").size());
//			try {
//				e.printStackTrace(new PrintStream(new FileOutputStream("/Users/laptop/Desktop/errors/" + acc.ACCESSION() + ".txt")));
//			} catch (FileNotFoundException e1) {
//				//e1.printStackTrace();
//			}
		} catch (Exception e) {
			StringBuilder tmp = new StringBuilder();
			tmp.append(e.getMessage());
			tmp.append("\n");
			for (StackTraceElement el : e.getStackTrace()) {
				tmp.append(el.toString());
				tmp.append("\n");
			}
			System.out.println("DB ERROR " + tmp + " for accession: " + acc.ACCESSION());
//			try {
//				e.printStackTrace(new PrintStream(new FileOutputStream("/Users/laptop/Desktop/errors/" + acc.ACCESSION() + ".txt")));
//			} catch (FileNotFoundException e1) {
//				//e1.printStackTrace();
//			}
			this.closeConnection();
		}
//		this.closeConnection();
	}
	
	
	/**
	 * Returns the complete record TODO solve 1:1 relations by a single sql
	 * statement with joins (PK_ANNOTATION_HEADER, acc.PK_NUM_PEAK, Compound stuff,
	 * SP_SCIENTIFIC_NAME, SP_LINEAGE, AC_INSTRUMENT, AC_INSTRUMENT_TYPE)
	 * 
	 * @param accessionId
	 * @return Record
	 */
	public Record getAccessionData(String accessionId) {
		Record.Contributor Contributor=getContributorFromAccession(accessionId);
		if (Contributor==null) return null;
		String contributor=Contributor.SHORT_NAME;
		Record acc = new Record(contributor);
		try {
			this.statementSelectRECORD.setString(1, accessionId);
			ResultSet set = this.statementSelectRECORD.executeQuery();
			int compoundID = -1;
			int sampleID = -1;
			int instrumentID = -1;
			if (set.next()) {
				acc.ACCESSION(set.getString("ACCESSION"));
				acc.RECORD_TITLE1(set.getString("RECORD_TITLE"));
				acc.DATE(set.getString("DATE"));
				acc.AUTHORS(set.getString("AUTHORS"));
				acc.LICENSE(set.getString("LICENSE"));
				acc.COPYRIGHT(set.getString("COPYRIGHT"));
				acc.PUBLICATION(set.getString("PUBLICATION"));
				compoundID = set.getInt("CH");
				sampleID = set.getInt("SP");
				instrumentID = set.getInt("AC_INSTRUMENT");
				acc.AC_MASS_SPECTROMETRY_MS_TYPE(set.getString("AC_MASS_SPECTROMETRY_MS_TYPE"));
				acc.AC_MASS_SPECTROMETRY_ION_MODE(set.getString("AC_MASS_SPECTROMETRY_ION_MODE"));
				acc.PK_SPLASH(set.getString("PK_SPLASH"));
				this.statementSelectAC_CHROMATOGRAPHY.setString(1, set.getString("ACCESSION"));
				this.statementSelectAC_MASS_SPECTROMETRY.setString(1, set.getString("ACCESSION"));
				this.statementSelectMS_DATA_PROCESSING.setString(1, set.getString("ACCESSION"));
				this.statementSelectMS_FOCUSED_ION.setString(1, set.getString("ACCESSION"));
				this.statementSelectCOMMENT.setString(1, set.getString("ACCESSION"));
				this.statementSelectPEAK.setString(1, set.getString("ACCESSION"));
				// this.statementPK_NUM_PEAK.setString(1, set.getString("ACCESSION"));
				this.statementSelectANNOTATION_HEADER.setString(1, accessionId);
				
				ResultSet tmp = this.statementSelectAC_CHROMATOGRAPHY.executeQuery();
				List<Pair<String, String>> tmpList	= new ArrayList<Pair<String, String>>();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.AC_CHROMATOGRAPHY(tmpList);
				
				tmp = this.statementSelectAC_MASS_SPECTROMETRY.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.AC_MASS_SPECTROMETRY(tmpList);
				
				tmp = this.statementSelectMS_DATA_PROCESSING.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.MS_DATA_PROCESSING(tmpList);
				
				tmp = this.statementSelectMS_FOCUSED_ION.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.MS_FOCUSED_ION(tmpList);
				
				tmp = this.statementSelectCOMMENT.executeQuery();
				List<String> tmpList2	= new ArrayList<String>();
				while (tmp.next())
					tmpList2.add(tmp.getString("COMMENT"));
				acc.COMMENT(tmpList2);
				
				tmp = this.statementSelectANNOTATION_HEADER.executeQuery();
//				int PK_ANNOTATION_HEADER_numberOfTokens	= -1;
				if (tmp.next()) {
					String PK_ANNOTATION_HEADER	= tmp.getString("HEADER");
					String[] PK_ANNOTATION_HEADER_tokens	= PK_ANNOTATION_HEADER.split(" ");
					acc.PK_ANNOTATION_HEADER(Arrays.asList(PK_ANNOTATION_HEADER_tokens));
//					PK_ANNOTATION_HEADER_numberOfTokens	= PK_ANNOTATION_HEADER_tokens.length;
					
					this.statementSelectANNOTATION.setString(1, set.getString("ACCESSION"));
					tmp = this.statementSelectANNOTATION.executeQuery();
					while (tmp.next()) {
						BigDecimal mz = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_MZ")))).setScale(tmp.getInt("PK_PEAK_MZ_SIGNIFICANT"));
						List<String> annotation = Arrays.asList(tmp.getString("PK_ANNOTATION").split(" "));
						acc.PK_ANNOTATION_ADD_LINE(Pair.of(mz, annotation));
					}
					
				}
				
				tmp = this.statementSelectPEAK.executeQuery();
				while (tmp.next()) {
					BigDecimal mz = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_MZ")))).setScale(tmp.getInt("PK_PEAK_MZ_SIGNIFICANT"));
					BigDecimal intensity = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_INTENSITY")))).setScale(tmp.getInt("PK_PEAK_INTENSITY_SIGNIFICANT"));
					acc.PK_PEAK_ADD_LINE(Triple.of(mz, intensity, tmp.getInt("PK_PEAK_RELATIVE")));
				}
			} else {
				// try to find the ACCESSION in DEPRECATED_RECORD
				this.statementSelectDEPRECATED_RECORD.setString(1, accessionId);
				set = this.statementSelectDEPRECATED_RECORD.executeQuery();
				if (set.next()) {
					acc.ACCESSION(set.getString("ACCESSION"));
					acc.DEPRECATED(true);
					acc.DEPRECATED_CONTENT(set.getString("CONTENT"));
					return acc;
				} else throw new IllegalStateException("accessionId '" + accessionId + "' is not in database");
			}
			set.close();
			
			
			if (compoundID == -1)
				throw new IllegalStateException("compoundID is not set");
			this.statementSelectCOMPOUND.setInt(1, compoundID);
			set = this.statementSelectCOMPOUND.executeQuery();
			while (set.next()) {
				acc.CH_FORMULA(set.getString("CH_FORMULA"));
				BigDecimal exactMass = (new BigDecimal(String.valueOf(set.getDouble("CH_EXACT_MASS")))).setScale(set.getInt("CH_EXACT_MASS_SIGNIFICANT"));
				acc.CH_EXACT_MASS(exactMass);
				acc.CH_SMILES(set.getString("CH_SMILES"));
				acc.CH_IUPAC(set.getString("CH_IUPAC"));
								
				// TODO CH$CDK_DEPICT_SMILES
				// TODO CH$CDK_DEPICT_GENERIC_SMILES
				// TODO CH$CDK_DEPICT_STRUCTURE_SMILES
//				acc.add("CH$CDK_DEPICT_SMILES", null, set.getString("CH_CDK_DEPICT_SMILES"));
//				acc.add("CH$CDK_DEPICT_GENERIC_SMILES", null, set.getString("CH_CDK_DEPICT_GENERIC_SMILES"));
//				acc.add("CH$CDK_DEPICT_STRUCTURE_SMILES", null, set.getString("CH_CDK_DEPICT_STRUCTURE_SMILES"));
			}
			set.close();
			
			this.statementSelectCH_LINK.setInt(1, compoundID);
			set = this.statementSelectCH_LINK.executeQuery();
			List<Pair<String, String>> tmpList	= new ArrayList<Pair<String, String>>();
			while (set.next()) {
				Pair<String, String> link = Pair.of(set.getString("DATABASE_NAME"), set.getString("DATABASE_ID"));
				tmpList.add(link);
			}
			acc.CH_LINK(tmpList);
			
			this.statementSelectCOMPOUND_COMPOUND_CLASS.setInt(1, compoundID);
			set = this.statementSelectCOMPOUND_COMPOUND_CLASS.executeQuery();
			List<String> tmpList2	= new ArrayList<String>();
			while (set.next()) {
				this.statementSelectCOMPOUND_CLASS.setInt(1, set.getInt("CLASS"));
				ResultSet tmp = this.statementSelectCOMPOUND_CLASS.executeQuery();
				while (tmp.next()) {
					tmpList2.add(tmp.getString("CH_COMPOUND_CLASS"));
				}
			}
			acc.CH_COMPOUND_CLASS(tmpList2);
			
			this.statementSelectCOMPOUND_NAME.setInt(1, compoundID);
			set = this.statementSelectCOMPOUND_NAME.executeQuery();
			tmpList2.clear();
			while (set.next()) {
				int name = set.getInt("NAME") ;
				this.statementSelectNAME.setInt(1, name);	
				//this.statementSelectNAME.setInt(1, set.getInt("NAME"));
				ResultSet tmp = this.statementSelectNAME.executeQuery();
				while (tmp.next()) {
					tmpList2.add(tmp.getString("CH_NAME"));
				}
			}
			acc.CH_NAME(tmpList2);
			
			this.statementSelectSAMPLE.setInt(1,sampleID);
			set = this.statementSelectSAMPLE.executeQuery();
			if (set.next()) {
				acc.SP_SCIENTIFIC_NAME(set.getString("SP_SCIENTIFIC_NAME"));
				acc.SP_LINEAGE(set.getString("SP_LINEAGE"));
			}
			
			this.statementSelectSP_LINK.setString(1,acc.ACCESSION());
			set = this.statementSelectSP_LINK.executeQuery();
			tmpList.clear();
			while (set.next()) {
				String spLink	= set.getString("SP_LINK");
				String[] tokens	= spLink.split(" ");
				tmpList.add(Pair.of(tokens[0], tokens[1]));
			}
			acc.SP_LINK(tmpList);
				
			this.statementSelectSP_SAMPLE.setString(1,acc.ACCESSION());
			set = this.statementSelectSP_SAMPLE.executeQuery();
			tmpList2.clear();
			while (set.next()) {
				tmpList2.add(set.getString("SP_SAMPLE"));
			}
			acc.SP_SAMPLE(tmpList2);
			
			if (instrumentID == -1)	throw new IllegalStateException("instrumentID is not set");
			this.statementSelectINSTRUMENT.setInt(1, instrumentID);
			set = this.statementSelectINSTRUMENT.executeQuery();
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
	
	
	public Record.Structure getStructureOfAccession(String accessionId) {
		String CH_SMILES	= null;
		String CH_IUPAC		= null;
		
		try {
			this.statementSelectRECORD.setString(1, accessionId);
			ResultSet set = this.statementSelectRECORD.executeQuery();
			int compoundID = -1;
			if (set.next()) {
				compoundID = set.getInt("CH");
			} else throw new IllegalStateException("accessionId '" + accessionId + "' is not in database");
			
			if (compoundID == -1)
				throw new IllegalStateException("compoundID is not set");
			this.statementSelectCOMPOUND.setInt(1, compoundID);
			set = this.statementSelectCOMPOUND.executeQuery();
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
	



	
	public Record.Contributor getContributorFromAccession(String accessionId) {
//		String accessionId	= "OUF01001";
		Record.Contributor contributor	= null;
		try {
			this.statementGetContributorFromAccession.setString(1, accessionId);
			
			ResultSet tmp = this.statementGetContributorFromAccession.executeQuery();
			
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
		try {
			ResultSet tmp = this.statementGetAccessions.executeQuery();
			while(tmp.next())
				accessions.add(tmp.getString("ACCESSION"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return accessions.toArray(new String[accessions.size()]);
	}
}
