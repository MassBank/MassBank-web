package massbank.web.quicksearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

import massbank.web.SearchFunction;

public class QuickSearchByKeyword implements SearchFunction {

	private String compound;

	private String op1;

	private String mz;

	private String tol;

	private String op2;

	private String formula;

	private String[] inst;

	private String[] ms;

	private String ion;

	public void getParameters(HttpServletRequest request) {
		this.compound = request.getParameter("compound");
		this.op1 = request.getParameter("op1");
		this.mz = request.getParameter("mz");
		this.tol = request.getParameter("tol");
		this.op2 = request.getParameter("op2");
		this.formula = request.getParameter("formula");
		this.inst = request.getParameterValues("inst");
		this.ms = request.getParameterValues("ms");
		this.ion = request.getParameter("ion");
	}

	public ArrayList<String> search(Connection connection) {
		ArrayList<String> resList = new ArrayList<String>();

		String sql = "SELECT RECORD.ACCESSION, RECORD.RECORD_TITLE, RECORD.AC_MASS_SPECTROMETRY_MS_TYPE, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, INSTRUMENT.AC_INSTRUMENT_TYPE, CH_FORMULA, CH_EXACT_MASS, CH_NAME "
				+ "FROM RECORD,INSTRUMENT,COMPOUND,COMPOUND_NAME,NAME "
				+ "WHERE COMPOUND.ID = COMPOUND_NAME.COMPOUND AND COMPOUND_NAME.NAME = NAME.ID AND RECORD.CH = COMPOUND.ID AND RECORD.AC_INSTRUMENT = INSTRUMENT.ID";
		StringBuilder sb = new StringBuilder();
		sb.append(sql);
		sb.append(" AND (NAME.CH_NAME = ? ");
		if (mz.compareTo("") != 0 && tol.compareTo("") != 0)
			sb.append(op1 + " (? <= CH_EXACT_MASS <= ?) ");
		if (formula.compareTo("") != 0)
			sb.append(op2 + " CH_FORMULA = ? ");
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
		try {
			PreparedStatement stmnt = connection.prepareStatement(sb.toString());
			int idx = 1;
			stmnt.setString(idx, compound);
			idx++;
			if (mz.compareTo("") != 0 && tol.compareTo("") != 0) {
				stmnt.setDouble(idx, Double.parseDouble(mz) - Double.parseDouble(tol));
				idx++;
				stmnt.setDouble(idx, Double.parseDouble(mz) + Double.parseDouble(tol));
				idx++;
			}
			if (formula.compareTo("") != 0) {
				stmnt.setString(idx, formula);
				idx++;
			}
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
