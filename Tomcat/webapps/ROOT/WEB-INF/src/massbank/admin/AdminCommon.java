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
 * Admin Tool 共通クラス
 *
 * ver 1.0.8 2010.02.10
 *
 ******************************************************************************/
package massbank.admin;

import java.io.*;

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
		String subDir = reqUrl.substring( pos1 + 1, pos2 );
		subDir = subDir.replace( "jsp", "" );
		subDir = subDir.replace( "mbadmin", "" );
		realPath = realPath.replace( subDir, "" );
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
	 * Gifルートパス取得
	 */
	public String getGifRootPath() {
		String path = getSetting( "gif_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/gif/";
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
	 * ApacheのMassBankディレクトリのパス取得
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
	 * admin.confに定義された値を取得する
	 * @param name 項目名
	 */
	private String getSetting( String key, boolean isPath ) {
		String val = "";
		String line = "";
		try {
			BufferedReader in = new BufferedReader( new FileReader( confFilePath ) );
			while ( ( line = in.readLine() ) != null ) {
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
