package massbank.web.peaksearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;
import massbank.web.SearchFunction;

public class PeakSearchByPeakDifference implements SearchFunction<String> {

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

	public ArrayList<String> search(DatabaseManager databaseManager) {
		ArrayList<String> resList = new ArrayList<String>();
		String sql;
		PreparedStatement stmnt;
		ResultSet res;
		HashMap<String, ArrayList<Boolean>> hits = new HashMap<String, ArrayList<Boolean>>();
		for (int i = 0; i < num; i++) {
			sql = "SELECT T1.RECORD "
					+ "FROM (SELECT * FROM PEAK WHERE PK_PEAK_RELATIVE > ?) AS T1 LEFT JOIN (SELECT * FROM PEAK WHERE PK_PEAK_RELATIVE > ?) AS T2 ON T1.RECORD = T2.RECORD "
					+ "WHERE (T1.PK_PEAK_MZ BETWEEN T2.PK_PEAK_MZ + ? AND T2.PK_PEAK_MZ + ?)";
			try {
				stmnt = databaseManager.getConnection().prepareStatement(sql);
				stmnt.setInt(1, Integer.parseInt(intens));
				stmnt.setInt(2, Integer.parseInt(intens));
				stmnt.setDouble(3, Double.parseDouble(mz[i]) - Double.parseDouble(tol));
				stmnt.setDouble(4, Double.parseDouble(mz[i]) + Double.parseDouble(tol));
				res = stmnt.executeQuery();
				while (res.next()) {
					String id = res.getString("RECORD");
					if (hits.containsKey(id)) {
						hits.get(id).add(i, true);
					} else {
						ArrayList<Boolean> newEl = new ArrayList<Boolean>();
						for (int j = 0; j < num; j++) {
							newEl.add(j, false);
						}
						newEl.add(i, true);
						hits.put(id, newEl);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		ArrayList<String> finIds = new ArrayList<String>();
		for (String key : hits.keySet()) {
			boolean expr = false;
			ArrayList<Boolean> val = hits.get(key);
			for (int i = 0; i < num; i++) {
				if (val.get(i) == null) {
					val.set(i, false);
				}
				if (i == 0) {
					expr = val.get(i);
				} else {
					if (op[i].compareTo("or") == 0) {
						expr = expr || val.get(i);
					}
					if (op[i].compareTo("and") == 0) {
						expr = expr && val.get(i);
					}
				}
			}
			if (expr) {
				finIds.add(key);
			}
		}

		sql = "SELECT RECORD.ACCESSION, RECORD.RECORD_TITLE, RECORD.AC_MASS_SPECTROMETRY_MS_TYPE, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, INSTRUMENT.AC_INSTRUMENT_TYPE, CH_FORMULA, CH_EXACT_MASS "
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

		try {
			stmnt = databaseManager.getConnection().prepareStatement(sb.toString());
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
			res = stmnt.executeQuery();
			while (res.next()) {
				resList.add(res.getString("RECORD_TITLE") + "\t" + res.getString("ACCESSION") + "\t"
						+ res.getString("AC_MASS_SPECTROMETRY_ION_MODE") + "\t" + res.getString("CH_FORMULA") + "\t"
						+ res.getDouble("CH_EXACT_MASS"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return resList;
	}
}
