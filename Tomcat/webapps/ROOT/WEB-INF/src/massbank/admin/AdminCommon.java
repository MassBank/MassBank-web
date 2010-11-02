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
 * ver 1.0.15 2010.11.01
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import massbank.MassBankEnv;

/**
 * 管理者設定共通クラス
 * 以下の機能を提供する
 * 
 *   ＜機能＞                                 ＜取得先＞
 *   DBサーバホスト名取得（非推奨）           MassBankEnv
 *   CGIヘッダ取得                            admin.conf(cgi_header)
 *   Annotationルートパス取得（非推奨）       MassBankEnv
 *   Molfileルートパス取得（非推奨）          MassBankEnv
 *   Profileルートパス取得（非推奨）          MassBankEnv
 *   GIFルートパス取得（非推奨）              MassBankEnv
 *   GIFSMALLルートパス取得（非推奨）         MassBankEnv
 *   GIFLARGEルートパス取得（非推奨）         MassBankEnv
 *   出力先パス取得                           admin.conf(out_path)
 *   出力先パス取得（非推奨）                 MassBankEnv
 *   MassBankディレクトリパス取得（非推奨）   MassBankEnv
 *   管理者権限フラグ取得                     admin.conf(admin)
 *   ポータルサイトフラグ取得                 admin.conf(portal)
 *   SMTPアドレス取得（非推奨）               MassBankEnv
 *   送信者名取得（非推奨）                   MassBankEnv
 *   Fromアドレス取得（非推奨）               MassBankEnv
 *   スケジュール取得                         admin.conf(schedule)
 *   
 *   ※非推奨の昨日に関してはMassBankEnv#get(String)の使用を推奨する
 *   
 */
public class AdminCommon {
	
	/**
	 * デフォルトコンストラクタ
	 */
	public AdminCommon() {
	}
	
	/**
	 * コンストラクタ
	 * @param reqUrl リクエストURL
	 * @param realPath アプリケーションパスの絶対パス
	 * @deprecated 非推奨コンストラクタ
	 * @see AdminCommon#AdminCommon()
	 */
	public AdminCommon( String reqUrl, String realPath ) {
	}
	
	/**
	 * DBサーバホスト名取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getDbHostName() {
		return MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
	}
	
	/**
	 * CGIヘッダ取得
	 */
	public String getCgiHeader() {
		String header = getSetting( "cgi_header" );
		if ( !header.equals("") ) {
			header = "#! " + header;
		}
		return header;
	}
	
	/**
	 * Annotationルートパス取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getDbRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH);
	}
	
	/**
	 * Molfileルートパス取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getMolRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH);
	}
	
	/**
	 * Profileルートパス取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getProfileRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_PROFILE_PATH);
	}
	
	/**
	 * GIFルートパス取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getGifRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_GIF_PATH);
	}
	
	/**
	 * GIFSMALLルートパス取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getGifSmallRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_GIF_SMALL_PATH);
	}
	
	/**
	 * GIFLARGEルートパス取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getGifLargeRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_GIF_LARGE_PATH);
	}
	
	/**
	 * 出力先パス取得
	 */
	public String getOutPath() {
		String outPath = getSetting( "out_path" );
		if ( !outPath.equals("") ) {
			// パス末尾にファイルの区切り文字なければ付加する
			char chrLast = outPath.charAt( outPath.length()-1 );
			if ( chrLast != '/' && chrLast != '\\' ) {
				outPath += File.separator;
			}
		}
		return outPath;
	}

	/**
	 * プライマリサーバURL取得
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getPServerUrl() {
		return MassBankEnv.get(MassBankEnv.KEY_PRIMARY_SERVER_URL);
	}
	
	/**
	 * MassBankディレクトリパス取得
	 * ApacheのMassBankディレクトリのリアルパスを取得する
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getMassBankPath() {
		return MassBankEnv.get(MassBankEnv.KEY_APACHE_APPROOT_PATH);
	}

	/**
	 * 管理者権限フラグ取得
	 */
	public boolean isAdmin() {
		boolean ret = false;
		String adminFlag = getSetting( "admin" );
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
		String adminFlag = getSetting( "portal" );
		if ( adminFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * SMTPアドレス取得（Batch Service用）
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getMailSmtp() {
		return MassBankEnv.get(MassBankEnv.KEY_BATCH_SMTP);
	}
	
	/**
	 * 送信者名取得（Batch Service用）
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getMailName() {
		return MassBankEnv.get(MassBankEnv.KEY_BATCH_NAME);
	}
	
	/**
	 * Fromアドレス取得（Batch Service用）
	 * @deprecated 非推奨メソッド
	 * @see MassBankEnv#get(String)
	 */
	public String getMailFrom() {
		return MassBankEnv.get(MassBankEnv.KEY_BATCH_FROM);
	}
	
	/**
	 * スケジュール取得
	 */
	public ArrayList<String> getSchedule() {
		String[] tmp = getSetting( "schedule" ).split("\t");
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
	 * @return 値
	 */
	private String getSetting( String key ) {
		String adminConfPath = MassBankEnv.get(MassBankEnv.KEY_ADMIN_CONF_PATH);
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { if ( br != null ) { br.close(); } } catch (IOException e) {}
		}
		return val;
	}
}
