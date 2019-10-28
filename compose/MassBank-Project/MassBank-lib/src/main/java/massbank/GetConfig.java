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
 * 環境設定ファイルの情報を取得するクラス
 *
 * ver 1.0.7 2012.09.06
 *
 ******************************************************************************/
package massbank;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.w3c.dom.*;
import massbank.Config;


public class GetConfig {
	public static final int MYSVR_INFO_NUM = 0;
	private Element m_root;

	/**
	 * コンストラクタ
	 */ 
	public GetConfig() {
	}

	/**
	 * DB名を取得する
	 * @throws ConfigurationException 
	 */ 
	public String[] getDbName() throws ConfigurationException {
		String[] infoList = null;
		infoList = new String[1];
		infoList[0] = Config.get().dbName();
		return infoList;
	}

	/**
	 * セカンダリDB名を取得する
	 */ 
	public String[] getSecondaryDBName() {
		return getSetting("SecondaryDB");
	}

	/**
	 * サーバーURLを取得する
	 */ 
	public String getServerUrl() {
		String url = this.getServerSetting("FrontServer");
		return url;
	}

	/**
	 * サイトURLを取得する
	 * @throws ConfigurationException 
	 */ 
//	public String[] getSiteUrl() throws ConfigurationException {
//		String[] infoList = null;
//		infoList = new String[1];
//		infoList[0] = Config.get().BASE_URL();
//		return infoList;
//	}
	
	/**
	 * Get the DocumentRoot folder of Apache HTTPD.
	 */ 
	public String getApacheDocumentRoot() {
		return getValByTagName( "ApacheDocumentRoot" );
	}
	
	/**
	 * タイムアウト値取得する
	 */ 
	public int getTimeout() {
		// デフォルト
		int val = 120;
		String ret = getValByTagName( "Timeout" );
		if ( !ret.equals("") ) {
			val = Integer.parseInt(ret);
		}
		return val;
	}

	/**
	 * トレースログ出力有効かどうかを判定する
	 */ 
	public boolean isTraceEnable() {
		// デフォルト
		boolean val = false;
		String ret = getValByTagName( "TraceLog" );
		if ( ret.equals("true") ) {
			val = true;
		}
		return val;
	}

	/**
	 * 
	 */
	private String[] getSetting( String tagName ) {
		String[] infoList = null;
		String tagName1;
		String tagName2;
		tagName1 = tagName2 = tagName;
		if ( tagName.equals("URL") ) {
			tagName1 = "MiddleServer";
		}
		String info1 = this.getServerSetting(tagName1);
		String[] info2 = this.getRelatedSetting(tagName2);
		if ( info2 == null ) {
			infoList = new String[1];
		}
		else {
			int len = info2.length;
			infoList = new String[len+1];
			for ( int i = 0; i < len; i++ ) {
				infoList[i+1] = info2[i];
			}
		}
		infoList[MYSVR_INFO_NUM] = info1;
		return infoList;
	}
	
	/**
	 * 自サーバーの設定を取得する
	 */
	private String getServerSetting(String tagName) {
		String val = "";
		try {
			NodeList nodeList = m_root.getElementsByTagName( "MyServer" );
			if ( nodeList == null ) {
				return val;
			}
			Element child = (Element)nodeList.item(0);
			NodeList childNodeList = child.getElementsByTagName( tagName );
			Element child2 = (Element)childNodeList.item(0);
			if ( child2 != null ) {
				if ( tagName.equals("FrontServer") || tagName.equals("MiddleServer") ) {
					val = child2.getAttribute("URL");
				}
				else {
					Node node = child2.getFirstChild();
					if ( node != null ) {
						val = node.getNodeValue();
					}
				}
				if ( val == null ) {
					val = "";
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 * 連携サイトの設定を取得する
	 */
	private String[] getRelatedSetting(String tagName) {
		String[] vals = null;
		try {
			NodeList nodeList = m_root.getElementsByTagName( "Related" );
			if ( nodeList == null ) {
				return null;
			}
			int len = nodeList.getLength();
			vals = new String[len];
			for ( int i = 0; i < len; i++ ) {
				Element child = (Element)nodeList.item(i);
				NodeList childNodeList = child.getElementsByTagName( tagName );
				Element child2 = (Element)childNodeList.item(0);
				vals[i] = "";
				if ( child2 == null ) {
					continue;
				}
				Node node = child2.getFirstChild();
				if ( node == null ) {
					continue;
				}
				String val = node.getNodeValue();
				if ( val != null ) {
					vals[i] = val;
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return vals;
	}

	/**
	 * 指定されたタグの値を取得
	 */ 
	private String getValByTagName( String tagName ) {
		String val = "";
		try {
			NodeList nodeList = m_root.getElementsByTagName( tagName );
			Element child = (Element)nodeList.item(0);
			val = child.getFirstChild().getNodeValue();
		}
		catch ( Exception e ) {
			System.out.println("\"" + tagName + "\" tag doesn't exist in massbank.conf.");
		}
		return val;
	}
}
