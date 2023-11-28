package massbank.web.recordindex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;

import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;
import massbank.web.recordindex.RecordIndexCount.RecordIndexCountResult;

public class RecordIndexCount implements SearchFunction<RecordIndexCountResult> {

	public void getParameters(HttpServletRequest request) {
	}

	public RecordIndexCountResult search() {
		Map<String, Integer> mapSiteToRecordCount		= new TreeMap<String, Integer>();
		Map<String, Integer> mapInstrumentToRecordCount	= new TreeMap<String, Integer>();
		Map<String, Integer> mapMsTypeToRecordCount		= new TreeMap<String, Integer>();
		Map<String, Integer> mapIonModeToRecordCount	= new TreeMap<String, Integer>();
		TreeMap<String, Integer> mapSymbolToCount	= new TreeMap<String, Integer>();
		int spectraCount = 0;
		int compoundCount = 0;
		int isomerCount = 0;
		
		char[] compoundA_Z_symbols		= new char[] {
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
		};
		int[] compoundA_ZToRecordCount	= new int[26];
		int compound0_9RecordCount		= 0;
		int compoundOtherRecordCount	= 0;
		
		try (Connection con = DatabaseManager.getConnection()) {
			String sql = "SELECT SHORT_NAME AS CONTRIBUTOR, COUNT FROM (SELECT CONTRIBUTOR, COUNT(*) AS COUNT "
				+ "FROM RECORD GROUP BY CONTRIBUTOR) AS C, CONTRIBUTOR WHERE CONTRIBUTOR = ID";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					while (set.next()) {
						mapSiteToRecordCount.put(set.getString("CONTRIBUTOR"), set.getInt("COUNT"));
					}
				}
			}	
			
			sql = "SELECT AC_INSTRUMENT_TYPE AS INSTRUMENT, COUNT(*) AS COUNT FROM INSTRUMENT GROUP BY AC_INSTRUMENT_TYPE";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					while (set.next()) {
						mapInstrumentToRecordCount.put(set.getString("INSTRUMENT"), set.getInt("COUNT"));
					}
				}
			}
				
			sql = "SELECT AC_MASS_SPECTROMETRY_MS_TYPE AS TYPE, COUNT(*) AS COUNT FROM RECORD GROUP BY AC_MASS_SPECTROMETRY_MS_TYPE";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					while (set.next()) {
						mapMsTypeToRecordCount.put(set.getString("TYPE"), set.getInt("COUNT"));
					}
				}
			}
				
			sql = "SELECT AC_MASS_SPECTROMETRY_ION_MODE AS MODE, COUNT(*) AS COUNT FROM RECORD GROUP BY AC_MASS_SPECTROMETRY_ION_MODE";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					while (set.next()) {
						mapIonModeToRecordCount.put(set.getString("MODE"), set.getInt("COUNT"));
					}
				}
			}
				
			sql = "SELECT FIRST AS COMPOUND, COUNT(*) AS COUNT FROM (SELECT SUBSTRING(UPPER(RECORD_TITLE),1,1) AS FIRST FROM RECORD) AS T GROUP BY FIRST";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					Integer numCnt = 0;
					Integer othCnt = 0;
					while (set.next()) {
						String compound = set.getString("COMPOUND");
						if (compound.matches("[a-zA-Z]")) {
							compoundA_ZToRecordCount[((int) compound.charAt(0)) - ((int) 'A')]	= set.getInt("COUNT");
						} else if (compound.matches("[0-9]")) {
							numCnt = numCnt + set.getInt("COUNT");
						} else {
							othCnt = othCnt + set.getInt("COUNT");
						}
					}
					compound0_9RecordCount		= numCnt;
					compoundOtherRecordCount	= othCnt;
				}
			}
			ArrayList<String> symbolList	= new ArrayList<String>();
			for(int i = 0; i < compoundA_Z_symbols.length; i++) {
				symbolList.add(compoundA_Z_symbols[i] + "");
				mapSymbolToCount.put(compoundA_Z_symbols[i] + "", compoundA_ZToRecordCount[i]);
			}
			symbolList.add("0-9");
			mapSymbolToCount.put("0-9", compound0_9RecordCount);
			symbolList.add("Others");
			mapSymbolToCount.put("Others", compoundOtherRecordCount);
			
			sql = "SELECT COUNT(ACCESSION) AS SPECTRA FROM RECORD";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					while (set.next()) {
						spectraCount = set.getInt(1);
					}
				}
			}
						
			sql = "SELECT COUNT(DISTINCT SUBSTRING(DATABASE_ID,1,14)) FROM CH_LINK WHERE DATABASE_NAME=\"INCHIKEY\"";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					while (set.next()) {
						compoundCount = set.getInt(1);
					}
				}
			}
						
			sql = "SELECT COUNT(DISTINCT DATABASE_ID) FROM CH_LINK WHERE DATABASE_NAME=\"INCHIKEY\"";
			try (PreparedStatement pst = con.prepareStatement(sql)) {
				try (ResultSet set = pst.executeQuery()) {
					while (set.next()) {
						isomerCount = set.getInt(1);
					}
				}
			}
		}
		catch (SQLException e) {
		e.printStackTrace();
			mapSiteToRecordCount = new TreeMap<String, Integer>();
			mapInstrumentToRecordCount = new TreeMap<String, Integer>();
			mapMsTypeToRecordCount = new TreeMap<String, Integer>();
			mapIonModeToRecordCount = new TreeMap<String, Integer>();
			mapSymbolToCount = new TreeMap<String, Integer>();
			spectraCount = 0;
			compoundCount = 0;
			isomerCount = 0;
		}
						
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
