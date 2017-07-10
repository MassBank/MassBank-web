/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
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
 * MassBank 環境変数管理クラス
 *
 * ver 1.0.5 2012.01.04
 *
 ******************************************************************************/
package massbank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MassBank 環境変数管理クラス
 * 
 * サーブレットの初期化で MassBank で使用する変数を初期化し、
 * MassBank 環境変数を Java、JSP、CGI などのプログラムから取得できる機能を提供する。
 * なお、Apache が起動していないと取得できない値に関してはリトライを試みる。（VAL_BASE_URL、VAL_APACHE_DOCROOT_PATH）
 * 
 * web.xml に必要なサーブレット初期パラメータは以下のとおり
 *   localUrl ... 必須（サーブレットが massbank.conf を取得したり CGI を実行するための URL）
 *   retryCnt ... 任意（Apache が起動していない場合に値の取得を試みる回数）
 *   retryTime .. 任意（Apache が起動していない場合に値の取得を試みるミリ秒間隔）
 *   
 * 各値の取得方法は以下のとおり
 *   Java、JSP
 *     MassBankEnv.get(String key)
 *     MassBankEnv.get()
 *   CGI
 *     http://[ホスト名]/MassBank/MassBankEnv?key=[キー]
 *     http://[ホスト名]/MassBank/MassBankEnv
 *     ※上記リクエストへのレスポンスとして取得する
 *     
 * ただし、VAL_BASE_URL、VAL_MASSBANK_CONF_URL、VAL_ADMIN_CONF_URL の値に関しては、
 * AdminTool の DatabaseManager で 0 番目の URL が変更された場合には更新される
 */
public class MassBankEnv extends HttpServlet {

	// MassBank 環境変数（固定値）取得キー
	public static final String KEY_LOCAL_URL			= "url.local";				// ex. "http://localhost/MassBank/"
	public static final String KEY_BASE_URL				= "url.base";				// ex. "http://[ホスト名]/MassBank/"
	public static final String KEY_SUB_URL				= "url.sub";				// ex. "MassBank/"
	public static final String KEY_SUB_PATH				= "path.sub";				// ex. "MassBank/"
	public static final String KEY_APACHE_DOCROOT_PATH	= "path.apache.root";		// ex. "/var/www/html/"
	public static final String KEY_APACHE_APPROOT_PATH	= "path.apache.app.root";	// ex. "/var/www/html/MassBank/"
	public static final String KEY_TOMCAT_DOCROOT_PATH	= "path.tomcat.root";		// ex. "/usr/local/tomcat/webapps/ROOT/"
	public static final String KEY_TOMCAT_TEMP_PATH		= "path.tomcat.temp";		// ex. "/usr/local/tomcat/temp/"
	public static final String KEY_TOMCAT_APPROOT_PATH	= "path.tomcat.app.root";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/"
	public static final String KEY_TOMCAT_APPJSP_PATH	= "path.tomcat.app.jsp";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/jsp/"
	public static final String KEY_TOMCAT_APPADMIN_PATH	= "path.tomcat.app.mbadmin";// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/mbadmin/"
	public static final String KEY_TOMCAT_APPEXT_PATH	= "path.tomcat.app.extend";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/extend/"
	public static final String KEY_TOMCAT_APPPSERV_PATH	= "path.tomcat.app.pserver";// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/pserver/"
	public static final String KEY_TOMCAT_APPTEMP_PATH	= "path.tomcat.app.temp";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/temp/"
	public static final String KEY_MASSBANK_CONF_URL	= "url.massbank_conf";		// ex. "http://[ホスト名]/MassBank/massbank.conf"
	public static final String KEY_MASSBANK_CONF_PATH	= "path.massbank_conf";		// ex. "/var/www/html/MassBank/massbank.conf"
	public static final String KEY_ADMIN_CONF_URL		= "url.admin_conf";			// ex. "http://[ホスト名]/MassBank/mbadmin/admin.conf"
	public static final String KEY_ADMIN_CONF_PATH		= "path.admin_conf";		// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/mbadmin/admin.conf"
	public static final String KEY_DATAROOT_PATH		= "path.data_root";			// ex. "/var/www/html/MassBank/DB/"
	public static final String KEY_ANNOTATION_PATH		= "path.annotation";		// ex. "/var/www/html/MassBank/DB/annotation/"
	public static final String KEY_MOLFILE_PATH			= "path.molfile";			// ex. "/var/www/html/MassBank/DB/molfile/"
	public static final String KEY_PROFILE_PATH			= "path.profile";			// ex. "/var/www/html/MassBank/DB/profile/"
	public static final String KEY_GIF_PATH				= "path.gif";				// ex. "/var/www/html/MassBank/DB/gif/"
	public static final String KEY_GIF_SMALL_PATH		= "path.gif.small";			// ex. "/var/www/html/MassBank/DB/gif_small/"
	public static final String KEY_GIF_LARGE_PATH		= "path.gif.large";			// ex. "/var/www/html/MassBank/DB/gif_large/"
	// MassBank 環境変数（可変値）取得キー
	public static final String KEY_PRIMARY_SERVER_URL	= "url.pserver";			// ex. "[サーバ監視用URL]"
	public static final String KEY_DB_HOST_NAME			= "db.host_name";			// ex. "[DBアクセス用ホスト名]"
	public static final String KEY_DB_MASTER_NAME		= "db.master_name";			// ex. "[ロードバランス用マスタDB名]"
	public static final String KEY_BATCH_SMTP			= "mail.batch.smtp";		// ex. "[BatchService用メールSMTPサーバ名]"
	public static final String KEY_BATCH_NAME			= "mail.batch.name";		// ex. "[BatchService用メール送信者名]"
	public static final String KEY_BATCH_FROM			= "mail.batch.from";		// ex. "[BatchService用メール送信アドレス]"
	public static final String KEY_DB_USER				= "db.user";
	public static final String KEY_DB_PASSWORD			= "db.password";
	
	// MassBank 環境変数（固定値）	
	private static String VAL_LOCAL_URL					= "";	// ex. "http://localhost/MassBank/"
	private static String VAL_BASE_URL					= "";	// ex. "http://[ホスト名]/MassBank/"
	private static String VAL_SUB_URL					= "";	// ex. "MassBank/"
	private static String VAL_SUB_PATH					= "";	// ex. "MassBank/"
	private static String VAL_APACHE_DOCROOT_PATH		= "";	// ex. "/var/www/html/"
	private static String VAL_APACHE_APPROOT_PATH		= "";	// ex. "/var/www/html/MassBank/"
	private static String VAL_TOMCAT_DOCROOT_PATH		= "";	// ex. "/usr/local/tomcat/webapps/ROOT/"
	private static String VAL_TOMCAT_TEMP_PATH			= "";	// ex. "/usr/local/tomcat/temp/"
	private static String VAL_TOMCAT_APPROOT_PATH		= "";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/"
	private static String VAL_TOMCAT_APPJSP_PATH		= "";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/jsp/"
	private static String VAL_TOMCAT_APPADMIN_PATH		= "";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/mbadmin/"
	private static String VAL_TOMCAT_APPEXT_PATH		= "";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/extend/"
	private static String VAL_TOMCAT_APPPSERV_PATH		= "";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/pserver/"
	private static String VAL_TOMCAT_APPTEMP_PATH		= "";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/temp/"
	private static String VAL_MASSBANK_CONF_URL			= "";	// ex. "http://[ホスト名]/MassBank/massbank.conf"
	private static String VAL_MASSBANK_CONF_PATH		= "";	// ex. "/var/www/html/MassBank/massbank.conf"
	private static String VAL_ADMIN_CONF_URL			= "";	// ex. "http://[ホスト名]/MassBank/mbadmin/admin.conf"
	private static String VAL_ADMIN_CONF_PATH			= "";	// ex. "/usr/local/tomcat/webapps/ROOT/MassBank/mbadmin/admin.conf"
	private static String VAL_DATAROOT_PATH				= "";	// ex. "/var/www/html/MassBank/DB/"
	private static String VAL_ANNOTATION_PATH			= "";	// ex. "/var/www/html/MassBank/DB/annotation/"
	private static String VAL_MOLFILE_PATH				= "";	// ex. "/var/www/html/MassBank/DB/molfile/"
	private static String VAL_PROFILE_PATH				= "";	// ex. "/var/www/html/MassBank/DB/profile/"
	private static String VAL_GIF_PATH					= "";	// ex. "/var/www/html/MassBank/DB/gif/"
	private static String VAL_GIF_SMALL_PATH			= "";	// ex. "/var/www/html/MassBank/DB/gif_small/"
	private static String VAL_GIF_LARGE_PATH			= "";	// ex. "/var/www/html/MassBank/DB/gif_large/"
	// MassBank 環境変数（可変値）
	private static String VAL_PRIMARY_SERVER_URL		= "http://www.massbank.jp/";	// ex. "[サーバ監視用URL]"
	private static String VAL_DB_HOST_NAME				= "localhost";					// ex. "[DBアクセス用ホスト名]"
	private static String VAL_DB_MASTER_NAME			= "";	// ex. "[ロードバランス用マスタDB名]"
	private static String VAL_BATCH_SMTP				= "";	// ex. "[BatchService用メールSMTPサーバ名]"
	private static String VAL_BATCH_NAME				= "";	// ex. "[BatchService用メール送信者名]"
	private static String VAL_BATCH_FROM				= "";	// ex. "[BatchService用メール送信アドレス]"
	private static String VAL_DB_USER					= "";
	private static String VAL_DB_PASSWORD				= "";
	
	// Apache 接続リトライ回数
	private static int APACHE_CONNECT_RETRY_CNT = 15;
	// Apache 接続リトライ間隔（ミリ秒）
	private static long APACHE_CONNECT_RETRY_TIME = 2000L;
	
	/**
	 * サービス初期処理を行う
	 */
	public void init() throws ServletException {
		
		// OS判定
		boolean isWindows = false;
		if ( System.getProperty("os.name").indexOf("Windows") != -1 ) {
			isWindows = true;
		}
		
		// 初期パラメータ取得
		String tmpRetryCnt = getInitParameter("retryCnt");
		if ( tmpRetryCnt != null && !tmpRetryCnt.equals("") ) {
			APACHE_CONNECT_RETRY_CNT = Integer.parseInt(tmpRetryCnt);
		}
		String tmpRetryTime = getInitParameter("retryTime");
		if ( tmpRetryTime != null && !tmpRetryTime.equals("") ) {
			APACHE_CONNECT_RETRY_TIME = Long.parseLong(tmpRetryTime);
		}		
		
		
		// VAL_LOCAL_URL
		String tmpLocalUrl = getInitParameter("localUrl");
		if ( tmpLocalUrl != null && !tmpLocalUrl.equals("") ) {
			if ( !tmpLocalUrl.endsWith("/") ) {
				tmpLocalUrl += "/";
			}
			VAL_LOCAL_URL = tmpLocalUrl;
		}
		else {
			Logger.getLogger("global").severe("\"localUrl\" is not set to web.xml.");
			Logger.getLogger("global").severe("It failed in the initialization of the MassBank environment variable.\n");
			envListLog();
			return;
		}
		
		// VAL_SUB_URL, VAL_SUB_PATH
		if ( !VAL_LOCAL_URL.equals("") ) {
			Pattern p = Pattern.compile("http:\\/\\/.*\\/(.*\\/)");
			Matcher m = p.matcher(VAL_LOCAL_URL);
			if( m.find() ) {
				VAL_SUB_URL = m.group(1);
				if ( isWindows ) {
					VAL_SUB_PATH = VAL_SUB_URL.replaceAll("/", "\\\\");
				}
				else {
					VAL_SUB_PATH = VAL_SUB_URL;
				}
			}
		}
		
		// VAL_BASE_URL
		String tmpBaseUrl = "";
		for (int cnt=0; cnt<=APACHE_CONNECT_RETRY_CNT; cnt++) {			// Apache 停止時はリトライ
			if ( cnt > 0 ) {
				Logger.getLogger("global").info("Waiting for a start of Apache... " + cnt + "/" + APACHE_CONNECT_RETRY_CNT + "\n");
				try {
					Thread.sleep(APACHE_CONNECT_RETRY_TIME);
				}
				catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			GetConfig conf = new GetConfig(VAL_LOCAL_URL);
			tmpBaseUrl = conf.getServerUrl();
			if ( !tmpBaseUrl.equals("") ) {
				break;
			}
		}
		if ( !tmpBaseUrl.equals("") ) {
			VAL_BASE_URL = tmpBaseUrl;
		}
		else {
			Logger.getLogger("global").severe("Apache doesn't start.  " + 
					"Cannot access \"" + VAL_LOCAL_URL + "massbank.conf\".");
			Logger.getLogger("global").severe("It failed in the initialization of the MassBank environment variable.\n");
			envListLog();
			return;
		}
		
		// VAL_APACHE_DOCROOT_PATH
		String tmpApacheDocrootPath = "";
		BufferedReader br = null;
		try {
			URL url = new URL(VAL_LOCAL_URL + "cgi-bin/GetDocRoot.cgi");
			URLConnection con = null;
			for (int cnt=0; cnt<=APACHE_CONNECT_RETRY_CNT; cnt++) {		// Apache 停止時はリトライ
				if ( cnt > 0 ) {
					Logger.getLogger("global").info("Waiting for a start of Apache... " + cnt + "/" + APACHE_CONNECT_RETRY_CNT + "\n");
					try {
						Thread.sleep(APACHE_CONNECT_RETRY_TIME);
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				try {
					con = url.openConnection();
					br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8") );
					break;
				}
				catch ( ConnectException ce ) {
					if ( cnt == APACHE_CONNECT_RETRY_CNT ) {
						throw ce;
					}
				}
			}
			String line = "";
			while ((line = br.readLine()) != null) {
				if ( !line.equals("") ) {
					tmpApacheDocrootPath = line;
					break;
				}
			}
			if ( !tmpApacheDocrootPath.equals("") ) {
				if ( !tmpApacheDocrootPath.endsWith("/") && !tmpApacheDocrootPath.endsWith("\\") ) {
					tmpApacheDocrootPath = tmpApacheDocrootPath + "/";
				}
				if ( isWindows ) {
					tmpApacheDocrootPath = tmpApacheDocrootPath.replaceAll("/", "\\\\");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();				
		}
		finally {
			try { if ( br != null ) { br.close(); } } catch (IOException e) {}
		}
		if ( !tmpApacheDocrootPath.equals("") ) {
			VAL_APACHE_DOCROOT_PATH = tmpApacheDocrootPath;
		}
		else {
			Logger.getLogger("global").severe("Apache doesn't start.  " + 
					"Cannot access \"" + VAL_LOCAL_URL + "cgi-bin/GetDocRoot.cgi\".");
			Logger.getLogger("global").severe("It failed in the initialization of the MassBank environment variable.\n");
			envListLog();
			return;
		}
		
		// VAL_DB_USER, VAL_DB_PASSWORD
		VAL_DB_USER		= getServletContext().getInitParameter("user");
		VAL_DB_PASSWORD	= getServletContext().getInitParameter("password");
		
		// VAL_APACHE_APPROOT_PATH
		VAL_APACHE_APPROOT_PATH = VAL_APACHE_DOCROOT_PATH + VAL_SUB_PATH;
		if ( isWindows ) {
			VAL_APACHE_APPROOT_PATH = VAL_APACHE_DOCROOT_PATH + VAL_SUB_PATH;
		}
		
		// VAL_TOMCAT_DOCROOT_PATH
		VAL_TOMCAT_DOCROOT_PATH = this.getServletContext().getRealPath("/");
		/*// re if ( VAL_TOMCAT_DOCROOT_PATH.indexOf("api") != -1 ) {
			VAL_TOMCAT_DOCROOT_PATH = VAL_TOMCAT_DOCROOT_PATH.replaceAll("webapps/api/", "webapps/ROOT/");
			if ( isWindows ) {
				VAL_TOMCAT_DOCROOT_PATH = VAL_TOMCAT_DOCROOT_PATH.replaceAll("webapps\\\\api\\\\", "webapps\\\\ROOT\\\\");
			}
			if ( !(new File(VAL_TOMCAT_DOCROOT_PATH + VAL_SUB_PATH)).exists() ) {
				VAL_TOMCAT_DOCROOT_PATH = VAL_TOMCAT_DOCROOT_PATH.substring(0, VAL_TOMCAT_DOCROOT_PATH.indexOf("ROOT"));
			}
		}*/
	
		// VAL_TOMCAT_TEMP_PATH
		VAL_TOMCAT_TEMP_PATH = VAL_TOMCAT_DOCROOT_PATH + "temp/";
		/*// reif ( !VAL_TOMCAT_DOCROOT_PATH.equals("") ) {
			VAL_TOMCAT_TEMP_PATH = VAL_TOMCAT_DOCROOT_PATH.substring(0, VAL_TOMCAT_DOCROOT_PATH.indexOf("webapps")) + "temp/";
			if ( isWindows ) {
				VAL_TOMCAT_TEMP_PATH = VAL_TOMCAT_DOCROOT_PATH.substring(0, VAL_TOMCAT_DOCROOT_PATH.indexOf("webapps")) + "temp\\";
			}
		}*/
		
		// VAL_TOMCAT_APPROOT_PATH
		if ( !VAL_TOMCAT_DOCROOT_PATH.equals("") ) {
			VAL_TOMCAT_APPROOT_PATH = VAL_TOMCAT_DOCROOT_PATH;
			if ( !VAL_TOMCAT_DOCROOT_PATH.endsWith(VAL_SUB_PATH) ) {
				VAL_TOMCAT_APPROOT_PATH = VAL_TOMCAT_DOCROOT_PATH + VAL_SUB_PATH;
			}
		}
		
		// VAL_TOMCAT_APPJSP_PATH
		if ( !VAL_TOMCAT_APPROOT_PATH.equals("") ) {
			VAL_TOMCAT_APPJSP_PATH = VAL_TOMCAT_APPROOT_PATH + "jsp/";
			if ( isWindows ) {
				VAL_TOMCAT_APPJSP_PATH = VAL_TOMCAT_APPROOT_PATH + "jsp\\";
			}
		}
		
		// VAL_TOMCAT_APPADMIN_PATH
		if ( !VAL_TOMCAT_APPROOT_PATH.equals("") ) {
			VAL_TOMCAT_APPADMIN_PATH = VAL_TOMCAT_APPROOT_PATH + "mbadmin/";
			if ( isWindows ) {
				VAL_TOMCAT_APPADMIN_PATH = VAL_TOMCAT_APPROOT_PATH + "mbadmin\\";
			}
		}
		
		// VAL_TOMCAT_APPEXT_PATH
		if ( !VAL_TOMCAT_APPROOT_PATH.equals("") ) {
			VAL_TOMCAT_APPEXT_PATH = VAL_TOMCAT_APPROOT_PATH + "extend/";
			if ( isWindows ) {
				VAL_TOMCAT_APPEXT_PATH = VAL_TOMCAT_APPROOT_PATH + "extend\\";
			}
		}
		
		// VAL_TOMCAT_APPPSERV_PATH
		if ( !VAL_TOMCAT_APPROOT_PATH.equals("") ) {
			VAL_TOMCAT_APPPSERV_PATH = VAL_TOMCAT_APPROOT_PATH + "pserver/";
			if ( isWindows ) {
				VAL_TOMCAT_APPPSERV_PATH = VAL_TOMCAT_APPROOT_PATH + "pserver\\";
			}
		}
		
		// VAL_TOMCAT_APPTEMP_PATH
		if ( !VAL_TOMCAT_APPROOT_PATH.equals("") ) {
			VAL_TOMCAT_APPTEMP_PATH = VAL_TOMCAT_APPROOT_PATH + "temp/";
			if ( isWindows ) {
				VAL_TOMCAT_APPTEMP_PATH = VAL_TOMCAT_APPROOT_PATH + "temp\\";
			}
		}
		
		// VAL_MASSBANK_CONF_URL
		if ( !VAL_BASE_URL.equals("") ) {
			VAL_MASSBANK_CONF_URL = VAL_BASE_URL + "massbank.conf";
		}
		
		// VAL_MASSBANK_CONF_PATH
		if ( !VAL_APACHE_APPROOT_PATH.equals("") ) {
			VAL_MASSBANK_CONF_PATH = VAL_APACHE_APPROOT_PATH + "massbank.conf";
		}
		
		// VAL_ADMIN_CONF_URL
		if ( !VAL_BASE_URL.equals("") ) {
			VAL_ADMIN_CONF_URL = VAL_BASE_URL + "mbadmin/admin.conf";
		}
		
		// VAL_ADMIN_CONF_PATH
		if ( !VAL_TOMCAT_APPADMIN_PATH.equals("") ) {
			VAL_ADMIN_CONF_PATH = VAL_TOMCAT_APPADMIN_PATH + "admin.conf";
			if ( isWindows ) {
				VAL_ADMIN_CONF_PATH = VAL_TOMCAT_APPADMIN_PATH + "admin.conf";
			}
		}
		
		// VAL_DATAROOT_PATH
		if ( !VAL_APACHE_APPROOT_PATH.equals("") ) {
			VAL_DATAROOT_PATH = VAL_APACHE_APPROOT_PATH + "DB/";
			if ( isWindows ) {
				VAL_DATAROOT_PATH = VAL_APACHE_APPROOT_PATH + "DB\\";
			}
		}
		
		// VAL_ANNOTATION_PATH
		if ( !VAL_DATAROOT_PATH.equals("") ) {
			VAL_ANNOTATION_PATH = VAL_DATAROOT_PATH + "annotation/";
			if ( isWindows ) {
				VAL_ANNOTATION_PATH = VAL_DATAROOT_PATH + "annotation\\";
			}
		}
		
		// VAL_MOLFILE_PATH
		if ( !VAL_DATAROOT_PATH.equals("") ) {
			VAL_MOLFILE_PATH = VAL_DATAROOT_PATH + "molfile/";
			if ( isWindows ) {
				VAL_MOLFILE_PATH = VAL_DATAROOT_PATH + "molfile\\";
			}
		}
		
		// VAL_PROFILE_PATH
		if ( !VAL_DATAROOT_PATH.equals("") ) {
			VAL_PROFILE_PATH = VAL_DATAROOT_PATH + "profile/";
			if ( isWindows ) {
				VAL_PROFILE_PATH = VAL_DATAROOT_PATH + "profile\\";
			}
		}
		
		// VAL_GIF_PATH
		if ( !VAL_DATAROOT_PATH.equals("") ) {
			VAL_GIF_PATH = VAL_DATAROOT_PATH + "gif/";
			if ( isWindows ) {
				VAL_GIF_PATH = VAL_DATAROOT_PATH + "gif\\";
			}
		}
		
		// VAL_GIF_SMALL_PATH
		if ( !VAL_DATAROOT_PATH.equals("") ) {
			VAL_GIF_SMALL_PATH = VAL_DATAROOT_PATH + "gif_small/";
			if ( isWindows ) {
				VAL_GIF_SMALL_PATH = VAL_DATAROOT_PATH + "gif_small\\";
			}
		}
		
		// VAL_GIF_LARGE_PATH
		if ( !VAL_DATAROOT_PATH.equals("") ) {
			VAL_GIF_LARGE_PATH = VAL_DATAROOT_PATH + "gif_large/";
			if ( isWindows ) {
				VAL_GIF_LARGE_PATH = VAL_DATAROOT_PATH + "gif_large\\";
			}
		}
		
		// VAL_PRIMARY_SERVER_URL
		String tmpPrimaryServerUrl = getAdminConf(VAL_ADMIN_CONF_PATH, "primary_server_url");
		if ( !tmpPrimaryServerUrl.equals("") ) {
			if ( !tmpPrimaryServerUrl.endsWith("/") ) {
				tmpPrimaryServerUrl += "/";
			}
			VAL_PRIMARY_SERVER_URL = tmpPrimaryServerUrl;
		}
		
		// VAL_DB_HOST_NAME
		String tmpDbHostName = getAdminConf(VAL_ADMIN_CONF_PATH, "db_host_name");
		if ( !tmpDbHostName.equals("") ) {
			VAL_DB_HOST_NAME = tmpDbHostName;
		}
		
		// VAL_DB_MASTER_NAME
		VAL_DB_MASTER_NAME = getAdminConf(VAL_ADMIN_CONF_PATH, "master_db");
		
		// VAL_BATCH_SMTP
		VAL_BATCH_SMTP = getAdminConf(VAL_ADMIN_CONF_PATH, "mail_batch_smtp");
		
		// VAL_BATCH_NAME
		VAL_BATCH_NAME = getAdminConf(VAL_ADMIN_CONF_PATH, "mail_batch_name");
		
		// VAL_BATCH_FROM
		VAL_BATCH_FROM = getAdminConf(VAL_ADMIN_CONF_PATH, "mail_batch_from");
		
		// ログ出力
		envListLog();
	}
	
	/**
	 * サーブレットサービス処理
	 * サーブレットを呼び出す際のリクエストパラメータに「key=[MassBank 環境変数名]」を
	 * 付加して呼び出すことで、MassBank 環境変数を取得できる
	 * @param req HttpServletRequest
	 * @param res HttpServletResponse
	 */
	public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		
		// MassBank 環境変数用キー取得
		String key = req.getParameter("key");
		if ( key == null || key.equals("") ) {
			out.println( get() );
		}
		else {
			out.println( get(key) );
		}
	}
	
	/**
	 * MassBank 環境変数取得
	 * @param key キー
	 * @return 値
	 */
	public static String get(String key) {
		
		String val = "";
		
		if ( key.equals(KEY_LOCAL_URL) ) {
			val = VAL_LOCAL_URL;
		}
		else if ( key.equals(KEY_BASE_URL) ) {
			val = VAL_BASE_URL;
		}
		else if ( key.equals(KEY_SUB_URL) ) {
			val = VAL_SUB_URL;
		}
		else if ( key.equals(KEY_SUB_PATH) ) {
			val = VAL_SUB_PATH;
		}
		else if ( key.equals(KEY_APACHE_DOCROOT_PATH) ) {
			val = VAL_APACHE_DOCROOT_PATH;
		}
		else if ( key.equals(KEY_APACHE_APPROOT_PATH) ) {
			val = VAL_APACHE_APPROOT_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_DOCROOT_PATH) ) {
			val = VAL_TOMCAT_DOCROOT_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_TEMP_PATH) ) {
			val = VAL_TOMCAT_TEMP_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_APPROOT_PATH) ) {
			val = VAL_TOMCAT_APPROOT_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_APPJSP_PATH) ) {
			val = VAL_TOMCAT_APPJSP_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_APPADMIN_PATH) ) {
			val = VAL_TOMCAT_APPADMIN_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_APPEXT_PATH) ) {
			val = VAL_TOMCAT_APPEXT_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_APPPSERV_PATH) ) {
			val = VAL_TOMCAT_APPPSERV_PATH;
		}
		else if ( key.equals(KEY_TOMCAT_APPTEMP_PATH) ) {
			val = VAL_TOMCAT_APPTEMP_PATH;
		}
		else if ( key.equals(KEY_MASSBANK_CONF_URL) ) {
			val = VAL_MASSBANK_CONF_URL;
		}
		else if ( key.equals(KEY_MASSBANK_CONF_PATH) ) {
			val = VAL_MASSBANK_CONF_PATH;
		}
		else if ( key.equals(KEY_ADMIN_CONF_URL) ) {
			val = VAL_ADMIN_CONF_URL;
		}
		else if ( key.equals(KEY_ADMIN_CONF_PATH) ) {
			val = VAL_ADMIN_CONF_PATH;
		}
		else if ( key.equals(KEY_DATAROOT_PATH) ) {
			val = VAL_DATAROOT_PATH;
		}
		else if ( key.equals(KEY_ANNOTATION_PATH) ) {
			val = VAL_ANNOTATION_PATH;
		}
		else if ( key.equals(KEY_MOLFILE_PATH) ) {
			val = VAL_MOLFILE_PATH;
		}
		else if ( key.equals(KEY_PROFILE_PATH) ) {
			val = VAL_PROFILE_PATH;
		}
		else if ( key.equals(KEY_GIF_PATH) ) {
			val = VAL_GIF_PATH;
		}
		else if ( key.equals(KEY_GIF_SMALL_PATH) ) {
			val = VAL_GIF_SMALL_PATH;
		}
		else if ( key.equals(KEY_GIF_LARGE_PATH) ) {
			val = VAL_GIF_LARGE_PATH;
		}
		else if ( key.equals(KEY_PRIMARY_SERVER_URL) ) {
			val = VAL_PRIMARY_SERVER_URL;
		}
		else if ( key.equals(KEY_DB_HOST_NAME) ) {
			val = VAL_DB_HOST_NAME;
		}
		else if ( key.equals(KEY_DB_MASTER_NAME) ) {
			val = VAL_DB_MASTER_NAME;
		}
		else if ( key.equals(KEY_BATCH_SMTP) ) {
			val = VAL_BATCH_SMTP;
		}
		else if ( key.equals(KEY_BATCH_NAME) ) {
			val = VAL_BATCH_NAME;
		}
		else if ( key.equals(KEY_BATCH_FROM) ) {
			val = VAL_BATCH_FROM;
		}
		else if ( key.equals(KEY_DB_USER) ) {
			val = VAL_DB_USER;
		}
		else if ( key.equals(KEY_DB_PASSWORD) ) {
			val = VAL_DB_PASSWORD;
		}
		else {
			val = "-1";
		}
		
		return val;
	}
	
	/**
	 * 全ての MassBank 環境変数取得
	 * [キー]=[値]の改行区切りで全ての MassBank 環境変数を取得する
	 * @param key キー
	 * @return 値
	 */
	public static String get() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(KEY_LOCAL_URL).append("=").append(VAL_LOCAL_URL).append("\n");
		sb.append(KEY_BASE_URL).append("=").append(VAL_BASE_URL).append("\n");
		sb.append(KEY_SUB_URL).append("=").append(VAL_SUB_URL).append("\n");
		sb.append(KEY_SUB_PATH).append("=").append(VAL_SUB_PATH).append("\n");
		sb.append(KEY_APACHE_DOCROOT_PATH).append("=").append(VAL_APACHE_DOCROOT_PATH).append("\n");
		sb.append(KEY_APACHE_APPROOT_PATH).append("=").append(VAL_APACHE_APPROOT_PATH).append("\n");
		sb.append(KEY_TOMCAT_DOCROOT_PATH).append("=").append(VAL_TOMCAT_DOCROOT_PATH).append("\n");
		sb.append(KEY_TOMCAT_TEMP_PATH).append("=").append(VAL_TOMCAT_TEMP_PATH).append("\n");
		sb.append(KEY_TOMCAT_APPROOT_PATH).append("=").append(VAL_TOMCAT_APPROOT_PATH).append("\n");
		sb.append(KEY_TOMCAT_APPJSP_PATH).append("=").append(VAL_TOMCAT_APPJSP_PATH).append("\n");
		sb.append(KEY_TOMCAT_APPADMIN_PATH).append("=").append(VAL_TOMCAT_APPADMIN_PATH).append("\n");
		sb.append(KEY_TOMCAT_APPEXT_PATH).append("=").append(VAL_TOMCAT_APPEXT_PATH).append("\n");
		sb.append(KEY_TOMCAT_APPPSERV_PATH).append("=").append(VAL_TOMCAT_APPPSERV_PATH).append("\n");
		sb.append(KEY_TOMCAT_APPTEMP_PATH).append("=").append(VAL_TOMCAT_APPTEMP_PATH).append("\n");
		sb.append(KEY_MASSBANK_CONF_URL).append("=").append(VAL_MASSBANK_CONF_URL).append("\n");
		sb.append(KEY_MASSBANK_CONF_PATH).append("=").append(VAL_MASSBANK_CONF_PATH).append("\n");
		sb.append(KEY_ADMIN_CONF_URL).append("=").append(VAL_ADMIN_CONF_URL).append("\n");
		sb.append(KEY_ADMIN_CONF_PATH).append("=").append(VAL_ADMIN_CONF_PATH).append("\n");
		sb.append(KEY_DATAROOT_PATH).append("=").append(VAL_DATAROOT_PATH).append("\n");
		sb.append(KEY_ANNOTATION_PATH).append("=").append(VAL_ANNOTATION_PATH).append("\n");
		sb.append(KEY_MOLFILE_PATH).append("=").append(VAL_MOLFILE_PATH).append("\n");
		sb.append(KEY_PROFILE_PATH).append("=").append(VAL_PROFILE_PATH).append("\n");
		sb.append(KEY_GIF_PATH).append("=").append(VAL_GIF_PATH).append("\n");
		sb.append(KEY_GIF_SMALL_PATH).append("=").append(VAL_GIF_SMALL_PATH).append("\n");
		sb.append(KEY_GIF_LARGE_PATH).append("=").append(VAL_GIF_LARGE_PATH).append("\n");
		sb.append(KEY_PRIMARY_SERVER_URL).append("=").append(VAL_PRIMARY_SERVER_URL).append("\n");
		sb.append(KEY_DB_HOST_NAME).append("=").append(VAL_DB_HOST_NAME).append("\n");
		sb.append(KEY_DB_MASTER_NAME).append("=").append(VAL_DB_MASTER_NAME).append("\n");
		sb.append(KEY_BATCH_SMTP).append("=").append(VAL_BATCH_SMTP).append("\n");
		sb.append(KEY_BATCH_NAME).append("=").append(VAL_BATCH_NAME).append("\n");
		sb.append(KEY_BATCH_FROM).append("=").append(VAL_BATCH_FROM).append("\n");
		
		return sb.toString();
	}
	
	/**
	 * BaseUrl の値設定
	 * AdminTool の DatabaseManager で0番目のURLが変更された場合のみ呼ばれる
	 * @param url 新しい BaseUrl
	 */
	public static void setBaseUrl(String url) {
		VAL_BASE_URL = url;
		VAL_MASSBANK_CONF_URL = VAL_BASE_URL + "massbank.conf";
		VAL_ADMIN_CONF_URL = VAL_BASE_URL + "mbadmin/admin.conf";
		
		// ログ出力
		(new MassBankEnv()).envListLog();
	}
	
	/**
	 * admin.confに定義された値を取得する
	 * あえて引数でadminConfPathを受け取る
	 * @param adminConfPath admin.confパス
	 * @param key キー名
	 * @return 値
	 */
	private String getAdminConf( String adminConfPath, String key ) {
		String val = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader( new FileReader( adminConfPath ) );
			String line = "";
			while ( ( line = br.readLine() ) != null ) {
				// "#" で始まる行はコメント行とする
				if (line.startsWith("#") || line.equals("")) {
					continue;
				}
				int pos = line.indexOf( "=" );
				if ( pos >= 0 ) {
					String keyInfo = line.substring( 0, pos );
					String valInfo = line.substring( pos + 1 );
					if ( key.equals( keyInfo ) ) {
						val = valInfo.trim();
						break;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { if ( br != null ) { br.close(); } } catch (IOException e) {}
		}
		return val;
	}
	
	/**
	 * MassBank 環境変数一覧のログ出力
	 */
	private void envListLog(){
		final String ls = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("MassBank environment variable list.").append(ls);
		sb.append("-------------------------------------------------------------------------").append(ls);
		sb.append(KEY_LOCAL_URL).append("=").append(VAL_LOCAL_URL).append(ls);
		sb.append(KEY_BASE_URL).append("=").append(VAL_BASE_URL).append(ls);
		sb.append(KEY_SUB_URL).append("=").append(VAL_SUB_URL).append(ls);
		sb.append(KEY_SUB_PATH).append("=").append(VAL_SUB_PATH).append(ls);
		sb.append(KEY_APACHE_DOCROOT_PATH).append("=").append(VAL_APACHE_DOCROOT_PATH).append(ls);
		sb.append(KEY_APACHE_APPROOT_PATH).append("=").append(VAL_APACHE_APPROOT_PATH).append(ls);
		sb.append(KEY_TOMCAT_DOCROOT_PATH).append("=").append(VAL_TOMCAT_DOCROOT_PATH).append(ls);
		sb.append(KEY_TOMCAT_TEMP_PATH).append("=").append(VAL_TOMCAT_TEMP_PATH).append(ls);
		sb.append(KEY_TOMCAT_APPROOT_PATH).append("=").append(VAL_TOMCAT_APPROOT_PATH).append(ls);
		sb.append(KEY_TOMCAT_APPJSP_PATH).append("=").append(VAL_TOMCAT_APPJSP_PATH).append(ls);
		sb.append(KEY_TOMCAT_APPADMIN_PATH).append("=").append(VAL_TOMCAT_APPADMIN_PATH).append(ls);
		sb.append(KEY_TOMCAT_APPEXT_PATH).append("=").append(VAL_TOMCAT_APPEXT_PATH).append(ls);
		sb.append(KEY_TOMCAT_APPPSERV_PATH).append("=").append(VAL_TOMCAT_APPPSERV_PATH).append(ls);
		sb.append(KEY_TOMCAT_APPTEMP_PATH).append("=").append(VAL_TOMCAT_APPTEMP_PATH).append(ls);
		sb.append(KEY_MASSBANK_CONF_URL).append("=").append(VAL_MASSBANK_CONF_URL).append(ls);
		sb.append(KEY_MASSBANK_CONF_PATH).append("=").append(VAL_MASSBANK_CONF_PATH).append(ls);
		sb.append(KEY_ADMIN_CONF_URL).append("=").append(VAL_ADMIN_CONF_URL).append(ls);
		sb.append(KEY_ADMIN_CONF_PATH).append("=").append(VAL_ADMIN_CONF_PATH).append(ls);
		sb.append(KEY_DATAROOT_PATH).append("=").append(VAL_DATAROOT_PATH).append(ls);
		sb.append(KEY_ANNOTATION_PATH).append("=").append(VAL_ANNOTATION_PATH).append(ls);
		sb.append(KEY_MOLFILE_PATH).append("=").append(VAL_MOLFILE_PATH).append(ls);
		sb.append(KEY_PROFILE_PATH).append("=").append(VAL_PROFILE_PATH).append(ls);
		sb.append(KEY_GIF_PATH).append("=").append(VAL_GIF_PATH).append(ls);
		sb.append(KEY_GIF_SMALL_PATH).append("=").append(VAL_GIF_SMALL_PATH).append(ls);
		sb.append(KEY_GIF_LARGE_PATH).append("=").append(VAL_GIF_LARGE_PATH).append(ls);
		sb.append(KEY_PRIMARY_SERVER_URL).append("=").append(VAL_PRIMARY_SERVER_URL).append(ls);
		sb.append(KEY_DB_HOST_NAME).append("=").append(VAL_DB_HOST_NAME).append(ls);
		sb.append(KEY_DB_MASTER_NAME).append("=").append(VAL_DB_MASTER_NAME).append(ls);
		sb.append(KEY_BATCH_SMTP).append("=").append(VAL_BATCH_SMTP).append(ls);
		sb.append(KEY_BATCH_NAME).append("=").append(VAL_BATCH_NAME).append(ls);
		sb.append(KEY_BATCH_FROM).append("=").append(VAL_BATCH_FROM).append(ls);
		sb.append("-------------------------------------------------------------------------");
		Logger.getLogger("global").info(sb.toString());
	}
}
