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
 * バージョン情報管理クラス
 *
 * ver 1.0.3 2008.12.19
 *
 ******************************************************************************/
package massbank.admin;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.lang.NumberUtils;
import massbank.GetConfig;
import massbank.admin.AdminCommon;
import massbank.admin.VersionInfo;
import massbank.admin.FileUtil;

public class VersionManager {

	// コンポーネント名(表示順)
	public static final String[] COMPONENT_NAMES = {
		"Applet", "Common Lib", "JSP", "CGI", "Java Script",
		"CSS", "Admin Tool"
	};
	// コンポーネントのディレクトリ(表示順)
	public static final String[] COMPONENT_DIR = {
		"applet", "WEB-INF/lib", "jsp", "cgi-bin", "script",
		"css", "mbadmin"
	};

	// コンポーネントのバージョン格納順
	private static final int COMPONENT_APPLET    = 0;
	private static final int COMPONENT_LIB       = 1;
	private static final int COMPONENT_JSP       = 2;
	private static final int COMPONENT_CGI       = 3;
	private static final int COMPONENT_SCRIPT    = 4;
	private static final int COMPONENT_CSS       = 5;
	private static final int COMPONENT_ADMIN     = 6;

	// JavaScript, CSSファイルのディレクトリ
	private static final String OTHER_DIR[] = {
		"script", "css"
	};
	// JavaScript, CSSファイルの拡張子
	private static final String OTHER_EXTENSION[] = {
		".js", ".css"
	};
	// JavaScript, CSSファイルの格納順
	private static final int OTHER_ARRAY_NUM[] = {
		COMPONENT_SCRIPT, COMPONENT_CSS
	};
	// Admin Toolのディレクトリ
	private static final String ADMIN_DIR[] = {
		"", "css/"
	};
	// Admin Toolの拡張子
	private static final String ADMIN_EXTENSION_REGEX[] = {
		".*\\.(jsp|html)$", ".*\\.css$"
	};
	// 更新除外ファイル
	private static final String EXCLUSION_LIST[][] = {
		{ "JSP", "index.jsp" },
		{ "JSP", "BatchSearch.jsp" },
		{ "CGI", "BatchSender.cgi" }
	};
	// Common Lib 更新除外ファイル
	private static final String EXCLUSION_COM_LIB[] = {
		"catalina-root.jar"
	};
	// アーカイブ名
	private static String ARCHIVE_NAME = "update";

	// JSP名
	private String jspName = "";
	// ベースURL
	private String baseUrl = "";
	// ApacheのMassBanパス
	private String massBankPath = "";
	// TomcatのROOTパス
	private String webRootPath = "";
	// TomcatのMassBankパス
	private String tomcatMbPath = "";
	// CGIヘッダー
	private String cgiHeader = "";
	// プライマリサーバURL
	private String priServerUrl = "";
	// 自サーバURL
	private String myServerUrl = "";
	// バージョン情報格納配列
	private List<VersionInfo>[] verInfoMyServer = null;
	private List<VersionInfo>[] verInfoPriServer = null;

	// 更新対象ファイルのカウント
	private int oldCnt = 0;
	private int addCnt = 0;
	private int newCnt = 0;
	private int delCnt = 0;

	/**
	 * コンストラクタ
	 */
	public VersionManager(String reqUrl, String realPath) {
		int pos1 = reqUrl.indexOf("/jsp");
		// JSPファイル名をセット
		this.jspName = reqUrl.substring( reqUrl.lastIndexOf("/") + 1 );
		// ベースURLセット
		this.baseUrl = reqUrl.substring( 0, pos1 + 1 );

		// 設定ファイル読込み
		GetConfig conf = new GetConfig(baseUrl);
		// URLリスト取得
		String[] urlList = conf.getSiteUrl();
		this.myServerUrl = urlList[GetConfig.MYSVR_INFO_NUM];

		AdminCommon admin = new AdminCommon(reqUrl, realPath);
		// プライマリサーバURL取得
		this.priServerUrl = admin.getPServerUrl();
		// ApacheのMassBanパス取得
		this.massBankPath = admin.getMassBankPath();
		// CGIヘッダー取得
		this.cgiHeader = admin.getCgiHeader();

		// Tomcatパスをセット
		String tomcatPath = System.getProperty("catalina.home");
		this.webRootPath = tomcatPath + "/webapps/ROOT/";
		String path = this.webRootPath;
		int pos2 = baseUrl.lastIndexOf("MassBank");
		if ( pos2 >= 0 ) {
			path += baseUrl.substring(pos2);
		}
		this.tomcatMbPath = path;
	}


	/**
	 * アップデート処理を実行する
	 * @return true:正常 / false:異常
	 */
	public boolean doUpdate(List copyFiles, List removeFiles) {
		// プライマリサーバに対しアーカイブ作成を指示する
		if ( !reqMakeArchive() ) {
			return false;
		}
		boolean isOK = false;
		
		// プライマリサーバからアーカイブをダウンロードする
		String archiveName = ARCHIVE_NAME + ".tgz";
		String srcUrl = priServerUrl + "temp/" + archiveName;
		String archivePath = tomcatMbPath + archiveName;
		FileUtil.downloadFile(srcUrl, archivePath);

		// アーカイブを解凍する
		boolean isPh1 = FileUtil.uncompress(archivePath, tomcatMbPath);

		// 更新対象ファイルをコピーする
		boolean isPh2 = true;
		for ( int i = 0; i < copyFiles.size(); i++ ) {
			String relativePath = (String)copyFiles.get(i);
			String srcPath = tomcatMbPath + ARCHIVE_NAME + "/" + relativePath;
			String destPath = getAbsolutePath(relativePath) + relativePath;
			if ( isOverwriteHeader(relativePath) ) {
				overwriteHeader(srcPath);
			}
			if ( !FileUtil.copyFile( srcPath, destPath ) ) {
				isPh2 = false;
				break;
			}
		}

		// 更新対象ファイルを削除する
		boolean isPh3 = true;
		if ( isPh2 ) {
			for ( int i = 0; i < removeFiles.size(); i++ ) {
				String relativePath = (String)removeFiles.get(i);
				String filePath = getAbsolutePath(relativePath) + relativePath;
				isOK = FileUtil.removeFile(filePath);
				if ( !isOK ) {
					isPh3 = false;
					break;
				}
			}
		}
		else {
			isPh3 = false;
		}

		// アーカイブ削除
		FileUtil.removeDir(tomcatMbPath + "update");
		FileUtil.removeFile(archivePath);
		if ( isPh3 ) {
			isOK = true;
		}
		return isOK;
	}

	/**
	 * アーカイブ作成処理を実行する
	 * @return true:成功 / false:失敗
	 */
	public boolean doArchive() {
		String shellPath = "/MassBank/script/archiver.sh";
		return FileUtil.executeShell(shellPath);
	}

	/**
	 * バージョンチェック処理を実行する
	 * @param verInfoMyServer  自サーバのバージョン情報リスト
	 * @return true:正常 / false:異常(プライマリサーバに接続できない場合)
	 */
	public boolean doCheckVersion(List<VersionInfo>[] verInfoMyServer) {
		// 自サーバのバージョン情報を設定する
		this.verInfoMyServer = verInfoMyServer;
		// プライマリサーバのバージョン情報を取得
		this.verInfoPriServer = getVerPriServer();
		if ( verInfoPriServer == null ) {
			return false;
		}
		// バージョン情報ステータスを設定
		setVerStaus();
		return true;
	}

	/**
	 * プライマリサーバのバージョン情報取得処理を実行する
	 * @param verInfoServer サーバのバージョン情報リスト
	 * @return プライマリサーバのバージョン情報（文字列）
	 */
	public String doGetVerPServer(List<VersionInfo>[] verInfoServer) {
		StringBuffer res = new StringBuffer();
		// バージョン情報をタブ区切りの形式で返す
		for ( int i = 0; i < COMPONENT_NAMES.length; i++ ) {
			for ( int j = 0; j < verInfoServer[i].size(); j++ ) {
				VersionInfo verInfo = (VersionInfo)verInfoServer[i].get(j);
				res.append(
					COMPONENT_NAMES[i] + "\t" + verInfo.getName() + "\t"
					+ verInfo.getVersion() + "\t" + verInfo.getDate() + "\n" );
			}
		}
		return res.toString();
	}

	/**
	 * 自サーバのバージョン情報リストを取得する
	 * @return 自サーバのバージョン情報リスト
	 */
	public List<VersionInfo>[] getVerMyServer() {
		List<VersionInfo>[] verInfoMyServer = new ArrayList[COMPONENT_NAMES.length];

		// Appletバージョン取得
		verInfoMyServer[COMPONENT_APPLET] = getVerApplet();

		// Common Libバージョン取得
		verInfoMyServer[COMPONENT_LIB] = getVerComLib();

		// JSPバージョン取得
		String jspPath = tomcatMbPath + COMPONENT_DIR[COMPONENT_JSP] + "/";
		String extension = "jsp";
		verInfoMyServer[COMPONENT_JSP] = getVerOther( jspPath, extension );

		// CGIバージョン取得
		verInfoMyServer[COMPONENT_CGI] = getVerCgi();

		// CSS, Scriptバージョン取得
		for ( int i = 0; i < OTHER_DIR.length; i++ ) {
			String otherPath = massBankPath + OTHER_DIR[i] + "/";
			verInfoMyServer[OTHER_ARRAY_NUM[i]] = getVerOther( otherPath, OTHER_EXTENSION[i] );
		}

		// Admin Tool バージョン取得
		verInfoMyServer[COMPONENT_ADMIN] = getVerAdminTool();

		return verInfoMyServer;
	}

	/**
	 * 更新有無を判定する
	 * @return true:更新あり / false:更新なし
	 */
	public boolean isUpdate() {
		if ( oldCnt + addCnt + newCnt + delCnt == 0 ) {
			return false;
		}
		return true;
	}

	/**
	 * ステータス"OLD"のカウントを取得
	 */
	public int getOldCnt() {
		return oldCnt;
	}
	/**
	 * ステータス"ADD"のカウントを取得
	 */
	public int getAddCnt() {
		return addCnt;
	}
	/**
	 * ステータス"NEW"のカウントを取得
	 */
	public int getNewCnt() {
		return newCnt;
	}
	/**
	 * ステータス"DEL"のカウントを取得
	 */
	public int getDelCnt() {
		return delCnt;
	}

	/**
	 * プライマリサーバに対しアーカイブ作成を指示する
	 * @return true:正常 / false:異常
	 */
	private boolean reqMakeArchive() {
		String res = "";
		try {
			URL url = new URL( priServerUrl + "jsp/" + jspName + "?act=archive" );
			URLConnection con = url.openConnection();
			if ( con == null ) {
				return false;
			}
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				if ( !line.trim().equals("") ) {
					res += line.trim();
				}
			}
		}
		catch ( Exception e ) {
			return false;
		}
		if ( !res.equals("OK") ) {
			return false;
		}
		return true;
	}

	/**
	 * プライマリサーバのバージョン情報を取得する
	 * @return プライマリサーバのバージョン情報リスト
	 *         (プライマリサーバに接続できない場合はnull)
	 */
	private List<VersionInfo>[] getVerPriServer() {
		List<VersionInfo>[] verInfoPriServer = new ArrayList[COMPONENT_NAMES.length];
		for ( int i = 0; i < COMPONENT_NAMES.length; i++ ) {
			verInfoPriServer[i] = new ArrayList();
		}
		String strUrl = priServerUrl + "jsp/" + jspName + "?act=get";
		try {
			URL url = new URL( strUrl );
			URLConnection con = url.openConnection();
			if ( con == null ) {
				return null;
			}
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				String data = line.trim();
				if ( data.equals("") ) {
					continue;
				}
				String[] items = data.trim().split("\t");
				String compoName = items[0];
				String name      = items[1];
				String ver       = items[2];
				String date      = items[3];

				// 除外ファイルはリストに追加しない
				boolean isExclusion = false;
				for ( int j = 0; j < EXCLUSION_LIST.length; j++ ) {
					if ( compoName.equals(EXCLUSION_LIST[j][0])
					  && name.equals(EXCLUSION_LIST[j][1]) ) {
						isExclusion = true;
						break;
					}
				}
				if ( isExclusion ) {
					continue;
				}
				// バージョン情報格納
				for ( int j = 0; j < COMPONENT_NAMES.length; j++ ) {
					VersionInfo verInfo = new VersionInfo(name, ver, date);
					if ( compoName.equals(COMPONENT_NAMES[j]) ) {
						verInfoPriServer[j].add(verInfo);
						break;
					}
				}
			}
			in.close();
		}
		catch ( Exception e ) {
			return null;
		}
		return verInfoPriServer;
	}

	/**
	 * バージョン情報のステータスを設定する
	 */
	private void setVerStaus() {
		int oldCnt = 0;
		int addCnt = 0;
		int newCnt = 0;
		int delCnt = 0;

		for ( int i = 0; i < COMPONENT_NAMES.length; i++ ) {
			//-------------------------------------------------------------
			// 自サーバ側のファイルがプライマリサーバに存在するかチェック
			//-------------------------------------------------------------
			for ( int l1 = 0; l1 < verInfoMyServer[i].size(); l1++ ) {
				boolean isFound = false;
				VersionInfo verInfo1 = (VersionInfo)verInfoMyServer[i].get(l1);
				VersionInfo verInfo2 = null;
				// 自サーバ側のファイル名
				String name1 = verInfo1.getName();

				// 除外ファイルはチェック対象外にする
				boolean isExclusion = false;
				for ( int j = 0; j < EXCLUSION_LIST.length; j++ ) {
					if ( COMPONENT_NAMES[i].equals(EXCLUSION_LIST[j][0] )
					  && name1.equals(EXCLUSION_LIST[j][1]) ) {
						isExclusion = true;
						break;
					}
				}
				if ( isExclusion ) {
					continue;
				}
				// 存在チェック
				for ( int l2 = 0; l2 < verInfoPriServer[i].size(); l2++ ) {
					verInfo2 = (VersionInfo)verInfoPriServer[i].get(l2);
					String name2 = verInfo2.getName();
					if ( name2.equals(name1) ) {
						isFound = true;
						break;
					}
				}

				//== ファイルが見つかった場合 ==
				if ( isFound ) {
					// バージョン情報の日付
					String date1 = verInfo1.getDate();
					String date2 = verInfo2.getDate();
					// 日付がないものは0詰めにする
					if ( date1.equals("-") ) {
						date1 = "00.00.00";
					}
					if ( date2.equals("-") ) {
						date2 = "00.00.00";
					}
					// ピリオド区切りで日付を分割
					String[] vals1 = date1.split("\\.");
					String[] vals2 = date2.split("\\.");

					// 年月日をピリオドなしで連結
					String convDate1 = "";
					String convDate2 = "";

					for ( int k = 0; k < vals1.length; k++ ) {
						// 年が2桁以上の場合は2桁にする

						if ( k == 0 ) {
							vals1[k] = vals1[k].substring(vals1[k].length() - 2);
							vals2[k] = vals2[k].substring(vals2[k].length() - 2);
						}
						// 年月日1桁の場合は、0詰め2桁にする
						convDate1 += "00".substring(vals1[k].length()) + vals1[k];
						convDate2 += "00".substring(vals2[k].length()) + vals2[k];
					}
					int iDate1 = Integer.parseInt(convDate1);
					int iDate2 = Integer.parseInt(convDate2);
					int status = 0;
					if ( iDate1 < iDate2 ) {
						//** ステータスセット「古い」
						status = VersionInfo.STATUS_OLD;
						oldCnt++;
					}
					else if ( iDate1 > iDate2 ) {
						//** ステータスセット「新しい」
						status = VersionInfo.STATUS_NEW;
						newCnt++;
					}
					else {
						status = VersionInfo.STATUS_NON;
					}
					verInfo1.setStatus(status);
				}
				//== ファイルが見つからない場合 ==  自サーバのみ存在
				else {
					//** ステータスセット「削除」
					verInfo1.setStatus(VersionInfo.STATUS_DEL);
					delCnt++;
				}
			}
			//-------------------------------------------------------------
			// プライマリサーバ側にのみ存在するファイルをチェック
			//-------------------------------------------------------------
			for ( int l1 = 0; l1 < verInfoPriServer[i].size(); l1++ ) {
				VersionInfo verInfo1 = (VersionInfo)verInfoPriServer[i].get(l1);
				VersionInfo verInfo2 = null;
				// プライマリサーバ側のファイル名
				String name1 = verInfo1.getName();
				boolean isFound = false;

				// 存在チェック
				for ( int l2 = 0; l2 < verInfoMyServer[i].size(); l2++ ) {
					verInfo2 = (VersionInfo)verInfoMyServer[i].get(l2);
					String name2 = verInfo2.getName();
					if ( name2.equals(name1) ) {
						isFound = true;
						break;
					}
				}
				if ( !isFound ) {
					VersionInfo verInfo3 = new VersionInfo( name1, "-", "-" );
					verInfoMyServer[i].add(verInfo3);
					//** ステータスセット「追加」
					verInfo3.setStatus(VersionInfo.STATUS_ADD);
					addCnt++;
				}
			}
		}
		this.oldCnt = oldCnt;
		this.addCnt = addCnt;
		this.newCnt = newCnt;
		this.delCnt = delCnt;
	}

	/**
	 * Appletのバージョン情報を取得する
	 * @return バージョン情報
	 */
	private List<VersionInfo> getVerApplet() {
		List verInfoList = new ArrayList<VersionInfo>();
	
		// appletディレクトリのファイルリスト取得
		String appletPath = massBankPath + "applet/";
		File file = new File( appletPath );
		String allList[] = file.list();
		ArrayList<String> targetList = new ArrayList();
		for ( int i = 0; i < allList.length; i++ ) {
			if ( allList[i].indexOf(".jar") >= 0 ) {
				targetList.add( allList[i] );
			}
		}
		// ファイル名でソート
		Collections.sort(targetList);
		for ( int i = 0; i < targetList.size(); i++ ) {
			String fileName = targetList.get(i);
			VersionInfo verInfo = getVerJarFile( appletPath + fileName );
			// バージョン情報格納
			verInfo.setName(fileName);
			verInfoList.add(verInfo);
		}
		return verInfoList;
	}

	/**
	 * Common Libのバージョン情報を取得する
	 * @return バージョン情報
	 */
	private List<VersionInfo> getVerComLib() {
		List verInfoList = new ArrayList<VersionInfo>();

		// WEB-INF/libディレクトリのファイルリスト取得
		String libPath = webRootPath + "WEB-INF/lib/";
		File file = new File(libPath);
		String allList[] = file.list();
		ArrayList<String> targetList = new ArrayList();
		for ( int i = 0; i < allList.length; i++ ) {
			boolean isExclusion = false;
			for ( int j = 0; j < EXCLUSION_COM_LIB.length; j++ ) {
				if ( allList[i].equals(EXCLUSION_COM_LIB[j]) ) {
					isExclusion = true;
					break;
				}
			}
			if ( !isExclusion && allList[i].indexOf(".jar") >= 0 ) {
				targetList.add( allList[i] );
			}
		}
		// ファイル名でソート
		Collections.sort( targetList );
		for ( int i = 0; i < targetList.size(); i++ ) {
			String fileName = targetList.get(i);
			VersionInfo verInfo = getVerJarFile( libPath + fileName );
			// バージョン情報格納
			verInfo.setName(fileName);
			verInfoList.add(verInfo);
		}
		return verInfoList;
	}

	/**
	 * CGIのバージョン情報を取得する
	 * @return バージョン情報
	 */
	private List<VersionInfo> getVerCgi() {
		List verInfoList = new ArrayList<VersionInfo>();

		// バージョン情報取得CGIを実行
		String strUrl = myServerUrl + "/cgi-bin/GetVersion.cgi";
		try {
			URL url = new URL(strUrl);
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				String data = line.trim();
				if ( data.equals("") ) {
					continue;
				}
				String[] info = data.split("\t");
		
				// スペース区切りのバージョンと日付を取り出す
				String name = info[0];
				String[] item = info[1].trim().split(" ");
				String ver  = item[0];
				String date = "-";
				for ( int j = 1; j < item.length; j++ ) {
					if ( !item[j].equals("") ) {
						date = item[j];
						break;
					}
				}
				// バージョン情報格納
				verInfoList.add( new VersionInfo(name, ver, date) );
			}
			in.close();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return verInfoList;
	}

	/**
	 * Admin Toolのバージョン情報を取得する
	 * @return バージョン情報
	 */
	private List<VersionInfo> getVerAdminTool() {
		List verInfoList = new ArrayList<VersionInfo>();

		// ファイルリスト取得
		for ( int i = 0; i < ADMIN_DIR.length; i++ ) {
			String path = tomcatMbPath + "mbadmin/" + ADMIN_DIR[i];
			File file = new File(path);
			String allList[] = file.list();
			List<String> targetList = new ArrayList();
			for ( int j = 0; j < allList.length; j++ ) {
				if ( allList[j].matches(ADMIN_EXTENSION_REGEX[i]) ) {
					targetList.add(allList[j]);
				}
			}

			// ファイル名でソート
			Collections.sort( targetList );
			for ( int j = 0; j < targetList.size(); j++ ) {
				String filName = targetList.get(j);
				File file2 = new File( path + filName );
				// ディレクトリは無視する
				if ( file2.isDirectory() ) {
					continue;
				}
				// テキストファイルのバージョン取得
				VersionInfo verInfo = getVerTextFile( path + filName );
				// バージョン情報格納
				verInfo.setName( ADMIN_DIR[i] + filName );
				verInfoList.add(verInfo);
			}
		}
		return verInfoList;
	}

	/**
	 * その他(JSP,CSS, Script)のバージョン情報を取得する
	 * @return バージョン情報
	 */
	private List<VersionInfo> getVerOther(String path, String extension) {
		List verInfoList = new ArrayList<VersionInfo>();

		// ファイルリスト取得
		File file1 = new File(path);
		String allList[] = file1.list();
		List<String> targetList = new ArrayList();
		for ( int i = 0; i < allList.length; i++ ) {
			if ( allList[i].indexOf(extension) >= 0 ) {
				targetList.add(allList[i]);
			}
		}

		// ファイル名でソート
		Collections.sort(targetList);
		for ( int i = 0; i < targetList.size(); i++ ) {
			String fileName = targetList.get(i);
			File file2 = new File( path + fileName );
			// ディレクトリは無視
			if ( file2.isDirectory() ) {
				continue;
			}
			// テキストファイルのバージョン取得
			VersionInfo verInfo = getVerTextFile( path + fileName );
			// バージョン情報格納
			verInfo.setName(fileName);
			verInfoList.add(verInfo);
		}
		return verInfoList;
	}

	/**
	 * jarファイルのバージョン情報を取得する
	 * @param path ファイルのパス
	 * @return バージョン情報
	 */
	private VersionInfo getVerJarFile(String path) {
		VersionInfo verInfo = null;
		try {
			// マニュフェストよりバージョン情報取得
			JarFile jar = new JarFile( new File(path) );
			Manifest manifest = jar.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			String item1 = attributes.getValue("Implementation-Version");
			if ( item1 == null ) {
				item1 = "-";
			}
			// スペース区切りのバージョンと日付を取り出す
			String[] item2 = item1.trim().split(" ");
			String ver  = item2[0];
			String date = "-";
			if ( item2.length > 1 ) {
				date = item2[1];
			}
			verInfo = new VersionInfo(null, ver, date);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return verInfo;
	}


	/*
	 * テキストファイルのバージョン情報を取得する
	 * @param path ファイルのパス
	 * @return バージョン情報
	 */
	private VersionInfo getVerTextFile(String path) {
		VersionInfo verInfo = null;
		try {
			String info = "-";
			final String FIND_STR[] = { "//**", "**/" };
			BufferedReader in = new BufferedReader( new FileReader(path) );
			boolean isFound = false;
			boolean isEnd = false;
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				if ( isFound ) {
					// ヘッダー部分コメント開始行を検索
					for ( int i = 0; i < FIND_STR.length; i++ ) {
						int pos1 = line.indexOf(FIND_STR[i]);
						if ( pos1 >= 0 ) {
							isEnd = true;
							break;
						}
					}
					if ( isEnd ) {
						break;
					}
				}
				// バージョン情報記述部分を検索
				String find2 = "ver";
				int pos2 = line.indexOf(find2);
				if ( pos2 >= 0 ) {
					info = line.substring( pos2 + find2.length() + 1 ).trim();
					isFound = true;
				}
			}
			in.close();

			// スペース区切りのバージョンと日付を取り出す
			String[] item = info.split(" ");
			// バージョンセット
			String ver = "-";
			String[] vals = null;
			for ( int j = 0; j < item.length; j++ ) {
				if ( !item[j].equals("") ) {
					vals = item[j].split("\\.");
					if ( NumberUtils.isNumber(vals[0])
					  && vals[0].length() < 4 ) {
						ver = item[0];
						break;
					}
				}
			}

			// 日付セット
			String date = "-";
			for ( int j = 1; j < item.length; j++ ) {
				// スペースは読み飛ばす
				if ( !item[j].equals("") ) {
					vals = item[j].split("\\.");
					if ( NumberUtils.isNumber(vals[0])
					  && Integer.parseInt(vals[0]) > 2000 ) {
						date = item[j];
						break;
					}
				}
			}
			verInfo = new VersionInfo(null, ver, date);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return verInfo;
	}

	/**
	 * ファイルの絶対パスを取得する
	 * @param relativePath ファイルの相対パス
	 * @return ファイルの絶対パス
	 */
	private String getAbsolutePath(String relativePath) {
		String absPath = "";
		int pos = relativePath.lastIndexOf("/");
		String dir = relativePath.substring( 0, pos );
		if ( dir.equals("jsp") || dir.equals("mbadmin") || dir.equals("mbadmin/css") ) {
			// TomcatのMassBankパスをセット
			absPath = tomcatMbPath;
		}
		else if ( dir.equals("WEB-INF/lib") ) {
			// Tomcatのアプリケーションルートパスをセット
			absPath = webRootPath;
		}
		else {
			// ApacheのMassBankパスをセット
			absPath = massBankPath;
		}
		return absPath;
	}

	/**
	 * CGIヘッダーの書き換え要否を判定する
	 * @param relativePath ファイルの相対パス
	 * @return true:要 / false:否
	 */
	private boolean isOverwriteHeader(String relativePath) {
		if ( cgiHeader.equals("") ) {
			return false;
		}
		int pos = relativePath.lastIndexOf("/");
		String dir = relativePath.substring( 0, pos );
		if ( dir.equals("cgi-bin") ) {
			return true;
		}
		return false;
	}

	/**
	 * CGIヘッダーを書き換える
	 * @param absPath ファイルのパス
	 */
	private void overwriteHeader(String absPath) {
		try {
			// ファイル読込み
			InputStreamReader reader = new InputStreamReader(new FileInputStream(absPath), "UTF-8");
			BufferedReader br= new BufferedReader( reader );
			StringBuffer text = new StringBuffer("");
			String line = "";
			while ( ( line = br.readLine() ) != null ) {
				int pos = line.indexOf( "#!" );
				if ( pos >= 0 ) {
					// ヘッダー部書き換え
					text.append( cgiHeader + "\n" );
				}
				else {
					text.append( line + "\n" );
				}
			}
			br.close();

			// ファイル書込み
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(absPath), "UTF-8");
			BufferedWriter bw = new BufferedWriter( writer );
			bw.write( text.toString() );
			bw.flush();
			bw.close();			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
