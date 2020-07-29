package massbank.web.instrument;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;
import massbank.web.instrument.InstrumentSearch.InstrumentSearchResult;

public class InstrumentSearch implements SearchFunction<InstrumentSearchResult> {

	public void getParameters(HttpServletRequest request) {
		
	}

	public InstrumentSearchResult search(DatabaseManager databaseManager) {

		String sqlInst = 
				"SELECT AC_INSTRUMENT, AC_INSTRUMENT_TYPE FROM INSTRUMENT " + 
				"GROUP BY AC_INSTRUMENT, AC_INSTRUMENT_TYPE";
		String sqlMs = "SELECT DISTINCT AC_MASS_SPECTROMETRY_MS_TYPE FROM RECORD";
		
		List<String> instNo		= new ArrayList<String>();
		List<String> instType	= new ArrayList<String>();
		List<String> instName	= new ArrayList<String>();
		List<String> msType		= new ArrayList<String>();
		
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sqlInst);
			ResultSet res = stmnt.executeQuery();
			int instNoCounter = 1;
			while (res.next()) {
				instNo.add("" + instNoCounter++);
				instType.add(res.getString("AC_INSTRUMENT_TYPE"));
				instName.add(res.getString("AC_INSTRUMENT"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sqlMs);
			ResultSet res = stmnt.executeQuery();
			while (res.next()) {
				msType.add(res.getString("AC_MASS_SPECTROMETRY_MS_TYPE"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return new InstrumentSearchResult(
				instNo.toArray(new String[instNo.size()]), 
				instType.toArray(new String[instType.size()]), 
				instName.toArray(new String[instName.size()]), 
				msType.toArray(new String[msType.size()])
		);
	}
	public static class InstrumentSearchResult {
		public final String[] instNo;
		public final String[] instType;
		public final String[] instName;
		public final String[] msType;
		public InstrumentSearchResult(String[] instNo, String[] instType, String[] instName, String[] msType) {
			this.instNo		= instNo;
			this.instType	= instType;
			this.instName	= instName;
			this.msType		= msType;
		}
	}
}
