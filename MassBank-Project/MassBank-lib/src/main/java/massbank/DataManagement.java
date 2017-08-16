package massbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class DataManagement {
	public static String toMsp(ResultList resultList, String fileName){
		// global variables
		String path		= MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH);
		String baseUrl	= MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
		GetConfig conf	= new GetConfig(baseUrl);
		String[] databaseNames	= conf.getDbName();
		
		// get files
		File[] files	= new File[resultList.getResultNum()];
		for(int i = 0; i < files.length; i++){
			ResultRecord record	= resultList.getRecord(i);
			String databaseName	= databaseNames[Integer.parseInt(record.getContributor())];
			String accession	= record.getId();
			files[i]	= new File(path + databaseName + File.separator + accession + ".txt");
		}
		
		// read and process files
		StringBuilder sb_recordList	= new StringBuilder();
		for(File file : files){
			// read file
			List<String> list	= FileUtil.readFromFile(file);
			
			// process file
			final String delimiterMB	= ": ";
			final String delimiterMSP	= ": ";
			StringBuilder sb_record	= new StringBuilder();
			boolean peaks	= false;
			for(String line : list){
				if(line.equals("//"))
					continue;
				
				// peaks at end of entry
				if(peaks){
					sb_record.append(line.trim() + "\n");
					continue;
				}
				
				// disassemble tag and value
				int delimiterIndex	= line.indexOf(delimiterMB);
				if(delimiterIndex == -1){
					sb_record.append("COMMENT" + delimiterMSP + line + "\n");
					continue;
				}
				
				String tag		= line.substring(0, delimiterIndex);
				String value	= line.substring(delimiterIndex + delimiterMB.length());
				String tag2		= tag.contains("$") ? tag.substring(tag.indexOf("$") + "$".length()) : tag;
				
				// process record entries
				if(tag.equals("CH$IUPAC"))	tag2	= "INCHI";
				if(tag.equals("CH$LINK")){
					String delimiter2	= " ";
					int delimiterIndex2	= value.indexOf(delimiter2);
					if(delimiterIndex2 != -1){
						tag2	= value.substring(0, delimiterIndex2);
						value	= value.substring(delimiterIndex2 + delimiter2.length());
					}
				}
				if(tag.equals("PK$PEAK"))	peaks	= true;
				
				sb_record.append(tag2 + delimiterMSP + value + "\n");
			}
			
			// add
			sb_recordList.append(sb_record.toString() + "\n");
		}
		
		// write msp
		String tmpUrlFolder		= MassBankEnv.get(MassBankEnv.KEY_BASE_URL) + "temp";
		String tmpFileFolder	= MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPTEMP_PATH);
		String tmpUrl			= tmpUrlFolder + "/" + fileName;
		String tmpFile			= (new File(tmpFileFolder + fileName	)).getPath();
		FileUtil.writeToFile(sb_recordList.toString(),	tmpFile);
		
		return tmpUrl;
	}
	public static ResultList search(String idxtype, String srchkey, String sortKey, String sortAction, String exec) throws UnsupportedEncodingException{
		Hashtable<String, Object> reqParams	= new Hashtable<String, Object>();
		reqParams.put("idxtype",	idxtype);
		reqParams.put("srchkey",	srchkey);
		reqParams.put("sortKey",	sortKey);
		reqParams.put("sortAction",	sortAction);
		reqParams.put("exec",		exec);
		
		return search(reqParams);
	}
	public static ResultList search(Hashtable<String, Object> reqParams) throws UnsupportedEncodingException{
		// sanity checks
		if(reqParams.get("idxtype") == null)
			throw new IllegalArgumentException();
		if(reqParams.get("srchkey") == null)
			throw new IllegalArgumentException();
		if(reqParams.get("exec") == null)
			throw new IllegalArgumentException();
		
		// defaults
//		if ( reqParams.get("pageNo") == null ) {
//			reqParams.put("pageNo", "1");
//		}
		
		if ( reqParams.get("type") == null ) {
			reqParams.put("type", "rcdidx");
		}
		if ( reqParams.get("sortKey") == null ) {
			reqParams.put("sortKey", ResultList.SORT_KEY_NAME);
		}
		if ( reqParams.get("sortAction") == null ) {
			reqParams.put("sortAction", String.valueOf(ResultList.SORT_ACTION_ASC));
		}
		
		//-------------------------------------
		// リクエストパラメータ加工、及びURLパラメータ生成
		// Request parameter processing and URL parameter generation
		//-------------------------------------
		String searchParam = "";				// URLパラメータ（検索実行用）
		
		// URLパラメータ（検索実行用）生成
		// Parameter generation (for search execution)
		for ( Enumeration<String> keys = reqParams.keys(); keys.hasMoreElements(); ){
			String key = (String)keys.nextElement();
			if ( !key.equals("inst_grp") && !key.equals("inst") && !key.equals("ms") && !key.equals("inst_grp_adv") && !key.equals("inst_adv") && !key.equals("ms_adv") ) {
				// キーがInstrumentType,MSType以外の場合はStringパラメータ
				String val = (String)reqParams.get(key);
				if ( key.indexOf("site") != -1 && val.equals("-1") ) {
					continue;
				}
				else if ( !val.equals("") ) {
					searchParam += key + "=" + URLEncoder.encode(val,"utf-8") + "&";
				}
			}
			else {
				String[] vals = null;
				try {
					vals = (String[])reqParams.get(key);
				}
				catch (ClassCastException cce) {
					vals = new String[]{ (String)reqParams.get(key) };
				}
				for ( int i=0; i<vals.length; i++ ) {
					searchParam += key + "=" + URLEncoder.encode(vals[i], "utf-8") + "&";
				}
			}
		}
		searchParam = StringUtils.chop(searchParam);
		
		//-------------------------------------------
		// 設定ファイルから各種情報を取得
		// Acquire various information from setting file
		//-------------------------------------------
		String baseUrl = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
		GetConfig conf = new GetConfig(baseUrl);
		String serverUrl = conf.getServerUrl();				// サーバURL取得
		
		//-------------------------------------
		// 検索実行・結果取得
		// execute search and acquire results
		//-------------------------------------
		String typeName = "";
		ResultList list = null;
		boolean isMulti = true;
		int siteNo = -1;
		
		// ◇ RecordIndexの場合
		try { siteNo = Integer.parseInt(String.valueOf(reqParams.get( "srchkey" ))); } catch (NumberFormatException nfe) {}
		String pIdxtype = ((String)reqParams.get( "idxtype" ) != null) ? (String)reqParams.get( "idxtype" ) : "";
		if ( pIdxtype.equals("site") ) {
			isMulti = false;
			searchParam = searchParam.replaceAll( "srchkey", "site" );
		}
		typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_RCDIDX];
		
		// 検索実行
		// execute search
		MassBankCommon mbcommon = new MassBankCommon();
		if ( isMulti ) {;
			list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, true, null, conf );
		}
		else {
			list = mbcommon.execDispatcherResult( serverUrl, typeName, searchParam, false, String.valueOf(siteNo), conf );
		}
		
		return list;
	}
	
	public static final String DISPATCHER_NAME = "Dispatcher.jsp";
	public static final String MULTI_DISPATCHER_NAME = "MultiDispatcher";
	public static ResultList search(
			String serverUrl,
			String type,
			String reqParam,
			boolean isMulti,
			String siteNo,
			GetConfig conf ) {
		
		String reqUrl = "";
		int site = 0;
		if ( isMulti ) {
			reqUrl = serverUrl + MULTI_DISPATCHER_NAME;
		}
		else {
			reqUrl = serverUrl + "jsp/" + DISPATCHER_NAME;
			site = Integer.parseInt(siteNo);
		}
		
		// 結果取得
		ArrayList<String> allLine = new ArrayList<String>();
		try {
			URL url = new URL( reqUrl );
			URLConnection con = url.openConnection();
//			if ( !reqParam.equals("") ) {
//				con.setDoOutput(true);
//				PrintStream psm = new PrintStream( con.getOutputStream() );
//				psm.print( reqParam );
//			}
			
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			boolean isStartSpace = true;
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				// 先頭スペースを読み飛ばすため
				if ( isStartSpace ) {
					if ( line.equals("") ) {
						continue;
					}
					else {
						isStartSpace = false;
					}
				}
				
				if ( !line.equals("") ) {
					if ( isMulti ) {
						allLine.add(line);
					}
					else {
						allLine.add(line + "\t" + Integer.toString(site) );
					}
				}
			}
			in.close();
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		
		// 結果情報レコード生成
		// Result information record generation
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
			record.setContributor(fields[fields.length-1]);
			// ノードグループ設定
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
		
		// ソートキー取得
		String sortKey = ResultList.SORT_KEY_NAME;
		if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_FORMULA) != -1) {
			sortKey = ResultList.SORT_KEY_FORMULA;
		}
		else if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_EMASS) != -1) {
			sortKey = ResultList.SORT_KEY_EMASS;
		}
		else if (reqParam.indexOf("sortKey=" + ResultList.SORT_KEY_ID) != -1) {
			sortKey = ResultList.SORT_KEY_ID;
		}
		
		// ソートアクション取得
		int sortAction = ResultList.SORT_ACTION_ASC;
		if (reqParam.indexOf("sortAction=" + ResultList.SORT_ACTION_DESC) != -1) {
			sortAction = ResultList.SORT_ACTION_DESC;
		}
		
		// レコードソート
		list.sortList(sortKey, sortAction);
		
		
		return list;
	}
}
