package massbank.web.recordindex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import massbank.ResultRecord;
import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;

public class RecordIndexByCategory implements SearchFunction<ResultRecord[]> {

	private String idxtype;
	private String srchkey;

	public void getParameters(HttpServletRequest request) {
		this.idxtype	= request.getParameter("idxtype");
		this.srchkey	= request.getParameter("srchkey");
	}

	public ResultRecord[] search(DatabaseManager databaseManager) {
		// ###########################################################################################
		// fetch matching records
		List<ResultRecord> resList = new ArrayList<ResultRecord>();
		
		String sql = "";
		PreparedStatement stmnt;
		ResultSet res;
		
		try {
			if (idxtype.compareTo("site") == 0) {
				sql = "SELECT RECORD.RECORD_TITLE, RECORD.ACCESSION, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM RECORD, COMPOUND, (SELECT ID AS CON_ID FROM CONTRIBUTOR WHERE SHORT_NAME = ?) AS CON "
						+ "WHERE RECORD.CH = COMPOUND.ID AND RECORD.CONTRIBUTOR = CON.CON_ID";
			}

			if (idxtype.compareTo("inst") == 0) {
				sql = "SELECT RECORD.RECORD_TITLE, RECORD.ACCESSION, RECORD.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM RECORD, COMPOUND, (SELECT ID AS INST_ID FROM INSTRUMENT WHERE AC_INSTRUMENT_TYPE = ?) AS INST "
						+ "WHERE RECORD.CH = COMPOUND.ID AND RECORD.AC_INSTRUMENT = INST.INST_ID";
			}

			if (idxtype.compareTo("ms") == 0) {
				sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM (SELECT * FROM RECORD WHERE AC_MASS_SPECTROMETRY_MS_TYPE = ?) AS REC, COMPOUND "
						+ "WHERE REC.CH = COMPOUND.ID";
			}

			// TODO there are no more merged spectra in the data!?
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

			if (idxtype.compareTo("ion") == 0) {
				sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM (SELECT * FROM RECORD WHERE AC_MASS_SPECTROMETRY_ION_MODE = ?) AS REC, COMPOUND "
						+ "WHERE REC.CH = COMPOUND.ID";
			}

			if (idxtype.compareTo("cmpd") == 0) {
				sql = "SELECT REC.RECORD_TITLE, REC.ACCESSION, REC.AC_MASS_SPECTROMETRY_ION_MODE, COMPOUND.CH_FORMULA, COMPOUND.CH_EXACT_MASS "
						+ "FROM (SELECT * FROM (SELECT *, UPPER(SUBSTRING(RECORD_TITLE,1,1)) AS FIRST FROM RECORD) AS F WHERE F.FIRST REGEXP ?) AS REC, COMPOUND "
						+ "WHERE REC.CH = COMPOUND.ID";
			}

			stmnt = databaseManager.getConnection().prepareStatement(sql);
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
			
			// ###########################################################################################
			// execute and fetch results
			res = stmnt.executeQuery();
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
