package massbank.web.recordindex;

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

public class RecordIndexByCategory {

	private Database database = null;
	
	private Connection connection = null;
	
	private HttpServletRequest request = null;
	
	private GetConfig conf = null;
	
	public RecordIndexByCategory(HttpServletRequest request, GetConfig conf) {
		this.database = new Database(); 
		this.connection = this.database.getConnection();
		this.request = request;
		this.conf = conf;
	}
	
	private String idxtype;
	private String srchkey;
	
	private void getParameters() {
		this.idxtype = request.getParameter("idxtype");
		this.srchkey = request.getParameter("srchkey");
	}
	
	public ResultList exec() {
		this.getParameters();
		return this.toResultList(this.rcdidx());
	}
	
	// TODO remove sql queries from within the function
	public ArrayList<String> rcdidx() {
		ArrayList<String> resList = new ArrayList<String>();
		
		String sql = "";
		PreparedStatement stmnt;
		ResultSet res;

		try {
			// TODO search by contributor
			if (idxtype.compareTo("site") == 0) {
				sql = "SELECT RECORD.RECORD_TITLE, RECORD.ACCESSION, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM RECORD, COMPOUND, (SELECT ID AS CON_ID FROM CONTRIBUTOR WHERE SHORT_NAME = ?) AS CON "
						+ "WHERE RECORD.CH = COMPOUND.ID AND RECORD.CONTRIBUTOR = CON.CON_ID";
			} 
			
			// TODO search by instrument type
			if (idxtype.compareTo("inst") == 0) {
				sql = "SELECT RECORD.RECORD_TITLE, RECORD.ACCESSION, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM RECORD, COMPOUND, (SELECT ID AS INST_ID FROM INSTRUMENT WHERE AC_INSTRUMENT_TYPE = ?) AS INST "
						+ "WHERE RECORD.CH = COMPOUND.ID AND RECORD.AC_INSTRUMENT = INST.INST_ID";
			}
			
			// TODO search by ms type
			if (idxtype.compareTo("ms") == 0) {
				sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM (SELECT * FROM RECORD WHERE AC_MASS_SPECTROMETRY_MS_TYPE = ?) AS REC, COMPOUND "
						+ "WHERE REC.CH = COMPOUND.ID";
			}
			
			// TODO search by spectrum type
			// there are no more merged spectra in the data!?
			if (idxtype.compareTo("merged") == 0) {
				if (srchkey.compareTo("Merged") == 0) {
					sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
							+ "FROM (SELECT * FROM RECORD WHERE INSTR(RECORD_TITLE,'MERGED') > 0) AS REC, COMPOUND "
							+ "WHERE REC.CH = COMPOUND.ID";
				}
				if (srchkey.compareTo("Normal") == 0) {
					sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
							+ "FROM (SELECT * FROM RECORD WHERE INSTR(RECORD_TITLE,'MERGED') = 0) AS REC, COMPOUND "
							+ "WHERE REC.CH = COMPOUND.ID";
				}
			}
			
			// TODO search by ion mode
			if (idxtype.compareTo("ion") == 0) {
				sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM (SELECT * FROM RECORD WHERE AC_MASS_SPECTROMETRY_ION_MODE = ?) AS REC, COMPOUND "
						+ "WHERE REC.CH = COMPOUND.ID";
			}
			
			// TODO search by compound first letter
			if (idxtype.compareTo("cmpd") == 0) {
				sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM (SELECT * FROM (SELECT *, UPPER(SUBSTRING(RECORD_TITLE,1,1)) AS FIRST FROM RECORD) AS F WHERE F.FIRST REGEXP ?) AS REC, COMPOUND "
						+ "WHERE REC.CH = COMPOUND.ID";
			}
			
			stmnt = connection.prepareStatement(sql);
			if (idxtype.compareTo("merged") != 0) {
				if (idxtype.compareTo("ion") != 0 && idxtype.compareTo("cmpd") != 0) {
					stmnt.setString(1, srchkey);
				}
				if (idxtype.compareTo("ion") == 0) {
					stmnt.setString(1, srchkey.toUpperCase());
				}
				if (idxtype.compareTo("cmpd") == 0) {
					if (srchkey.compareTo("Others") == 0) {
						stmnt.setString(1, "[^A-Z0-9]");
					} else if (srchkey.compareTo("1-9") == 0) {
						stmnt.setString(1, "[1-9]");
					} else {
						stmnt.setString(1, "[" + srchkey + "]");
					}
				}
			}
			res = stmnt.executeQuery();
			while(res.next()) {
				resList.add(res.getString("record_title") + "\t" + res.getString("accession") + "\t" + res.getString("ac_mass_spectrometry_ion_mode") + "\t" + res.getString("ch_formula") + "\t" + res.getDouble("ch_exact_mass"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
