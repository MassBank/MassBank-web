<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *******************************************************************************
 *
 * レコード情報ユーティリティ
 *
 * ver 1.0.15 2012.02.13
 *
 ******************************************************************************/
%>

<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.BufferedWriter" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.PrintStream" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="massbank.admin.AdminCommon" %>
<%@ page import="massbank.admin.SqlFileGenerator" %>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.Config" %>
<%!
	//** ファイル出力先ディレクトリ **
	private static final String DEF_OUT_DIR = System.getProperty("java.io.tmpdir") + File.separator;
	
	//** SQLファイル名 **
	private static final String[] SQL_FILE_NAME = {
		"RECORD.sql", "CH_NAME.sql", "CH_LINK.sql"
	};
	private static final String[] GENE_TYPE_NAME = {
		"PEAK", "RECORD", "TREE", "MERGED"
	};
	private static final int GENE_TYPE_PEAK   = 0;
	private static final int GENE_TYPE_RECORD = 1;
	private static final int GENE_TYPE_TREE   = 2;
	private static final int GENE_TYPE_PARENT = 3;
	
	private String execCgi( String strUrl, String param ) throws Exception {
		URL url = new URL( strUrl );
		URLConnection con = url.openConnection();
		con.setDoOutput(true);
		PrintStream psm = new PrintStream( con.getOutputStream() );
		psm.print( param );
		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
		String line = "";
		StringBuffer ret = new StringBuffer();
		while ( (line = in.readLine()) != null ) {
			ret.append( line + "\n" );
		}
		in.close();
		return ret.toString();
	}
%>
<%
	//---------------------------------------------
	// リクエストパラメータ取得
	//---------------------------------------------
	boolean isInput = true;
	if ( request.getParameter("mode") != null ) {
		String mode = request.getParameter("mode");
		if ( mode.equals("execute") ) {
			isInput = false;
		}
	}
	
	String[] paramFileNames = request.getParameterValues("fname");
	
	String act = "check";
	if ( request.getParameter("act") != null ) {
		act = request.getParameter("act");
	}
	String selDbName = "";
	if ( request.getParameter("db") != null ) {
		selDbName = request.getParameter("db");
	}
	String selSort = "";
	if ( request.getParameter("sort") != null ) {
		selSort = request.getParameter("sort");
	}
	int selGeneType = GENE_TYPE_PEAK;
	if ( request.getParameter("gene") != null ) {
		String gene = request.getParameter("gene");
		for ( int i = 0; i < GENE_TYPE_NAME.length; i++ ) {
			if ( gene.equals(GENE_TYPE_NAME[i]) ) {
				selGeneType = i;
				break;
			}
		}
	}
	String title = "";
	if ( act.equals("check") ) {
		title = "Validator";
	}
	else if ( act.equals("sql") ) {
		title = "SQL File Generator";
	}

%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<link rel="stylesheet" type="text/css" href="css/admin.css">
<title><% out.print(title); %></title>
<script language="javascript" type="text/javascript">
<!--
function selectAll() {
	for ( cnt = 0; cnt < document.form1.fname.options.length; cnt++ ) {
		document.form1.fname.options[cnt].selected = true;
	}
}
function changeSort() {
	document.form1.target = "_self";
	document.form1.method = "post";
	document.form1.mode.value= "";
	document.form1.submit();
}
//-->
</script>
</head>
<body>
<iframe src="menu.jsp" width="100%" height="55" frameborder="0" marginwidth="0" scrolling="no"></iframe>
<h2><% out.print(title); %></h2>
<%
	//---------------------------------------------
	// リクエストURLを取得
	//---------------------------------------------
	String reqUrl = request.getRequestURL().toString();
	String find = "mbadmin/";
	int pos1 = reqUrl.indexOf( find );
	String baseUrl = Config.get().BASE_URL();
	String jspName = reqUrl.substring( pos1 + find.length() );
	String url = "";
	
	// 設定情報取得
	GetConfig conf = new GetConfig(baseUrl);
	String [] siteLongName = conf.getSiteLongName();
	String [] dbNameList = conf.getDbName();
	String[] urlList = conf.getSiteUrl();
	String cgiUrl = urlList[GetConfig.MYSVR_INFO_NUM] + "cgi-bin/";
	
	//---------------------------------------------
	// サーバー側パス取得
	//---------------------------------------------
	AdminCommon admin = new AdminCommon();
	String dbRootPath = Config.get().DataRootPath();
	String outPath = admin.getOutPath();
	
	if ( outPath.equals("") ) {
		outPath = DEF_OUT_DIR;
	}
	
	out.println( "<b>DB Directory : </b><br>" );
	out.println( "&nbsp;&nbsp;<font color=\"DarkGoldenrod\"><b>" + dbRootPath + "</b></font>" );
	
	boolean isDirErr = false;
	File fileDbDir = new File( dbRootPath );
	if ( !fileDbDir.exists() ) {
		// DBディレクトリがない場合
		out.println( "<br><font color=\"red\" size=\"+1\"><b>Directory does not exist</b></font>");
		isDirErr = true;
	}
	
	if ( act.equals("sql") ) {
		out.println( "<br><br>" );
		out.println( "<b>Output Directory : </b><br>" );
		out.println( "&nbsp;&nbsp;<font color=\"DarkGoldenrod\"><b>" + outPath + "</b></font>" );
		out.println( "<br><br>" );
		out.println( "<b>Record Version : </b><br>" );
		out.println( "&nbsp;&nbsp;<font color=\"DarkGoldenrod\"><b>1 only</b>&nbsp;<span class=\"note\">(old record version)</span></font>" );
		File fileOutDir = new File( outPath );
		if ( !fileOutDir.exists() ) {
			// 格納先ディレクトリがない場合
			out.println( "<br><font color=\"red\" size=\"+1\"><b>Directory does not exist</b></font>");
			isDirErr = true;
		}
	}
	else if ( act.equals("check") ) {
		out.println( "<br><br>" );
		out.println( "<b>Record Version : </b><br>" );
		out.println( "&nbsp;&nbsp;<font color=\"DarkGoldenrod\"><b>1 only</b>&nbsp;<span class=\"note\">(old record version)</span></font>" );
	}
	if ( isDirErr ) {
		// エラー
		return;
	}
	else {
		out.println( "<hr size=\"1\" width=\"780\" align=\"left\">" );
		
		//-----------------------------------------------
		// DB一覧表示
		//-----------------------------------------------
		if ( isInput && act.equals("sql") ) {
			out.println( "<form name=\"form0\" action=\"" + jspName + "\" method=\"post\">" );
			out.println( "<table>" );
			out.println( " <tr>" );
			out.println( "  <td colspan=\"3\">" );
			out.println( "   <b>Type:</b>" );
			for ( int i = 0; i < GENE_TYPE_NAME.length; i++ ) {
				out.print( "   &nbsp;&nbsp;&nbsp;<input type=\"radio\" name=\"gene\" value=\""
							+ GENE_TYPE_NAME[i] + "\" onClick=\"document.form0.submit();\"" );
				if ( i == selGeneType ) {
					out.print( " checked" );
				}
				out.println( ">&nbsp;<b>" + GENE_TYPE_NAME[i] + ".sql</b>" );
			}
			out.println( "   <input type=\"hidden\" name=\"act\" value=\"" + act + "\">" );
			out.println( "   <input type=\"hidden\" name=\"db\" value=\"" + selDbName + "\">" );
			out.println( "   <input type=\"hidden\" name=\"sort\" value=\"" + selSort + "\">" );
			out.println( "  </td>" );
			out.println( " </tr>" );
			out.println( "</table>" );
			out.println( "</form>" );
		}
		
		if ( isInput ) {
			out.println( "<form name=\"form1\" action=\"" + jspName + "\" method=\"post\">" );
			out.println( "<table>" );
			out.println( " <tr valign=\"top\">" );
			out.println( "  <td width=\"200\">" );
			out.println( "   <b>Select DB:</b>" );
			
			// ファイルリスト取得
			List<String> confDbList = Arrays.asList(dbNameList);
			List tmpList = Arrays.asList(fileDbDir.list());
			Collections.sort(tmpList);
			String inpDirName[] = (String[])tmpList.toArray(new String[0]);
			StringBuffer html = new StringBuffer("");
			html.append( "   <select name=\"db\" onChange=\"changeSort();\">\n" );
			int cntDir = 0;
			for ( int i = 0; i < inpDirName.length; i++ ) {
				File file2 = new File( dbRootPath + inpDirName[i] );
				// ディレクトリが存在し、massbank.confに記述があるDBのみ有効とする
				if ( file2.isDirectory() && confDbList.contains(inpDirName[i]) ) {
					html.append( "    <option value=\"" + inpDirName[i] + "\"" );
					if ( selDbName.equals(inpDirName[i])
					 || (selDbName.equals("") && cntDir == 0) ) {
						if ( selDbName.equals("") ) {
							selDbName = inpDirName[i];
						}
						html.append( " selected" );
						cntDir++;
					}
					html.append( ">" + inpDirName[i] + "\n" );
				}
			}
			html.append( "   </select>\n" );
			html.append( "  </td>\n" );
			html.append( "  <td>\n" );
			
			if ( cntDir > 0 ) {
				out.println( html.toString() );
			}
			else {
				out.println( "   <br><font color=\"red\" size=\"+1\"><b>DB does not exist</b></font>" );
				return;
			}
		}
		else {
		}
		
		String dbPath = dbRootPath + selDbName;
		File file = new File( dbPath );
		String inpFileNames[] = file.list();
		List fileNameList = Arrays.asList( inpFileNames );
		Collections.sort( fileNameList );
		inpFileNames = (String[])fileNameList.toArray( new String[0] );
		
		//-----------------------------------------------
		// ●入力フォーム表示
		//-----------------------------------------------
		if ( isInput ) {
			if ( selGeneType == GENE_TYPE_PARENT ) {
				String strUrl = cgiUrl + "GetName.cgi";
				String param = "dsn=" + selDbName;
				String ret = execCgi( strUrl, param );
				String[] lines = ret.split("\n");
				out.println( "   <b>Select ID</b>" );
				out.println( "   &nbsp;&nbsp;&nbsp;<input type=\"button\" value=\"Select All\" onClick=\"selectAll();\"><br>" );
				out.println( "   <select name=\"fname\" multiple=\"multiple\" size=\"20\">" );
				for ( int l = 0; l < lines.length; l++ ) {
					String[] items = lines[l].split("\t");
					out.println( "    <option value=\"" + items[1] + "\">" + items[1] + "</option>" );
					out.println(items[1] + "<br>");
				}
				out.println( "   </select><br><br>" );
			}
			else if ( selGeneType != GENE_TYPE_TREE ) {
				int nameListLen = inpFileNames.length;
				// ファイルが存在しない
				if ( nameListLen == 0 ) {
					out.println( "<font color=\"red\" size=\"+1\"><b>File does not exist</b></font>");
					return;
				}
				// ファイルが存在する
				else {
					int lp = 0;
					// 更新日付取得
					SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm", Locale.US );
					String inpFileDates[] = new String[nameListLen];
					for ( lp = 0; lp < nameListLen; lp++ ) {
						String filePath = dbPath + "/"  + inpFileNames[lp];
						long lngDate = ( new File(filePath) ).lastModified();
						String strDate = sdf.format( new Date(lngDate) );
						inpFileDates[lp] = strDate;
					}
					// 日付順でソート
					if ( selSort.equals("date") ) {
						List list = new ArrayList(); 
						for ( lp = 0; lp < nameListLen; lp++ ) {
							list.add( inpFileDates[lp] + "\t" + inpFileNames[lp] );
						}
						Collections.sort( list );
						for ( lp = 0; lp < list.size(); lp++ ) {
							String val = (String)list.get(lp);
							String[] vals = val.split("\t");
							inpFileDates[lp] = vals[0];
							inpFileNames[lp] = vals[1];
						}
					}
					
					//-----------------------------------------------
					// ファイル一覧表示
					//-----------------------------------------------
					String[][] sortType = {
						{ "file", "File Name" },
						{ "date", "Date" }
					};
					out.println( "   <b>Select File</b>" );
					out.println( "   &nbsp;&nbsp;&nbsp;<input type=\"button\" value=\"Select All\" onClick=\"selectAll();\">"
									+ "&nbsp;&nbsp;&nbsp&nbsp;&nbsp;&nbsp;" );
					
					// ソートセレクトボックス表示
					out.println( "   <b>Sort : </b>" );
					out.println( "   <select name=\"sort\" onChange=\"changeSort();\">" );
					for ( int l = 0; l < sortType.length; l++ ) {
						out.print( "    <option value=\"" + sortType[l][0] + "\"" );
						if ( sortType[l][0].equals(selSort) ) {
							out.print( " selected" );
						}
						out.println( ">" + sortType[l][1] );
					}
					out.println( "   </select><br>" );
					
					// ファイルセレクトボックス表示
					out.println( "   <select name=\"fname\" multiple=\"multiple\" size=\"20\">" );
					for ( int i = 0; i < inpFileNames.length; i++ ) {
						if ( !inpFileNames[i].equals( "Copyright" ) ) {
							out.println( "    <option value=\"" + inpFileNames[i] + "\">"
										 + inpFileNames[i] + " [ " + inpFileDates[i] + " ]</option>" );
						}
					}
					out.println( "   </select><br><br>" );
				}
			}
			out.println( "   <input type=\"hidden\" name=\"act\" value=\"" + act + "\">" );
			out.println( "   <input type=\"hidden\" name=\"gene\" value=\"" + GENE_TYPE_NAME[selGeneType] + "\">" );
			out.println( "   <input type=\"hidden\" name=\"mode\" value=\"execute\">" );
			out.println( "   <input type=\"submit\" value=\"Execute\">" );
			out.println( "  </td>" );
			out.println( " </tr>" );
			out.println( "</table>" );
			out.println( "</form>" );
		}
		//-----------------------------------------------
		// ●レコード情報チェック
		//-----------------------------------------------
		else if ( act.equals("check") ) {
			out.println( "<b>Result</b>" );
			out.println( "<table bgcolor=\"Gainsboro\" border=\"0\" cellpadding=\"3\" cellspacing=\"1\">" );
			if(paramFileNames != null)
			for ( int i = 0; i < paramFileNames.length; i++ ) {
				String filePath = dbPath + "/" + paramFileNames[i];
				Validator valid = new Validator( filePath );
				String errValue = valid.getValueErr();
				String errMandaroty = valid.getMandarotyErr();
				
				//-----------------------------------------------
				// 検証結果表示
				//-----------------------------------------------
				boolean isErr = false;
				if ( errValue.length() > 0 || errMandaroty.length() > 0  ) {
					isErr = true;
				}
				
				if ( isErr ) { out.println( "<tr bgcolor=\"LightCyan\">" );   }
				else 		 { out.println( "<tr bgcolor=\"LightYellow\">" ); }
				
				// ファイル名表示
				url = "View.jsp?db=" + selDbName + "&fname=" + paramFileNames[i];
				out.println( "<td width=\"170\">" );
				out.print( paramFileNames[i] + "&nbsp;&nbsp;" );
				out.println( "<input type=\"button\" value=\"View\" onClick=\"javaScript:window.open('" + url + "');\">" );
				
				out.println( "</td>" );
				if ( isErr ) {
					// エラーがある場合
					out.println( "<td align=\"center\"><span class=\"ng\">&nbsp;NG&nbsp;</span></td>" );
					out.println( "<td width=\"600\">" );
					if ( errValue.length() > 0 ) {
						out.println( "<b>No value of items.</b><br>" + errValue + "<br>" );
					}
					if ( errMandaroty.length() > 0 ) {
						out.println( "<b>Items does not exist. </b><br>" + errMandaroty );
					}
					out.println( "</td>" );
				}
				else {
					// エラーがない場合
					out.println( "<td colspan=\"2\"><span class=\"ok\">&nbsp;OK&nbsp;</span></td>" );
				}
				
				out.println( "</tr>" );
			}
			out.println( "</table>" );
		}
		//-----------------------------------------------
		// ●SQLファイル生成
		//-----------------------------------------------
		else {
			out.println( "<br>" );
			int lp = 0;
			for ( ; lp < dbNameList.length; lp++ ) {
				if ( dbNameList[lp].equals(selDbName) ) {
					break;
				}
			}
			String siteName = siteLongName[lp];
			
			String strUrl = "";
			String param = "src_dir=" + dbPath+ "&out_dir=" + outPath + "&db=" + selDbName;
			String ret = "";
			switch (selGeneType) {
				// peak.sql生成
				case GENE_TYPE_PEAK:
					out.println( "<font color=\"blue\"><b>Generate "
					 + selDbName + "_" + GENE_TYPE_NAME[selGeneType] + ".sql</b></font><br>" );
					out.println( "<font color=\"DarkViolet\"><b>Executing...</b></font><br>" );
					StringBuffer fname = new StringBuffer("");
					// ファイル名をカンマ区切りでセット
					for ( int i = 0; i < paramFileNames.length; i++ ) {
						out.println( "&nbsp;" + paramFileNames[i] + "&nbsp" );
						fname.append( paramFileNames[i] );
						if ( i < paramFileNames.length - 1 ) {
							fname.append( ',' );
						}
					}
					out.flush();
					strUrl = cgiUrl + "GenPeakSql.cgi";
					param += "&fname=" + fname.toString();
					// CGI実行
					ret = execCgi( strUrl, param );
					break;
				// RECORD.sql生成
				case GENE_TYPE_RECORD:
					out.print( "<font color=\"blue\"><b>Generate" );
					PrintWriter[] pw = new PrintWriter[SQL_FILE_NAME.length];
					for ( int i = 0; i < SQL_FILE_NAME.length; i++ ) {
						String geneFileName = selDbName + "_" + SQL_FILE_NAME[i];
						String filePath = outPath + "/" + geneFileName;
						pw[i] = new PrintWriter( new BufferedWriter(new FileWriter(filePath)) );
						pw[i].println( "START TRANSACTION;");
						out.print( "&nbsp;&nbsp;" + geneFileName );
					}
					out.println( "</b></font><br><br>" );
					out.println( "<font color=\"DarkViolet\"><b>Executing...</b></font><br>" );
					SqlFileGenerator sfg = new SqlFileGenerator( baseUrl, selDbName, 1 );
					for ( int i = 0; i < paramFileNames.length; i++ ) {
						out.println( "&nbsp;" + paramFileNames[i] + "&nbsp" );
						String filePath = dbPath + "/" + paramFileNames[i];
						sfg.readFile( filePath );
						for ( int j = 0; j < SQL_FILE_NAME.length; j++ ) {
							if ( sfg.isExist(j) ) {
								// SQL書込み
								pw[j].println( sfg.getSql(j) );
							}
						}
					}
					for ( int i = 0; i < SQL_FILE_NAME.length; i++ ) {
						pw[i].println( "COMMIT;");
						pw[i].close();
					}
					break;
				// tree.sql生成
				case GENE_TYPE_TREE:
					out.println( "<font color=\"blue\"><b>Generate "
					 + selDbName + "_" + GENE_TYPE_NAME[selGeneType] + ".sql</b></font><br>" );
					strUrl = cgiUrl + "GenTreeSql.cgi";
					param += "&name=" + siteName;
					// CGI実行
					ret = execCgi( strUrl, param );
					break;
				case GENE_TYPE_PARENT:
					out.println( "<font color=\"blue\"><b>Generate "
					 + selDbName + "_" + GENE_TYPE_NAME[selGeneType] + ".sql</b></font><br>" );
					out.println( "<font color=\"DarkViolet\"><b>Executing...</b></font><br>" );
					StringBuffer ids = new StringBuffer("");
					// ファイル名をカンマ区切りでセット
					for ( int i = 0; i < paramFileNames.length; i++ ) {
						ids.append( paramFileNames[i] );
						if ( i < paramFileNames.length - 1 ) {
							ids.append( ',' );
						}
					}
					strUrl = cgiUrl + "GenMergeRecord.cgi";
					param += "&id=" + ids.toString();
					// CGI実行
					ret = execCgi( strUrl, param );
					break;
				default:
					break;
			}
			out.println( "<br><font color=\"DarkViolet\"><b>Done.</font></b><br>" );
			out.flush();
		}
	}
%>
</body>
</html>
