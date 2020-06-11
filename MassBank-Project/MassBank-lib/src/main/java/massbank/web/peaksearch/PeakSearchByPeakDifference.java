package massbank.web.peaksearch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import massbank.ResultRecord;
import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;

public class PeakSearchByPeakDifference implements SearchFunction<ResultRecord[]> {

	private String[] inst;

	private String[] ms;

	private String ion;

	private int num;

	private String[] op;

	private String[] mz;

	private String[] fom;

	private String tol;

	private String intens;

	private String mode;

	public void getParameters(HttpServletRequest request) {
		this.inst	= request.getParameterValues("inst");
		this.ms		= request.getParameterValues("ms");
		this.ion	= request.getParameter("ion");
		this.num	= 0;
		for (int i = 0; i < 6; i++)
			if (!request.getParameter("mz" + i).isEmpty())
				this.num++;
		
		this.op		= new String[this.num];
		this.mz		= new String[this.num];
		this.fom	= new String[this.num];
		for (int i = 0; i < this.num; i++) {
			this.op[i]	= request.getParameter("op" + i);
			this.mz[i]	= request.getParameter("mz" + i).trim();
			this.fom[i]	= request.getParameter("fom" + i).trim();
		}
		this.tol	= request.getParameter("tol").trim();
		this.intens	= request.getParameter("int").trim();
		this.mode	= request.getParameter("mode");
	}

	public ResultRecord[] search(DatabaseManager databaseManager) {
		// ###########################################################################################
		// fetch matching peaks for each record
		HashMap<String, boolean[]> hits = new HashMap<String, boolean[]>();
		for (int i = 0; i < num; i++) {
			String sql = 
					"SELECT T1.RECORD " + 
					"FROM " +
						"(SELECT * FROM PEAK WHERE PK_PEAK_RELATIVE > ?) AS T1 LEFT JOIN " +
						"(SELECT * FROM PEAK WHERE PK_PEAK_RELATIVE > ?) AS T2 ON T1.RECORD = T2.RECORD " +
					"WHERE (T1.PK_PEAK_MZ BETWEEN T2.PK_PEAK_MZ + ? AND T2.PK_PEAK_MZ + ?)";
			try {
				PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sql);
				stmnt.setInt(1, Integer.parseInt(intens));
				stmnt.setInt(2, Integer.parseInt(intens));
				stmnt.setDouble(3, Double.parseDouble(mz[i]) - Double.parseDouble(tol));
				stmnt.setDouble(4, Double.parseDouble(mz[i]) + Double.parseDouble(tol));
				
				ResultSet res = stmnt.executeQuery();
				while (res.next()) {
					String id = res.getString("RECORD");
					if (hits.containsKey(id)) {
						hits.get(id)[i]	= true;
					} else {
						boolean[] newEl = new boolean[num];
						newEl[i]	= true;
						hits.put(id, newEl);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		// ###########################################################################################
		// fetch matching records
		ArrayList<String> finIds = new ArrayList<String>();
		for (String key : hits.keySet()) {
			boolean[] val = hits.get(key);
			boolean expr = val[0];
			for (int i = 1; i < num; i++) {
				if (op[i].compareTo("or") == 0) {
					expr = expr || val[i];
				}
				if (op[i].compareTo("and") == 0) {
					expr = expr && val[i];
				}
			}
			if (expr) {
				finIds.add(key);
			}
		}
		
		// ###########################################################################################
		// fetch records
		String sql = "SELECT RECORD.ACCESSION, RECORD.RECORD_TITLE, RECORD.AC_MASS_SPECTROMETRY_MS_TYPE, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, INSTRUMENT.AC_INSTRUMENT_TYPE, CH_FORMULA, CH_EXACT_MASS "
				+ "FROM RECORD, INSTRUMENT, COMPOUND "
				+ "WHERE RECORD.CH = COMPOUND.ID AND RECORD.AC_INSTRUMENT = INSTRUMENT.ID";

		StringBuilder sb = new StringBuilder();
		sb.append(sql);
		sb.append(" AND RECORD.ACCESSION IN (");
		StringJoiner joiner = new StringJoiner(", ");
		for (String acc : finIds) {
			joiner.add("'" + acc + "'");
		}
		sb.append(joiner.toString());
		sb.append(")");
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
		
		List<ResultRecord> resList = new ArrayList<ResultRecord>();
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sb.toString());
			int idx = 1;
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
