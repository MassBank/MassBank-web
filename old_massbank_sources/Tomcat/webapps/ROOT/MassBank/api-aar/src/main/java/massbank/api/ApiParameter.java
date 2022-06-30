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
 * [WEB-API] パラメータ処理クラス
 *
 * ver 1.0.3 2011.08.08
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.Iterator;
import org.apache.commons.lang3.math.NumberUtils;


public class ApiParameter {
	private String type = "";
	private String param = "";
	private Map<String,Object> mapParam = null;
	private ArrayList<String> errDetails = null;

	/**
	 * コンストラクタ
	 */
	public ApiParameter(String type, Map<String,Object> mapParam) {
		this.type = type;
		this.mapParam = mapParam;
		this.errDetails = new ArrayList<String>();
	}

	/**
	 * デフォルトコンストラクタ
	 */
	public ApiParameter() {
	}

	/**
	 * パラメータをチェックする
	 */
	public boolean check() {
		if ( type.equals("searchSpectrum") ) {
			checkSearchSpectrum();
		}
		else if ( type.equals("searchPeak") ) {
			checkSearchPeak();
		}

		// 共通項目チェック
		checkSearchCommon();

		// エラーがあるか
		boolean ret = true;
		if ( errDetails.size() > 0 ) {
			ret = false;
		}
		return ret;
	}

	/**
	 * CGI用をパラメータ取得する
	 */
	public String getCgiParam() {
		return this.param;
	}


	/**
	 * CGI用パラメータ(ID)を取得する
	 */
	public String getCgiParamId( String[] ids ) {
		// IDの重複を排除し、ソートする
		TreeSet<String> tree = new TreeSet<String>();
		for ( int i = 0; i < ids.length; i ++ ) {
			tree.add(ids[i]);
		}

		String id = "ids=";
		Iterator it = tree.iterator();
		while ( it.hasNext() ) {
			id += it.next() + ",";
		}
		if ( !id.equals("") ) {
			id = id.substring( 0, id.length() - 1 );
		}
		return id;
	}


	/**
	 * エラー詳細情報を取得する
	 */
	public String getErrorDetail() {
		String errDetail = "";
		for ( int i = 0; i < errDetails.size(); i++ ) {
			errDetail += "\"" + errDetails.get(i) + "\",";
		}
		errDetail = errDetail.substring( 0, errDetail.length() - 1 );
		return errDetail;
	}


	/**
	 * searchSpectrum メソッドのパラメータをチェックする
	 */
	private void checkSearchSpectrum() {
		//---------------------------------------
		// tolerance 
		//---------------------------------------
		String unit = (String)mapParam.get("unit");
		String tolerance = (String)mapParam.get("tolerance");
		if ( unit.toLowerCase().equals("unit") || unit.equals("") ) {
			param += "&TOLUNIT=unit";
			if ( tolerance.equals("") ) {
				param += "&TOLERANCE=0.3";
			}
			else {
				param += "&TOLERANCE=" + tolerance;
			}
		}
		else if ( unit.toLowerCase().equals("ppm")) {
			param += "&TOLUNIT=ppm";
			if ( tolerance.equals("") ) {
			param += "&TOLERANCE=50";
			}
			else {
				param += "&TOLERANCE=" + tolerance;
			}
		}
		else {
			// "unit", "ppm" 以外はエラー
			errDetails.add( "unit=" + unit );
		}

		// 数値であるかチェック
		if ( !tolerance.equals("") && !NumberUtils.isCreatable(tolerance) ) {
			errDetails.add( "tolerance=" + tolerance );
		}

		//---------------------------------------
		// cutoff
		//---------------------------------------
		String cutoff = (String)mapParam.get("cutoff");
		if ( cutoff.equals("") ) {
			cutoff = "50";
		}
		else if ( NumberUtils.isCreatable(cutoff) ) {
			int val = Integer.parseInt(cutoff);
			if ( val < 0 || val > 999 ) {
				errDetails.add( "cutoff=" + cutoff );
			}
		}
		else {
			errDetails.add( "cutoff=" + cutoff );
		}
		param += "&CUTOFF=" + cutoff;

		//---------------------------------------
		// m/z, intensity
		//---------------------------------------
		String[] mzs = (String[])mapParam.get("mzs");
		String[] intensities = (String[])mapParam.get("intensities");

		// mzsとintensitiesの数をチェック
		if ( mzs.length != intensities.length ) {
			errDetails.add( "number of \"mzs\" NOT EQUAL number of \"intensities\"");
		}
		else {
			// 絶対->相対強度へ変換する
			Double maxInte = 0.0;
			for ( int i = 0; i < intensities.length; i++ ) {
				Double inte = Double.parseDouble(intensities[i]);
				if ( inte > maxInte ) {
					maxInte = inte;
				}
			}
			String[] relIntensities = new String[intensities.length];
			for ( int i = 0; i < intensities.length; i++ ) {
				if ( maxInte != 999 ) {
					Double inte = Double.parseDouble(intensities[i]);
					Double dblRelInte = inte / maxInte * 999 + 0.5;
					relIntensities[i] = String.valueOf(dblRelInte.intValue());
				}
				else {
					relIntensities[i] = String.valueOf(intensities[i]);
				}
			}
			String peak = "";
			for ( int i = 0; i < mzs.length; i++ ) {
				peak += mzs[i] + "," + relIntensities[i] + "@";
			}
			param += "&VAL=" + peak;
		}
	}


	/**
	 * searchSpectrum メソッドのパラメータをチェックする
	 */
	private void checkSearchPeak() {
		//---------------------------------------
		// m/z, intensity
		//---------------------------------------
		String[] mzs = (String[])mapParam.get("mzs");

		String relInte = (String)mapParam.get("relativeIntensity");
		if ( !NumberUtils.isCreatable(relInte) ) {
			errDetails.add( "relativeIntensity=" + relInte );
		}
		else {
			int val = Integer.parseInt(relInte);
			if ( val < 0 || val > 999 ) {
				errDetails.add( "relativeIntensity=" + relInte );
			}
		}

		String tol = (String)mapParam.get("tolerance");
		if ( !NumberUtils.isCreatable(tol) ) {
			errDetails.add( "tolerance=" + tol );
		}

		int num = mzs.length;
		param += "&num=" + String.valueOf(num);
		for ( int i = 0; i < num; i++ ) {
			param += "&mz"  + String.valueOf(i)   + "=" + mzs[i];
			param += "&int" + String.valueOf(i)   + "=" + relInte;
			param += "&tol" + String.valueOf(i) + "=" + tol;
			param += "&op" + String.valueOf(i) + "=";
			if ( i == 0 ) {
				param += "or";
			}
			else {
				param += "and";
			}
		}
	}

	/**
	 * 共通項目をチェックする
	 */
	private void checkSearchCommon() {
		//---------------------------------------
		// instrumentTypes
		//---------------------------------------
		String[] instrumentTypes = (String[])mapParam.get("instrumentTypes");
		boolean isInstAll = false;
		for ( int i = 0; i < instrumentTypes.length; i++ ) {
			String inst = instrumentTypes[i].toUpperCase();
			if ( inst.equals("ALL") ) {
				isInstAll = true;
				break;
			}
		}
		if ( isInstAll ) {
			if ( type.equals("searchSpectrum") ) {
				param += "&INST=ALL";
			}
			else if ( type.equals("execBatchJob") ) {
				param += "&inst=ALL";
			}
			else {
				param += "&inst=all";
			}
		}
		else {
			if ( type.equals("searchSpectrum") ) {
				param += "&INST=";
				for ( int i = 0; i < instrumentTypes.length; i++ ) {
					param += instrumentTypes[i] + ",";
				}
				param = param.substring( 0, param.length() - 1 );
			}
			else if ( type.equals("searchPeak") ) {
				for ( int i = 0; i < instrumentTypes.length; i++ ) {
					param += "&inst=" + instrumentTypes[i];
				}
			}
			else if ( type.equals("execBatchJob") ) {
				param += "&inst=";
				for ( int i = 0; i < instrumentTypes.length; i++ ) {
					param += instrumentTypes[i] + ",";
				}
				param = param.substring( 0, param.length() - 1 );
			}
		}

		//---------------------------------------
		// massTypes
		//---------------------------------------
		String[] massTypes = (String[])mapParam.get("massTypes");
		boolean isMsAll = false;
		for ( int i = 0; i < massTypes.length; i++ ) {
			String ms = massTypes[i].toUpperCase();
			if ( ms.equals("ALL") ) {
				isMsAll = true;
				break;
			}
		}
		if ( isMsAll ) {
			if ( type.equals("searchSpectrum") ) {
				param += "&MS=ALL";
			}
			else if ( type.equals("execBatchJob") ) {
				param += "&ms=all";
			}
			else {
				param += "&ms=all";
			}
		}
		else {
			if ( type.equals("searchSpectrum") ) {
				param += "&MS=";
				for ( int i = 0; i < massTypes.length; i++ ) {
					param += massTypes[i] + ",";
				}
				param = param.substring( 0, param.length() - 1 );
			}
			else if ( type.equals("searchPeak") ) {
				for ( int i = 0; i < massTypes.length; i++ ) {
					param += "&ms=" + massTypes[i];
				}
			}
			else if ( type.equals("execBatchJob") ) {
				param += "&ms=";
				for ( int i = 0; i < massTypes.length; i++ ) {
					param += massTypes[i] + ",";
				}
				param = param.substring( 0, param.length() - 1 );
			}
		}
		
		//---------------------------------------
		// ionMode
		//---------------------------------------
		String ionMode = (String)mapParam.get("ionMode");
		String mode = ionMode.toUpperCase();
		String ion = "";
		if ( mode.equals("POSITIVE") ) {
			ion = "1";
		}
		else if ( mode.equals("NEGATIVE") ) {
			ion = "-1";
		}
		else if ( mode.equals("BOTH") ) {
			ion = "0";
		}
		else {
			errDetails.add( "ionMode=" + ionMode );
		}
		if ( type.equals("searchSpectrum") ) {
			param += "&ION=" + ion;
		}
		else {
			param += "&ion=" + ion;
		}
	}
}
