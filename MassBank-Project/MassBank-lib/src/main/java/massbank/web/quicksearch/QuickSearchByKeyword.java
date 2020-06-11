package massbank.web.quicksearch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;

import massbank.ResultRecord;
import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;

public class QuickSearchByKeyword implements SearchFunction<ResultRecord[]> {

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
		this.compound	= request.getParameter("compound").trim();
		this.op1		= request.getParameter("op1");
		this.mz			= request.getParameter("mz").trim();
		this.tol		= request.getParameter("tol").trim();
		this.op2		= request.getParameter("op2");
		this.formula	= request.getParameter("formula").trim();
		this.inst		= request.getParameterValues("inst");
		this.ms			= request.getParameterValues("ms");
		this.ion		= request.getParameter("ion");
	}

	public ResultRecord[] search(DatabaseManager databaseManager) {
		// check input
		// return empty Result array if no ms is selected
		if (ms == null) return ArrayUtils.toArray();
		// return empty Result array if no inst is selected
		if (inst == null) return ArrayUtils.toArray();
	
		// ###########################################################################################
		// fetch matching records
		// SELECT * FROM NAME WHERE UPPER(NAME.CH_NAME) like UPPER("%BENzENE%") ORDER BY LENGTH(NAME.CH_NAME);
		String sql = 
				//"SELECT RECORD.ACCESSION, RECORD.RECORD_TITLE, RECORD.AC_MASS_SPECTROMETRY_MS_TYPE, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, INSTRUMENT.AC_INSTRUMENT_TYPE, CH_FORMULA, CH_EXACT_MASS, CH_NAME " +
				"SELECT RECORD.ACCESSION, RECORD.RECORD_TITLE, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, CH_FORMULA, CH_EXACT_MASS " +
				"FROM RECORD,INSTRUMENT,COMPOUND,COMPOUND_NAME,NAME " +
				"WHERE " +
					"COMPOUND.ID = COMPOUND_NAME.COMPOUND AND " +
					"COMPOUND_NAME.NAME = NAME.ID AND " +
					"RECORD.CH = COMPOUND.ID AND " +
					"RECORD.AC_INSTRUMENT = INSTRUMENT.ID";
		StringBuilder sb = new StringBuilder();
		sb.append(sql);
		//sb.append(" AND (NAME.CH_NAME = ? ");
		sb.append(" AND (UPPER(NAME.CH_NAME) like UPPER(?) ");
		if (mz.compareTo("") != 0 && tol.compareTo("") != 0)
			sb.append(op1 + " (? <= CH_EXACT_MASS AND CH_EXACT_MASS <= ?) ");
		if (formula.compareTo("") != 0)
			sb.append(op2 + " CH_FORMULA LIKE ? ");
			//sb.append(op2 + " CH_FORMULA = ? ");
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
		sb.append(" ORDER BY LENGTH(NAME.CH_NAME)");
		
		// ###########################################################################################
		// execute and fetch results
		Set<String> names	= new HashSet<String>();
		List<ResultRecord> resList = new ArrayList<ResultRecord>();
		try {
			PreparedStatement stmnt = databaseManager.getConnection().prepareStatement(sb.toString());
			int idx = 1;
			String compoundAsSubstring	= "%" + compound + "%";
			//stmnt.setString(idx, compound);
			stmnt.setString(idx, compoundAsSubstring);
			idx++;
			if (mz.compareTo("") != 0 && tol.compareTo("") != 0) {
				double lowerBound	= Double.parseDouble(mz) - Double.parseDouble(tol);
				double upperBound	= Double.parseDouble(mz) + Double.parseDouble(tol);
				stmnt.setDouble(idx, lowerBound);
				idx++;
				stmnt.setDouble(idx, upperBound);
				idx++;
			}
			if (formula.compareTo("") != 0) {
				stmnt.setString(idx, formula.replace('*','%'));
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
				ResultRecord record = new ResultRecord();
				record.setInfo(		res.getString("RECORD_TITLE"));
				record.setId(		res.getString("ACCESSION"));
				record.setIon(		res.getString("AC_MASS_SPECTROMETRY_ION_MODE"));
				record.setFormula(	res.getString("CH_FORMULA"));
				record.setEmass(	res.getDouble("CH_EXACT_MASS") + "");
				if(!names.contains(record.getId())) {
					resList.add(record);
					names.add(record.getId());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resList.toArray(new ResultRecord[resList.size()]);
	}
	public String toString() {
		return 
				"" +
				"compound: "	+ compound + "\t" +
				"op1: "			+ op1 + "\t" +
				"mz: "			+ mz + "\t" +
				"tol: "			+ tol + "\t" +
				"op2: "			+ op2 + "\t" +
				"formula: "		+ formula + "\t" +
				"inst: "		+ Arrays.toString(inst) + "\t" +
				"ms: "			+ Arrays.toString(ms) + "\t" +
				"ion: "			+ ion		;
	}
}
