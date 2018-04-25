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
 * ver 1.0.17 2010.11.26
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration2.ex.ConfigurationException;

import massbank.Config;

//import massbank.MassBankEnv;

/**
 * 管理者設定共通クラス
 * 以下の機能を提供する
 * 
 *   ＜機能＞                                       ＜取得先＞
 *   DBサーバホスト名取得（非推奨）                 MassBankEnv
 *   CGIヘッダ取得                                  admin.conf(cgi_header)
 *   Annotationルートパス取得（非推奨）             MassBankEnv
 *   Molfileルートパス取得（非推奨）                MassBankEnv
 *   Profileルートパス取得（非推奨）                MassBankEnv
 *   出力先パス取得                                 admin.conf(out_path)
 *   出力先パス取得（非推奨）                       MassBankEnv
 *   MassBankディレクトリパス取得（非推奨）         MassBankEnv
 *   ポータルサイトフラグ取得                       admin.conf(portal)
 *   管理者権限フラグ取得                           admin.conf(auth_root)
 *   Peak Search（Molecular Formula）表示フラグ取得 admin.conf(service_peakadv)
 *   Batch Service表示フラグ取得                    admin.conf(service_batch)
 *   Substructure Search（KNApSAcK）表示フラグ取得  admin.conf(service_knapsack)
 *   Advanced Search表示フラグ取得                  admin.conf(service_advanced)
 *   WEB-API提供フラグ                              admin.conf(service_api)
 *   AdminTool表示フラグ                            admin.conf(admin_all)
 *   SMTPアドレス取得（非推奨）                     MassBankEnv
 *   送信者名取得（非推奨）                         MassBankEnv
 *   Fromアドレス取得（非推奨）                     MassBankEnv
 *   スケジュール取得                               admin.conf(schedule)
 *   
 *   ※非推奨の機能に関してはMassBankEnv#get(String)の使用を推奨する
 *   
 */
public class AdminCommon {
	
	/**
	 * デフォルトコンストラクタ
	 */
	public AdminCommon() {
	}
	
//	/**
//	 * Molfileルートパス取得
//	 * @deprecated 非推奨メソッド
//	 * @see MassBankEnv#get(String)
//	 */
//	public String getMolRootPath() {
//		return MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH);
//	}
	
	/**
	 * 出力先パス取得
	 * @throws ConfigurationException 
	 */
	public String getOutPath() throws ConfigurationException {
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
	 * ポータルサイトフラグ取得
	 * @throws ConfigurationException 
	 */
	public boolean isPortal() throws ConfigurationException {
		boolean ret = false;
		String portalFlag = getSetting( "portal" );
		if ( portalFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 管理者権限フラグ取得
	 * @throws ConfigurationException 
	 */
	public boolean isAdmin() throws ConfigurationException {
		boolean ret = false;
		String adminFlag = getSetting( "auth_root" );
		if ( adminFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * Peak Search（Molecular Formula）表示フラグ取得
	 * @throws ConfigurationException 
	 */
	public boolean isPeakAdv() throws ConfigurationException {
		boolean ret = false;
		String peakAdvFlag = getSetting( "service_peakadv" );
		if ( peakAdvFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * Batch Service表示フラグ取得
	 * @throws ConfigurationException 
	 */
	public boolean isBatch() throws ConfigurationException {
		boolean ret = false;
		String batchFlag = getSetting( "service_batch" );
		if ( batchFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * Substructure Search（KNApSAcK）表示フラグ取得
	 * @throws ConfigurationException 
	 */
	public boolean isKnapsack() throws ConfigurationException {
		boolean ret = false;
		String knapsackFlag = getSetting( "service_knapsack" );
		if ( knapsackFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * Advanced Search表示フラグ取得
	 * @throws ConfigurationException 
	 */
	public boolean isAdvanced() throws ConfigurationException {
		boolean ret = false;
		String advancedFlag = getSetting( "service_advanced" );
		if ( advancedFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * WEB-API提供フラグ取得
	 * @throws ConfigurationException 
	 */
	public boolean isApi() throws ConfigurationException {
		boolean ret = false;
		String apiFlag = getSetting( "service_api" );
		if ( apiFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * スケジュール取得
	 * @throws ConfigurationException 
	 */
	public ArrayList<String> getSchedule() throws ConfigurationException {
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
	 * @throws ConfigurationException 
	 */
	private String getSetting( String key ) throws ConfigurationException {
		String adminConfPath = Config.get().ADMIN_CONF_PATH();
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
