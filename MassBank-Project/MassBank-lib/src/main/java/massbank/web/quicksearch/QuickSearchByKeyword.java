package massbank.web.quicksearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import massbank.GetConfig;
import massbank.ResultList;
import massbank.ResultRecord;
import massbank.web.Database;
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
		
		String sql = "select record.accession, record.record_title, record.ac_mass_spectrometry_ms_type, record.ac_mass_spectrometry_ion_mode, instrument.ac_instrument_type, ch_formula, ch_exact_mass, ch_name "
				+ "from record,instrument,compound,compound_name,name "
				+ "where compound.id = compound_name.compound AND compound_name.name = name.id AND record.ch = compound.id AND record.ac_instrument = instrument.id";
		StringBuilder sb = new StringBuilder();
		sb.append(sql);
		sb.append(" AND (name.ch_name = ? ");
		if (mz.compareTo("") != 0 && tol.compareTo("") != 0)
			sb.append(op1 + " (? <= ch_exact_mass <= ?) ");
		if (formula.compareTo("") != 0)
			sb.append(op2 + " ch_formula = ? " );
		sb.append(") AND (");
		for (int i = 0; i < inst.length; i++) {
			sb.append("instrument.ac_instrument_type = ?");
			if (i < inst.length-1) {
				sb.append(" OR ");
			}
		}
		sb.append(") AND (");
		for (int i = 0; i < ms.length; i++) {
			sb.append("record.ac_mass_spectrometry_ms_type = ?");
			if (i < ms.length-1) {
				sb.append(" OR ");
			}
		}
		sb.append(")");
		if (Integer.parseInt(ion) != 0) {
			sb.append(" AND record.ac_mass_spectrometry_ion_mode = ?");
		}
//		System.out.println(sb.toString());
		try {
			PreparedStatement stmnt = connection.prepareStatement(sb.toString().toUpperCase());
			int idx = 1;
			stmnt.setString(idx, compound);
			idx++;
			if (mz.compareTo("") != 0 && tol.compareTo("") != 0) {
				stmnt.setDouble(idx, Double.parseDouble(mz)-Double.parseDouble(tol));
				idx++;
				stmnt.setDouble(idx, Double.parseDouble(mz)+Double.parseDouble(tol));
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
			while(res.next()) {
				resList.add(res.getString("record_title") + "\t" + res.getString("accession") + "\t" + res.getString("ac_mass_spectrometry_ion_mode") + "\t" + res.getString("ch_formula") + "\t" + res.getDouble("ch_exact_mass"));
//				System.out.println(res.getString("record_title") + "\t" + res.getString("accession") + "\t" + res.getString("ac_mass_spectrometry_ion_mode") + "\t" + res.getString("ch_formula") + "\t" + res.getDouble("ch_exact_mass"));
			}
		} catch (SQLException e) {
		}		
		return resList;
	}	
}
