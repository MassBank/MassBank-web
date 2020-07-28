package massbank.web.quicksearch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;
import massbank.web.quicksearch.Search.SearchResult;

public class QuickSearchByPeak implements SearchFunction<SearchResult[]> {
	
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
		public String toString() {
			return 
					"start=" + start + "; " + 
					"num=" + num + "; " + 
					"floor=" + floor + "; " + 
					"celing=" + celing + "; " + 
					"threshold=" + threshold + "; " + 
					"cutoff=" + cutoff + "; " + 
					"tolerance=" + tolerance + "; " + 
					"colType=" + colType + "; " + 
					"weight=" + weight + "; " + 
					"norm=" + norm + "; " + 
					"tolUnit=" + tolUnit + "; " + 
					"val=" + val + "; " +
					"instType=" + instType + "; " +
					"ion=" + ion + "; " +
					"precursor=" + precursor + "; " +
					"mstype=" + mstype;
		}
	}
	
	public static class SearchHitPeak {

		public String qMz;
		public double qVal;
		public String hitMz;
		public double hitVal;

		public SearchHitPeak() {
		}
		public String toString() {
			return 
					"qMz=" + qMz + "; " +
					"qVal=" + qVal + "; " +
					"hitMz=" + hitMz + "; " +
					"hitVal=" + hitVal;
		}
	}
	
	public static class SearchResScore{

		public String id;
		public int hitNumber;
		public double hitScore;

		SearchResScore() {
		}
		public String toString() {
			return 
					"id=" + id + "; " +
					"hitNumber=" + hitNumber + "; " +
					"hitScore=" + hitScore;
		}
	}
	
	private ArrayList<SearchResult> result = new ArrayList<SearchResult>();
	
	private static boolean PARAM_WEIGHT_LINEAR = true;
	private static boolean PARAM_WEIGHT_SQUARE = false;
	private static boolean PARAM_NORM_LOG = true;
	private static boolean PARAM_NORM_SQRT = false;
	
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
//	private boolean isInteg = false;
	private boolean isAPI = false;
	private Connection con;
	
	
	public void getParameters(HttpServletRequest request) {
		String strKey;
		String[] strVal;

		// REQUEST_METHOD
		//System.out.println(request.getRequestURI());
		String strReqType = request.getMethod();
		if (strReqType.equals("GET") || strReqType.equals("POST")) {
			//System.out.println(strReqType);
			Map<String, String[]> parameterMap = request.getParameterMap();

			for (Entry<String, String[]> e : parameterMap.entrySet()) {
				//System.out.println(e.getKey() + " " + Arrays.toString(e.getValue()));
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
	}

	public SearchResult[] search(DatabaseManager databaseManager) {
		con = databaseManager.getConnection();
		execute();
		return result.toArray(new SearchResult[result.size()]);
	}
	



	

	public void execute() {
//		System.out.println();
//		System.out.println("PARAM_WEIGHT_LINEAR: " + PARAM_WEIGHT_LINEAR);
//		System.out.println("PARAM_WEIGHT_SQUARE: " + PARAM_WEIGHT_SQUARE);
//		System.out.println("PARAM_NORM_LOG: " + PARAM_NORM_LOG);
//		System.out.println("PARAM_NORM_SQRT: " + PARAM_NORM_SQRT);
//		System.out.println("isQuick: " + isQuick);
////		System.out.println("isInteg: " + isInteg);
//		System.out.println("isAPI: " + isAPI);

//		System.out.println("setQueryParam");
		setQueryParam();	// fill SearchQueryParam queryParam from HashMap<String, ArrayList<String>> mapReqParam
//		System.out.println("queryParam: " + queryParam);
//		System.out.println();
//		System.out.println("setQueryPeak");
		setQueryPeak();		// fill queryMz, queryVal, m_fLen, m_fSum, m_iCnt from queryParam
//		System.out.println("queryMz: " + queryMz.size() + "\t" + queryMz);
//		System.out.println("queryVal: " + queryVal.size() + "\t" + queryVal);
//		System.out.println(m_fLen);
//		System.out.println(m_fSum);
//		System.out.println(m_iCnt);
//		System.out.println();
//		System.out.println("searchPeak");
		searchPeak();		// get hits from DB filling HashMap<String, ArrayList<SearchHitPeak>> mapHitPeak and HashMap<String, Integer> mapMzCnt
//		System.out.println("mapHitPeak: " + mapHitPeak.size());
//		System.out.println(mapHitPeak);
//		for(Entry<String, ArrayList<SearchHitPeak>> entry : mapHitPeak.entrySet())
//			System.out.println(entry.getKey() + "\t" + entry.getValue());
//		System.out.println("mapMzCnt: " + mapMzCnt.size());
//		System.out.println(mapMzCnt);
//		System.out.println();
//		System.out.println("setScore");
		setScore();			// score hits filling ArrayList<SearchResScore> vecScore
//		System.out.println("vecScore: " + vecScore.size());
//		System.out.println(vecScore);
//		System.out.println();
//		System.out.println("outResult");
		outResult();		// aggregate results filling ArrayList<String> result
//		System.out.println("result: " + result.size());
//		System.out.println(result);
	}

	

	private void outResult() {
		// sort by score
		SearchResScore[] resultObjects	= vecScore.toArray(new SearchResScore[vecScore.size()]);
		Arrays.sort(resultObjects, new Comparator<SearchResScore>() {
			public int compare(SearchResScore arg0, SearchResScore arg1) {
				if(arg1.hitScore - arg0.hitScore == 0)
					return 0;
				if(arg1.hitScore - arg0.hitScore > 0)
					return 1;
				else
					return -1;
			}
		});
		
		String sql = "";
		PreparedStatement stmnt;
		try {
			for (int i = 0; i < resultObjects.length; i++) {
				SearchResScore resScore = resultObjects[i];

				if (isQuick || isAPI) {
					sql = "SELECT RECORD_TITLE, AC_MASS_SPECTROMETRY_ION_MODE, CH_FORMULA, CH_EXACT_MASS "
							+ "FROM RECORD R, COMPOUND C " + "WHERE R.ACCESSION = '" + resScore.id
							+ "' AND R.CH = C.ID";
//				} else if (isInteg) {
//					sql = "SELECT NAME, ION, ID FROM PARENT_SPECTRUM WHERE SPECTRUM_NO=" + resScore.id;
				} else {
					sql = "SELECT RECORD_TITLE AS NAME, AC_MASS_SPECTROMETRY_ION_MODE AS ION FROM RECORD WHERE ACCESSION ='"
							+ resScore.id + "'";
				}
				stmnt = con.prepareStatement(sql);
				resMySql = stmnt.executeQuery();
				while (resMySql.next()) {
//					String strName = resMySql.getString(1);
					String strName = resMySql.getString("RECORD_TITLE");
//					String strIon = resMySql.getString(2);
					String strIon = resMySql.getString("AC_MASS_SPECTROMETRY_ION_MODE");
					String strId;
//					if (isInteg) {
////						strId = resMySql.getString(1);
//						strId = resMySql.getString("NAME");
//					} else {
						strId = resScore.id;
//					}
					
//					sb.append(strId + "\t" + strName + "\t" + resScore.score + "\t" + strIon);
//					sb.append(strId + "\t" + strName + "\t" + resScore.hitNumber + "\t" + resScore.hitScore + "\t" + strIon);
					String formula	= null;
					if (isQuick || isAPI) {
//						String formula = resMySql.getString(3);
						formula = resMySql.getString("CH_FORMULA");
//						sb.append("\t" + formula);
					}
					double exactMass	= Double.NaN;
					if (isAPI) {
//						String emass = resMySql.getString(4);
						String emass = resMySql.getString("CH_EXACT_MASS");
						exactMass	= Double.parseDouble(emass);
//						sb.append("\t" + emass);
					}
					
					SearchResult searchResult	= new SearchResult(strId, strName, resScore.hitNumber, resScore.hitScore, strIon, formula, exactMass);
//					result.add(sb.toString());
					result.add(searchResult);
				}
				resMySql.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void setScore() {
		if (!queryParam.colType.equals("COSINE")) throw new IllegalArgumentException("Unknown score function");
		
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
//				if (isInteg) {
//					sql = "SELECT MZ, RELATIVE FROM PARENT_PEAK WHERE SPECTRUM_NO = " + strId + " AND RELATIVE >= "
//							+ queryParam.cutoff;
//					stmnt = con.prepareStatement(sql);
//				} else {
					sql = "SELECT PK_PEAK_MZ, PK_PEAK_RELATIVE FROM PEAK WHERE RECORD = ? AND PK_PEAK_RELATIVE >= ?";
					stmnt = con.prepareStatement(sql);
					stmnt.setString(1, strId);
					stmnt.setInt(2, queryParam.cutoff);
//				}
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
					fVal = normalizePeakIntensity(fMz, fVal, false);
					
//					if (queryParam.weight == PARAM_WEIGHT_LINEAR) {
//						fVal *= fMz / 10;
//					} else if (queryParam.weight == PARAM_WEIGHT_SQUARE) {
//						fVal *= fMz * fMz / 100;
//					}
//					if (queryParam.norm == PARAM_NORM_LOG) {
//						fVal = Math.log(fVal);
//					} else if (queryParam.norm == PARAM_NORM_SQRT) {
//						fVal = Math.sqrt(fVal);
//					}

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
			if (dblScore >= 0.9999) {
				dblScore = 0.999999999999;
			} else if (dblScore < 0) {
				dblScore = 0;
			}
			SearchResScore resScore = new SearchResScore();
			resScore.id = strId;
//				resScore.score = iHitNum + dblScore;
			resScore.hitNumber = iHitNum;
			resScore.hitScore = dblScore;
			vecScore.add(resScore);
		}
	}

	private boolean existHeapTable(String string) {
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
		
		// SHOW COLUMNS FROM RECORD LIKE 'AC_MASS_SPECTROMETRY_MS_TYPE'
		String sqlw2 = "";
		ArrayList<String> sqlw2Params = new ArrayList<String>();
		boolean isMsType = false;
		if (!(queryParam.mstype == null) && !(queryParam.mstype.isEmpty()) && queryParam.mstype.toUpperCase().indexOf("ALL") == -1) {
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
					for(String ms_token : ms.split(",")) {
						sqlw2Params.add(ms_token);
						sqlw2 += "?,";
					}
//					while ((idx = ms.indexOf(",")) != -1) {
//						sqlw2Params.add(ms.substring(0, idx - 1));
//						ms = ms.substring(idx + 1);
//						sqlw2 += "?,";
//					}
					sqlw2 = sqlw2.substring(0, sqlw2.length() - 1) + ")";
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		//System.out.println(sqlw1);
		//System.out.println(sqlw2);
		//System.out.println(sql);
		// ####################################################################################
		// get accessions with right instrument type, MS type, precursor m/z
		boolean isFilter = false;
		ArrayList<String> vecTargetId = new ArrayList<String>();
		if (
				queryParam.instType == null || 
				queryParam.instType.isEmpty() || 
				queryParam.instType.toUpperCase().indexOf("ALL") != -1
		) {
			if (!queryParam.ion.equals("0")) {
				sql = "SELECT T.ID "
						+ "FROM (SELECT * FROM "
						+ "(SELECT AC_INSTRUMENT AS AC_INSTRUMENT, ACCESSION AS ID, AC_MASS_SPECTROMETRY_MS_TYPE AS MS_TYPE, RECORD_TITLE AS NAME, AC_MASS_SPECTROMETRY_ION_MODE AS ION FROM RECORD) AS R"
						+ (isPre ? ", (SELECT RECORD, VALUE FROM MS_FOCUSED_ION WHERE SUBTAG = 'PRECURSOR_M/Z') AS S WHERE R.ID= S.RECORD" : "")
						+ ") AS T "
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
					e.printStackTrace();
					return false;
				}
			}
		} else {
			// ------------------------------------------------------------
			// (1)
			// instrument type restrictions
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
//				for (String s : instNo) {
//				}
				resMySql.close();
				if (isEmpty) {
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			
			sql = "SELECT T.ID "
					+ "FROM (SELECT * FROM "
					+ "(SELECT AC_INSTRUMENT AS AC_INSTRUMENT, ACCESSION AS ID, AC_MASS_SPECTROMETRY_MS_TYPE AS MS_TYPE, RECORD_TITLE AS NAME, AC_MASS_SPECTROMETRY_ION_MODE AS ION FROM RECORD) AS R"
					+ (isPre ? ", (SELECT RECORD, VALUE FROM MS_FOCUSED_ION WHERE SUBTAG = 'PRECURSOR_M/Z') AS S WHERE R.ID= S.RECORD" : "")
					+ ") AS T "
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
				isFilter = true;
				// ------------------------------------------------------------
				// (3)
				// ------------------------------------------------------------
				while (resMySql.next()) {
					vecTargetId.add(resMySql.getString(1));
				}
				resMySql.close();
				if (vecTargetId.isEmpty()) {
					return false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
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
				// PPM
				fMin = fMz * (1 - fTolerance / 1000000);
				fMax = fMz * (1 + fTolerance / 1000000);
			}
			fMin -= 0.00001;
			fMax += 0.00001;

//			if (isInteg) {
//				sql = "SELECT SPECTRUM_NO, MAX(PK_PEAK_RELATIVE), MZ FROM PARENT_PEAK WHERE ";
//				sqlw = "PK_PEAK_RELATIVE >= " + queryParam.cutoff + " AND (MZ BETWEEN " + fMin + " AND " + fMax
//						+ ") GROUP BY SPECTRUM_NO";
//			} else {
				sql = "SELECT MAX(CONCAT(LPAD(PK_PEAK_RELATIVE, 3, ' '), ' ', RECORD, ' ', PK_PEAK_MZ)) FROM PEAK WHERE ";
				sqlw = "PK_PEAK_RELATIVE >= " + queryParam.cutoff + " AND (PK_PEAK_MZ BETWEEN " + fMin + " AND " + fMax
						+ ") GROUP BY RECORD";
//			}
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
						mapMzCnt.put(key, value + 1);
					}
				}
				resMySql.close();
			} catch (SQLException e) {
				e.printStackTrace();
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
				if(s.trim().length() > 0)
					vecPeak.add(s.trim());
			}
			String sMz = vecPeak.get(0);
			double fMz = Double.parseDouble(vecPeak.get(0));
			double fVal = Double.parseDouble(vecPeak.get(1));
			fVal	= normalizePeakIntensity(fMz, fVal, true);

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
	public double normalizePeakIntensity(double fMz, double fVal, boolean filterByIntensityCutoff) {
		if (fVal < 1) {
			fVal = 1;
		} else if (fVal > 999) {
			fVal = 999;
		}
		if (filterByIntensityCutoff && fVal < queryParam.cutoff) {
			return 0;
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
		return fVal;
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
			if(mapReqParam.get("QPEAK").get(0).indexOf(";") != -1) {
				String[] sa	= mapReqParam.get("QPEAK").get(0).split(";");
				ArrayList<String> list	= new ArrayList<String>();
				for(String s : sa)	list.add(s);
				mapReqParam.put("QPEAK", list);
			}
			
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
				throw new IllegalArgumentException("Parameter 'INTEG' not supported (any more)");
//				isInteg = true;
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


	

}
