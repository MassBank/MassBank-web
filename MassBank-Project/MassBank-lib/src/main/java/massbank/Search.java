package massbank;

import java.awt.List;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class Search {
	
	private static boolean PARAM_WEIGHT_LINEAR = true;
	
	private static boolean PARAM_WEIGHT_SQUARE = false;
	
	private static boolean PARAM_NORM_LOG = true;
	
	private static boolean PARAM_NORM_SQRT = false;

	private HttpServletRequest request;

	private ResultSet resMySql;
	
	private HashMap<String,ArrayList<String>> mapReqParam = new HashMap<String,ArrayList<String>>();
	
	private SearchQueryParam queryParam = new SearchQueryParam();
	
	private ArrayList<String> queryMz = new ArrayList<String>();
	
	private ArrayList<Double> queryVal = new ArrayList<Double>();
	
	private HashMap<String,ArrayList<SearchHitPeak>> mapHitPeak = new HashMap<String,ArrayList<SearchHitPeak>>();

	private HashMap<String,Integer> mapMzCnt = new HashMap<String,Integer>();
	
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
		new DevLogger();
		execute();
	}
	
	public void execute() {
//		// ¥Ð¡¼¥¸¥ç¥ó¾ðÊóÉ½¼¨
//		if ( argv[1] != NULL ) {
//			if ( strcmp(argv[1], "-v") == 0 ) {
//				cout << VERSION_INFO << endl;
//				return 0;
//			}
//		}

//		cout << "Content-Type: text/html" << endl << endl;

		// ¥ê¥¯¥¨¥¹¥È¥Ñ¥é¥á¡¼¥¿¼èÆÀ
		if ( !getReqParam() ) {
//			printf("parameter error");
//			exit(1);
		}

		// DBÀÜÂ³
		if ( !dbConnect() ) {
//			exit(1);
		}

		// ¥¯¥¨¥ê¥Ñ¥é¥á¡¼¥¿¥»¥Ã¥È
		setQueryParam();
		setQueryPeak();
		if ( !searchPeak() ) {
//			return 0;
		}
		setScore();
		outResult();
		dbClose();
//		return 0;
	}

	private void dbClose() {
		// TODO Auto-generated method stub
		
	}
	
	private ArrayList<String> result = new ArrayList<String>();
	
	public ArrayList<String> getResult() {
		return this.result;
	}

	private void outResult() {
		new DevLogger();
		String sql = "";
		PreparedStatement stmnt;
//		char sql[1000];
		try {
			for (int i = 0; i<vecScore.size(); i++) {
	//		for ( unsigned int i = 0; i < vecScore.size(); i++ ) {
				SearchResScore resScore = vecScore.get(i);
	//			RES_SCORE resScore = vecScore.at(i);
	
				if ( isQuick || isAPI ) {
					sql = "SELECT RECORD_TITLE, AC_MASS_SPECTROMETRY_ION_MODE, CH_FORMULA, CH_EXACT_MASS "
							+ "FROM RECORD R, COMPOUND C "
							+ "WHERE R.ACCESSION = '" + resScore.id + "' AND R.CH = C.ID";
					DevLogger.printToDBLog(sql);
				}
				else if ( isInteg ) {
					sql = "select NAME, ION, ID from PARENT_SPECTRUM where SPECTRUM_NO=" + resScore.id;
					DevLogger.printToDBLog(sql);
				}
				else {
					sql = "select RECORD_TITLE as NAME, AC_MASS_SPECTROMETRY_ION_MODE as ION from RECORD where ACCESSION ='" + resScore.id + "'";
					DevLogger.printToDBLog(sql);
				}			
	//			if ( isQuick || isAPI ) {
	//				sprintf( sql, "select NAME, ION, FORMULA, EXACT_MASS from SPECTRUM S, RECORD R"
	//							  " where S.ID = R.ID and S.ID='%s'", resScore.id.c_str() );
	//			}
	//			else if ( isInteg ) {
	//				sprintf( sql, "select NAME, ION, ID from PARENT_SPECTRUM "
	//							  "where SPECTRUM_NO=%s", resScore.id.c_str() );
	//			}
	//			else {
	//				sprintf( sql, "select NAME, ION from SPECTRUM where ID='%s'", resScore.id.c_str() );
	//			}
	
				stmnt = con.prepareStatement(sql);
				resMySql = stmnt.executeQuery();
	//			long lNumRows = dbExecuteSql( sql );
	//			if ( lNumRows > 0 ) {
				while (resMySql.next()) {
					String strName = resMySql.getString(1);
					String strIon = resMySql.getString(2);
					String strId;
					if ( isInteg ) {
						strId = resMySql.getString(1);
					}
					else {
						strId = resScore.id;
					}
	//				MYSQL_ROW fields = mysql_fetch_row( resMySql );
	//				string strName = fields[0];
	//				string strIon = fields[1];
	//				mysql_free_result( resMySql );
	//				string strId;
	//				if ( isInteg ) {
	//					strId = fields[2];
	//				}
	//				else {
	//					strId = resScore.id;
	//				}
	
					StringBuilder sb = new StringBuilder();
					sb.append(strId + "\t" + strName + "\t" + resScore.score + "\t" + strIon);
					if ( isQuick || isAPI ) {
						String formula = resMySql.getString(3);
						sb.append("\t" + formula);
					}
					if ( isAPI ) {
						String emass = resMySql.getString(4);
						sb.append( "\t" + emass );
					}
//					sb.append( "\n" );
					result.add(sb.toString());
	//				printf( "%s\t%s\t%.12f\t%s",
	//					strId.c_str(), strName.c_str(), resScore.score, strIon.c_str() );
	//				if ( isQuick || isAPI ) {
	//					string formula = fields[2];
	//					printf( "\t%s", formula.c_str() );
	//				}
	//				if ( isAPI ) {
	//					string emass = fields[3];
	//					printf( "\t%s", emass.c_str() );
	//				}
	//				printf( "\n" );
					resMySql.close();
				}
			}
		} catch(SQLException e) {
			//TODO exepction handling
		}
		new DevLogger();
		for (String s : result) {
			DevLogger.printToDBLog(s);
		}
	}
	
	private void setScore() {
		{
			new DevLogger();
			String sql;
			PreparedStatement stmnt;
			ArrayList<SearchHitPeak> vecHitPeak = new ArrayList<SearchHitPeak>();
//			char sql[1000];
//			vector<HIT_PEAK*> vecHitPeak;
//			typedef multimap<string, HIT_PEAK*>::iterator MMAP_ITE;

			String tblName = "PEAK";
			if ( existHeapTable("PEAK_HEAP") ) {
				// TODO
				tblName = "PEAK_HEAP";
			}
//			string tblName = "PEAK";
//			if ( existHeapTable("PEAK_HEAP") ) {
//				tblName = "PEAK_HEAP";
//			}

			Set<Entry<String,ArrayList<SearchHitPeak>>> mapEntrySet = mapHitPeak.entrySet();
			for (Entry<String,ArrayList<SearchHitPeak>> pIte1 : mapEntrySet) {
//			for ( MMAP_ITE pIte1 = mapHitPeak.begin(); pIte1 != mapHitPeak.end(); pIte1++ ) {
				String strId = pIte1.getKey();
//				string strId = pIte1->first;

				// Æ±°ìID¤Î¥Ò¥Ã¥È¥Ô¡¼¥¯¤ò¼è¤ê½Ð¤¹
				vecHitPeak.clear();
				ArrayList<SearchHitPeak> mapValue = mapHitPeak.get(strId);
				for (SearchHitPeak pIte2 : mapValue) {
//					pIte1 = pIte2 ?????;
					vecHitPeak.add(pIte2);
				}
//				vecHitPeak.clear();
//				pair<MMAP_ITE, MMAP_ITE> range = mapHitPeak.equal_range( strId );			// ther is no multimap in java, we use Map<Type,ListType> so there is no range we iterate the list that is the value of the key
//				for ( MMAP_ITE pIte2 = range.first; pIte2 != range.second; ++pIte2 ) { 			
//					pIte1 = pIte2;
//					vecHitPeak.push_back(pIte2->second);
//				}

				// ¥Ò¥Ã¥È¥Ô¡¼¥¯¿ô¤¬¥¹¥ì¥·¥Û¡¼¥ë¥É°Ê²¼¤Î¾ì¹ç¤Ï½ü³°
				int iHitNum = vecHitPeak.size();
				if ( iHitNum <= queryParam.threshold ) {
					continue;
				}
//				int iHitNum = vecHitPeak.size();
//				if ( iHitNum <= queryParam.threshold ) {
//					continue;
//				}
				
				double fSum = 0;
				double fLen = 0;
				int iCnt = 0;
//				double fSum = 0;
//				double fLen = 0;
//				int iCnt = 0;

				// ¥Ò¥Ã¥È¤·¤¿¥¹¥Ú¥¯¥È¥ë¤Î¥Ô¡¼¥¯¤òDB¤è¤ê¼èÆÀ
				try {
					if ( isInteg ) {
						// TODO
						sql = "select MZ, RELATIVE from PARENT_PEAK where SPECTRUM_NO = " + strId + " and RELATIVE >= " + queryParam.cutoff;
						stmnt = con.prepareStatement(sql);
						DevLogger.printToDBLog(sql);
					}
					else {
						sql = "select PK_PEAK_MZ, PK_PEAK_RELATIVE from PEAK where RECORD = ? and PK_PEAK_RELATIVE >= ?";
						stmnt = con.prepareStatement(sql);
						DevLogger.printToDBLog(sql);
						stmnt.setString(1, strId);
						stmnt.setInt(2, queryParam.cutoff);
					}
				} catch (SQLException e) {
					DevLogger.printToDBLog("catch block at line 277");
					stmnt = null;
					e.printStackTrace();
				}
//				if ( isInteg ) {
//					sprintf( sql,
//						"select MZ, RELATIVE from PARENT_PEAK where SPECTRUM_NO = %s "
//						"and RELATIVE >= %d", strId.c_str(), queryParam.cutoff );
//				}
//				else {
//					sprintf( sql,
//						"select MZ, RELATIVE from %s where ID = '%s' and RELATIVE >= %d",
//						tblName.c_str(), strId.c_str(), queryParam.cutoff );
//				}

//				long lNumRows = dbExecuteSql( sql);
				try {
					resMySql = stmnt.executeQuery();
//				 	for ( long l = 0; l < lNumRows; l++ ) {
//			 		MYSQL_ROW fields = mysql_fetch_row( resMySql );
					while (resMySql.next()) {
						String strMz = resMySql.getString(1);
						String strRelInt = resMySql.getString(2);
						double fMz = Double.parseDouble( strMz );
						double fVal = Double.parseDouble( strRelInt );
//						string strMz = fields[0];
//						string strRelInt = fields[1];
//						double fMz = atof( strMz.c_str() );
//						double fVal = atof( strRelInt.c_str() );

						if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
							fVal *= fMz / 10;
						}
						else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
							fVal *= fMz * fMz / 100;
						}
						if ( queryParam.norm == PARAM_NORM_LOG ) {
							fVal = Math.log(fVal);
						}
						else if ( queryParam.norm == PARAM_NORM_SQRT ) {
							fVal = Math.sqrt(fVal);
						}
//						if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
//							fVal *= fMz / 10;
//						}
//						else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
//							fVal *= fMz * fMz / 100;
//						}
//						if ( queryParam.norm == PARAM_NORM_LOG ) {
//							fVal = log(fVal);
//						}
//						else if ( queryParam.norm == PARAM_NORM_SQRT ) {
//							fVal = sqrt(fVal);
//						}
				
						String key;
						key = strId + " " + strMz;
						// TODO cpp initializes key not present with 0 we need to emulate this, but isn't iMul always one after this what is this good for?
						if (!mapMzCnt.containsKey(key)) {
							mapMzCnt.put(key,1);
						}
						int iMul = mapMzCnt.get(key);
						if ( iMul == 0 ) {
							iMul = 1;
						}
						fLen += fVal * fVal * iMul;
						fSum += fVal * iMul;
						iCnt += iMul;					
//						char key[100];
//						sprintf( key, "%s %s", strId.c_str(), strMz.c_str() );
//						int iMul = mapMzCnt[key];
//						if ( iMul == 0 ) {
//							iMul = 1;
//						}
//						fLen += fVal * fVal * iMul;
//						fSum += fVal * iMul;
//						iCnt += iMul;
					}
					// ·ë²Ì¥»¥Ã¥È²òÊü
					resMySql.close();
//					mysql_free_result( resMySql );					
				} catch(SQLException e) {
					e.printStackTrace();
				}

				// ¥¹¥³¥¢¥»¥Ã¥È
				double dblScore = 0;
				if ( queryParam.colType == "COSINE" ) {
					double fCos = 0;
					for ( int i = 0; i < vecHitPeak.size(); i++ ) {
						SearchHitPeak pHitPeak = vecHitPeak.get(i); 
						fCos += (double)(pHitPeak.qVal * pHitPeak.hitVal);
					}
					if ( m_fLen * fLen == 0 ) {
						dblScore = 0;
					}
					else {
						dblScore = fCos / Math.sqrt(m_fLen * fLen);
					}
				}
				if ( dblScore >= 0.9999 ) {
					// double¤Ç°·¤¨¤ë¤Î¤Ï15·å¤Þ¤Ç¤Î¤¿¤á¡¢¾®¿ôÉô¤Ï12·å¤È¤¹¤ë
					dblScore = 0.999999999999;
				}
				else if ( dblScore < 0 ) {
					dblScore = 0;
				}
				SearchResScore resScore = new SearchResScore();
				resScore.id = strId;
				resScore.score = iHitNum + dblScore;
				vecScore.add(resScore);				
//				double dblScore = 0;
//				if ( queryParam.colType == "COSINE" ) {
//					double fCos = 0;
//					for ( unsigned int i = 0; i < vecHitPeak.size(); i++ ) {
//						HIT_PEAK *pHitPeak = vecHitPeak.at(i);
//						fCos += (double)(pHitPeak->qVal * pHitPeak->hitVal);
//					}
//					if ( m_fLen * fLen == 0 ) {
//						dblScore = 0;
//					}
//					else {
//						dblScore = fCos / sqrt(m_fLen * fLen);
//					}
//				}
//				if ( dblScore >= 0.9999 ) {
//					// double¤Ç°·¤¨¤ë¤Î¤Ï15·å¤Þ¤Ç¤Î¤¿¤á¡¢¾®¿ôÉô¤Ï12·å¤È¤¹¤ë
//					dblScore = 0.999999999999;
//				}
//				else if ( dblScore < 0 ) {
//					dblScore = 0;
//				}
//				RES_SCORE resScore;
//				resScore.id = strId;
//				resScore.score = iHitNum + dblScore;
//				vecScore.push_back(resScore);
			}	
		}
	}

	private boolean existHeapTable(String string) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean searchPeak() {
		new DevLogger();
		String sql = "";
		PreparedStatement stmnt;
//		string sql;

		//----------------------------------------------------------------
		// precursor m/z¤Ç¤Î¹Ê¤ê¹þ¤ß¤Ë»ÈÍÑ¤¹¤ë¸¡º÷¾ò·ï¤òÍÑ°Õ¤·¤Æ¤ª¤¯
		//----------------------------------------------------------------
		String sqlw1 = "";
		ArrayList<Integer> sqlw1Params = new ArrayList<Integer>();
		boolean isPre = false;
		if ( queryParam.precursor > 0) {
			isPre = true;
			int pre1 = queryParam.precursor - 1;
			int pre2 = queryParam.precursor + 1;
			sqlw1Params.add(pre1);
			sqlw1Params.add(pre2);
			sqlw1 = " and (T.PRECURSOR_MZ is not null and T.PRECURSOR_MZ between ? and ?)";
			// ? pre1, ? pre 2
		}
//		char sqlw1[1024];
//		bool isPre = false;
//		if ( queryParam.precursor > 0 ) {
//			isPre = true;
//			int pre1 = queryParam.precursor - 1;
//			int pre2 = queryParam.precursor + 1;
//			sprintf( sqlw1, " and (S.PRECURSOR_MZ is not null and S.PRECURSOR_MZ between %d and %d)", pre1, pre2 );
//		}

		//----------------------------------------------------------------
		// MS TYPE¤Ç¤Î¹Ê¤ê¹þ¤ß¤Ë»ÈÍÑ¤¹¤ë¸¡º÷¾ò·ï¤òÍÑ°Õ¤·¤Æ¤ª¤¯
		//----------------------------------------------------------------
		String sqlw2 = "";
		ArrayList<String> sqlw2Params = new ArrayList<String>();
		boolean isMsType = false;
		if ( !(queryParam.mstype == null) 
				&& !(queryParam.mstype.isEmpty())
				&& queryParam.mstype.indexOf("ALL") == -1 
				&& queryParam.mstype.indexOf("all") == -1 ) {
			// MS_TYPE¥«¥é¥àÍ­Ìµ¥Á¥§¥Ã¥¯
			sql = "show columns from RECORD like 'AC_MASS_SPECTROMETRY_MS_TYPE'";
			try {
				stmnt = con.prepareStatement(sql);
				DevLogger.printToDBLog(sql);
				resMySql = stmnt.executeQuery();
				while (resMySql.next()) {
					isMsType = true;
					String ms = queryParam.mstype;
					int idx;
					sqlw2 = " and T.MS_TYPE in(";
					while ((idx = ms.indexOf(",")) != -1) {
						sqlw2Params.add(ms.substring(0, idx-1));
						ms = ms.substring(idx+1);
						sqlw2 += "?,";
					}
					sqlw2 = sqlw2.substring(0, sqlw2.length()-1) + ")";
				}
			} catch (SQLException e) {
				DevLogger.printToDBLog("catch block of line 475");
				return false;
			}
		}
//		char sqlw2[1024];
//		bool isMsType = false;
//		if ( !queryParam.mstype.empty()
//		  && queryParam.mstype.find("ALL") == string::npos && queryParam.mstype.find("all") == string::npos ) {
//			// MS_TYPE¥«¥é¥àÍ­Ìµ¥Á¥§¥Ã¥¯
//			sql = "show columns from RECORD like 'MS_TYPE'";
//			long lNumRows = dbExecuteSql( sql.c_str() );
//			if ( lNumRows > 0 ) {
//				isMsType = true;
//				string ms = replace_all(queryParam.mstype, ",", "','");
//				sprintf( sqlw2, " and MS_TYPE in('%s')", ms.c_str() );
//			}
//		}

		//¡ü ¸¡º÷ÂÐ¾ÝALL¤Î¾ì¹ç
		boolean isFilter = false;
		ArrayList<String> vecTargetId = new ArrayList<String>();
		if ( queryParam.instType != null 
				|| !queryParam.instType.isEmpty()
				|| queryParam.instType.indexOf("ALL") != -1
				|| queryParam.instType.indexOf("all") != -1) {
			if ( queryParam.ion != "0" ) {
				sql = "select T.ID "
						+ "from (select * from (select AC_INSTRUMENT as AC_INSTRUMENT, ACCESSION as ID, AC_MASS_SPECTROMETRY_MS_TYPE as MS_TYPE, RECORD_TITLE as NAME, AC_MASS_SPECTROMETRY_ION_MODE as ION from RECORD) as R, (select RECORD, VALUE from MS_FOCUSED_ION where SUBTAG = 'PRECURSOR_M/Z') as S where R.ID= S.RECORD) as T "
						+ "where T.ION = ?";
//				sql = "select R.ID from RECORD R, SPECTRUM S where R.ID = S.ID and ION = ?";
//				sql += queryParam.ion;
				// precursor m/z¹Ê¤ê¹þ¤ß¾ò·ï¥»¥Ã¥È
				if ( isPre ) {
					sql += sqlw1;
				}
				if ( isMsType ) {
					sql += sqlw2;
				}
				sql += " order by ID";
				try {
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
					if ( isPre ) {
						for (Integer i : sqlw1Params) {
							stmnt.setInt(paramIdx, i);
							paramIdx++;
						}
					}
					if ( isMsType ) {
						for (String s : sqlw2Params) {
							stmnt.setString(paramIdx, s);
							paramIdx++;
						}
					}
					
					// TODO does this have to be here
					DevLogger.printToDBLog(sql);
					resMySql = stmnt.executeQuery();
					boolean isEmpty = true;
					isFilter = true;
					//--------------------------------------------------------
					// ¸¡º÷ÂÐ¾Ý¤ÎID¤ò³ÊÇ¼
					//--------------------------------------------------------
					while (resMySql.next()) {
						vecTargetId.add(resMySql.getString(1));
						isEmpty = false;
					}
					
					//** ¸¡º÷ÂÐ¾Ý¤ÎID¤¬¤Ê¤¤¤Î¤Ç½ªÎ»
					if ( isEmpty ) {
						return false;
					}
					
					
					resMySql.close();
					
				} catch (SQLException e) {
					DevLogger.printToDBLog("catch block of line 558");
					return false;
				}
				// ·ë²Ì¥»¥Ã¥È²òÊü
			}
		}
		//¡ü ¸¡º÷ÂÐ¾ÝALL°Ê³°¤Î¾ì¹ç
		else {
			//------------------------------------------------------------
			// (1) ¸¡º÷ÂÐ¾Ý¤ÎINSTRUMENT_TYPE¤¬Â¸ºß¤¹¤ë¤«¥Á¥§¥Ã¥¯
			//------------------------------------------------------------
			String[] vecInstType = queryParam.instType.split(",");
			String strInstType = "";
			for ( int i = 0; i < vecInstType.length; i++ ) {
				if ( vecInstType[i].compareTo("ALL") != 0 && vecInstType[i].compareTo("all") != 0) {
					strInstType += "?,";
//					strInstType += vecInstType[i];
//					strInstType += "',";
				}
			}
			strInstType = strInstType.substring(0,strInstType.length()-1);
			sql = "select ID from INSTRUMENT where AC_INSTRUMENT_TYPE in(";
//			sql = "select INSTRUMENT_NO from INSTRUMENT where INSTRUMENT_TYPE in(";
			sql += strInstType;
//			sql += strInstType.substring(0, strInstType.length()-1);
			sql += ")";
			ArrayList<String> instNo = new ArrayList<String>();
			try {
				stmnt = con.prepareStatement(sql);
				DevLogger.printToDBLog(sql);
				int paramIdx = 1;
				for (String s : vecInstType) {
					stmnt.setString(paramIdx, s);
					paramIdx++;
				}
				resMySql = stmnt.executeQuery();
				boolean isEmpty = true;
				//------------------------------------------------------------
				// (2) ¸¡º÷ÂÐ¾Ý¤ÎINSTRUMENT_NO¤Î¥ì¥³¡¼¥É¤¬Â¸ºß¤¹¤ë¤«¥Á¥§¥Ã¥¯
				//------------------------------------------------------------
				while (resMySql.next()) {
					isEmpty = false;
					instNo.add(resMySql.getString(1));
				}
				for (String s : instNo) {
					DevLogger.printToDBLog(s);
				}
				// ·ë²Ì¥»¥Ã¥È²òÊü
				resMySql.close();
				//** ¸¡º÷ÂÐ¾Ý¤ÎINSTRUMENT TYPE¤¬¤Ê¤¤¤Î¤Ç½ªÎ»
				if ( isEmpty ) {
					return false;
				}
			} catch (SQLException e) {
				DevLogger.printToDBLog("catch block of line 609");
				return false;
			}
			
			sql = "select T.ID "
					+ "from (select * from (select AC_INSTRUMENT as AC_INSTRUMENT, ACCESSION as ID, AC_MASS_SPECTROMETRY_MS_TYPE as MS_TYPE, RECORD_TITLE as NAME, AC_MASS_SPECTROMETRY_ION_MODE as ION from RECORD) as R, (select RECORD, VALUE from MS_FOCUSED_ION where SUBTAG = 'PRECURSOR_M/Z') as S where R.ID= S.RECORD) as T "
					+ "where";
			if ( queryParam.ion.equals("0") ) {
				sql += " AC_INSTRUMENT in (";
				for (String s : instNo) {
					sql += "?,";
				}
				sql = sql.substring(0,sql.length()-1);
				sql += ")";
			}
			else {
				sql += " ION = ?";
				sql += " and AC_INSTRUMENT in (";
				for (String s : instNo) {
					sql += "?,";
				}
				sql = sql.substring(0,sql.length()-1);
				sql += ")";
			}
			// precursor m/z¹Ê¤ê¹þ¤ß¾ò·ï¥»¥Ã¥È
			if ( isPre ) {
				sql += sqlw1;
			}
			if ( isMsType ) {
				sql += sqlw2;
			}

			sql += " order by ID";
			
			try {
				stmnt = con.prepareStatement(sql);
				DevLogger.printToDBLog(sql);
				int paramIdx = 1;
				if (queryParam.ion.equals("0")) {
					for (String s : instNo) {
						stmnt.setString(paramIdx, s);
						paramIdx++;
					}
					if ( isPre ) {
						for (Integer i : sqlw1Params) {
							stmnt.setInt(paramIdx, i);
							paramIdx++;
						}
					}
					if ( isMsType ) {
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
					if ( isPre ) {
						for (Integer i : sqlw1Params) {
							stmnt.setInt(paramIdx, i);
							paramIdx++;
						}
					}
					if ( isMsType ) {
						for (String s : sqlw2Params) {
							stmnt.setString(paramIdx, s);
							paramIdx++;
						}
					}
				}
				
				resMySql = stmnt.executeQuery();
				boolean isEmpty = true;
				isFilter = true;
				//------------------------------------------------------------
				// (3) ¸¡º÷ÂÐ¾Ý¤ÎID¤ò³ÊÇ¼
				//------------------------------------------------------------
				while (resMySql.next()) {
					vecTargetId.add(resMySql.getString(1));
				}
				// ·ë²Ì¥»¥Ã¥È²òÊü
				resMySql.close();	
				//** ¸¡º÷ÂÐ¾Ý¤ÎID¤¬¤Ê¤¤¤Î¤Ç½ªÎ»
				if (isEmpty) {
					return false;
				}
			} catch(SQLException e) {
				DevLogger.printToDBLog("catch block of line 703");
				return false;
			}
		}		
//		bool isFilter = false;
//		vector<string> vecTargetId;
//		if ( queryParam.instType.empty() || queryParam.instType.find("ALL") != string::npos ) {
//			if ( queryParam.ion != "0" ) {
//				sql = "select R.ID from RECORD R, SPECTRUM S where R.ID = S.ID and ION = ";
//				sql += queryParam.ion;
//				// precursor m/z¹Ê¤ê¹þ¤ß¾ò·ï¥»¥Ã¥È
//				if ( isPre ) {
//					sql += sqlw1;
//				}
//				if ( isMsType ) {
//					sql += sqlw2;
//				}
//				sql += " order by ID";
////				printf(sql.c_str());
//				long lNumRows = dbExecuteSql( sql.c_str() );
//
//				//** ¸¡º÷ÂÐ¾Ý¤ÎID¤¬¤Ê¤¤¤Î¤Ç½ªÎ»
//				if ( lNumRows == 0 ) {
//					return false;
//				}
//
//				isFilter = true;
//
//				//--------------------------------------------------------
//				// ¸¡º÷ÂÐ¾Ý¤ÎID¤ò³ÊÇ¼
//				//--------------------------------------------------------
//			 	for ( long l = 0; l < lNumRows; l++ ) {
//					MYSQL_ROW fields = mysql_fetch_row( resMySql );
//					vecTargetId.push_back(fields[0]);
//				}
//				// ·ë²Ì¥»¥Ã¥È²òÊü
//				mysql_free_result( resMySql );
//			}
//		}
//		//¡ü ¸¡º÷ÂÐ¾ÝALL°Ê³°¤Î¾ì¹ç
//		else {
//			//------------------------------------------------------------
//			// (1) ¸¡º÷ÂÐ¾Ý¤ÎINSTRUMENT_TYPE¤¬Â¸ºß¤¹¤ë¤«¥Á¥§¥Ã¥¯
//			//------------------------------------------------------------
//			vector<string> vecInstType = split(queryParam.instType, ",");
//			string strInstType;
//			for ( unsigned int i = 0; i < vecInstType.size(); i++ ) {
//				if ( vecInstType[i] != "ALL" ) {
//					strInstType += "'";
//					strInstType += vecInstType[i];
//					strInstType += "',";
//				}
//			}
//			sql = "select INSTRUMENT_NO from INSTRUMENT where INSTRUMENT_TYPE in(";
//			sql += strInstType.erase( strInstType.length() -1, 1 );
//			sql += ")";
//			long lNumRows = dbExecuteSql( sql.c_str() );
//
//			//** ¸¡º÷ÂÐ¾Ý¤ÎINSTRUMENT TYPE¤¬¤Ê¤¤¤Î¤Ç½ªÎ»
//			if ( lNumRows == 0 ) {
//				return false;
//			}
//
//			//------------------------------------------------------------
//			// (2) ¸¡º÷ÂÐ¾Ý¤ÎINSTRUMENT_NO¤Î¥ì¥³¡¼¥É¤¬Â¸ºß¤¹¤ë¤«¥Á¥§¥Ã¥¯
//			//------------------------------------------------------------
//			string instNo;
//		 	for ( long l = 0; l < lNumRows; l++ ) {
//				MYSQL_ROW fields = mysql_fetch_row( resMySql );
//				instNo += fields[0];
//				if ( l < lNumRows - 1 ) {
//					instNo += ",";
//				}
//			}
//			// ·ë²Ì¥»¥Ã¥È²òÊü
//			mysql_free_result( resMySql );
//
//			if ( queryParam.ion == "0" ) {
//				sql = "select R.ID from RECORD R, SPECTRUM S where R.ID = S.ID and INSTRUMENT_NO in(";
//				sql += instNo;
//				sql += ")";
//			}
//			else {
//				sql = "select R.ID from RECORD R, SPECTRUM S where R.ID = S.ID and ION = ";
//				sql += queryParam.ion;
//				sql += " and INSTRUMENT_NO in(";
//				sql += instNo;
//				sql += ")";
//			}
//			// precursor m/z¹Ê¤ê¹þ¤ß¾ò·ï¥»¥Ã¥È
//			if ( isPre ) {
//				sql += sqlw1;
//			}
//			if ( isMsType ) {
//				sql += sqlw2;
//			}
//
//			sql += " order by ID";
////			printf(sql.c_str());
//			lNumRows = dbExecuteSql( sql.c_str() );
//
//			//** ¸¡º÷ÂÐ¾Ý¤ÎID¤¬¤Ê¤¤¤Î¤Ç½ªÎ»
//			if ( lNumRows == 0 ) {
//				return false;
//			}
//
//			isFilter = true;
//			//------------------------------------------------------------
//			// (3) ¸¡º÷ÂÐ¾Ý¤ÎID¤ò³ÊÇ¼
//			//------------------------------------------------------------
//		 	for ( long l = 0; l < lNumRows; l++ ) {
//				MYSQL_ROW fields = mysql_fetch_row( resMySql );
//				vecTargetId.push_back(fields[0]);
//			}
//			// ·ë²Ì¥»¥Ã¥È²òÊü
//			mysql_free_result( resMySql );
//		}


		//---------------------------------------------------
		// ¥Ô¡¼¥¯ÃÍ¼èÆÀ
		//---------------------------------------------------
		double fMin;
		double fMax;
		String sqlw;
//		double fMin;
//		double fMax;
//		char sqlw[1000];
		for ( int i = 0; i < queryMz.size(); i++ ) {
//		for ( unsigned int i = 0; i < queryMz.size(); i++ ) {
			String strMz = queryMz.get(i);
			double fMz = Double.parseDouble( strMz );
			double fVal = queryVal.get(i);
//			string strMz = queryMz.at(i);
//			double fMz = atof( strMz.c_str() );
//			double fVal = queryVal.at(i);

			float fTolerance = queryParam.tolerance;
			if ( queryParam.tolUnit.equals("unit") ) {
				fMin = fMz - fTolerance;
				fMax = fMz + fTolerance;
			}
			else {
				fMin = fMz * (1 - fTolerance / 1000000);
				fMax = fMz * (1 + fTolerance / 1000000);
			}
			fMin -= 0.00001;
			fMax += 0.00001;			
//			float fTolerance = queryParam.tolerance;
//			if ( queryParam.tolUnit == "unit" ) {
//				fMin = fMz - fTolerance;
//				fMax = fMz + fTolerance;
//			}
//			else {
//				fMin = fMz * (1 - fTolerance / 1000000);
//				fMax = fMz * (1 + fTolerance / 1000000);
//			}
//			fMin -= 0.00001;
//			fMax += 0.00001;

			if ( isInteg ) {
				// TODO
				sql = "select SPECTRUM_NO, max(PK_PEAK_RELATIVE), MZ from PARENT_PEAK where ";
				sqlw = "PK_PEAK_RELATIVE >= " + queryParam.cutoff + " and (MZ between " + fMin + " and " + fMax + ") group by SPECTRUM_NO";
			}
			else {
				sql = "select max(concat(lpad(PK_PEAK_RELATIVE, 3, ' '), ' ', RECORD, ' ', PK_PEAK_MZ)) from PEAK where ";
				sqlw = "PK_PEAK_RELATIVE >= " + queryParam.cutoff + " and (PK_PEAK_MZ between " + fMin + " and " + fMax + ") group by RECORD";
			}
			sql += sqlw;
			try {
				stmnt = con.prepareStatement(sql);
				DevLogger.printToDBLog(sql);
				resMySql = stmnt.executeQuery();					
//			if ( isInteg ) {
//				sql = "select SPECTRUM_NO, max(RELATIVE), MZ from PARENT_PEAK where ";
//				sprintf( sqlw,
//					"RELATIVE >= %d and (MZ between %.6f and %.6f) group by SPECTRUM_NO",
//					queryParam.cutoff, fMin, fMax );
//			}
//			else {
//				sql = "select max(concat(lpad(RELATIVE, 3, ' ')"
//							", ' ', ID, ' ', MZ)) from PEAK where ";
//				sprintf( sqlw,
//					"RELATIVE >= %d and (MZ between %.6f and %.6f) group by ID",
//					queryParam.cutoff, fMin, fMax );
//			}
//			sql += sqlw;
//			long lNumRows = dbExecuteSql( sql.c_str() );

				int prevAryNum = 0;
				while(resMySql.next()) {
					String[] vacVal = resMySql.getString(1).trim().split(" ");
					String strId = vacVal[1];
	
					if ( isFilter ) {
						boolean isFound = false;
						for ( int j = prevAryNum; j < vecTargetId.size(); j++ ) {
							if ( strId.compareTo(vecTargetId.get(j)) == 0 ) {
								isFound = true;
								prevAryNum = j + 1;
								break;
							}
						}
						if ( !isFound ) {
							continue;
						}
					}			
	//			unsigned int prevAryNum = 0;
	//		 	for ( long l = 0; l < lNumRows; l++ ) {
	//				MYSQL_ROW fields = mysql_fetch_row( resMySql );
	//
	//				vector<string> vacVal = split( fields[0], " " );
	//				string strId = vacVal[1];
	//
	//				if ( isFilter ) {
	//					bool isFound = false;
	//					for ( unsigned int i = prevAryNum; i < vecTargetId.size(); i++ ) {
	//						if ( strId == vecTargetId.at(i) ) {
	//							isFound = true;
	//							prevAryNum = i + 1;
	//							break;
	//						}
	//					}
	//					if ( !isFound ) {
	//						continue;
	//					}
	//				}
	
					double fHitVal = Double.parseDouble( vacVal[0] );
					String strHitMz = vacVal[2];
					double fHitMz = Double.parseDouble( strHitMz );				
	//				double fHitVal = atof( vacVal[0].c_str() );
	//				string strHitMz = vacVal[2];
	//				double fHitMz = atof( strHitMz.c_str() );
	
					if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
						fHitVal *= fHitVal / 10;
					}
					else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
						fHitVal *= fHitMz * fHitMz / 100;
					}
					if ( queryParam.norm == PARAM_NORM_LOG ) {
						fHitVal = Math.log(fHitVal);
					}
					else if ( queryParam.norm == PARAM_NORM_SQRT ) {
						fHitVal = Math.sqrt(fHitVal);
					}				
	//				if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
	//					fHitVal *= fHitVal / 10;
	//				}
	//				else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
	//					fHitVal *= fHitMz * fHitMz / 100;
	//				}
	//				if ( queryParam.norm == PARAM_NORM_LOG ) {
	//					fHitVal = log(fHitVal);
	//				}
	//				else if ( queryParam.norm == PARAM_NORM_SQRT ) {
	//					fHitVal = sqrt(fHitVal);
	//				}
	
					// ¥¯¥¨¥ê¤È¥Ò¥Ã¥È¤·¤¿¥Ô¡¼¥¯¤Îm/z, rel.int.¤ò³ÊÇ¼ 
					SearchHitPeak pHitPeak = new SearchHitPeak();
					pHitPeak.qMz    = strMz;
					pHitPeak.qVal   = fVal;
					pHitPeak.hitMz  = strHitMz;
					pHitPeak.hitVal = fHitVal;
					if (!mapHitPeak.containsKey(strId))
						mapHitPeak.put(strId, new ArrayList<SearchHitPeak>());
					mapHitPeak.get(strId).add(pHitPeak);
	//				HIT_PEAK *pHitPeak = new HIT_PEAK();
	//				pHitPeak->qMz    = strMz;
	//				pHitPeak->qVal   = fVal;
	//				pHitPeak->hitMz  = strHitMz;
	//				pHitPeak->hitVal = fHitVal;
	//				mapHitPeak.insert( pair<string, HIT_PEAK*>(strId, pHitPeak) ); 
	
					String key = strId + " " + strHitMz;
					Integer value = mapMzCnt.get(key);
					if (value == null) {
						mapMzCnt.put(key,1);
					} else {
						mapMzCnt.put(key, value++);
					}
	//				char key[100];
	//				sprintf( key, "%s %s", strId.c_str(), strHitMz.c_str() );
	//				mapMzCnt[key]++;
				}
				// ·ë²Ì¥»¥Ã¥È²òÊü
				resMySql.close();
		 	} catch (SQLException e) {
		 		DevLogger.printToDBLog("catch block of line 988");
		 		return false;
		 	}
		}
		return true;		
	}
	
	public void setQueryPeak() {
//		ArrayList<String> vecVal = (ArrayList<String>)Arrays.asList(queryParam.val.split("@"));
		ArrayList<String> vecVal = new ArrayList<String>();
		for (String s : queryParam.val.split("@")) {
			vecVal.add(s);
		}
//		vector<string> vecVal = split( queryParam.val, "@" );
		for ( int i = 0; i < vecVal.size(); i++) {
//		for ( unsigned int i = 0; i < vecVal.size(); i++ ) {
//			ArrayList<String> vecPeak = (ArrayList<String>)Arrays.asList(vecVal.get(i).split(","));
			ArrayList<String> vecPeak = new ArrayList<String>();
			for (String s : vecVal.get(i).split(",")) {
				vecPeak.add(s);
			}
//			vector<string> vecPeak = split( vecVal[i], "," );
			String sMz = vecPeak.get(0);
//			string sMz = vecPeak[0];
			double fMz = Double.parseDouble(vecPeak.get(0));
//			double fMz = atof( vecPeak[0].c_str() );
			double fVal = Double.parseDouble(vecPeak.get(1));
//			double fVal = atof( vecPeak[1].c_str() );
//
			if ( fVal < 1 ) {
				fVal = 1;
			}
//			if ( fVal < 1 ) {
//				fVal = 1;
//			}
			else if ( fVal > 999 ) {
				fVal = 999;
			}
//			else if ( fVal > 999 ) {
//				fVal = 999;
//			}
			if ( fVal < queryParam.cutoff ) {
				continue;
			}
//			if ( fVal < queryParam.cutoff ) {
//				continue;
//			}
			if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
				fVal *= fMz / 10;
			}
//			if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
//				fVal *= fMz / 10;
//			}
			else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
				fVal *= fMz * fMz / 100;
			}
//			else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
//				fVal *= fMz * fMz / 100;
//			}
			// check if the cpp function log and the java function Math.log really both compute the natural logarithm
			if ( queryParam.norm == PARAM_NORM_LOG) {
				fVal = Math.log(fVal);
			}			
//			if ( queryParam.norm == PARAM_NORM_LOG) {
//				fVal = log(fVal);
//			}
			else if ( queryParam.norm == PARAM_NORM_SQRT ) {
				fVal = Math.sqrt(fVal);
			}
//			else if ( queryParam.norm == PARAM_NORM_SQRT ) {
//				fVal = sqrt(fVal);
//			}
//
			if ( fVal > 0 ) {
				queryMz.add( sMz );
				queryVal.add( fVal );
				m_fLen += fVal * fVal;
				m_fSum += fVal;
				m_iCnt++;
			}
//			if ( fVal > 0 ) {
//				queryMz.push_back( sMz );
//				queryVal.push_back( fVal );
//				m_fLen += fVal * fVal;
//				m_fSum += fVal;
//				m_iCnt++;
//			}
		}
//		}
		if ( m_iCnt - 1 < queryParam.threshold ) {
			queryParam.threshold = m_iCnt - 1;
		}
//		if ( m_iCnt - 1 < queryParam.threshold ) {
//			queryParam.threshold = m_iCnt - 1;
//		}
	}

	public void setQueryParam() {
		int val; 
		queryParam.start     = 1;
		queryParam.num       = 0;
		queryParam.floor     = 1;
		queryParam.celing    = 1000;
		queryParam.threshold = 3;
		queryParam.cutoff    = 20;
		queryParam.tolerance = 0.3f;
		queryParam.colType   = "COSINE";
		queryParam.weight    = PARAM_WEIGHT_SQUARE;
		queryParam.norm      = PARAM_NORM_SQRT;
		queryParam.tolUnit   = "unit";
		queryParam.precursor = 0;
		queryParam.mstype = "";
		
		if ( mapReqParam.containsKey("START") ) {
			val = Integer.parseInt(mapReqParam.get("Start").get(0));
			if ( val > 0 )
				queryParam.start = val;
		}		
//		if ( !mapReqParam["START"].empty() ) {
//			val = atoi( mapReqParam["START"].c_str() );
//			if ( val > 0 ) 	queryParam.start = val;
//		}
		if ( mapReqParam.containsKey("NUM") ) {
			val = Integer.parseInt(mapReqParam.get("NUM").get(0));
			if ( val > 0 )
				queryParam.num = val;
		}		
//		if ( !mapReqParam["NUM"].empty() ) {
//			val = atoi( mapReqParam["NUM"].c_str() );
//			if ( val > 0 ) 	queryParam.num = val;
//		}
		if ( mapReqParam.containsKey("NUMTHRESHOLD") ) {
			queryParam.threshold = Integer.parseInt(mapReqParam.get("NUMTHRESHOLD").get(0));
		}
//		if ( !mapReqParam["NUMTHRESHOLD"].empty() ) {
//			queryParam.threshold = atoi( mapReqParam["NUMTHRESHOLD"].c_str() );
//		}
		if ( mapReqParam.containsKey("CUTOFF") ) {
			queryParam.cutoff = Integer.parseInt(mapReqParam.get("CUTOFF").get(0));
		}
//		if ( !mapReqParam["CUTOFF"].empty() ) {
//			queryParam.cutoff = atoi( mapReqParam["CUTOFF"].c_str() );
//		}
		if ( mapReqParam.containsKey("TOLERANCE") ) {
			queryParam.tolerance = Float.parseFloat(mapReqParam.get("TOLERANCE").get(0));
		}
//		if ( !mapReqParam["TOLERANCE"].empty() ) {
//			queryParam.tolerance = atof( mapReqParam["TOLERANCE"].c_str() );
//		}
//
		if ( mapReqParam.containsKey("CORTYPE") ) {
			queryParam.colType = mapReqParam.get("CORTYPE").get(0);
		}
//		if ( !mapReqParam["CORTYPE"].empty() ) {
//			queryParam.colType = mapReqParam["CORTYPE"];
//		}
		if ( mapReqParam.containsKey("WEIGHT") ) {
			if ( mapReqParam.get("WEIGHT").get(0).compareTo("LINEAR") == 0 ) {
				queryParam.weight = PARAM_WEIGHT_LINEAR;
			}
			else if ( mapReqParam.get("WEIGHT").get(0).compareTo("SQUARE") == 0 ) {
				queryParam.weight = PARAM_WEIGHT_SQUARE;
			}
		}
//		if ( !mapReqParam["WEIGHT"].empty() ) {
//			if ( mapReqParam["WEIGHT"] == "LINEAR" ) {
//				queryParam.weight = PARAM_WEIGHT_LINEAR;
//			}
//			else if ( mapReqParam["WEIGHT"] == "SQUARE" ) {
//				queryParam.weight = PARAM_WEIGHT_SQUARE;
//			}
//		}
		if ( mapReqParam.containsKey("NORM") ) {
			if ( mapReqParam.get("NORM").get(0).compareTo("LOG") == 0 ) {
				queryParam.norm = PARAM_NORM_LOG;
			}
			else if ( mapReqParam.get("NORM").get(0).compareTo("SQRT") == 0 ) {
				queryParam.norm = PARAM_NORM_SQRT;
			}
		}		
//		if ( !mapReqParam["NORM"].empty() ) {
//			if ( mapReqParam["NORM"] == "LOG" ) {
//				queryParam.norm = PARAM_NORM_LOG;
//			}
//			else if ( mapReqParam["NORM"] == "SQRT" ) {
//				queryParam.norm = PARAM_NORM_SQRT;
//			}
//		}
		if ( mapReqParam.containsKey("TOLUNIT") ) {
			queryParam.tolUnit = mapReqParam.get("TOLUNIT").get(0);
		}
//		if ( !mapReqParam["TOLUNIT"].empty() ) {
//			queryParam.tolUnit = mapReqParam["TOLUNIT"];
//		}
		if ( mapReqParam.containsKey("QPEAK") ) {
			StringBuilder sb = new StringBuilder();
			for (String s : mapReqParam.get("QPEAK")) {
				s = s.trim().replaceAll(" ", ",");
				sb.append(s);
				sb.append("@");
			}
			String s = sb.toString();
			s = s.substring(0, s.length()-1);
			queryParam.val = s;
		}
// TODO VAL is the same as qpeak
//		if ( mapReqParam.containsKey("VAL") ) {
//			queryParam.val = mapReqParam.get("VAL").get(0);
//		}
//		if ( !mapReqParam["VAL"].empty() ) {
//			queryParam.val = mapReqParam["VAL"];
//		}
//
		// TODO there are no parameters like quick but type with value quick
		if ( mapReqParam.containsKey("QUICK") ) {
			isQuick = true;
		}
		isQuick = true;
//		if ( !mapReqParam["quick"].empty() ) {
//			isQuick = true;
//		}
//
		if ( mapReqParam.containsKey("INTEG") ) {
			String strVal = mapReqParam.get("INTEG").get(0);
			if ( strVal == "true" ) {
				isInteg = true;
			}
		}
//		if ( !mapReqParam["INTEG"].empty() ) {
//			string strVal = mapReqParam["INTEG"];
//			if ( strVal == "true" ) {
//				isInteg = true;
//			}
//		}
//
		if ( mapReqParam.containsKey("API") ) {
			isAPI = true;
		}
//		if ( !mapReqParam["API"].empty() ) {
//			isAPI = true;
//		}
//
		if ( mapReqParam.containsKey("INST") ) {
			StringBuilder sb = new StringBuilder();
			for(String s : mapReqParam.get("INST")) {
				sb.append(s + ",");
			}
			String s = sb.toString();
			s = s.substring(0, s.length()-1);
			queryParam.instType = s;
//			queryParam.instType = mapReqParam.get("INST");
		}
//		if ( !mapReqParam["INST"].empty() ) {
//			queryParam.instType = mapReqParam["INST"];
//		}
//
		if ( !mapReqParam.containsKey("ION") ) {
			queryParam.ion = "0";
		}
		else {
			queryParam.ion = mapReqParam.get("ION").get(0);
		}
//		if ( mapReqParam["ION"].empty() ) {
//			queryParam.ion = "0";
//		}
//		else {
//			queryParam.ion = mapReqParam["ION"];
//		}
//
		if ( mapReqParam.containsKey("PRE") ) {
			val = Integer.parseInt(mapReqParam.get("PRE").get(0));
			if ( val > 0 )
				queryParam.precursor = val;
		}
//		if ( !mapReqParam["PRE"].empty() ) {
//			val = atoi( mapReqParam["PRE"].c_str() );
//			if ( val > 0 ) 	queryParam.precursor = val;
//		}
//
		if ( mapReqParam.containsKey("MS") ) {
			StringBuilder sb = new StringBuilder();
			for(String s : mapReqParam.get("MS")) {
				sb.append(s + ",");
			}
			String s = sb.toString();
			s = s.substring(0, s.length()-1);
			queryParam.mstype = s;
//			queryParam.mstype = mapReqParam.get("MS");
		}
//		if ( !mapReqParam["MS"].empty() ) {
//			queryParam.mstype = mapReqParam["MS"];
//		}		
	}

	private boolean dbConnect() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean getReqParam() {
		String strLine;
		String strKey;
//		String strVal;
		String[] strVal;
//		string strLine;
//		string strKey;
//		string strVal;

		// REQUEST_METHOD¼èÆÀ
		String strReqType = request.getMethod();
//		string strReqType
//			= getenv( "REQUEST_METHOD" ) ? getenv( "REQUEST_METHOD" ): "";
		if (strReqType == null || strReqType.isEmpty())
			return false;
//		if ( strReqType.empty() ) {
//			return false;
//		}

		String strParam;
//		string strParam;
		// GET¥á¥½¥Ã¥É
		if (strReqType.equals("GET")) {
			strParam = request.getQueryString();
			if (strParam == null || strParam.isEmpty())
				return false;
		}
//		if ( strReqType == "GET" ) {
//			strParam = getenv( "QUERY_STRING" ) ? getenv( "QUERY_STRING" ): "";
//			if ( strParam.empty() ) {
//				return false;
//			}		
//		}
		
		// POST¥á¥½¥Ã¥É
		else if (strReqType.equals("POST")) {
			int iLen = request.getContentLength();
			if (iLen < 0) {
				return false;
			} else {
// TODO the following code is the direct implementation of the cpp code but it has problems
//				byte[] buf = new byte[iLen+1];
//				char[] buf = new char[iLen+1];
//				
// this part does not work because for some reason the InputStream reads nothing while gerParameterMap returns the content
//				try {
//					if (request.getInputStream().read(buf) != iLen) {	
//						dbError( "POST CONTENT_LENGTH " );
//						return false;
//					}
//				} catch (IOException e) {
//					return false;
//				}
//				try {
//					strParam = new String(buf, request.getCharacterEncoding());
//				} catch (UnsupportedEncodingException e) {
//					return false;
//				}
//				try {
//					strParam = URLDecoder.decode(strParam, request.getCharacterEncoding());
//				} catch (UnsupportedEncodingException e) {
//					return false;
//				}
				Map<String,String[]> parameterMap = request.getParameterMap();
				for (Entry<String,String[]> e : parameterMap.entrySet()) {
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
//		else if ( strReqType == "POST" ) {
//			string strLen
//				= getenv( "CONTENT_LENGTH" )? getenv( "CONTENT_LENGTH" ): "";
//			if( strLen.empty() ){
//				return false;
//			}
//			else {
//				unsigned int iLen = atoi( strLen.c_str() );
//				char buf[iLen+1];
//				if (fread( &buf, 1, iLen, stdin ) != iLen ) {
//				  dbError( "POST CONTENT_LENGTH " );
//				  return false;
//				}
//				urlDecode( buf, iLen );
//				strParam = buf;
//			}
//		}
//		else {
//			return false;
//		}
//		int delimiterIndex;
//		while ((delimiterIndex = strParam.indexOf('&')) != -1) {
//			strLine = strParam.substring(0, delimiterIndex);
//			strKey = strLine.substring(0, strLine.indexOf('='));
//			strVal = strLine.substring(strLine.indexOf('=')+1);
//			if (mapReqParam.containsKey(strKey)) {
//				mapReqParam.get(strKey).add(strVal);
//			} else {
//				mapReqParam.put(strKey, new ArrayList<String>());
//				mapReqParam.get(strKey).add(strVal);
//			}
////			mapReqParam.put(strKey, strVal);
//		}
//		istringstream iss( strParam.c_str() );
//		while ( getline( iss, strLine, '&' ) ) {
//			istringstream iss2( strLine.c_str() );
//			getline( iss2, strKey, '=' );
//			getline( iss2, strVal );
//			mapReqParam[strKey] = strVal;
//	 	}
		return true;
//		return true;
	}
	
	private void urlDecode(char[] buf, int iLen) {
		// TODO Auto-generated method stub
		
	}

	private void dbError(String string) {
		// TODO Auto-generated method stub
		
	}

	long dbExecuteSql( String sql ) {
		// TODO fill method dbExecuteSql
		return 0;
	}
	
}
