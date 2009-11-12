/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 ******************************************************************************/
#include <string>
#include <vector>
#include <list>
#include <iostream>
#include <sstream>
#include <fstream>
#include <map>
#include <math.h>
#include <time.h>
#ifdef WIN32
#include <windows.h>
#endif
#include <mysql.h>
#include "search.h"

using namespace std;

//■ バージョン情報 ■■■■■■■■■■■■■■■■■■■■■■■■■
const static string VERSION_INFO = "5.0.5 2008.12.05";
//■ バージョン情報 ■■■■■■■■■■■■■■■■■■■■■■■■■

MYSQL *hMySql;
MYSQL_RES *resMySql;
map<string, string> mapReqParam;
QUERY_PARAM queryParam;
vector<string> queryMz;
vector<double> queryVal;
multimap<string, HIT_PEAK*> mapHitPeak;
map<string, int> mapMzCnt;
vector<RES_SCORE> vecScore;
double m_fLen;
double m_fSum;
int m_iCnt;
bool isQuick = false;
bool isInteg = false;


/********************************************************************
 * メイン
 ********************************************************************/
int main( int argc, char *argv[] )
{
	// バージョン情報表示
	if ( argv[1] != NULL ) {
		if ( strcmp(argv[1], "-v") == 0 ) {
			cout << VERSION_INFO << endl;
			return 0;
		}
	}

	cout << "Content-Type: text/html" << endl << endl;

	// リクエストパラメータ取得
	if ( !getReqParam() ) {
		printf("parameter error");
		exit(1);
	}

	// DB接続
	if ( !dbConnect() ) {
		exit(1);
	}

	// クエリパラメータセット
	setQueryParam();
	setQueryPeak();
	if ( !searchPeak() ) {
		return 0;
	}
	setScore();
	outResult();
	dbClose();
	return 0;
}

/********************************************************************
 * 結果出力
 ********************************************************************/
void outResult()
{
	char sql[1000];
	for ( unsigned int i = 0; i < vecScore.size(); i++ ) {
		RES_SCORE resScore = vecScore.at(i);
		if ( isQuick ) {
			sprintf( sql, "select NAME, ION, FORMULA from SPECTRUM S, RECORD R"
						  " where S.ID = R.ID and S.ID='%s'", resScore.id.c_str() );
		}
		else if ( isInteg ) {
			sprintf( sql, "select NAME, ION, ID from PARENT_SPECTRUM "
						  "where SPECTRUM_NO=%s", resScore.id.c_str() );
		}
		else {
			sprintf( sql, "select NAME, ION from SPECTRUM where ID='%s'", resScore.id.c_str() );
		}

		long lNumRows = dbExecuteSql( sql );
		if ( lNumRows > 0 ) {
			MYSQL_ROW fields = mysql_fetch_row( resMySql );
			string strName = fields[0];
			string strIon = fields[1];
			mysql_free_result( resMySql );
			string strId;
			if ( isInteg ) {
				strId = fields[2];
			}
			else {
				strId = resScore.id;
			}

			printf( "%s\t%s\t%.12f\t%s",
				strId.c_str(), strName.c_str(), resScore.score, strIon.c_str() );
			if ( isQuick ) {
				string formula = fields[2];
				printf( "\t%s", formula.c_str() );
			}
			printf( "\n" );
		}
	}
}

/********************************************************************
 * スコアセット
 ********************************************************************/
void setScore()
{
	char sql[1000];
	vector<HIT_PEAK*> vecHitPeak;
	typedef multimap<string, HIT_PEAK*>::iterator MMAP_ITE;

	string tblName = "PEAK";
	if ( existHeapTable("PEAK_HEAP") ) {
		tblName = "PEAK_HEAP";
	}

	for ( MMAP_ITE pIte1 = mapHitPeak.begin(); pIte1 != mapHitPeak.end(); pIte1++ ) {
		string strId = pIte1->first;

		// 同一IDのヒットピークを取り出す
		vecHitPeak.clear();
		pair<MMAP_ITE, MMAP_ITE> range = mapHitPeak.equal_range( strId );
		for ( MMAP_ITE pIte2 = range.first; pIte2 != range.second; ++pIte2 ) {
			pIte1 = pIte2;
			vecHitPeak.push_back(pIte2->second);
		}

		// ヒットピーク数がスレシホールド以下の場合は除外
		int iHitNum = vecHitPeak.size();
		if ( iHitNum <= queryParam.threshold ) {
			continue;
		}

		double fSum = 0;
		double fLen = 0;
		int iCnt = 0;

		// ヒットしたスペクトルのピークをDBより取得
		if ( isInteg ) {
			sprintf( sql,
				"select MZ, RELATIVE from PARENT_PEAK where SPECTRUM_NO = %s "
				"and RELATIVE >= %d", strId.c_str(), queryParam.cutoff );
		}
		else {
			sprintf( sql,
				"select MZ, RELATIVE from %s where ID = '%s' and RELATIVE >= %d",
				tblName.c_str(), strId.c_str(), queryParam.cutoff );
		}

		long lNumRows = dbExecuteSql( sql);
	 	for ( long l = 0; l < lNumRows; l++ ) {
			MYSQL_ROW fields = mysql_fetch_row( resMySql );
			
			string strMz = fields[0];
			string strRelInt = fields[1];
			double fMz = atof( strMz.c_str() );
			double fVal = atof( strRelInt.c_str() );

			if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
				fVal *= fMz / 10;
			}
			else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
				fVal *= fMz * fMz / 100;
			}
			if ( queryParam.norm == PARAM_NORM_LOG ) {
				fVal = log(fVal);
			}
			else if ( queryParam.norm == PARAM_NORM_SQRT ) {
				fVal = sqrt(fVal);
			}

			char key[100];
			sprintf( key, "%s %s", strId.c_str(), strMz.c_str() );
			int iMul = mapMzCnt[key];
			if ( iMul == 0 ) {
				iMul = 1;
			}
			fLen += fVal * fVal * iMul;
			fSum += fVal * iMul;
			iCnt += iMul;
	 	}

		// 結果セット解放
		mysql_free_result( resMySql );

		// スコアセット
		double dblScore = 0;
		if ( queryParam.colType == "COSINE" ) {
			double fCos = 0;
			for ( unsigned int i = 0; i < vecHitPeak.size(); i++ ) {
				HIT_PEAK *pHitPeak = vecHitPeak.at(i);
				fCos += (double)(pHitPeak->qVal * pHitPeak->hitVal);
			}
			if ( m_fLen * fLen == 0 ) {
				dblScore = 0;
			}
			else {
				dblScore = fCos / sqrt(m_fLen * fLen);
			}
		}
		if ( dblScore >= 0.9999 ) {
			// doubleで扱えるのは15桁までのため、小数部は12桁とする
			dblScore = 0.999999999999;
		}
		else if ( dblScore < 0 ) {
			dblScore = 0;
		}
		RES_SCORE resScore;
		resScore.id = strId;
		resScore.score = iHitNum + dblScore;
		vecScore.push_back(resScore);
	}
}


/********************************************************************
 * ピーク検索
 ********************************************************************/
bool searchPeak()
{
	double fMin;
	double fMax;
	char sqlw[1000];

	string sql;
	// 検索対象ALLの場合
	bool isFilter = false;
	vector<string> vecTargetId;
	if ( queryParam.instType.empty() || queryParam.instType.find("ALL") != string::npos ) {
		if ( queryParam.ion != "0" ) {
			sql = "select R.ID from RECORD R, SPECTRUM S where R.ID = S.ID and ION = ";
			sql += queryParam.ion;
			sql += " order by ID";
			long lNumRows = dbExecuteSql( sql.c_str() );

			//** 検索対象のIDがないので終了
			if ( lNumRows == 0 ) {
				return false;
			}

			isFilter = true;

			// 検索対象のIDを格納
		 	for ( long l = 0; l < lNumRows; l++ ) {
				MYSQL_ROW fields = mysql_fetch_row( resMySql );
				vecTargetId.push_back(fields[0]);
			}
			// 結果セット解放
			mysql_free_result( resMySql );
		}
	}
	// 検索対象ALL以外の場合
	else {
		//------------------------------------------------------------
		// (1) 検索対象のINSTRUMENT_TYPEが存在するかチェック
		//------------------------------------------------------------
		vector<string> vecInstType = split(queryParam.instType, ",");
		string strInstType;
		for ( unsigned int i = 0; i < vecInstType.size(); i++ ) {
			if ( vecInstType[i] != "ALL" ) {
				strInstType += "'";
				strInstType += vecInstType[i];
				strInstType += "',";
			}
		}
		sql = "select INSTRUMENT_NO from INSTRUMENT where INSTRUMENT_TYPE in(";
		sql +=  strInstType.erase( strInstType.length() -1, 1 );
		sql += ")";
		long lNumRows = dbExecuteSql( sql.c_str() );

		//** 検索対象のINSTRUMENT TYPEがないので終了
		if ( lNumRows == 0 ) {
			return false;
		}

		//------------------------------------------------------------
		// (2) 検索対象のINSTRUMENT_NOのレコードが存在するかチェック
		//------------------------------------------------------------
		string instNo;
	 	for ( long l = 0; l < lNumRows; l++ ) {
			MYSQL_ROW fields = mysql_fetch_row( resMySql );
			instNo += fields[0];
			if ( l < lNumRows - 1 ) {
				instNo += ",";
			}
		}
		// 結果セット解放
		mysql_free_result( resMySql );

		if ( queryParam.ion == "0" ) {
			sql = "select ID from RECORD where INSTRUMENT_NO in(";
			sql += instNo;
			sql += ")";
		}
		else {
			sql = "select R.ID from RECORD R, SPECTRUM S where R.ID = S.ID and ION = ";
			sql += queryParam.ion;
			sql += " and INSTRUMENT_NO in(";
			sql += instNo;
			sql += ")";
		}
		sql += " order by ID";
		lNumRows = dbExecuteSql( sql.c_str() );

		//** 検索対象のIDがないので終了
		if ( lNumRows == 0 ) {
			return false;
		}

		isFilter = true;
		//------------------------------------------------------------
		// (3) 検索対象のIDを格納
		//------------------------------------------------------------
	 	for ( long l = 0; l < lNumRows; l++ ) {
			MYSQL_ROW fields = mysql_fetch_row( resMySql );
			vecTargetId.push_back(fields[0]);
		}
		// 結果セット解放
		mysql_free_result( resMySql );
	}

	for ( unsigned int i = 0; i < queryMz.size(); i++ ) {
		string strMz = queryMz.at(i);
		double fMz = atof( strMz.c_str() );
		double fVal = queryVal.at(i);

		float fTolerance = queryParam.tolerance;
		if ( queryParam.tolUnit == "unit" ) {
			fMin = fMz - fTolerance;
			fMax = fMz + fTolerance;
		}
		else {
			fMin = fMz * (1 - fTolerance / 1000);
			fMax = fMz * (1 + fTolerance / 1000);
		}
		fMin -= 0.00001;
		fMax += 0.00001;

		if ( isInteg ) {
			sql = "select SPECTRUM_NO, max(RELATIVE), MZ from PARENT_PEAK where ";
			sprintf( sqlw,
				"RELATIVE >= %d and (MZ between %.6f and %.6f) group by SPECTRUM_NO",
				queryParam.cutoff, fMin, fMax );
		}
		else {
			sql = "select max(concat(lpad(RELATIVE, 3, ' ')"
						", ' ', ID, ' ', MZ)) from PEAK where ";
			sprintf( sqlw,
				"RELATIVE >= %d and (MZ between %.6f and %.6f) group by ID",
				queryParam.cutoff, fMin, fMax );
		}
		sql += sqlw;
		long lNumRows = dbExecuteSql( sql.c_str() );

		unsigned int prevAryNum = 0;
	 	for ( long l = 0; l < lNumRows; l++ ) {
			MYSQL_ROW fields = mysql_fetch_row( resMySql );

			vector<string> vacVal = split( fields[0], " " );
			string strId = vacVal[1];

			if ( isFilter ) {
				bool isFound = false;
				for ( unsigned int i = prevAryNum; i < vecTargetId.size(); i++ ) {
					if ( strId == vecTargetId.at(i) ) {
						isFound = true;
						prevAryNum = i + 1;
						break;
					}
				}
				if ( !isFound ) {
					continue;
				}
			}

			double fHitVal = atof( vacVal[0].c_str() );
			string strHitMz = vacVal[2];
			double fHitMz = atof( strHitMz.c_str() );

			if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
				fHitVal *= fHitVal / 10;
			}
			else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
				fHitVal *= fHitMz * fHitMz / 100;
			}
			if ( queryParam.norm == PARAM_NORM_LOG ) {
				fHitVal = log(fHitVal);
			}
			else if ( queryParam.norm == PARAM_NORM_SQRT ) {
				fHitVal = sqrt(fHitVal);
			}

			// クエリとヒットしたピークのm/z, rel.int.を格納 
			HIT_PEAK *pHitPeak = new HIT_PEAK();
			pHitPeak->qMz    = strMz;
			pHitPeak->qVal   = fVal;
			pHitPeak->hitMz  = strHitMz;
			pHitPeak->hitVal = fHitVal;
			mapHitPeak.insert( pair<string, HIT_PEAK*>(strId, pHitPeak) ); 

			char key[100];
			sprintf( key, "%s %s", strId.c_str(), strHitMz.c_str() );
			mapMzCnt[key]++;
		}
		// 結果セット解放
		mysql_free_result( resMySql );
	}
	return true;
}


/********************************************************************
 * クエリピークセット
 ********************************************************************/
void setQueryPeak()
{
	vector<string> vecVal = split( queryParam.val, "@" );
	for ( unsigned int i = 0; i < vecVal.size(); i++ ) {
		vector<string> vecPeak = split( vecVal[i], "," );
		string sMz = vecPeak[0];
		double fMz = atof( vecPeak[0].c_str() );
		double fVal = atof( vecPeak[1].c_str() );

		if ( fVal < 1 ) {
			fVal = 1;
		}
		else if ( fVal > 999 ) {
			fVal = 999;
		}
		if ( fVal < queryParam.cutoff ) {
			continue;
		}
		if ( queryParam.weight == PARAM_WEIGHT_LINEAR ) {
			fVal *= fMz / 10;
		}
		else if ( queryParam.weight == PARAM_WEIGHT_SQUARE ) {
			fVal *= fMz * fMz / 100;
		}
		if ( queryParam.norm == PARAM_NORM_LOG) {
			fVal = log(fVal);
		}
		else if ( queryParam.norm == PARAM_NORM_SQRT ) {
			fVal = sqrt(fVal);
		}

		if ( fVal > 0 ) {
			queryMz.push_back( sMz );
			queryVal.push_back( fVal );
			m_fLen += fVal * fVal;
			m_fSum += fVal;
			m_iCnt++;
		}
	}
	if ( m_iCnt - 1 < queryParam.threshold ) {
		queryParam.threshold = m_iCnt - 1;
	}
}


/********************************************************************
 * リクエストパラメータ取得
 ********************************************************************/
bool getReqParam()
{
	string strLine;
	string strKey;
	string strVal;

	// REQUEST_METHOD取得
	string strReqType
		= getenv( "REQUEST_METHOD" ) ? getenv( "REQUEST_METHOD" ): "";
	if ( strReqType.empty() ) {
		return false;
	}

	string strParam;
	// GETメソッド
	if ( strReqType == "GET" ) {
		strParam = getenv( "QUERY_STRING" ) ? getenv( "QUERY_STRING" ): "";
		if ( strParam.empty() ) {
			return false;
		}
	
	}
	// POSTメソッド
	else if ( strReqType == "POST" ) {
		string strLen
			= getenv( "CONTENT_LENGTH" )? getenv( "CONTENT_LENGTH" ): "";
		if( strLen.empty() ){
			return false;
		}
		else {
			int iLen = atoi( strLen.c_str() );
			char buf[iLen+1];
			fread( &buf, 1, iLen, stdin );
			urlDecode( buf, iLen );
			strParam = buf;
		}
	}
	else {
		return false;
	}

 	istringstream iss( strParam.c_str() );
	while ( getline( iss, strLine, '&' ) ) {
		istringstream iss2( strLine.c_str() );
		getline( iss2, strKey, '=' );
		getline( iss2, strVal );
		mapReqParam[strKey] = strVal;
 	}
	return true;
}

/********************************************************************
 * クエリパラメータセット
 ********************************************************************/
void setQueryParam()
{
	int val; 
	queryParam.start     = 1;
	queryParam.num       = 0;
	queryParam.floor     = 1;
	queryParam.celing    = 1000;
	queryParam.threshold = 3;
	queryParam.cutoff    = 20;
	queryParam.tolerance = 0.3;
	queryParam.colType   = "COSINE";
	queryParam.weight    = PARAM_WEIGHT_SQUARE;
	queryParam.norm      = PARAM_NORM_SQRT;
	queryParam.tolUnit   = "unit";

	if ( !mapReqParam["START"].empty() ) {
		val = atoi( mapReqParam["START"].c_str() );
		if ( val > 0 ) 	queryParam.start = val;
	}
	if ( !mapReqParam["NUM"].empty() ) {
		val = atoi( mapReqParam["NUM"].c_str() );
		if ( val > 0 ) 	queryParam.num = val;
	}
	if ( !mapReqParam["NUMTHRESHOLD"].empty() ) {
		queryParam.threshold = atoi( mapReqParam["NUMTHRESHOLD"].c_str() );
	}
	if ( !mapReqParam["CUTOFF"].empty() ) {
		queryParam.cutoff = atoi( mapReqParam["CUTOFF"].c_str() );
	}
	if ( !mapReqParam["TOLERANCE"].empty() ) {
		queryParam.tolerance = atof( mapReqParam["TOLERANCE"].c_str() );
	}

	if ( !mapReqParam["CORTYPE"].empty() ) {
		queryParam.colType = mapReqParam["CORTYPE"];
	}
	if ( !mapReqParam["WEIGHT"].empty() ) {
		if ( mapReqParam["WEIGHT"] == "LINEAR" ) {
			queryParam.weight = PARAM_WEIGHT_LINEAR;
		}
		else if ( mapReqParam["WEIGHT"] == "SQUARE" ) {
			queryParam.weight = PARAM_WEIGHT_SQUARE;
		}
	}
	if ( !mapReqParam["NORM"].empty() ) {
		if ( mapReqParam["NORM"] == "LOG" ) {
			queryParam.norm = PARAM_NORM_LOG;
		}
		else if ( mapReqParam["NORM"] == "SQRT" ) {
			queryParam.norm = PARAM_NORM_SQRT;
		}
	}
	if ( !mapReqParam["TOLUNIT"].empty() ) {
		queryParam.tolUnit = mapReqParam["TOLUNIT"];
	}
	if ( !mapReqParam["VAL"].empty() ) {
		queryParam.val = mapReqParam["VAL"];
	}

	if ( !mapReqParam["quick"].empty() ) {
		isQuick = true;
	}

	if ( !mapReqParam["INTEG"].empty() ) {
		string strVal = mapReqParam["INTEG"];
		if ( strVal == "true" ) {
			isInteg = true;
		}
	}

	if ( !mapReqParam["INST"].empty() ) {
		queryParam.instType = mapReqParam["INST"];
	}

	if ( mapReqParam["ION"].empty() ) {
		queryParam.ion = "0";
	}
	else {
		queryParam.ion = mapReqParam["ION"];
	}
}

/********************************************************************
 * Heapテーブル存在確認
 ********************************************************************/
bool existHeapTable( string findTblName )
{
	char sql[1000];
	sprintf( sql, "show tables like '%s'", findTblName.c_str() );
	if ( dbExecuteSql( sql ) > 0 ) {
		MYSQL_ROW fields = mysql_fetch_row( resMySql );
		string ans = fields[0];
		mysql_free_result( resMySql );

		if ( strncmpi( ans.c_str(), findTblName.c_str(), ans.length() ) == 0 ) {
			sprintf( sql, "select count(*) from %s", ans.c_str() );
			if ( dbExecuteSql( sql ) > 0 ) {
				fields = mysql_fetch_row( resMySql );
				ans = fields[0];
				mysql_free_result( resMySql );
				if ( atoi(ans.c_str()) > 0 ) {
					return true;
				} 
			}
		}
	}
	return false;
}

/********************************************************************
 * DB接続
 ********************************************************************/
bool dbConnect()
{
	// リクエストパラメータよりDSNを取得
	string dbName;
	if ( mapReqParam["dsn"].empty() ) {
		dbName = "MassBank";
	}
	else {
		dbName = mapReqParam["dsn"];
	}

	// テキストファイルよりホスト名を取得
	ifstream ifs( "DB_HOST_NAME", ios::in );
	string strDbHost;
	ifs >> strDbHost;

	// MYSQLオブジェクト初期化
	hMySql = mysql_init( NULL );

	// my.cnfよりオプション読込み(socket設定取得のため)
	mysql_options( hMySql, MYSQL_READ_DEFAULT_GROUP, "mysqld" );

	// 接続
	MYSQL *hcon = mysql_real_connect( hMySql,
				strDbHost.c_str(), DB_USER, DB_PASSWD, dbName.c_str(),
				0, NULL, 0 );
	// エラー
	if ( hcon == NULL ) {
		dbError( "DB connect" );
		return false;
	}

	// 接続ハンドルを格納
	hMySql = hcon;
	return true;
}

/********************************************************************
 * DBクエリ実行
 ********************************************************************/
long dbExecuteSql( const char *sql )
{
	// SQLクエリ実行
	if ( mysql_query( hMySql, sql ) != 0 ) {
		// エラー
		dbError( "mysql_query" );
		return -1;
	}

	// 結果取得
	resMySql = mysql_store_result( hMySql );
	if ( resMySql == NULL ) {
		mysql_free_result( resMySql );
		dbError( "mysql_store_result" );
		return -1;
	}

	return mysql_num_rows( resMySql );
}

/********************************************************************
 * DB切断
 ********************************************************************/
void dbClose()
{
	if ( hMySql ) {
		mysql_close( hMySql );
	}
}

/********************************************************************
 * DBエラー出力
 ********************************************************************/
void dbError( char *msg )
{
	printf( "[DB ERROR] %s %d %s\n",
			msg, mysql_errno(hMySql), mysql_error(hMySql) );
}

/********************************************************************
 * split関数
 ********************************************************************/
static vector<string> split( string str, string delim )
{
	vector<string> vecRes;
	size_t cutAt;
	while ( (cutAt = str.find_first_of(delim)) != str.npos ) {
		if ( cutAt > 0 ) {
			vecRes.push_back( str.substr(0, cutAt) );
		}
		str = str.substr( cutAt + 1 );
	}
	if ( str.length() > 0 ) {
		vecRes.push_back( str );
	}
	return vecRes;
}


/********************************************************************
 * URLデコード関数
 ********************************************************************/
static int urlDecode( char *s, int len )
{
	int i, j, k;
	char buf;
	char s1[ len + 1 ];

	for( i = 0, j = 0; i < len; i++, j++ ){
		//** スペース
		if ( s[i] == '+' ) {
			s1[j] = ' ';
			continue;
		}
		//** 英数字
		else if ( s[i] != '%' ) {
			s1[j] = s[i];
			continue;
		}
		//** 記号
		buf = '\0';
		for ( k = 0; k < 2; k++ ) {
			buf *= 16;
			if( s[++i] >= 'A' ) {
				buf += ( s[i] - 'A' + 10 );
			}
			else {
				buf += ( s[i] - '0' );
			}
		}
		s1[j] = buf;
	}
	// 格納
	for ( i = 0; i < j; i++ ) {
		s[i] = s1[i];
	}
	// 文字列終端
	s[i] = '\0';
	return 0;
}

/********************************************************************
 * 大文字と小文字の区別なしで文字列を比較
 ********************************************************************/
int strncmpi(const char *a1, const char *a2, unsigned size) {
	char c1, c2;
	while((size > 0) && (c1=*a1) | (c2=*a2)) {
		if (!c1 || !c2 ||
			(islower(c1) ? toupper(c1) : c1) != (islower(c2) ? toupper(c2) : c2))
			return (c1 - c2);
		a1++;
		a2++;
		size--;
	}
	return 0;
}
