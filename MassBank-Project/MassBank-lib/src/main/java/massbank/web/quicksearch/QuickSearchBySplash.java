package massbank.web.quicksearch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;
import massbank.web.SearchFunction;

public class QuickSearchBySplash implements SearchFunction<List<String>> {

	private String splash;
	
	private String[] inst;

	private String[] ms;

	private String ion;
	
	@Override
	public void getParameters(HttpServletRequest request) {
		this.splash	= request.getParameter("splash").trim();
		this.inst	= request.getParameterValues("inst");
		this.ms		= request.getParameterValues("ms");
		this.ion	= request.getParameter("ion");
	}

	@Override
	public List<String> search(DatabaseManager databaseManager) {
		List<String> resList = new ArrayList<String>();

		String sql = "SELECT RECORD.ACCESSION, RECORD.RECORD_TITLE, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, CH_FORMULA, CH_EXACT_MASS "
				+ "FROM RECORD,INSTRUMENT,COMPOUND "
				+ "WHERE RECORD.CH = COMPOUND.ID AND RECORD.AC_INSTRUMENT = INSTRUMENT.ID";
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
				resList.add(res.getString("RECORD_TITLE") + "\t" + res.getString("ACCESSION") + "\t"
						+ res.getString("AC_MASS_SPECTROMETRY_ION_MODE") + "\t" + res.getString("CH_FORMULA") + "\t"
						+ res.getDouble("CH_EXACT_MASS"));
			}
		} catch (SQLException e) {
			// TODO
		}
		return resList;
	}

}