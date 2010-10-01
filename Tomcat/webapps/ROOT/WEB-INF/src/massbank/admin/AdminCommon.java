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
 * 管理者設定共通クラス
 *
 * ver 1.0.14 2010.09.30
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * 管理者設定共通クラス
 * 以下の機能を提供する
 * 
 *   ＜機能＞                       ＜admin.conf キー名＞
 *   DBサーバホスト名取得           db_host_name
 *   CGIヘッダ取得                  cgi_header
 *   DBルートパス取得               db_path
 *   Molfileルートパス取得          mol_path
 *   Profileルートパス取得          profile_path
 *   GIFルートパス取得              gif_path
 *   GIFSMALLルートパス取得         gif_small_path
 *   GIFLARGEルートパス取得         gif_large_path
 *   出力先パス取得                 out_path
 *   出力先パス取得                 primary_server_url
 *   MassBankディレクトリパス取得   -
 *   管理者権限フラグ取得           admin
 *   ポータルサイトフラグ取得       portal
 *   SMTPアドレス取得               mail_batch_smtp
 *   送信者名取得                   mail_batch_name
 *   Fromアドレス取得               mail_batch_from
 *   スケジュール取得               schedule
 *   
 */
public class AdminCommon {
	
	private String confFilePath = "";
	
	/**
	 * コンストラクタ
	 * @param reqUrl リクエストURL
	 * @param realPath アプリケーションパスの絶対パス
	 */
	public AdminCommon( String reqUrl, String realPath ) {
		int pos1 = reqUrl.indexOf( "/", (new String("http://")).length() );
		int pos2 = reqUrl.lastIndexOf( "/" );
		String subDir = "";
		if (pos1 + 1 < reqUrl.length()) {
			subDir = reqUrl.substring( pos1 + 1, pos2 );
			subDir = subDir.replace( "jsp", "" );
			subDir = subDir.replace( "mbadmin", "" );
			subDir = subDir.replace( "Knapsack", "" );
			subDir = subDir.replace( "extend", "" );
			if (!subDir.equals("")) {
				if (!subDir.endsWith("/")) {
					subDir += "/";
				}
				if ((new File(realPath).getName()).equals(subDir.substring(0, subDir.length()-1))) {
					subDir = "";
				}
			}
		}
		this.confFilePath = realPath + subDir + "mbadmin/admin.conf";
	}
	
	/**
	 * DBサーバホスト名取得
	 */
	public String getDbHostName() {
		String hostName = "localhost";
		String val = getSetting( "db_host_name", false );
		if ( !val.equals("") ) {
			hostName = val;
		}
		return hostName;
	}
	
	/**
	 * CGIヘッダ取得
	 */
	public String getCgiHeader() {
		String header = getSetting( "cgi_header", false );
		if ( !header.equals("") ) {
			header = "#! " + header;
		}
		return header;
	}
	
	/**
	 * DBルートパス取得
	 */
	public String getDbRootPath() {
		String path = getSetting( "db_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/annotation/";
		}
		return path;
	}
	
	/**
	 * Molfileルートパス取得
	 */
	public String getMolRootPath() {
		String path = getSetting( "mol_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/molfile/";
		}
		return path;
	}
	
	/**
	 * Profileルートパス取得
	 */
	public String getProfileRootPath() {
		String path = getSetting( "profile_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/profile/";
		}
		return path;
	}
	
	/**
	 * GIFルートパス取得
	 */
	public String getGifRootPath() {
		String path = getSetting( "gif_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/gif/";
		}
		return path;
	}
	
	/**
	 * GIFSMALLルートパス取得
	 */
	public String getGifSmallRootPath() {
		String path = getSetting( "gif_small_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/gif_small/";
		}
		return path;
	}
	
	/**
	 * GIFLARGEルートパス取得
	 */
	public String getGifLargeRootPath() {
		String path = getSetting( "gif_large_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/gif_large/";
		}
		return path;
	}
	
	/**
	 * 出力先パス取得
	 */
	public String getOutPath() {
		return getSetting( "out_path", true );
	}

	/**
	 * プライマリサーバURL取得
	 */
	public String getPServerUrl() {
		String url = getSetting( "primary_server_url", true );
		if ( url.equals("") ) {
			url = "http://www.massbank.jp/";
		}
		return url;
	}
	
	/**
	 * MassBankディレクトリパス取得
	 * ApacheのMassBankディレクトリのリアルパスを取得する
	 */
	public String getMassBankPath() {
		String path = "";
		String dbPath = getDbRootPath();
		int pos = dbPath.lastIndexOf("DB");
		if ( pos >= 0 ) {
			path = dbPath.substring(0, pos);
		}
		return path;
	}

	/**
	 * 管理者権限フラグ取得
	 */
	public boolean isAdmin() {
		boolean ret = false;
		String adminFlag = getSetting( "admin", false );
		if ( adminFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * ポータルサイトフラグ取得
	 */
	public boolean isPortal() {
		boolean ret = false;
		String adminFlag = getSetting( "portal", false );
		if ( adminFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * SMTPアドレス取得（Batch Service用）
	 */
	public String getMailSmtp() {
		return getSetting( "mail_batch_smtp", false );
	}
	
	/**
	 * 送信者名取得（Batch Service用）
	 */
	public String getMailName() {
		return getSetting( "mail_batch_name", false );
	}
	
	/**
	 * Fromアドレス取得（Batch Service用）
	 */
	public String getMailFrom() {
		return getSetting( "mail_batch_from", false );
	}
	
	/**
	 * スケジュール取得
	 */
	public ArrayList<String> getSchedule() {
		String[] tmp = getSetting( "schedule", false ).split("\t");
		ArrayList<String> vals = new ArrayList<String>();
		for (String val : tmp) {
			if ( !val.trim().equals("") ) {
				vals.add(val.trim());
			}
		}
		return vals;
	}
	
	/**
	 * admin.confに定義された値を取得する
	 * 「#」で始まる行はコメント行とする
	 * @param key キー名
	 * @param isPath 取得しようとする値がパスであるかどうか
	 */
	private String getSetting( String key, boolean isPath ) {
		String val = "";
		String line = "";
		try {
			BufferedReader in = new BufferedReader( new FileReader( confFilePath ) );
			while ( ( line = in.readLine() ) != null ) {
				if (line.startsWith("#") || line.equals("")) {
					continue;
				}
				int pos = line.indexOf( "=" );
				if ( pos >= 0 ) {
					String keyInfo = line.substring( 0, pos );
					String valInfo = line.substring( pos + 1 );
					if ( key.equals( keyInfo ) ) {
						if ( !key.equals("schedule") ) {
							val = valInfo.trim();
							break;
						}
						else {
							val += valInfo.trim() + "\t";
						}
					}
				}
			}
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		if ( isPath && !val.equals("") ) {
			// パス末尾にファイルの区切り文字なければ付加する
			char chrLast = val.charAt( val.length()-1 );
			if ( chrLast != '/' && chrLast != '\\' ) {
				val += File.separator;
			}
		}
		return val;
	}
}
