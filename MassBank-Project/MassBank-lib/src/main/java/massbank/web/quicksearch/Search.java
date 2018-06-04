package massbank.web.quicksearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

public class Search {

	private static boolean PARAM_WEIGHT_LINEAR = true;

	private static boolean PARAM_WEIGHT_SQUARE = false;

	private static boolean PARAM_NORM_LOG = true;

	private static boolean PARAM_NORM_SQRT = false;

	private HttpServletRequest request;

	private ResultSet resMySql;

	private HashMap<String, ArrayList<String>> mapReqParam = new HashMap<String, ArrayList<String>>();

	private SearchQueryParam queryParam = new SearchQueryParam();

	private ArrayList<String> queryMz = new ArrayList<String>();

	private ArrayList<Double> queryVal = new ArrayList<Double>();

	private HashMap<String, ArrayList<SearchHitPeak>> mapHitPeak = new HashMap<String, ArrayList<SearchHitPeak>>();

	private HashMap<String, Integer> mapMzCnt = new HashMap<String, Integer>();

	private ArrayList<SearchResScore> vecScore = new ArrayList<SearchResScore>();

	private double m_fLen;

	private double m_fSum;

	private int m_iCnt;

	private boolean isQuick = false;

	private boolean isInteg = false;

	private boolean isAPI = false;

	private Connection con;

	public Search(HttpServletRequest request, Connection con) {
		this.request = request;
		this.con = con;
		execute();
	}

	public void execute() {
		getReqParam();
		setQueryParam();
		setQueryPeak();
		searchPeak();
		setScore();
		outResult();
	}

	private ArrayList<String> result = new ArrayList<String>();

	public ArrayList<String> getResult() {
		return this.result;
	}

	private void outResult() {
		String sql = "";
		PreparedStatement stmnt;
		try {
			for (int i = 0; i < vecScore.size(); i++) {
				SearchResScore resScore = vecScore.get(i);

				if (isQuick || isAPI) {
					sql = "SELECT RECORD_TITLE, AC_MASS_SPECTROMETRY_ION_MODE, CH_FORMULA, CH_EXACT_MASS "
							+ "FROM RECORD R, COMPOUND C " + "WHERE R.ACCESSION = '" + resScore.id
							+ "' AND R.CH = C.ID";
				} else if (isInteg) {
					sql = "SELECT NAME, ION, ID FROM PARENT_SPECTRUM WHERE SPECTRUM_NO=" + resScore.id;
				} else {
					sql = "SELECT RECORD_TITLE AS NAME, AC_MASS_SPECTROMETRY_ION_MODE AS ION FROM RECORD WHERE ACCESSION ='"
							+ resScore.id + "'";
				}
				stmnt = con.prepareStatement(sql);
				resMySql = stmnt.executeQuery();
				while (resMySql.next()) {
					String strName = resMySql.getString(1);
					String strIon = resMySql.getString(2);
					String strId;
					if (isInteg) {
						strId = resMySql.getString(1);
					} else {
						strId = resScore.id;
					}

					StringBuilder sb = new StringBuilder();
					sb.append(strId + "\t" + strName + "\t" + resScore.score + "\t" + strIon);
					if (isQuick || isAPI) {
						String formula = resMySql.getString(3);
						sb.append("\t" + formula);
					}
					if (isAPI) {
						String emass = resMySql.getString(4);
						sb.append("\t" + emass);
					}
					result.add(sb.toString());
					resMySql.close();
				}
			}
		} catch (SQLException e) {
			// TODO exepction handling
		}
	}

	private void setScore() {
		{
			String sql;
			PreparedStatement stmnt;
			ArrayList<SearchHitPeak> vecHitPeak = new ArrayList<SearchHitPeak>();

			String tblName = "PEAK";
			if (existHeapTable("PEAK_HEAP")) {
				// TODO
				tblName = "PEAK_HEAP";
			}

			Set<Entry<String, ArrayList<SearchHitPeak>>> mapEntrySet = mapHitPeak.entrySet();
			for (Entry<String, ArrayList<SearchHitPeak>> pIte1 : mapEntrySet) {
				String strId = pIte1.getKey();

				vecHitPeak.clear();
				ArrayList<SearchHitPeak> mapValue = mapHitPeak.get(strId);
				for (SearchHitPeak pIte2 : mapValue) {
					vecHitPeak.add(pIte2);
				}

				int iHitNum = vecHitPeak.size();
				if (iHitNum <= queryParam.threshold) {
					continue;
				}

				double fSum = 0;
				double fLen = 0;
				int iCnt = 0;

				try {
					if (isInteg) {
						sql = "SELECT MZ, RELATIVE FROM PARENT_PEAK WHERE SPECTRUM_NO = " + strId + " AND RELATIVE >= "
								+ queryParam.cutoff;
						stmnt = con.prepareStatement(sql);
					} else {
						sql = "SELECT PK_PEAK_MZ, PK_PEAK_RELATIVE FROM PEAK WHERE RECORD = ? AND PK_PEAK_RELATIVE >= ?";
						stmnt = con.prepareStatement(sql);
						stmnt.setString(1, strId);
						stmnt.setInt(2, queryParam.cutoff);
					}
				} catch (SQLException e) {
					stmnt = null;
					e.printStackTrace();
				}

				try {
					resMySql = stmnt.executeQuery();
					while (resMySql.next()) {
						String strMz = resMySql.getString(1);
						String strRelInt = resMySql.getString(2);
						double fMz = Double.parseDouble(strMz);
						double fVal = Double.parseDouble(strRelInt);

						if (queryParam.weight == PARAM_WEIGHT_LINEAR) {
							fVal *= fMz / 10;
						} else if (queryParam.weight == PARAM_WEIGHT_SQUARE) {
							fVal *= fMz * fMz / 100;
						}
						if (queryParam.norm == PARAM_NORM_LOG) {
							fVal = Math.log(fVal);
						} else if (queryParam.norm == PARAM_NORM_SQRT) {
							fVal = Math.sqrt(fVal);
						}

						String key;
						key = strId + " " + strMz;
						// TODO cpp initializes key not present with 0 we need to emulate this, but
						// isn't iMul always one after this what is this good for?
						if (!mapMzCnt.containsKey(key)) {
							mapMzCnt.put(key, 1);
						}
						int iMul = mapMzCnt.get(key);
						if (iMul == 0) {
							iMul = 1;
						}
						fLen += fVal * fVal * iMul;
						fSum += fVal * iMul;
						iCnt += iMul;
					}
					resMySql.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				double dblScore = 0;
				if (queryParam.colType.equals("COSINE")) {
					double fCos = 0;
					for (int i = 0; i < vecHitPeak.size(); i++) {
						SearchHitPeak pHitPeak = vecHitPeak.get(i);
						fCos += (double) (pHitPeak.qVal * pHitPeak.hitVal);
					}
					if (m_fLen * fLen == 0) {
						dblScore = 0;
					} else {
						dblScore = fCos / Math.sqrt(m_fLen * fLen);
					}
				}
				if (dblScore >= 0.9999) {
					dblScore = 0.999999999999;
				} else if (dblScore < 0) {
					dblScore = 0;
				}
				SearchResScore resScore = new SearchResScore();
				resScore.id = strId;
				resScore.score = iHitNum + dblScore;
				vecScore.add(resScore);
			}
		}
	}

	private boolean existHeapTable(String string) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean searchPeak() {
		String sql = "";
		PreparedStatement stmnt;

		// ----------------------------------------------------------------
		// precursor m/z
		// ----------------------------------------------------------------
		String sqlw1 = "";
		ArrayList<Integer> sqlw1Params = new ArrayList<Integer>();
		boolean isPre = false;
		if (queryParam.precursor > 0) {
			isPre = true;
			int pre1 = queryParam.precursor - 1;
			int pre2 = queryParam.precursor + 1;
			sqlw1Params.add(pre1);
			sqlw1Params.add(pre2);
			sqlw1 = " AND (T.PRECURSOR_MZ IS NOT NULL AND T.PRECURSOR_MZ BETWEEN ? AND ?)";
		}
		
		// ----------------------------------------------------------------
		// MS TYPE
		// ----------------------------------------------------------------
		String sqlw2 = "";
		ArrayList<String> sqlw2Params = new ArrayList<String>();
		boolean isMsType = false;
		if (!(queryParam.mstype == null) && !(queryParam.mstype.isEmpty()) && queryParam.mstype.indexOf("ALL") == -1
				&& queryParam.mstype.indexOf("all") == -1) {
			// MS_TYPE
			sql = "SHOW COLUMNS FROM RECORD LIKE 'AC_MASS_SPECTROMETRY_MS_TYPE'";
			try {
				stmnt = con.prepareStatement(sql);
				resMySql = stmnt.executeQuery();
				while (resMySql.next()) {
					isMsType = true;
					String ms = queryParam.mstype;
					int idx;
					sqlw2 = " AND T.MS_TYPE IN(";
					while ((idx = ms.indexOf(",")) != -1) {
						sqlw2Params.add(ms.substring(0, idx - 1));
						ms = ms.substring(idx + 1);
						sqlw2 += "?,";
					}
					sqlw2 = sqlw2.substring(0, sqlw2.length() - 1) + ")";
				}
			} catch (SQLException e) {
				return false;
			}
		}

		boolean isFilter = false;
		ArrayList<String> vecTargetId = new ArrayList<String>();
		if (queryParam.instType != null || !queryParam.instType.isEmpty() || queryParam.instType.indexOf("ALL") != -1
				|| queryParam.instType.indexOf("all") != -1) {
			if (!queryParam.ion.equals("0")) {
				sql = "SELECT T.ID "
						+ "FROM (SELECT * FROM (SELECT AC_INSTRUMENT AS AC_INSTRUMENT, ACCESSION AS ID, AC_MASS_SPECTROMETRY_MS_TYPE AS MS_TYPE, RECORD_TITLE AS NAME, AC_MASS_SPECTROMETRY_ION_MODE AS ION FROM RECORD) AS R, (SELECT RECORD, VALUE FROM MS_FOCUSED_ION WHERE SUBTAG = 'PRECURSOR_M/Z') AS S WHERE R.ID= S.RECORD) AS T "
						+ "WHERE T.ION = ?";
				// precursor m/z
				if (isPre) {
					sql += sqlw1;
				}
				if (isMsType) {
					sql += sqlw2;
				}
				sql += " ORDER BY ID";
				try {
					/*
SELECT T.ID 
FROM 
	(SELECT * 
	FROM 
		(SELECT AC_INSTRUMENT AS AC_INSTRUMENT, ACCESSION AS ID, AC_MASS_SPECTROMETRY_MS_TYPE AS MS_TYPE, RECORD_TITLE AS NAME, AC_MASS_SPECTROMETRY_ION_MODE AS ION FROM RECORD) AS R, 
		(SELECT RECORD, VALUE FROM MS_FOCUSED_ION WHERE SUBTAG = 'PRECURSOR_M/Z') AS S 
	WHERE R.ID= S.RECORD) AS T 
WHERE T.ION = ? ORDER BY ID
					 */
					int paramIdx = 1;
					stmnt = con.prepareStatement(sql);
					if (queryParam.ion.equals("-1")) {
						stmnt.setString(paramIdx, "NEGATIVE");
						paramIdx++;
					}
					if (queryParam.ion.equals("1")) {
						stmnt.setString(paramIdx, "POSITIVE");
						paramIdx++;
					}
					if (isPre) {
						for (Integer i : sqlw1Params) {
							stmnt.setInt(paramIdx, i);
							paramIdx++;
						}
					}
					if (isMsType) {
						for (String s : sqlw2Params) {
							stmnt.setString(paramIdx, s);
							paramIdx++;
						}
					}

					// TODO does this have to be here
					resMySql = stmnt.executeQuery();
					boolean isEmpty = true;
					isFilter = true;
					while (resMySql.next()) {
						vecTargetId.add(resMySql.getString(1));
						isEmpty = false;
					}

					if (isEmpty) {
						return false;
					}

					resMySql.close();

				} catch (SQLException e) {
					return false;
				}
			}
		} else {
			// ------------------------------------------------------------
			// (1)
			// ------------------------------------------------------------
			String[] vecInstType = queryParam.instType.split(",");
			String strInstType = "";
			for (int i = 0; i < vecInstType.length; i++) {
				if (vecInstType[i].compareTo("ALL") != 0 && vecInstType[i].compareTo("all") != 0) {
					strInstType += "?,";
				}
			}
			strInstType = strInstType.substring(0, strInstType.length() - 1);
			sql = "SELECT ID FROM INSTRUMENT WHERE AC_INSTRUMENT_TYPE IN(";
			sql += strInstType;
			sql += ")";
			ArrayList<String> instNo = new ArrayList<String>();
			try {
				stmnt = con.prepareStatement(sql);
				int paramIdx = 1;
				for (String s : vecInstType) {
					stmnt.setString(paramIdx, s);
					paramIdx++;
				}
				resMySql = stmnt.executeQuery();
				boolean isEmpty = true;
				// ------------------------------------------------------------
				// (2)
				// ------------------------------------------------------------
				while (resMySql.next()) {
					isEmpty = false;
					instNo.add(resMySql.getString(1));
				}
				for (String s : instNo) {
				}
				resMySql.close();
				if (isEmpty) {
					return false;
				}
			} catch (SQLException e) {
				return false;
			}

			sql = "SELECT T.ID "
					+ "FROM (SELECT * FROM (SELECT AC_INSTRUMENT AS AC_INSTRUMENT, ACCESSION AS ID, AC_MASS_SPECTROMETRY_MS_TYPE AS MS_TYPE, RECORD_TITLE AS NAME, AC_MASS_SPECTROMETRY_ION_MODE AS ION FROM RECORD) AS R, (SELECT RECORD, VALUE FROM MS_FOCUSED_ION WHERE SUBTAG = 'PRECURSOR_M/Z') AS S WHERE R.ID= S.RECORD) AS T "
					+ "WHERE";
			if (queryParam.ion.equals("0")) {
				sql += " AC_INSTRUMENT IN (";
				for (String s : instNo) {
					sql += "?,";
				}
				sql = sql.substring(0, sql.length() - 1);
				sql += ")";
			} else {
				sql += " ION = ?";
				sql += " AND AC_INSTRUMENT IN (";
				for (String s : instNo) {
					sql += "?,";
				}
				sql = sql.substring(0, sql.length() - 1);
				sql += ")";
			}
			// precursor m/z
			if (isPre) {
				sql += sqlw1;
			}
			if (isMsType) {
				sql += sqlw2;
			}

			sql += " ORDER BY ID";

			try {
				stmnt = con.prepareStatement(sql);
				int paramIdx = 1;
				if (queryParam.ion.equals("0")) {
					for (String s : instNo) {
						stmnt.setString(paramIdx, s);
						paramIdx++;
					}
					if (isPre) {
						for (Integer i : sqlw1Params) {
							stmnt.setInt(paramIdx, i);
							paramIdx++;
						}
					}
					if (isMsType) {
						for (String s : sqlw2Params) {
							stmnt.setString(paramIdx, s);
							paramIdx++;
						}
					}
				} else {
					if (queryParam.ion.equals("-1")) {
						stmnt.setString(paramIdx, "NEGATIVE");
						paramIdx++;
					}
					if (queryParam.ion.equals("1")) {
						stmnt.setString(paramIdx, "POSITIVE");
						paramIdx++;
					}
					for (String s : instNo) {
						stmnt.setString(paramIdx, s);
						paramIdx++;
					}
					if (isPre) {
						for (Integer i : sqlw1Params) {
							stmnt.setInt(paramIdx, i);
							paramIdx++;
						}
					}
					if (isMsType) {
						for (String s : sqlw2Params) {
							stmnt.setString(paramIdx, s);
							paramIdx++;
						}
					}
				}

				resMySql = stmnt.executeQuery();
				boolean isEmpty = true;
				isFilter = true;
				// ------------------------------------------------------------
				// (3)
				// ------------------------------------------------------------
				while (resMySql.next()) {
					vecTargetId.add(resMySql.getString(1));
				}
				resMySql.close();
				if (isEmpty) {
					return false;
				}
			} catch (SQLException e) {
				return false;
			}
		}
		
		double fMin;
		double fMax;
		String sqlw;
		for (int i = 0; i < queryMz.size(); i++) {
			String strMz = queryMz.get(i);
			double fMz = Double.parseDouble(strMz);
			double fVal = queryVal.get(i);

			float fTolerance = queryParam.tolerance;
			if (queryParam.tolUnit.equals("unit")) {
				fMin = fMz - fTolerance;
				fMax = fMz + fTolerance;
			} else {
				fMin = fMz * (1 - fTolerance / 1000000);
				fMax = fMz * (1 + fTolerance / 1000000);
			}
			fMin -= 0.00001;
			fMax += 0.00001;

			if (isInteg) {
				sql = "SELECT SPECTRUM_NO, MAX(PK_PEAK_RELATIVE), MZ FROM PARENT_PEAK WHERE ";
				sqlw = "PK_PEAK_RELATIVE >= " + queryParam.cutoff + " AND (MZ BETWEEN " + fMin + " AND " + fMax
						+ ") GROUP BY SPECTRUM_NO";
			} else {
				sql = "SELECT MAX(CONCAT(LPAD(PK_PEAK_RELATIVE, 3, ' '), ' ', RECORD, ' ', PK_PEAK_MZ)) FROM PEAK WHERE ";
				sqlw = "PK_PEAK_RELATIVE >= " + queryParam.cutoff + " AND (PK_PEAK_MZ BETWEEN " + fMin + " AND " + fMax
						+ ") GROUP BY RECORD";
			}
			sql += sqlw;
			try {
				stmnt = con.prepareStatement(sql);
				resMySql = stmnt.executeQuery();

				int prevAryNum = 0;
				while (resMySql.next()) {
					String[] vacVal = resMySql.getString(1).trim().split(" ");
					String strId = vacVal[1];

					if (isFilter) {
						boolean isFound = false;
						for (int j = prevAryNum; j < vecTargetId.size(); j++) {
							if (strId.compareTo(vecTargetId.get(j)) == 0) {
								isFound = true;
								prevAryNum = j + 1;
								break;
							}
						}
						if (!isFound) {
							continue;
						}
					}

					double fHitVal = Double.parseDouble(vacVal[0]);
					String strHitMz = vacVal[2];
					double fHitMz = Double.parseDouble(strHitMz);

					if (queryParam.weight == PARAM_WEIGHT_LINEAR) {
						fHitVal *= fHitVal / 10;
					} else if (queryParam.weight == PARAM_WEIGHT_SQUARE) {
						fHitVal *= fHitMz * fHitMz / 100;
					}
					if (queryParam.norm == PARAM_NORM_LOG) {
						fHitVal = Math.log(fHitVal);
					} else if (queryParam.norm == PARAM_NORM_SQRT) {
						fHitVal = Math.sqrt(fHitVal);
					}

					// m/z, rel.int
					SearchHitPeak pHitPeak = new SearchHitPeak();
					pHitPeak.qMz = strMz;
					pHitPeak.qVal = fVal;
					pHitPeak.hitMz = strHitMz;
					pHitPeak.hitVal = fHitVal;
					if (!mapHitPeak.containsKey(strId))
						mapHitPeak.put(strId, new ArrayList<SearchHitPeak>());
					mapHitPeak.get(strId).add(pHitPeak);

					String key = strId + " " + strHitMz;
					Integer value = mapMzCnt.get(key);
					if (value == null) {
						mapMzCnt.put(key, 1);
					} else {
						mapMzCnt.put(key, value++);
					}
				}
				resMySql.close();
			} catch (SQLException e) {
				return false;
			}
		}
		return true;
	}

	public void setQueryPeak() {
		ArrayList<String> vecVal = new ArrayList<String>();
		for (String s : queryParam.val.split("@")) {
			vecVal.add(s);
		}
		for (int i = 0; i < vecVal.size(); i++) {
			ArrayList<String> vecPeak = new ArrayList<String>();
			for (String s : vecVal.get(i).split(",")) {
				vecPeak.add(s);
			}
			String sMz = vecPeak.get(0);
			double fMz = Double.parseDouble(vecPeak.get(0));
			double fVal = Double.parseDouble(vecPeak.get(1));

			if (fVal < 1) {
				fVal = 1;
			} else if (fVal > 999) {
				fVal = 999;
			}
			if (fVal < queryParam.cutoff) {
				continue;
			}
			if (queryParam.weight == PARAM_WEIGHT_LINEAR) {
				fVal *= fMz / 10;
			} else if (queryParam.weight == PARAM_WEIGHT_SQUARE) {
				fVal *= fMz * fMz / 100;
			}
			if (queryParam.norm == PARAM_NORM_LOG) {
				fVal = Math.log(fVal);
			} else if (queryParam.norm == PARAM_NORM_SQRT) {
				fVal = Math.sqrt(fVal);
			}

			if (fVal > 0) {
				queryMz.add(sMz);
				queryVal.add(fVal);
				m_fLen += fVal * fVal;
				m_fSum += fVal;
				m_iCnt++;
			}
		}
		if (m_iCnt - 1 < queryParam.threshold) {
			queryParam.threshold = m_iCnt - 1;
		}
	}

	public void setQueryParam() {
		int val;
		queryParam.start = 1;
		queryParam.num = 0;
		queryParam.floor = 1;
		queryParam.celing = 1000;
		queryParam.threshold = 3;
		queryParam.cutoff = 20;
		queryParam.tolerance = 0.3f;
		queryParam.colType = "COSINE";
		queryParam.weight = PARAM_WEIGHT_SQUARE;
		queryParam.norm = PARAM_NORM_SQRT;
		queryParam.tolUnit = "unit";
		queryParam.precursor = 0;
		queryParam.mstype = "";

		if (mapReqParam.containsKey("START")) {
			val = Integer.parseInt(mapReqParam.get("Start").get(0));
			if (val > 0)
				queryParam.start = val;
		}
		if (mapReqParam.containsKey("NUM")) {
			val = Integer.parseInt(mapReqParam.get("NUM").get(0));
			if (val > 0)
				queryParam.num = val;
		}
		if (mapReqParam.containsKey("NUMTHRESHOLD")) {
			queryParam.threshold = Integer.parseInt(mapReqParam.get("NUMTHRESHOLD").get(0));
		}
		if (mapReqParam.containsKey("CUTOFF")) {
			queryParam.cutoff = Integer.parseInt(mapReqParam.get("CUTOFF").get(0));
		}
		if (mapReqParam.containsKey("TOLERANCE")) {
			queryParam.tolerance = Float.parseFloat(mapReqParam.get("TOLERANCE").get(0));
		}

		if (mapReqParam.containsKey("CORTYPE")) {
			queryParam.colType = mapReqParam.get("CORTYPE").get(0);
		}
		if (mapReqParam.containsKey("WEIGHT")) {
			if (mapReqParam.get("WEIGHT").get(0).compareTo("LINEAR") == 0) {
				queryParam.weight = PARAM_WEIGHT_LINEAR;
			} else if (mapReqParam.get("WEIGHT").get(0).compareTo("SQUARE") == 0) {
				queryParam.weight = PARAM_WEIGHT_SQUARE;
			}
		}
		if (mapReqParam.containsKey("NORM")) {
			if (mapReqParam.get("NORM").get(0).compareTo("LOG") == 0) {
				queryParam.norm = PARAM_NORM_LOG;
			} else if (mapReqParam.get("NORM").get(0).compareTo("SQRT") == 0) {
				queryParam.norm = PARAM_NORM_SQRT;
			}
		}
		if (mapReqParam.containsKey("TOLUNIT")) {
			queryParam.tolUnit = mapReqParam.get("TOLUNIT").get(0);
		}
		if (mapReqParam.containsKey("QPEAK")) {
			StringBuilder sb = new StringBuilder();
			for (String s : mapReqParam.get("QPEAK")) {
				s = s.trim().replaceAll(" ", ",");
				sb.append(s);
				sb.append("@");
			}
			String s = sb.toString();
			s = s.substring(0, s.length() - 1);
			queryParam.val = s;
		}
		// TODO there are no parameters like quick but type with value quick
		if (mapReqParam.containsKey("QUICK")) {
			isQuick = true;
		}
		isQuick = true;

		if (mapReqParam.containsKey("INTEG")) {
			String strVal = mapReqParam.get("INTEG").get(0);
			if (strVal.equals("true")) {
				isInteg = true;
			}
		}

		if (mapReqParam.containsKey("API")) {
			isAPI = true;
		}

		if (mapReqParam.containsKey("INST")) {
			StringBuilder sb = new StringBuilder();
			for (String s : mapReqParam.get("INST")) {
				sb.append(s + ",");
			}
			String s = sb.toString();
			s = s.substring(0, s.length() - 1);
			queryParam.instType = s;
		}

		if (!mapReqParam.containsKey("ION")) {
			queryParam.ion = "0";
		} else {
			queryParam.ion = mapReqParam.get("ION").get(0);
		}

		if (mapReqParam.containsKey("PRE")) {
			val = Integer.parseInt(mapReqParam.get("PRE").get(0));
			if (val > 0)
				queryParam.precursor = val;
		}

		if (mapReqParam.containsKey("MS")) {
			StringBuilder sb = new StringBuilder();
			for (String s : mapReqParam.get("MS")) {
				sb.append(s + ",");
			}
			String s = sb.toString();
			s = s.substring(0, s.length() - 1);
			queryParam.mstype = s;
		}
	}

	public boolean getReqParam() {
		String strLine;
		String strKey;
		String[] strVal;

		// REQUEST_METHOD
		String strReqType = request.getMethod();
		if (strReqType == null || strReqType.isEmpty())
			return false;

		String strParam;
		// GET
		if (strReqType.equals("GET")) {
			strParam = request.getQueryString();
			if (strParam == null || strParam.isEmpty())
				return false;
		}

		// POST
		else if (strReqType.equals("POST")) {
			int iLen = request.getContentLength();
			if (iLen < 0) {
				return false;
			} else {
				Map<String, String[]> parameterMap = request.getParameterMap();
				for (Entry<String, String[]> e : parameterMap.entrySet()) {
					strKey = e.getKey().toUpperCase();
					strVal = e.getValue();
					for (String s : strVal) {
						String[] split = s.split("\n");
						for (String s2 : split) {
							if (mapReqParam.containsKey(strKey)) {
								mapReqParam.get(strKey).add(s2);
							} else {
								mapReqParam.put(strKey.toUpperCase(), new ArrayList<String>());
								mapReqParam.get(strKey).add(s2);
							}
						}
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}
	
	public static class SearchHitPeak {

		public String qMz;
		public double qVal;
		public String hitMz;
		public double hitVal;

		public SearchHitPeak() {
		}
	}
	public static class SearchQueryParam {
		public int start;
		public int num;
		public int floor;
		public int celing;
		public int threshold;
		public int cutoff;
		public float tolerance;
		public String colType;
		public boolean weight;
		public boolean norm;
		public String tolUnit;
		public String val;
		public String instType;
		public String ion;
		public int precursor;
		public String mstype;

		public SearchQueryParam() {
		}
	}
	public static class SearchResScore {

		public String id;
		public double score;

		SearchResScore() {
		}
	}
}
