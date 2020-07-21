package massbank.web.recordindex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;
import massbank.web.recordindex.RecordIndexCount.RecordIndexCountResult;

public class RecordIndexCount implements SearchFunction<RecordIndexCountResult> {

	public void getParameters(HttpServletRequest request) {
	}

	public RecordIndexCountResult search(DatabaseManager databaseManager) {
		Map<String, Integer> mapSiteToRecordCount		= new TreeMap<String, Integer>();
		Map<String, Integer> mapInstrumentToRecordCount	= new TreeMap<String, Integer>();
		Map<String, Integer> mapMsTypeToRecordCount		= new TreeMap<String, Integer>();
		Map<String, Integer> mapIonModeToRecordCount	= new TreeMap<String, Integer>();
		int spectraCount = 0;
		int compoundCount = 0;
		int isomerCount = 0;
		
		char[] compoundA_Z_symbols		= new char[] {
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
		};
		int[] compoundA_ZToRecordCount	= new int[26];
		int compound0_9RecordCount		= 0;
		int compoundOtherRecordCount	= 0;
		
		PreparedStatement stmnt;
		ResultSet res;
		
		String sql = "SELECT SHORT_NAME AS CONTRIBUTOR, COUNT FROM (SELECT CONTRIBUTOR, COUNT(*) AS COUNT "
				+ "FROM RECORD GROUP BY CONTRIBUTOR) AS C, CONTRIBUTOR WHERE CONTRIBUTOR = ID";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
				mapSiteToRecordCount.put(res.getString("CONTRIBUTOR"), res.getInt("COUNT"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		sql = "SELECT AC_INSTRUMENT_TYPE AS INSTRUMENT, COUNT(*) AS COUNT FROM INSTRUMENT GROUP BY AC_INSTRUMENT_TYPE";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
				mapInstrumentToRecordCount.put(res.getString("INSTRUMENT"), res.getInt("COUNT"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "SELECT AC_MASS_SPECTROMETRY_MS_TYPE AS TYPE, COUNT(*) AS COUNT FROM RECORD GROUP BY AC_MASS_SPECTROMETRY_MS_TYPE";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
				mapMsTypeToRecordCount.put(res.getString("TYPE"), res.getInt("COUNT"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "SELECT AC_MASS_SPECTROMETRY_ION_MODE AS MODE, COUNT(*) AS COUNT FROM RECORD GROUP BY AC_MASS_SPECTROMETRY_ION_MODE";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
				mapIonModeToRecordCount.put(res.getString("MODE"), res.getInt("COUNT"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "SELECT FIRST AS COMPOUND, COUNT(*) AS COUNT FROM (SELECT SUBSTRING(UPPER(RECORD_TITLE),1,1) AS FIRST FROM RECORD) AS T GROUP BY FIRST";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			Integer numCnt = 0;
			Integer othCnt = 0;
			while (res.next()) {
				String compound = res.getString("COMPOUND");
				if (compound.matches("[a-zA-Z]")) {
					compoundA_ZToRecordCount[((int) compound.charAt(0)) - ((int) 'A')]	= res.getInt("COUNT");
				} else if (compound.matches("[0-9]")) {
					numCnt = numCnt + res.getInt("COUNT");
				} else {
					othCnt = othCnt + res.getInt("COUNT");
				}
			}
			compound0_9RecordCount		= numCnt;
			compoundOtherRecordCount	= othCnt;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		sql = "SELECT COUNT(ACCESSION) AS SPECTRA FROM RECORD";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
				spectraCount = res.getInt(1);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		sql = "SELECT COUNT(DISTINCT SUBSTRING(DATABASE_ID,1,14)) FROM CH_LINK WHERE DATABASE_NAME=\"INCHIKEY\"";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
				compoundCount = res.getInt(1);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		sql = "SELECT COUNT(DISTINCT DATABASE_ID) FROM CH_LINK WHERE DATABASE_NAME=\"INCHIKEY\"";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
				isomerCount = res.getInt(1);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		ArrayList<String> symbolList	= new ArrayList<String>();
		TreeMap<String, Integer> mapSymbolToCount	= new TreeMap<String, Integer>();
		for(int i = 0; i < compoundA_Z_symbols.length; i++) {
			symbolList.add(compoundA_Z_symbols[i] + "");
			mapSymbolToCount.put(compoundA_Z_symbols[i] + "", compoundA_ZToRecordCount[i]);
		}
		symbolList.add("0-9");
		mapSymbolToCount.put("0-9", compound0_9RecordCount);
		symbolList.add("Others");
		mapSymbolToCount.put("Others", compoundOtherRecordCount);
		

		return new RecordIndexCountResult(
				mapSiteToRecordCount,
				mapInstrumentToRecordCount,
				mapMsTypeToRecordCount,
				mapIonModeToRecordCount,
				mapSymbolToCount,
				spectraCount,
				compoundCount,
				isomerCount
		);
	}
	public static class RecordIndexCountResult {
		public final Map<String, Integer> mapSiteToRecordCount;
		public final Map<String, Integer> mapInstrumentToRecordCount;
		public final Map<String, Integer> mapMsTypeToRecordCount;
		public final Map<String, Integer> mapIonModeToRecordCount;
		public final Map<String, Integer> mapSymbolToCount;
		public final int spectraCount;
		public final int compoundCount;
		public final int isomerCount;
		public RecordIndexCountResult(
				Map<String, Integer> mapSiteToRecordCount,
				Map<String, Integer> mapInstrumentToRecordCount,
				Map<String, Integer> mapMsTypeToRecordCount,
				Map<String, Integer> mapIonModeToRecordCount,
				Map<String, Integer> mapSymbolToCount,
				int spectraCount,
				int compoundCount,
				int isomerCount
		) {
			this.mapSiteToRecordCount		= mapSiteToRecordCount;
			this.mapInstrumentToRecordCount	= mapInstrumentToRecordCount;
			this.mapMsTypeToRecordCount		= mapMsTypeToRecordCount;
			this.mapIonModeToRecordCount	= mapIonModeToRecordCount;
			this.mapSymbolToCount           = mapSymbolToCount;
			this.spectraCount			    = spectraCount;
			this.compoundCount			    = compoundCount;
			this.isomerCount			    = isomerCount;
		}
	}
}
