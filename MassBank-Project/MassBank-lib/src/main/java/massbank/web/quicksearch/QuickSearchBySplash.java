package massbank.web.quicksearch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import massbank.ResultRecord;
import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;

public class QuickSearchBySplash implements SearchFunction<ResultRecord[]> {

	private String splash;
	
	private String[] inst;

	private String[] ms;

	private String ion;
	
	public void getParameters(HttpServletRequest request) {
		this.splash	= request.getParameter("splash").trim();
		this.inst	= request.getParameterValues("inst");
		this.ms		= request.getParameterValues("ms");
		this.ion	= request.getParameter("ion");
	}

	public ResultRecord[] search(DatabaseManager databaseManager) {
		// ###########################################################################################
		// fetch matching records
		String sql = 
				"SELECT RECORD.ACCESSION, RECORD.RECORD_TITLE, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, CH_FORMULA, CH_EXACT_MASS " + 
				"FROM RECORD,INSTRUMENT,COMPOUND " + 
				"WHERE " + 
					"RECORD.CH = COMPOUND.ID AND " + 
					"RECORD.AC_INSTRUMENT = INSTRUMENT.ID";
		StringBuilder sb = new StringBuilder();
		sb.append(sql);
		//sb.append(" AND (RECORD.PK_SPLASH = ?");
		sb.append(" AND (UPPER(RECORD.PK_SPLASH) like UPPER(?) ");
		if (this.inst != null && this.ms != null && this.ion != null) {
			sb.append(") AND (");
			for (int i = 0; i < inst.length; i++) {
				sb.append("INSTRUMENT.AC_INSTRUMENT_TYPE = ?");
				if (i < inst.length - 1) {
					sb.append(" OR ");
				}
			}
			sb.append(") AND (");
			for (int i = 0; i < ms.length; i++) {
				sb.append("RECORD.AC_MASS_SPECTROMETRY_MS_TYPE = ?");
				if (i < ms.length - 1) {
					sb.append(" OR ");
				}
			}
			sb.append(")");
			if (Integer.parseInt(ion) != 0) {
				sb.append(" AND RECORD.AC_MASS_SPECTROMETRY_ION_MODE = ?");
			}
		} else {
			sb.append(")");
		}
		
		// ###########################################################################################
		// execute and fetch results
		List<ResultRecord> resList = new ArrayList<ResultRecord>();
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sb.toString());
			int idx = 1;
			String splashAsSubstring	= "%" + this.splash + "%";
			stmnt.setString(idx, splashAsSubstring);
			if (this.inst != null && this.ms != null && this.ion != null) {
				idx++;
				for (int i = 0; i < inst.length; i++) {
					stmnt.setString(idx, inst[i]);
					idx++;
				}
				for (int i = 0; i < ms.length; i++) {
					stmnt.setString(idx, ms[i]);
					idx++;
				}
				if (Integer.parseInt(ion) == 1) {
					stmnt.setString(idx, "POSITIVE");
				}
				if (Integer.parseInt(ion) == -1) {
					stmnt.setString(idx, "NEGATIVE");
				}
			}
			ResultSet res = stmnt.executeQuery();
			while (res.next()) {
				ResultRecord record = new ResultRecord();
				record.setInfo(		res.getString("RECORD_TITLE"));
				record.setId(		res.getString("ACCESSION"));
				record.setIon(		res.getString("AC_MASS_SPECTROMETRY_ION_MODE"));
				record.setFormula(	res.getString("CH_FORMULA"));
				record.setEmass(	res.getDouble("CH_EXACT_MASS") + "");
				resList.add(record);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resList.toArray(new ResultRecord[resList.size()]);
	}

}
