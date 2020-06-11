package massbank.web.export;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import massbank.Record;
import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;

public class AccessionList implements SearchFunction<Record[]> {
	
	private String[] inst;

	private String[] ms;

	private String ion;
	
	public void getParameters(HttpServletRequest request) {
		this.inst		= request.getParameterValues("inst");
		this.ms			= request.getParameterValues("ms");
		this.ion		= request.getParameter("ion");
	}
	
	public Record[] search(DatabaseManager databaseManager) {
		// ###########################################################################################
		// fetch all accessions and corresponding contributors
		String sql	= 
				"SELECT RECORD.ACCESSION, CONTRIBUTOR.SHORT_NAME " + 
				"FROM RECORD INNER JOIN CONTRIBUTOR INNER JOIN INSTRUMENT " + 
				"WHERE RECORD.CONTRIBUTOR = CONTRIBUTOR.ID";
		
		StringBuilder sb = new StringBuilder();
		sb.append(sql);
		if (this.inst != null && this.ms != null && this.ion != null) {
			sb.append(" AND (");
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
		
		List<String> resList_accession	= new ArrayList<String>();
		List<String> resList_contribut	= new ArrayList<String>();
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sb.toString());
			int idx = 1;
			if (this.inst != null && this.ms != null && this.ion != null) {
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
				resList_accession.add(res.getString("RECORD.ACCESSION"));
				resList_contribut.add(res.getString("CONTRIBUTOR.SHORT_NAME"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// ###########################################################################################
		// fetch these records
		List<Record> resList_record		= new ArrayList<Record>();
		for(int recordIdx = 0; recordIdx < resList_accession.size(); recordIdx++) {
			resList_record.add(databaseManager.getAccessionData(resList_accession.get(recordIdx)));
		}
		return resList_record.toArray(new Record[resList_record.size()]);
	}
}
