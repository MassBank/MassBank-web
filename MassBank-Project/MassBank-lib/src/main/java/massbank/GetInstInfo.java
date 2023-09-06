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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.ex.ConfigurationException;

import massbank.web.SearchExecution;
import massbank.web.instrument.InstrumentSearch;
import massbank.web.instrument.InstrumentSearch.InstrumentSearchResult;

public class GetInstInfo {
	private final String[] instNo;
	private final String[] instType;
	private final String[] instName;
	private final String[] msType;

	/**
	 * Constructor (コンストラクタ)
	 * Record format version 2 (レコードフォーマットバージョン2の)
	 * Constructor that acquires INSTRUMENT information and MS information (INSTRUMENT情報とMS情報を取得するコンストラクタ)
	 * @param baseUrl ベースURL
	 * @throws ConfigurationException 
	 * @throws SQLException 
	 */
	public GetInstInfo( HttpServletRequest request) throws ConfigurationException, SQLException {
		InstrumentSearchResult result = new SearchExecution(request).exec(new InstrumentSearch());
		this.instNo		= result.instNo;
		this.instType	= result.instType;
		this.instName	= result.instName;
		this.msType		= result.msType;
	}
	
	/**
	 * Get INSTRUMENT_TYPE_NO (INSTRUMENT_TYPE_NOを取得)
	 */ 
	public String[] getNo() {
		return this.instNo;
	}
	
	/**
	 * Get INSTRUMENT_NAME (INSTRUMENT_NAMEを取得)
	 */
	public String[] getName() {
		return this.instName;
	}

	/**
	 * Get INSTRUMENT_TYPE (INSTRUMENT_TYPEを取得)
	 */
	public String[] getType() {
		return this.instType;
	}

	/**
	 * Get INSTRUMENT_TYPE (Acquire all sites without duplication) (INSTRUMENT_TYPEを取得（重複なしで全サイト分を取得）)
	 */
	public String[] getTypeAll() {
		Set<String> temp = new HashSet<String>(Arrays.asList(this.instType));
		String[] instType = temp.toArray(new String[temp.size()]);
		Arrays.sort(instType);
		return instType;
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
		return this.msType;
	}

	/**
	 * Obtain MS_TYPE (Acquire all sites without duplication) (MS_TYPEを取得（重複なしで全サイト分を取得）)
	 */
	public String[] getMsAll() {
		String[] msType	= this.msType.clone();
		Arrays.sort(msType);
		return msType;
	}
}
