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

public class QuickSearchByKeyword {

	private Database database = null;
	
	private Connection connection = null;
	
	private HttpServletRequest request = null;
	
	private GetConfig conf = null;
	
	public QuickSearchByKeyword(HttpServletRequest request, GetConfig conf) {
		this.database = new Database(); 
		this.connection = this.database.getConnection();
		this.request = request;
		this.conf = conf;
	}
	
	private String compound;
	
	private String op1;
	
	private String mz;
	
	private String tol;
	
	private String op2;
	
	private String formula;
	
	private String[] inst;
	
	private String[] ms;
	
	private String ion;

	private void getParameters() {
		this.compound = this.request.getParameter("compound");
		this.op1 = this.request.getParameter("op1");
		this.mz = this.request.getParameter("mz");
		this.tol = this.request.getParameter("tol");
		this.op2 = this.request.getParameter("op2");
		this.formula = this.request.getParameter("formula");
		this.inst = this.request.getParameterValues("inst");
		this.ms = this.request.getParameterValues("ms");
		this.ion = this.request.getParameter("ion");
	}
	
	public ResultList exec() {
		this.getParameters();
		return this.toResultList(this.quick());
	}
	
	public ArrayList<String> quick() {		
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

	private ResultList toResultList(ArrayList<String> allLine) {
		// Result information record generation (結果情報レコード生成)
				ResultList list = new ResultList(conf);
				ResultRecord record;
				int nodeGroup = -1;
				HashMap<String, Integer> nodeCount = new HashMap<String, Integer>();
				String[] fields;
				for (int i=0; i<allLine.size(); i++) {
					fields = allLine.get(i).split("\t");
					record = new ResultRecord();
					record.setInfo(fields[0]);
					record.setId(fields[1]);
					record.setIon(fields[2]);
					record.setFormula(fields[3]);
					record.setEmass(fields[4]);
//					record.setContributor(fields[fields.length-1]); 					need to set it to a fixed value because there are no more sites
					record.setContributor("0");
					// Node group setting (ノードグループ設定)
					if (!nodeCount.containsKey(record.getName())) {
						nodeGroup++;
						nodeCount.put(record.getName(), nodeGroup);
						record.setNodeGroup(nodeGroup);
					}
					else {
						record.setNodeGroup(nodeCount.get(record.getName()));
					}
					list.addRecord(record);
				}
				
				// Get sort key (ソートキー取得)
				String sortKey = ResultList.SORT_KEY_NAME;
				if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_FORMULA) == 0) {
					sortKey = ResultList.SORT_KEY_FORMULA;
				} else if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_EMASS) == 0) {
					sortKey = ResultList.SORT_KEY_EMASS;
				} else if (request.getParameter("sortKey").compareTo(ResultList.SORT_KEY_ID) == 0) {
					sortKey = ResultList.SORT_KEY_ID;
				}
				
				// Acquire sort action (ソートアクション取得)
				int sortAction = ResultList.SORT_ACTION_ASC;
				if (request.getParameter("sortAction").compareTo(String.valueOf(ResultList.SORT_ACTION_DESC)) == 0) {
					sortAction = ResultList.SORT_ACTION_DESC;
				}
				
				// Record sort (レコードソート)
				list.sortList(sortKey, sortAction);
				
//				if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_FORMULA) != -1) {
//					sortKey = ResultList.SORT_KEY_FORMULA;
//				}
//				else if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_EMASS) != -1) {
//					sortKey = ResultList.SORT_KEY_EMASS;
//				}
//				else if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_ID) != -1) {
//					sortKey = ResultList.SORT_KEY_ID;
//				}
//				
//				// Acquire sort action (ソートアクション取得)
//				int sortAction = ResultList.SORT_ACTION_ASC;
//				if (reqParam.indexOf("sortAction=" + ResultList.SORT_ACTION_DESC) != -1) {
//					sortAction = ResultList.SORT_ACTION_DESC;
//				}
//				
//				// Record sort (レコードソート)
//				list.sortList(sortKey, sortAction);
				
				
				return list;
	}
	
}
