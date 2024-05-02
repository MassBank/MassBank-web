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
 * [WEB-API] MultiDispatcher サーブレットを呼び出すクラス
 *
 * ver 1.0.3 2011.09.16
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;

import org.apache.commons.configuration2.ex.ConfigurationException;

import massbank.Config;
import massbank.GetConfig;
import massbank.MassBankCommon;
//import massbank.MassBankEnv;


public class DispatchInvoker {
	private String serverUrl = "";
	private ArrayList<String> response = null;
	private String typeName = "";

	/**
	 * コンストラクタ
	 * @throws ConfigurationException 
	 */
	public DispatchInvoker() throws ConfigurationException {
		GetConfig conf = new GetConfig(Config.get().BASE_URL());
		this.serverUrl = conf.getServerUrl();
	}

	/**
	 * MultiDispatcherの呼び出し
	 */
	public void invoke( String typeName, String param ) {
		MassBankCommon mbcommon = new MassBankCommon();
//		System.out.println( "typeName:" + typeName  + "/" + param  + "/" + this.serverUrl );
		this.response = mbcommon.execDispatcher( this.serverUrl, typeName, param, true, null );
		this.typeName = typeName;
	}

	/**
	 * レスポンスを取得する
	 */
	public ArrayList<String> getResponse() {
		return this.response;
	}


	/**
	 * 検索結果を取得する
	 */
	public SearchResult getSearchResult( int maxNumResults ) {
		ArrayList<String> ret = this.response;
//		Collections.sort(ret);	// ソートする
		SearchResult result = new SearchResult();
		int hitCnt = ret.size();
		if ( hitCnt > 0 ) {
			if ( maxNumResults > 0 && hitCnt > maxNumResults ) {
				hitCnt = maxNumResults;
			}
			for ( int i = 0; i < hitCnt; i++ ) {
				String line = ret.get(i);
				String[] fields = line.split("\t");
				String id       = "";
				String name     = "";
				String formula  = "";
				String emass    = "";
				String score    = "";

				if ( this.typeName.equals(MassBankCommon.REQ_TYPE_SEARCH) ) {
					id       = fields[0];
					name     = fields[1];
					formula  = fields[2];
					emass    = fields[3];
					score    = fields[4];
				}
				else {
					name     = fields[0];
					id       = fields[1];
					formula  = fields[3];
					emass    = fields[4];
				}

				//---------------------------------------
				// 検索結果をセットする
				//---------------------------------------
				Result info = new Result();
				info.setId(id);
				info.setTitle(name);
				info.setFormula(formula);
				info.setExactMass(emass);
				info.setScore(score);
				result.addInfo(info);
			}
		}
		return result;
	}
}
