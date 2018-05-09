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
 * INSTRUMENT情報とMS情報を取得するクラス
 *
 * ver 1.0.10 2011.07.22
 *
 ******************************************************************************/
package massbank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.ex.ConfigurationException;
import massbank.web.SearchExecution;
import massbank.web.instrument.InstrumentSearch;

public class GetInstInfo {
	ArrayList<String>[] instNo   = null;
	ArrayList<String>[] instType = null;
	ArrayList<String>[] instName = null;
	ArrayList<String>[] msType = null;
	private int index = 0;
	private HttpServletRequest request;

	/**
	 * Constructor (コンストラクタ)
	 * Record format version 2 (レコードフォーマットバージョン2の)
	 * Constructor that acquires INSTRUMENT information and MS information (INSTRUMENT情報とMS情報を取得するコンストラクタ)
	 * @param baseUrl ベースURL
	 * @throws ConfigurationException 
	 * @throws SQLException 
	 */
	public GetInstInfo( String baseUrl ) throws ConfigurationException, SQLException {
		getInformation(baseUrl);
	}

	public GetInstInfo( HttpServletRequest request) throws ConfigurationException, SQLException {
		this.request = request;
		getInformation(Config.get().BASE_URL());
	}
	
	/**
	 * Device type, MS type information acquisition (装置種別、MS種別情報取得)
	 * @param baseUrl ベースURL
	 * @param urlParam Parameters when executing CGI (CGI実行時のパラメータ)
	 * @throws ConfigurationException 
	 * @throws SQLException 
	 */
	private void getInformation( String baseUrl) throws ConfigurationException, SQLException {
		
		GetConfig conf = new GetConfig(baseUrl);
		String[] urlList = conf.getSiteUrl();

		String serverUrl = conf.getServerUrl();
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_INST];
		ArrayList<String> resultAll = new SearchExecution(request).exec(new InstrumentSearch());
		instNo = new ArrayList[urlList.length];
		instType = new ArrayList[urlList.length];
		instName = new ArrayList[urlList.length];
		msType = new ArrayList[urlList.length];
		for ( int i = 0; i < urlList.length; i++ ) {
			instNo[i]   = new ArrayList<String>();
			instType[i] = new ArrayList<String>();
			instName[i] = new ArrayList<String>();
			msType[i] = new ArrayList<String>();
		}

		boolean isInst = true;
		boolean isMs = false;
		int prevSiteNo = 0;
		for ( int i = 0; i < resultAll.size(); i++ ) {
			String line = resultAll.get(i).trim();
			
			if ( line.startsWith("INSTRUMENT_INFORMATION") ) { isInst = true; isMs = false; continue; }
			if ( line.startsWith("MS_INFORMATION") ) { isInst = false; isMs = true; continue; }
			
			if ( line.equals("") ) { continue; }
			String[] item = line.split("\t");
//			int siteNo = Integer.parseInt( item[item.length-1] );       we make a static site no
			int siteNo = 0;
			if ( prevSiteNo != siteNo) {
				prevSiteNo = siteNo;
				isInst = true; isMs = false;
			}
//			if ( line.startsWith("INSTRUMENT_INFORMATION") ) { isInst = true; isMs = false; continue; }
//			if ( line.startsWith("MS_INFORMATION") ) { isInst = false; isMs = true; continue; }
			if ( isInst ) {
				instNo[siteNo].add( item[0] );
				instType[siteNo].add( item[1] );
				instName[siteNo].add( item[2] );
			}
			else if ( isMs ) {
				msType[siteNo].add( item[0] );
			}
		}
	}
	
	/**
	 * Set site index (サイトインデックスをセット)
	 */ 
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Get INSTRUMENT_TYPE_NO (INSTRUMENT_TYPE_NOを取得)
	 */ 
	public String[] getNo() {
		return (String[])this.instNo[this.index].toArray( new String[0] );
	}
	
	/**
	 * Get INSTRUMENT_NAME (INSTRUMENT_NAMEを取得)
	 */
	public String[] getName() {
		return (String[])this.instName[this.index].toArray( new String[0] );
	}

	/**
	 * Get INSTRUMENT_TYPE (INSTRUMENT_TYPEを取得)
	 */
	public String[] getType() {
		return (String[])this.instType[this.index].toArray( new String[0] );
	}

	/**
	 * Get INSTRUMENT_TYPE (Acquire all sites without duplication) (INSTRUMENT_TYPEを取得（重複なしで全サイト分を取得）)
	 */
	public String[] getTypeAll() {
		ArrayList<String> instTypeList = new ArrayList<String>();
		for ( int i = 0; i < this.instType.length; i++ ) {
			for ( int j = 0; j < instType[i].size(); j++ ) {
				String type = instType[i].get(j);
				if ( !instTypeList.contains(type) ) {
					instTypeList.add( type );
				}
			}
		}
		// Sort by name (名前順でソート)
		Collections.sort( instTypeList );
		return (String[])instTypeList.toArray( new String[0] );
	}

	/**
	 * Get group information of INSTRUMENT_TYPE (INSTRUMENT_TYPEのグループ情報を取得)
	 */
	public Map<String, List<String>> getTypeGroup() {
		final String[] baseGroup = { "ESI", "EI", "Others" };

		String[] instTypes = getTypeAll();
		int num = baseGroup.length;
		List<String>[] listInstType = new ArrayList[num];
		for ( int i = 0; i < num; i++ ) {
			listInstType[i] = new ArrayList<String>();
		}
		for ( int j = 0; j < instTypes.length; j++ ) {
			String val = instTypes[j];
			boolean isFound = false;
			for ( int i = 0; i < num; i++ ) {
				if ( val.indexOf(baseGroup[i]) >= 0 ) {
					listInstType[i].add(val);
					isFound = true;
					break;
				}
			}
			if ( !isFound ) {
				listInstType[num - 1].add(val);
			}
		}

		Map<String, List<String>> group = new TreeMap<String, List<String>>();
		for ( int i = 0; i < num; i++ ) {
			if ( listInstType[i].size() > 0 ) {
				group.put( baseGroup[i], listInstType[i] );
			}
		}
		return group;
	}
	
	/**
	 * Get MS_TYPE (MS_TYPEを取得)
	 */
	public String[] getMsType() {
		return (String[])this.msType[this.index].toArray( new String[0] );
	}

	/**
	 * Obtain MS_TYPE (Acquire all sites without duplication) (MS_TYPEを取得（重複なしで全サイト分を取得）)
	 */
	public String[] getMsAll() {
		ArrayList<String> msTypeList = new ArrayList<String>();
		for ( int i = 0; i < this.msType.length; i++ ) {
			for ( int j = 0; j < msType[i].size(); j++ ) {
				String type = msType[i].get(j);
				if ( !msTypeList.contains(type) ) {
					msTypeList.add( type );
				}
			}
		}
		// Sort by name (名前順でソート)
		Collections.sort( msTypeList );
		return (String[])msTypeList.toArray( new String[0] );
	}
}
