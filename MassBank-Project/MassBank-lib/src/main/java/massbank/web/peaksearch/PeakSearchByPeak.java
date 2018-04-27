package massbank.web.peaksearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;

import massbank.GetConfig;
import massbank.ResultList;
import massbank.ResultRecord;
import massbank.web.Database;

public class PeakSearchByPeak {

private Database database = null;
	
	private Connection connection = null;
	
	private HttpServletRequest request = null;
	
	private GetConfig conf = null;
	
	public PeakSearchByPeak(HttpServletRequest request, GetConfig conf) {
		this.database = new Database(); 
		this.connection = this.database.getConnection();
		this.request = request;
		this.conf = conf;
	}
	
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
	
	private void getParameters() {
		this.inst = this.request.getParameterValues("inst");
		this.ms = this.request.getParameterValues("ms");
		this.ion = this.request.getParameter("ion");
		this.num = 0;
		for (int i=0; i < 6; i++) {
			if (!this.request.getParameter("mz"+i).isEmpty()) {
				this.num = this.num + 1;
			}
		}
		this.op = new String[this.num];
		this.mz = new String[this.num];
		this.fom = new String[this.num];
		for (int i = 0; i < this.num; i++) {
			this.op[i] = this.request.getParameter("op" + i);
			this.mz[i] = this.request.getParameter("mz" + i);
//			TODO PeakSearch2.cgi does not consider the formula at all
			this.fom[i] = this.request.getParameter("fom" + i);
		}
		this.tol = this.request.getParameter("tol");
		this.intens = this.request.getParameter("int");
//		TODO this parameter is necessary?
		this.mode = this.request.getParameter("mode");
	}	
	
	public ResultList exec() {
		this.getParameters();
		return this.toResultList(this.peak());
	}
	
	// TODO insert functionality of peak search peak by mz (replaces PeakSearch2.cgi)
	public ArrayList<String> peak() {
		ArrayList<String> resList = new ArrayList<String>();
		
		String sql;
		PreparedStatement stmnt;
		ResultSet res;
		HashMap<String,ArrayList<Boolean>> hits = new HashMap<String,ArrayList<Boolean>>(); 
		for (int i = 0; i < num; i++) {
			sql = "SELECT RECORD "
					+ "FROM PEAK "
					+ "WHERE ? <= PK_PEAK_MZ AND PK_PEAK_MZ <= ? AND PK_PEAK_RELATIVE > ?";
			try {
				stmnt = connection.prepareStatement(sql);
				stmnt.setDouble(1, Double.parseDouble(mz[i]) - Double.parseDouble(tol));
				stmnt.setDouble(2, Double.parseDouble(mz[i]) + Double.parseDouble(tol));
				stmnt.setInt(3, Integer.parseInt(intens));
//				stmnt.setDouble(1, 166.0795);
//				stmnt.setDouble(2, 166.0795);
//				stmnt.setInt(3, 1);
				res = stmnt.executeQuery();
				while (res.next()) {
					String id = res.getString("RECORD");
					if (hits.containsKey(id)) {
						hits.get(id).set(i, true);
					} else {
						ArrayList<Boolean> newEl = new ArrayList<Boolean>();
						for (int j = 0; j < num; j++) {
							newEl.add(j,false);
						}
						newEl.set(i, true);
						hits.put(id, newEl);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
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
		
		try {
			stmnt = connection.prepareStatement(sb.toString().toUpperCase());
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
			while(res.next()) {
				resList.add(res.getString("record_title") + "\t" + res.getString("accession") + "\t" + res.getString("ac_mass_spectrometry_ion_mode") + "\t" + res.getString("ch_formula") + "\t" + res.getDouble("ch_exact_mass"));
//				System.out.println(res.getString("record_title") + "\t" + res.getString("accession") + "\t" + res.getString("ac_mass_spectrometry_ion_mode") + "\t" + res.getString("ch_formula") + "\t" + res.getDouble("ch_exact_mass"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
