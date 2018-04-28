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
import massbank.web.SearchFunction;

public class RecordIndexByCategory implements SearchFunction {

	private String idxtype;
	private String srchkey;
	
	public void getParameters(HttpServletRequest request) {
		this.idxtype = request.getParameter("idxtype");
		this.srchkey = request.getParameter("srchkey");
	}
	
	// TODO remove sql queries from within the function
	public ArrayList<String> search(Connection connection) {
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
}
