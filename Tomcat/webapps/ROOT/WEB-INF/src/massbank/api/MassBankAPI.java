/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
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
 * [WEB-API] メインクラス
 *
 * ver 1.0.0 2009.08.19
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.axis2.AxisFault;
import massbank.MassBankCommon;
import massbank.GetInstInfo;

public class MassBankAPI {
//	public static final String BASE_URL = "http://localhost/MassBank/";
	public static final String BASE_URL = "http://massbank.jp/";


	/**
	 * デフォルトコンストラクタ
	 */
	public MassBankAPI() {
	}


	/**
	 * 類似スペクトルの検索を行う
	 */
	public SearchResult searchSpectrum(
			String[] mzs, String[] intensities,
			String unit, String tolerance,
			String cutoff, String[] instrumentTypes,
			String ionMode, int maxNumResults )
		throws AxisFault {

		//---------------------------------------
		// パラメータチェック
		//---------------------------------------
		HashMap<String,Object> mapParam = new HashMap();
		String[] keys = { "mzs", "intensities", "unit", "tolerance", "cutoff", "instrumentTypes", "ionMode" };
		Object[] vals = { mzs, intensities, unit, tolerance, cutoff, instrumentTypes, ionMode };
		for ( int i = 0; i < keys.length; i++ ) {
			mapParam.put( keys[i], vals[i] );
		}
		ApiParameter apiParam = new ApiParameter( "searchSpectrum", mapParam );
		if ( !apiParam.check() ) {
			// パラメータ不正の場合、SOAPFault を返す
			String errDetail = apiParam.getErrorDetail();
			throw new AxisFault( "Invalid parameter : " + errDetail );
		}

		//---------------------------------------
		// CGI用パラメータをセットする
		//---------------------------------------
		String param = apiParam.getCgiParam();
		param += "&API=true";

		//---------------------------------------
		// MultiDispatcherの呼び出し
		//---------------------------------------
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( MassBankCommon.REQ_TYPE_SEARCH, param );
		return inv.getSearchResult(maxNumResults);
	}


	/**
	 * 分析機器種別を取得する
	 */
	public String[] getInstrumentTypes() {
		GetInstInfo instInfo = new GetInstInfo(BASE_URL);
		String[] instTypes = instInfo.getTypeAll();
		return (String[])instTypes;
	}


	/**
	 * レコード情報を取得する
	 */
	public RecordInfo[] getRecordInfo(String[] ids) throws AxisFault {
		//---------------------------------------
		// CGI用パラメータをセットする
		//---------------------------------------
		ApiParameter apiParam = new ApiParameter();
		String param = apiParam.getCgiParamId(ids);
		param += "&mode=all";

		//---------------------------------------
		// MultiDispatcherの呼び出し
		//---------------------------------------
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( MassBankCommon.REQ_TYPE_GETRECORD, param );
		ArrayList<String> ret = inv.getResponse();
		ArrayList<RecordInfo> list = new ArrayList();
		RecordInfo recInfo = null;
		StringBuffer info = null;
		for ( int i = 0; i < ret.size(); i++ ) {
			String line = (String)ret.get(i);
			String[] val = line.split("\t");
			String item = val[0];
			if ( item.indexOf("ACCESSION") >= 0 ) {
				recInfo = new RecordInfo();
				info = new StringBuffer("");
				String id = item.substring(11);
				recInfo.setId(id);
			}
			else if ( item.equals("//") ){
				recInfo.setInfo(info.toString());
				list.add(recInfo);
				continue;
			}
			info.append(item + "\n");
		}

		RecordInfo[] result = null;
		int num = list.size();
		if ( num == 0 ) {
			// 1つも見つからない場合は、SOAPFaultを返す
			throw new AxisFault("Record is not found.");
		}
		else {
			result = new RecordInfo[num];
			list.toArray(result);
			return result;
		}
	}


	/**
	 * ピークデータを取得する
	 */
	public Peak[] getPeak(String[] ids) throws AxisFault {
		//---------------------------------------
		// CGI用パラメータをセットする
		//---------------------------------------
		ApiParameter apiParam = new ApiParameter();
		String param = apiParam.getCgiParamId(ids);
		param += "&mode=all";

		//---------------------------------------
		// MultiDispatcherの呼び出し
		//---------------------------------------
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( MassBankCommon.REQ_TYPE_GETRECORD, param );
		ArrayList<String> ret = inv.getResponse();
		ArrayList<Peak> list = new ArrayList();
		Peak peak = null;
		boolean isPeakLine = false;
		for ( int i = 0; i < ret.size(); i++ ) {
			String line = (String)ret.get(i);
			String[] val = line.split("\t");
			String item = val[0];
			if ( item.indexOf("ACCESSION") >= 0 ) {
				peak = new Peak();
				String id = item.substring(11);
				peak.setId(id);
			}
			else if ( item.equals("//") ){
				list.add(peak);
				isPeakLine = false;
				continue;
			}
			else if ( item.indexOf("PK$PEAK") >= 0 ){
				isPeakLine = true;
			}
			else {
				if ( isPeakLine ) {
					item = item.trim();
					String[] peakVals = item.split(" ");
					String mz = peakVals[0];
					String inte = peakVals[1];
					peak.addPeak(mz, inte);
				}
			}
		}

		int num = list.size();
		if ( num == 0 ) {
			// 1つも見つからない場合は、SOAPFaultを返す
			throw new AxisFault("MassBank Record is not found.");
		}
		else {
			Peak[] result = new Peak[num];
			list.toArray(result);
			return result;
		}
	}


	/**
	 * ピーク検索を行う
	 */
	public SearchResult searchPeak(
			String[] mzs,
			String relativeIntensity,
			String tolerance,
			String[] instrumentTypes,
			String ionMode,
			int maxNumResults )
		throws AxisFault {

		try {
			SearchResult ret = searchPeakCommon( false,
				mzs, relativeIntensity, tolerance,
				instrumentTypes, ionMode, maxNumResults
			);
			return ret;
		}
		catch (AxisFault ex) {
			throw ex;
		}
	}


	/**
	 * ピーク差検索を行う
	 */
	public SearchResult searchPeakDiff(
		String[] mzs,
		String relativeIntensity,
		String tolerance,
		String[] instrumentTypes,
		String ionMode,
		int maxNumResults ) throws AxisFault {

		try {
			SearchResult ret = searchPeakCommon( true,
				mzs, relativeIntensity, tolerance,
				instrumentTypes, ionMode, maxNumResults
			);
			return ret;
		}
		catch (AxisFault ex) {
			throw ex;
		}
	}


	/**
	 * ピーク検索共通処理
	 */
	private SearchResult searchPeakCommon (
		boolean isDiff,
		String[] mzs, 
		String relativeIntensity,
		String tolerance,
		String[] instrumentTypes,
		String ionMode,
		int maxNumResults ) throws AxisFault {

		//---------------------------------------
		// パラメータチェック
		//---------------------------------------
		HashMap<String,Object> mapParam = new HashMap();
		String[] keys = { "mzs", "relativeIntensity", "tolerance", "instrumentTypes", "ionMode" };
		Object[] vals = { mzs, relativeIntensity, tolerance, instrumentTypes, ionMode };
		for ( int i = 0; i < keys.length; i++ ) {
			mapParam.put( keys[i], vals[i] );
		}
		ApiParameter apiParam = new ApiParameter( "searchPeak", mapParam );
		if ( !apiParam.check() ) {
			// パラメータ不正の場合、SOAPFault を返す
			String errDetail = apiParam.getErrorDetail();
			throw new AxisFault( "Invalid parameter : " + errDetail );
		}
		String param = apiParam.getCgiParam();

		//---------------------------------------
		// MultiDispatcherの呼び出し
		//---------------------------------------
		String typeName = MassBankCommon.REQ_TYPE_PEAK;
		if ( isDiff ) {
			typeName = MassBankCommon.REQ_TYPE_PEAKDIFF;
		}
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( typeName, param );
		return inv.getSearchResult(maxNumResults);
	}
}
