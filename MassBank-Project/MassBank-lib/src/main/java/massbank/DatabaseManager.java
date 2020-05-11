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
import org.apache.logging.log4j.LogManager;

/**
 * 
 * This class provides the code for storage and retrieval of records in SQL databases. 
 * 
 * @author rmeier
 * @version 23-04-2019
 *
 */
public class DatabaseManager {
	private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
	
	private String databaseName;
	private final String connectUrl;
	private Connection con;

	private final static String sqlAC_CHROMATOGRAPHY = "SELECT * FROM AC_CHROMATOGRAPHY WHERE RECORD = ?";
	private final static String sqlAC_MASS_SPECTROMETRY = "SELECT * FROM AC_MASS_SPECTROMETRY WHERE RECORD = ?";
	private final static String sqlCH_LINK = "SELECT * FROM CH_LINK WHERE COMPOUND = ?";
	private final static String sqlCOMMENT = "SELECT * FROM COMMENT WHERE RECORD = ?";
	private final static String sqlCOMPOUND = "SELECT * FROM COMPOUND WHERE ID = ?";
	private final static String sqlCOMPOUND_CLASS = "SELECT * FROM COMPOUND_CLASS WHERE ID = ?";
	private final static String sqlCOMPOUND_COMPOUND_CLASS = "SELECT * FROM COMPOUND_COMPOUND_CLASS WHERE COMPOUND = ?";
	private final static String sqlCOMPOUND_NAME = "SELECT * FROM COMPOUND_NAME WHERE COMPOUND = ?";
	private final static String sqlINSTRUMENT = "SELECT * FROM INSTRUMENT WHERE ID = ?";
	private final static String sqlMS_DATA_PROCESSING = "SELECT * FROM MS_DATA_PROCESSING WHERE RECORD = ?";
	private final static String sqlMS_FOCUSED_ION = "SELECT * FROM MS_FOCUSED_ION WHERE RECORD = ?";
	private final static String sqlNAME = "SELECT * FROM NAME WHERE ID = ?";
	private final static String sqlPEAK = "SELECT * FROM PEAK WHERE RECORD = ?";
	private final static String sqlPK_NUM_PEAK = "SELECT * FROM PK_NUM_PEAK WHERE RECORD = ?";
	private final static String sqlRECORD = "SELECT * FROM RECORD WHERE ACCESSION = ?";
	private final static String sqlDEPRECATED_RECORD = "SELECT * FROM DEPRECATED_RECORD WHERE ACCESSION = ?";
	private final static String sqlSAMPLE = "SELECT * FROM SAMPLE WHERE ID = ?";
	private final static String sqlSP_LINK = "SELECT * FROM SP_LINK WHERE RECORD = ?";
	private final static String sqlSP_SAMPLE = "SELECT * FROM SP_SAMPLE WHERE RECORD = ?";
	private final static String sqlANNOTATION_HEADER = "SELECT * FROM ANNOTATION_HEADER WHERE RECORD = ?";
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
	
	private final PreparedStatement statementAC_CHROMATOGRAPHY;
	private final PreparedStatement statementAC_MASS_SPECTROMETRY;
	private final PreparedStatement statementCH_LINK;
	private final PreparedStatement statementCOMMENT;
	private final PreparedStatement statementCOMPOUND;
	private final PreparedStatement statementCOMPOUND_CLASS;
	private final PreparedStatement statementCOMPOUND_COMPOUND_CLASS;
	private final PreparedStatement statementCOMPOUND_NAME;
	private final PreparedStatement statementINSTRUMENT;
	private final PreparedStatement statementMS_DATA_PROCESSING;
	private final PreparedStatement statementMS_FOCUSED_ION;
	private final PreparedStatement statementNAME;
	private final PreparedStatement statementPEAK;
	private final PreparedStatement statementPK_NUM_PEAK;
	private final PreparedStatement statementRECORD;
	private final PreparedStatement statementDEPRECATED_RECORD;
	private final PreparedStatement statementSAMPLE;
	private final PreparedStatement statementSP_LINK;
	private final PreparedStatement statementSP_SAMPLE;
	private final PreparedStatement statementANNOTATION_HEADER;
	private final PreparedStatement statementGetContributorFromAccession;
	private final PreparedStatement statementGetAccessions;
	
	private final static String insertCompound = "INSERT INTO COMPOUND VALUES(?,?,?,?,?,?,?,?)";
	private final static String insertCompound_Class = "INSERT INTO COMPOUND_CLASS VALUES(?,?,?,?)";
	private final static String insertCompound_Compound_Class = "INSERT INTO COMPOUND_COMPOUND_CLASS VALUES(?,?)";
	private final static String insertName = "INSERT INTO NAME VALUES(?,?)";
	private final static String insertCompound_Name = "INSERT IGNORE INTO COMPOUND_NAME VALUES(?,?)";
	private final static String insertCH_LINK = "INSERT INTO CH_LINK VALUES(?,?,?)";
	private final static String insertSAMPLE = "INSERT INTO SAMPLE VALUES(?,?,?)";
	private final static String insertSP_LINK = "INSERT INTO SP_LINK VALUES(?,?)";
	private final static String insertSP_SAMPLE = "INSERT INTO SP_SAMPLE VALUES(?,?)";
	private final static String insertINSTRUMENT = "INSERT INTO INSTRUMENT VALUES(?,?,?)";
	private final static String insertRECORD = "INSERT INTO RECORD VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private final static String insertCOMMENT = "INSERT INTO COMMENT VALUES(?,?)";
	private final static String insertAC_MASS_SPECTROMETRY = "INSERT INTO AC_MASS_SPECTROMETRY VALUES(?,?,?)";
	private final static String insertAC_CHROMATOGRAPHY = "INSERT INTO AC_CHROMATOGRAPHY VALUES(?,?,?)";
	private final static String insertMS_FOCUSED_ION = "INSERT INTO MS_FOCUSED_ION VALUES(?,?,?)";
	private final static String insertMS_DATA_PROCESSING = "INSERT INTO MS_DATA_PROCESSING VALUES(?,?,?)";
	private final static String insertPEAK = "INSERT INTO PEAK VALUES(?,?,?,?,?,?,?)";
	private final static String updatePEAKs = "UPDATE PEAK SET PK_ANNOTATION = ? WHERE RECORD = ? AND PK_PEAK_MZ = ?";
	private final static String insertANNOTATION_HEADER = "INSERT INTO ANNOTATION_HEADER VALUES(?,?)";
	
	private final PreparedStatement statementInsertCompound;
	private final PreparedStatement statementInsertCompound_Class;
	private final PreparedStatement statementInsertCompound_Compound_Class;
	private final PreparedStatement statementInsertName;
	private final PreparedStatement statementInsertCompound_Name;
	private final PreparedStatement statementInsertCH_LINK;
	private final PreparedStatement statementInsertSAMPLE;
	private final PreparedStatement statementInsertSP_LINK;
	private final PreparedStatement statementInsertSP_SAMPLE;
	private final PreparedStatement statementInsertINSTRUMENT;
	private final PreparedStatement statementInsertRECORD;
	private final PreparedStatement statementInsertCOMMENT;
	private final PreparedStatement statementInsertAC_MASS_SPECTROMETRY;
	private final PreparedStatement statementInsertAC_CHROMATOGRAPHY;
	private final PreparedStatement statementInsertMS_FOCUSED_ION;
	private final PreparedStatement statementInsertMS_DATA_PROCESSING;
	private final PreparedStatement statementInsertPEAK;
	private final PreparedStatement statementUpdatePEAKs;
	private final PreparedStatement statementInsertANNOTATION_HEADER;
	
	
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
		
		// remove trigger
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TRIGGER MassBank.upd_check;\n");
		sb.append("DROP TRIGGER MassBankNew.upd_check;\n");
		
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
		
		// add trigger to MassBank
		sb.append(	"USE MassBank;\n" +
					"delimiter //\n" + 
					"CREATE TRIGGER upd_check BEFORE INSERT ON SAMPLE\n" + 
					"	FOR EACH ROW\n" + 
					"	BEGIN\n" + 
					"	IF ((NEW.SP_SCIENTIFIC_NAME IS NULL) AND (NEW.SP_LINEAGE IS NULL)) THEN\n" + 
					"		SET NEW.ID = -1;\n" + 
					"	END IF;\n" + 
					"END;//");

		logger.trace("Running sql commands:\n" + sb.toString());
		ScriptRunner runner = new ScriptRunner(connection, false, false);
		runner.runScript(new StringReader(sb.toString()));
		connection.commit();
		connection.close();
	}
	
	public DatabaseManager(String dbName) throws SQLException, ConfigurationException {
		this.databaseName = dbName;
		this.connectUrl = "jdbc:mariadb://" + Config.get().dbHostName() + ":3306/" 
			+ databaseName + "?rewriteBatchedStatements=true"
			+ "&user=root" 
			+ "&password=" + Config.get().dbPassword();
		this.openConnection();
		
		statementAC_CHROMATOGRAPHY = this.con.prepareStatement(sqlAC_CHROMATOGRAPHY);
		statementAC_MASS_SPECTROMETRY = this.con.prepareStatement(sqlAC_MASS_SPECTROMETRY);
		statementCH_LINK = this.con.prepareStatement(sqlCH_LINK);
		statementCOMMENT = this.con.prepareStatement(sqlCOMMENT);
		statementCOMPOUND = this.con.prepareStatement(sqlCOMPOUND);
		statementCOMPOUND_CLASS = this.con.prepareStatement(sqlCOMPOUND_CLASS);
		statementCOMPOUND_COMPOUND_CLASS = this.con.prepareStatement(sqlCOMPOUND_COMPOUND_CLASS);
		statementCOMPOUND_NAME = this.con.prepareStatement(sqlCOMPOUND_NAME);
		statementINSTRUMENT = this.con.prepareStatement(sqlINSTRUMENT);
		statementMS_DATA_PROCESSING = this.con.prepareStatement(sqlMS_DATA_PROCESSING);
		statementMS_FOCUSED_ION = this.con.prepareStatement(sqlMS_FOCUSED_ION);
		statementNAME = this.con.prepareStatement(sqlNAME);
		statementPEAK = this.con.prepareStatement(sqlPEAK);
		statementPK_NUM_PEAK = this.con.prepareStatement(sqlPK_NUM_PEAK);
		statementRECORD = this.con.prepareStatement(sqlRECORD);
		statementDEPRECATED_RECORD = this.con.prepareStatement(sqlDEPRECATED_RECORD);
		statementSAMPLE = this.con.prepareStatement(sqlSAMPLE);
		statementSP_LINK = this.con.prepareStatement(sqlSP_LINK);
		statementSP_SAMPLE = this.con.prepareStatement(sqlSP_SAMPLE);
		statementANNOTATION_HEADER = this.con.prepareStatement(sqlANNOTATION_HEADER);
		statementGetContributorFromAccession = this.con.prepareStatement(sqlGetContributorFromAccession);
		statementGetAccessions = this.con.prepareStatement(sqlGetAccessions);
		
		statementInsertCompound = this.con.prepareStatement(insertCompound, Statement.RETURN_GENERATED_KEYS);
		statementInsertCompound_Class = this.con.prepareStatement(insertCompound_Class, Statement.RETURN_GENERATED_KEYS);
		statementInsertCompound_Compound_Class = this.con.prepareStatement(insertCompound_Compound_Class);
		statementInsertName = this.con.prepareStatement(insertName, Statement.RETURN_GENERATED_KEYS);
		statementInsertCompound_Name = this.con.prepareStatement(insertCompound_Name);
		statementInsertCH_LINK = this.con.prepareStatement(insertCH_LINK);
		statementInsertSAMPLE = this.con.prepareStatement(insertSAMPLE, Statement.RETURN_GENERATED_KEYS);
		statementInsertSP_LINK = this.con.prepareStatement(insertSP_LINK);
		statementInsertSP_SAMPLE = this.con.prepareStatement(insertSP_SAMPLE);
		statementInsertINSTRUMENT = this.con.prepareStatement(insertINSTRUMENT, Statement.RETURN_GENERATED_KEYS);
		statementInsertRECORD = this.con.prepareStatement(insertRECORD);
		statementInsertCOMMENT = this.con.prepareStatement(insertCOMMENT);
		statementInsertAC_MASS_SPECTROMETRY = this.con.prepareStatement(insertAC_MASS_SPECTROMETRY);
		statementInsertAC_CHROMATOGRAPHY = this.con.prepareStatement(insertAC_CHROMATOGRAPHY);
		statementInsertMS_FOCUSED_ION = this.con.prepareStatement(insertMS_FOCUSED_ION);
		statementInsertMS_DATA_PROCESSING = this.con.prepareStatement(insertMS_DATA_PROCESSING);
		statementInsertPEAK = this.con.prepareStatement(insertPEAK);
//		statementUpdatePEAK = this.con.prepareStatement(updatePEAK);
		statementUpdatePEAKs = this.con.prepareStatement(updatePEAKs);
		statementInsertANNOTATION_HEADER = this.con.prepareStatement(insertANNOTATION_HEADER);

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
			this.statementRECORD.setString(1, accessionId);
			ResultSet set = this.statementRECORD.executeQuery();
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
				this.statementAC_CHROMATOGRAPHY.setString(1, set.getString("ACCESSION"));
				this.statementAC_MASS_SPECTROMETRY.setString(1, set.getString("ACCESSION"));
				this.statementMS_DATA_PROCESSING.setString(1, set.getString("ACCESSION"));
				this.statementMS_FOCUSED_ION.setString(1, set.getString("ACCESSION"));
				this.statementCOMMENT.setString(1, set.getString("ACCESSION"));
				this.statementPEAK.setString(1, set.getString("ACCESSION"));
				this.statementPK_NUM_PEAK.setString(1, set.getString("ACCESSION"));
				this.statementANNOTATION_HEADER.setString(1, accessionId);
				
				ResultSet tmp = this.statementAC_CHROMATOGRAPHY.executeQuery();
				List<Pair<String, String>> tmpList	= new ArrayList<Pair<String, String>>();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.AC_CHROMATOGRAPHY(tmpList);
				
				tmp = this.statementAC_MASS_SPECTROMETRY.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.AC_MASS_SPECTROMETRY(tmpList);
				
				tmp = this.statementMS_DATA_PROCESSING.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.MS_DATA_PROCESSING(tmpList);
				
				tmp = this.statementMS_FOCUSED_ION.executeQuery();
				tmpList.clear();
				while (tmp.next())
					tmpList.add(Pair.of(tmp.getString("SUBTAG"), tmp.getString("VALUE")));
				acc.MS_FOCUSED_ION(tmpList);
				
				tmp = this.statementCOMMENT.executeQuery();
				List<String> tmpList2	= new ArrayList<String>();
				while (tmp.next())
					tmpList2.add(tmp.getString("COMMENT"));
				acc.COMMENT(tmpList2);
				
				tmp = this.statementANNOTATION_HEADER.executeQuery();
//				int PK_ANNOTATION_HEADER_numberOfTokens	= -1;
				if (tmp.next()) {
					String PK_ANNOTATION_HEADER	= tmp.getString("HEADER");
					String[] PK_ANNOTATION_HEADER_tokens	= PK_ANNOTATION_HEADER.split(" ");
					acc.PK_ANNOTATION_HEADER(Arrays.asList(PK_ANNOTATION_HEADER_tokens));
//					PK_ANNOTATION_HEADER_numberOfTokens	= PK_ANNOTATION_HEADER_tokens.length;
				}
				
				tmp = this.statementPEAK.executeQuery();
				while (tmp.next()) {
					BigDecimal mz = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_MZ")))).setScale(tmp.getInt("PK_PEAK_MZ_SIGNIFICANT"));
					BigDecimal intensity = (new BigDecimal(String.valueOf(tmp.getDouble("PK_PEAK_INTENSITY")))).setScale(tmp.getInt("PK_PEAK_INTENSITY_SIGNIFICANT"));
					acc.PK_PEAK_ADD_LINE(Triple.of(mz, intensity, tmp.getInt("PK_PEAK_RELATIVE")));
					String PK_ANNOTATION	= tmp.getString("PK_ANNOTATION");
					if(PK_ANNOTATION != null)
						acc.PK_ANNOTATION_ADD_LINE(Arrays.asList(PK_ANNOTATION.split(" ")));
				}
				tmp = this.statementPK_NUM_PEAK.executeQuery();
				while (tmp.next()) {
					acc.PK_NUM_PEAK(Integer.valueOf(tmp.getInt("PK_NUM_PEAK")));
				}
			} else {
				// try to find the ACCESSION in DEPRECATED_RECORD
				this.statementDEPRECATED_RECORD.setString(1, accessionId);
				set = this.statementDEPRECATED_RECORD.executeQuery();
				if (set.next()) {
					acc.ACCESSION(set.getString("ACCESSION"));
					acc.DEPRECATED(true);
					acc.DEPRECATED_CONTENT(set.getString("CONTENT"));
					return acc;
				} else throw new IllegalStateException("accessionId '" + accessionId + "' is not in database");
			}
			
			
			
			if (compoundID == -1)
				throw new IllegalStateException("compoundID is not set");
			this.statementCOMPOUND.setInt(1, compoundID);
			set = this.statementCOMPOUND.executeQuery();
			while (set.next()) {
				acc.CH_FORMULA(set.getString("CH_FORMULA"));
				acc.CH_EXACT_MASS(new BigDecimal(set.getDouble("CH_EXACT_MASS")));
				acc.CH_SMILES(set.getString("CH_SMILES"));
				acc.CH_IUPAC(set.getString("CH_IUPAC"));
								
				// TODO CH$CDK_DEPICT_SMILES
				// TODO CH$CDK_DEPICT_GENERIC_SMILES
				// TODO CH$CDK_DEPICT_STRUCTURE_SMILES
//				acc.add("CH$CDK_DEPICT_SMILES", null, set.getString("CH_CDK_DEPICT_SMILES"));
//				acc.add("CH$CDK_DEPICT_GENERIC_SMILES", null, set.getString("CH_CDK_DEPICT_GENERIC_SMILES"));
//				acc.add("CH$CDK_DEPICT_STRUCTURE_SMILES", null, set.getString("CH_CDK_DEPICT_STRUCTURE_SMILES"));
			}
			this.statementCH_LINK.setInt(1, compoundID);
			set = this.statementCH_LINK.executeQuery();
			List<Pair<String, String>> tmpList	= new ArrayList<Pair<String, String>>();
			while (set.next()) {
				tmpList.add(Pair.of(set.getString("DATABASE_NAME"), set.getString("DATABASE_ID")));
			}
			acc.CH_LINK(tmpList);
			
			this.statementCOMPOUND_COMPOUND_CLASS.setInt(1, compoundID);
			set = this.statementCOMPOUND_COMPOUND_CLASS.executeQuery();
			List<String> tmpList2	= new ArrayList<String>();
			while (set.next()) {
				this.statementCOMPOUND_CLASS.setInt(1, set.getInt("CLASS"));
				ResultSet tmp = this.statementCOMPOUND_CLASS.executeQuery();
				while (tmp.next()) {
					tmpList2.add(tmp.getString("CH_COMPOUND_CLASS"));
				}
			}
			acc.CH_COMPOUND_CLASS(tmpList2);
			
			this.statementCOMPOUND_NAME.setInt(1, compoundID);
			set = this.statementCOMPOUND_NAME.executeQuery();
			tmpList2.clear();
			while (set.next()) {
				this.statementNAME.setInt(1, set.getInt("NAME"));
				ResultSet tmp = this.statementNAME.executeQuery();
				while (tmp.next()) {
					tmpList2.add(tmp.getString("CH_NAME"));
				}
			}
			acc.CH_NAME(tmpList2);
			
			this.statementSAMPLE.setInt(1,sampleID);
			set = this.statementSAMPLE.executeQuery();
			if (set.next()) {
				acc.SP_SCIENTIFIC_NAME(set.getString("SP_SCIENTIFIC_NAME"));
				acc.SP_LINEAGE(set.getString("SP_LINEAGE"));
			}
			
			this.statementSP_LINK.setString(1,acc.ACCESSION());
			set = this.statementSP_LINK.executeQuery();
			tmpList.clear();
			while (set.next()) {
				String spLink	= set.getString("SP_LINK");
				String[] tokens	= spLink.split(" ");
				tmpList.add(Pair.of(tokens[0], tokens[1]));
			}
			acc.SP_LINK(tmpList);
				
			this.statementSP_SAMPLE.setString(1,acc.ACCESSION());
			set = this.statementSP_SAMPLE.executeQuery();
			tmpList2.clear();
			while (set.next()) {
				tmpList2.add(set.getString("SP_SAMPLE"));
			}
			acc.SP_SAMPLE(tmpList2);
			
			if (instrumentID == -1)	throw new IllegalStateException("instrumentID is not set");
			this.statementINSTRUMENT.setInt(1, instrumentID);
			set = this.statementINSTRUMENT.executeQuery();
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
			this.statementRECORD.setString(1, accessionId);
			ResultSet set = this.statementRECORD.executeQuery();
			int compoundID = -1;
			if (set.next()) {
				compoundID = set.getInt("CH");
			} else throw new IllegalStateException("accessionId '" + accessionId + "' is not in database");
			
			if (compoundID == -1)
				throw new IllegalStateException("compoundID is not set");
			this.statementCOMPOUND.setInt(1, compoundID);
			set = this.statementCOMPOUND.executeQuery();
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
	

	public void persistAccessionFile(Record acc) {
		boolean bulk=false;
		
		// get contributor ID
		Integer conId = -1;
		try {
			String sql = "SELECT ID FROM CONTRIBUTOR WHERE SHORT_NAME = ?";
			PreparedStatement stmnt = con.prepareStatement(sql);
			stmnt.setString(1, acc.CONTRIBUTOR());
			ResultSet res = stmnt.executeQuery();
			if (res.next()) {
				conId = res.getInt(1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		if(conId == -1) {
			try {
				String sql = "INSERT INTO CONTRIBUTOR (ACRONYM, SHORT_NAME, FULL_NAME) VALUES (NULL,?,NULL)";
				PreparedStatement stmnt = con.prepareStatement(sql);
				stmnt.setString(1, acc.CONTRIBUTOR());
				stmnt.executeUpdate();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			try {
				String sql = "SELECT ID FROM CONTRIBUTOR WHERE SHORT_NAME = ?";
				PreparedStatement stmnt = con.prepareStatement(sql);
				stmnt.setString(1, acc.CONTRIBUTOR());
				ResultSet res = stmnt.executeQuery();
				if (res.next()) {
					conId = res.getInt(1);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			if (acc.DEPRECATED()) {
				// deprecated records go into table DEPRECATED_RECORD
				String sql = "INSERT INTO DEPRECATED_RECORD (ACCESSION, CONTRIBUTOR, CONTENT) VALUES (?,?,?)";
				PreparedStatement stmnt = con.prepareStatement(sql);
				stmnt.setString(1, acc.ACCESSION());
				stmnt.setInt(2, conId);
				stmnt.setBlob(3, new ByteArrayInputStream(acc.DEPRECATED_CONTENT().getBytes()));
				stmnt.executeUpdate();
			}
			else {		
				//System.out.println(System.nanoTime());
				statementInsertCompound.setNull(1, java.sql.Types.INTEGER);
				statementInsertCompound.setString(2, acc.CH_FORMULA());
				statementInsertCompound.setDouble(3, acc.CH_EXACT_MASS().doubleValue());
				statementInsertCompound.setString(4, acc.CH_SMILES());
				statementInsertCompound.setString(5, acc.CH_IUPAC());
				
				// TODO support CH$CDK_DEPICT_SMILES
				// TODO support CH$CDK_DEPICT_GENERIC_SMILES
				// TODO support CH$CDK_DEPICT_STRUCTURE_SMILES
		//		if (acc.get("CH$CDK_DEPICT_SMILES").size() != 0) {
		//			statementInsertCompound.setString(6, acc.get("CH$CDK_DEPICT_SMILES").get(0)[2]);
		//		} else {
					statementInsertCompound.setNull(6, java.sql.Types.VARCHAR);
		//		}
		//		if (acc.get("CH$CDK_DEPICT_GENERIC_SMILES").size() != 0) {
		//			statementInsertCompound.setString(7, acc.get("CH$CDK_DEPICT_GENERIC_SMILES").get(0)[2]);
		//		} else {
					statementInsertCompound.setNull(7, java.sql.Types.VARCHAR);
		//		}
		//		if (acc.get("CH$CDK_DEPICT_STRUCTURE_SMILES").size() != 0) {
		//			statementInsertCompound.setString(8, acc.get("CH$CDK_DEPICT_STRUCTURE_SMILES").get(0)[2]);
		//		} else {
					statementInsertCompound.setNull(8, java.sql.Types.VARCHAR);
		//		}
				statementInsertCompound.executeUpdate();
				ResultSet set = statementInsertCompound.getGeneratedKeys();
				set.next();
				int compoundId = set.getInt("ID");
				
				
				//System.out.println(System.nanoTime());
				int compoundClassId;
		//		String insertCompoundClass = "INSERT INTO COMPOUND_CLASS VALUES(?,?,?,?)";
		//		stmnt = con.prepareStatement(insertCompoundClass);
				for (String el : acc.CH_COMPOUND_CLASS()) {
					statementInsertCompound_Class.setNull(1, java.sql.Types.INTEGER);		
					statementInsertCompound_Class.setString(2, null);
					statementInsertCompound_Class.setString(3, null);
					statementInsertCompound_Class.setString(4, el);
					statementInsertCompound_Class.executeUpdate();
					set = statementInsertCompound_Class.getGeneratedKeys();
					set.next();
					compoundClassId = set.getInt("ID");
					
		//			String insertCompoundCompoundClass = "INSERT INTO COMPOUND_COMPOUND_CLASS VALUES(?,?)";
		//			stmnt = con.prepareStatement(insertCompoundCompoundClass);
					statementInsertCompound_Compound_Class.setInt(1, compoundId);
					statementInsertCompound_Compound_Class.setInt(2, compoundClassId);
					statementInsertCompound_Compound_Class.executeUpdate();
				}
				
				//System.out.println(System.nanoTime());
				int nameId;
		//		String insertName = "INSERT INTO NAME VALUES(?,?)";
		//		stmnt = con.prepareStatement(insertName);
				for (String el : acc.CH_NAME()) {
					statementInsertName.setNull(1, java.sql.Types.INTEGER);
					statementInsertName.setString(2, el);
					try {
						statementInsertName.executeUpdate();
						set = statementInsertName.getGeneratedKeys();
						set.next();
						nameId = set.getInt("ID");
		
		//				String insertCompoundName = "INSERT INTO COMPOUND_NAME VALUES(?,?)";
		//				stmnt = con.prepareStatement(insertCompoundName);
						statementInsertCompound_Name.setInt(1, compoundId);
						statementInsertCompound_Name.setInt(2, nameId);
						statementInsertCompound_Name.executeUpdate();
					} catch (SQLException e) {
						if (e.getErrorCode() == 1062) {
							PreparedStatement retrieveIdForName = con.prepareStatement("SELECT ID FROM NAME WHERE CH_NAME = ?");
							retrieveIdForName.setString(1, el);
							set = retrieveIdForName.executeQuery();
							set.next();
							nameId = set.getInt("ID");
							statementInsertCompound_Name.setInt(1, compoundId);
							statementInsertCompound_Name.setInt(2, nameId);
							statementInsertCompound_Name.executeUpdate();
						} else {
							this.closeConnection();
							throw e;
		//					e.printStackTrace();
		//					nameId = -1;
						}
					}
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
				statementInsertSAMPLE.setNull(1, java.sql.Types.INTEGER);
				if (acc.SP_SCIENTIFIC_NAME() != null) {
					statementInsertSAMPLE.setString(2, acc.SP_SCIENTIFIC_NAME());
				} else {
					statementInsertSAMPLE.setNull(2, java.sql.Types.VARCHAR);
				}
				if (acc.SP_LINEAGE() != null) {
					statementInsertSAMPLE.setString(3, acc.SP_LINEAGE());
				} else {
					statementInsertSAMPLE.setNull(3, java.sql.Types.VARCHAR);
				}
				if (acc.SP_SCIENTIFIC_NAME() != null && acc.SP_LINEAGE() != null) {
					statementInsertSAMPLE.executeUpdate();
					set = statementInsertSAMPLE.getGeneratedKeys();
					set.next();
					sampleId = set.getInt("ID");
				}
				
				//System.out.println(System.nanoTime());
				statementInsertINSTRUMENT.setNull(1, java.sql.Types.INTEGER);
				statementInsertINSTRUMENT.setString(2, acc.AC_INSTRUMENT());
				statementInsertINSTRUMENT.setString(3, acc.AC_INSTRUMENT_TYPE());
				statementInsertINSTRUMENT.executeUpdate();
				set = statementInsertINSTRUMENT.getGeneratedKeys();
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
				statementInsertRECORD.setInt(14, conId);
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
		//			statementInsertSP_LINK.executeUpdate();
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
					statementInsertPEAK.setNull(7, java.sql.Types.VARCHAR);
		//			statementInsertPEAK.executeUpdate();
					statementInsertPEAK.addBatch();
				}
				if (!bulk) {
					statementInsertPEAK.executeBatch();
				}
				
				//System.out.println(System.nanoTime());
				List<List<String>> annotation = acc.PK_ANNOTATION();
				if (annotation.size() != 0) {
					statementInsertANNOTATION_HEADER.setString(1, accession);
					statementInsertANNOTATION_HEADER.setString(2, String.join(" ", annotation.get(0)));
					statementInsertANNOTATION_HEADER.executeUpdate();
				}
				for (int i = 1; i < annotation.size(); i++) {
					String values = String.join(" ", annotation.get(i));
					Float mz = Float.parseFloat(annotation.get(i).get(0));
		//			values = values.substring(values.indexOf(" ")+1, values.length());
					statementUpdatePEAKs.setString(1, values);
					statementUpdatePEAKs.setString(2, accession);
					statementUpdatePEAKs.setFloat(3, mz);
		//			statementUpdatePEAK.setString(1, values.substring(0, values.indexOf(" ")));
		//			values = values.substring(values.indexOf(" ")+1, values.length());
		//			statementUpdatePEAK.setShort(2, Short.parseShort(values.substring(0, values.indexOf(" "))));
		//			values = values.substring(values.indexOf(" ")+1, values.length());
		//			statementUpdatePEAK.setFloat(3, Float.parseFloat(values.substring(0, values.indexOf(" "))));
		//			values = values.substring(values.indexOf(" ")+1, values.length());
		//			statementUpdatePEAK.setFloat(4, Float.parseFloat(values.substring(0, values.length())));
		//			statementUpdatePEAK.setString(5, accession);
		//			statementUpdatePEAK.setFloat(6, mz);
		//			statementUpdatePEAK.executeUpdate();
					statementUpdatePEAKs.addBatch();
				}
				if (!bulk) {
					statementUpdatePEAKs.executeBatch();
				}
			}
		
		
		
		
		
		//System.out.println(System.nanoTime());
		con.commit();
		//System.out.println(System.nanoTime());
		
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
