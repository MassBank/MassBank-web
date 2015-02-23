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
 * 環境設定ファイルの情報を更新するクラス
 *
 * ver 1.0.3 2010.11.25
 *
 ******************************************************************************/
package massbank.admin;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import massbank.MassBankEnv;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateConfig {

	private final int MYSVR_INFO_NUM = 0;
	private Document doc;
	private String confPath;
	
	/**
	 * デフォルトコンストラクタ
	 */
	public UpdateConfig() {
		String confUrl = MassBankEnv.get(MassBankEnv.KEY_MASSBANK_CONF_URL);
		this.confPath = MassBankEnv.get(MassBankEnv.KEY_MASSBANK_CONF_PATH);
		try {
			// ドキュメントビルダーファクトリを生成
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();

			// ドキュメントビルダーを生成
			DocumentBuilder builder = dbfactory.newDocumentBuilder();

			// パースを実行してDocumentオブジェクトを取得
			this.doc = builder.parse( confUrl );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 追加
	 * 連携サイトの設定のみ追加を許可する
	 * @param siteNo サイト番号
	 * @param name サイト名称
	 * @param longName ロングサイト名称
	 * @param url サーバURL
	 * @param db DB名称
	 * @return 結果
	 */
	public boolean addConfig(int siteNo, String name, String longName, String url, String db) {
		if ( siteNo == MYSVR_INFO_NUM ) {
			return false;
		}
		addRelatedSetting(name, longName, url, db);
		
		saveConf();
		return true;
	}
	
	/**
	 * 編集
	 * @param siteNo サイト番号
	 * @param internalSiteList 内部サイト番号リスト
	 * @param name サイト名称
	 * @param longName ロングサイト名称
	 * @param url サーバURL
	 * @param db DB名称
	 * @return 結果
	 */
	public boolean editConfig(int siteNo, ArrayList<Integer> internalSiteList, String name, String longName, String url, String db) {
		setSetting("Name", siteNo, name);
		setSetting("LongName", siteNo, longName);
		if ( siteNo == MYSVR_INFO_NUM ) {
			setSetting("FrontServer", siteNo, url);
			for (Integer internalSiteNo : internalSiteList) {
				if (internalSiteNo != MYSVR_INFO_NUM) {
					setSetting("URL", internalSiteNo, url);
				}
			}
			// BaseUrl 更新
			MassBankEnv.setBaseUrl(url);
		}
		else {
			setSetting("URL", siteNo, url);
		}
		if ( db != null && !db.equals("") ) {
			setSetting("DB", siteNo, db);
		}
		
		saveConf();
		return true;
	}
	
	/**
	 * 削除
	 * 連携サイトの設定のみ削除を許可する
	 * @param サイト番号
	 * @return 結果
	 */
	public boolean delConfig(int siteNo) {
		if ( siteNo == MYSVR_INFO_NUM ) {
			return false;
		}
		delRelatedSetting(siteNo);
		
		saveConf();
		return true;
	}
	
	/**
	 * 編集共通処理
	 */
	private boolean setSetting( String tagName, int siteNo, String value ) {
		boolean ret = false;
		
		// 設定処理
		if (siteNo == MYSVR_INFO_NUM) {
			ret = setServerSetting(tagName, siteNo, value);
		}
		else {
			ret = setRelatedSetting(tagName, siteNo, value);
		}
		return ret;
	}
	
	/**
	 * 自サーバーの設定を編集する
	 */
	private boolean setServerSetting(String tagName, int siteNo, String value) {
		try {
			NodeList nodeList = doc.getDocumentElement().getElementsByTagName( "MyServer" );
			if ( nodeList == null ) {
				return false;
			}
			Element child = (Element)nodeList.item(0);
			NodeList childNodeList = child.getElementsByTagName( tagName );
			Element child2 = (Element)childNodeList.item(0);
			if ( child2 != null ) {
				if ( tagName.equals("FrontServer") || tagName.equals("MiddleServer") ) {
					child2.setAttribute("URL", value);
					return true;
				}
				else {
					Node node = child2.getFirstChild();
					if ( node != null ) {
						node.setNodeValue(value);
						return true;
					}
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * 連携サイトの設定を編集する
	 */
	private boolean setRelatedSetting(String tagName, int siteNo, String value) {
		boolean ret = false;
		try {
			NodeList nodeList = doc.getDocumentElement().getElementsByTagName( "Related" );
			if ( nodeList == null ) {
				return ret;
			}
			for ( int i=0; i<nodeList.getLength(); i++ ) {
				if ( siteNo == (i+1) ) {
					Element child = (Element)nodeList.item(i);
					NodeList childNodeList = child.getElementsByTagName( tagName );
					Element child2 = (Element)childNodeList.item(0);
					if ( child2 == null ) {
						continue;
					}
					Node node = child2.getFirstChild();
					if ( node == null ) {
						continue;
					}
					node.setNodeValue(value);
					ret = true;
					break;
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
	/**
	 * 連携サイトの設定を追加する
	 * @param name サイト名称
	 * @param longName ロングサイト名称
	 * @param url サーバURL
	 * @param db DB名称
	 * @return 結果
	 */
	private boolean addRelatedSetting(String name, String longName, String url, String db) {
		boolean ret = false;
		try {
			// ノード追加処理
			Node root = doc.getDocumentElement();
			if (root.getNodeType() == Node.ELEMENT_NODE ) {
			
				Node nameNode = doc.createElement("Name");
				nameNode.appendChild(doc.createTextNode(name));
				
				Node longNameNode = doc.createElement("LongName");
				longNameNode.appendChild(doc.createTextNode(longName));
				
				Node urlNode = doc.createElement("URL");
				urlNode.appendChild(doc.createTextNode(url));
				
				Node dbNode = doc.createElement("DB");
				dbNode.appendChild(doc.createTextNode(db));
				
				Node browseModeNode = doc.createElement("BrowseMode");
				browseModeNode.appendChild(doc.createTextNode("3,5"));
				
				Node siteNode = doc.createElement("Related");
				siteNode.appendChild(nameNode);
				siteNode.appendChild(longNameNode);
				siteNode.appendChild(urlNode);
				siteNode.appendChild(dbNode);
				siteNode.appendChild(browseModeNode);
				
				// Timeout タグの前にノードを追加
				NodeList tmpNodeList = ((Element)root).getElementsByTagName("Timeout");
				if ( tmpNodeList.getLength() > 0 ) {
					root.insertBefore((Node)siteNode, tmpNodeList.item(0));
				}
				else {
					root.appendChild((Node)siteNode);
				}
				
				ret = true;
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * 連携サイトの設定を削除する
	 * @param siteNo サイト番号
	 * @return 結果
	 */
	private boolean delRelatedSetting(int siteNo) {
		boolean ret = false;
		try {
			Node root = doc.getDocumentElement();
			if (root.getNodeType() == Node.ELEMENT_NODE ) {
				NodeList nodeList = doc.getDocumentElement().getElementsByTagName( "Related" );
				if ( nodeList.getLength() >= (siteNo-1) ) {
					Element child = (Element)nodeList.item(siteNo-1);
					root.removeChild(child);
					ret = true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
    /**
     * 設定情報の書き込み処理
     * @return 結果
     */
    private boolean saveConf(){

		Transformer tf = null;
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			tf = factory.newTransformer();
			tf.setOutputProperty("indent",   "yes");
			tf.setOutputProperty("encoding", "utf-8");
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return false;
		}
		
		// 書き出し
		try {
			tf.transform(new DOMSource( doc ), new StreamResult( new File(confPath) ) );
		} catch (TransformerException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
    }    
}
