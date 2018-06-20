package massbank.web.recordindex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;
import massbank.web.SearchFunction;
import massbank.web.recordindex.RecordIndexCount.RecordIndexCountResult;

public class RecordIndexCount implements SearchFunction<RecordIndexCountResult> {

	public void getParameters(HttpServletRequest request) {

	}

	public RecordIndexCountResult search(DatabaseManager databaseManager) {
//		List<String> resList	= new ArrayList<String>();
		Map<String, Integer> mapSiteToRecordCount		= new TreeMap<String, Integer>();
		Map<String, Integer> mapInstrumentToRecordCount	= new TreeMap<String, Integer>();
		Map<String, Integer> mapMsTypeToRecordCount		= new TreeMap<String, Integer>();
		Map<String, Integer> mapIonModeToRecordCount	= new TreeMap<String, Integer>();
		
		char[] compoundA_Z_symbols		= new char[] {
				'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
		};
		int[] compoundA_ZToRecordCount	= new int[26];
		int compound0_9RecordCount		= 0;
		int compoundOtherRecordCount	= 0;
		int normalRecordCount			= 0;
		int mergedRecordCount			= 0;
		
		PreparedStatement stmnt;
		ResultSet res;
		
		String sql = "SELECT SHORT_NAME AS CONTRIBUTOR, COUNT FROM (SELECT CONTRIBUTOR, COUNT(*) AS COUNT "
				+ "FROM RECORD GROUP BY CONTRIBUTOR) AS C, CONTRIBUTOR " + "WHERE CONTRIBUTOR = ID";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
//				resList.add("SITE:" + res.getString(1) + "\t" + res.getString("COUNT"));
				mapSiteToRecordCount.put(res.getString("CONTRIBUTOR"), res.getInt("COUNT"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "SELECT AC_INSTRUMENT_TYPE as INSTRUMENT, COUNT(*) as COUNT FROM INSTRUMENT GROUP BY AC_INSTRUMENT_TYPE";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
//				resList.add("INSTRUMENT:" + res.getString("INSTRUMENT") + "\t" + res.getString("COUNT"));
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
//				resList.add("MS:" + res.getString("TYPE") + "\t" + res.getString("COUNT"));
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
//				resList.add("ION:" + res.getString("MODE") + "\t" + res.getString("COUNT"));
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
//					resList.add("COMPOUND:" + compound + "\t" + res.getString("COUNT"));
					compoundA_ZToRecordCount[((int) compound.charAt(0)) - ((int) 'A')]	= res.getInt("COUNT");
				} else if (compound.matches("[0-9]")) {
					numCnt = numCnt + res.getInt("COUNT");
				} else {
					othCnt = othCnt + res.getInt("COUNT");
				}
			}
//			resList.add("COMPOUND:1-9\t" + numCnt);
//			resList.add("COMPOUND:Others\t" + othCnt);
			compound0_9RecordCount		= numCnt;
			compoundOtherRecordCount	= othCnt;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "SELECT COUNT(*) AS COUNT FROM RECORD WHERE INSTR(RECORD_TITLE,'MERGED') = 0";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
//				resList.add("MERGED:Normal\t" + res.getString("COUNT"));
				normalRecordCount	= res.getInt("COUNT");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "SELECT COUNT(*) AS COUNT FROM RECORD WHERE INSTR(RECORD_TITLE,'MERGED') > 0";
		try {
			stmnt = databaseManager.getConnection().prepareStatement(sql);
			res = stmnt.executeQuery();
			while (res.next()) {
//				resList.add("MERGED:Merged\t" + res.getString("COUNT"));
				mergedRecordCount	= res.getInt("COUNT");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return new RecordIndexCountResult(
				mapSiteToRecordCount, 
				mapInstrumentToRecordCount, 
				mapMsTypeToRecordCount, 
				mapIonModeToRecordCount, 
				compoundA_Z_symbols, 
				compoundA_ZToRecordCount, 
				compound0_9RecordCount, 
				compoundOtherRecordCount, 
				normalRecordCount, 
				mergedRecordCount
		);
	}
	public static class RecordIndexCountResult {
		public final Map<String, Integer> mapSiteToRecordCount;
		public final Map<String, Integer> mapInstrumentToRecordCount;
		public final Map<String, Integer> mapMsTypeToRecordCount;
		public final Map<String, Integer> mapIonModeToRecordCount;
		public final char[] compoundA_Z_symbols;
		public final int[] compoundA_ZToRecordCount;
		public final int compound0_9RecordCount;
		public final int compoundOtherRecordCount;
		public final int normalRecordCount;
		public final int mergedRecordCount;
		public final List<String> symbolList;
		public final Map<String, Integer> mapSymbolToCount;
		public final Map<String, Integer> mapMergedToCount;
		public RecordIndexCountResult(
				Map<String, Integer> mapSiteToRecordCount,
				Map<String, Integer> mapInstrumentToRecordCount,
				Map<String, Integer> mapMsTypeToRecordCount,
				Map<String, Integer> mapIonModeToRecordCount,
				char[] compoundA_Z_symbols,
				int[] compoundA_ZToRecordCount,
				int compound0_9RecordCount,
				int compoundOtherRecordCount,
				int normalRecordCount,
				int mergedRecordCount
		) {
			this.mapSiteToRecordCount		= mapSiteToRecordCount;
			this.mapInstrumentToRecordCount	= mapInstrumentToRecordCount;
			this.mapMsTypeToRecordCount		= mapMsTypeToRecordCount;
			this.mapIonModeToRecordCount	= mapIonModeToRecordCount;
			this.compoundA_Z_symbols		= compoundA_Z_symbols;
			this.compoundA_ZToRecordCount	= compoundA_ZToRecordCount;
			this.compound0_9RecordCount		= compound0_9RecordCount;
			this.compoundOtherRecordCount	= compoundOtherRecordCount;
			this.normalRecordCount			= normalRecordCount;
			this.mergedRecordCount			= mergedRecordCount;
			
			this.symbolList	= new ArrayList<String>();
			this.mapSymbolToCount	= new TreeMap<String, Integer>();
			for(int i = 0; i < compoundA_Z_symbols.length; i++) {
				this.symbolList.add(compoundA_Z_symbols[i] + "");
				this.mapSymbolToCount.put(compoundA_Z_symbols[i] + "", compoundA_ZToRecordCount[i]);
			}
			this.symbolList.add("0-9");
			this.mapSymbolToCount.put("0-9", compound0_9RecordCount);
			this.symbolList.add("Others");
			this.mapSymbolToCount.put("Others", compoundOtherRecordCount);
			
			this.mapMergedToCount	= new TreeMap<String, Integer>();
			this.mapMergedToCount.put("Normal", normalRecordCount);
			this.mapMergedToCount.put("Merged", mergedRecordCount);
		}
	}
}
