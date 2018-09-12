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
 * 公開データページ用スクリプト
 *
 * ver 1.0.1 2011.01.06
 *
 ******************************************************************************/

/**
 * XMLHttpRequest オブジェクト生成
 */
function createHttpRequest(){
	// IE
	if(window.ActiveXObject){
		try {
			return new ActiveXObject("Msxml2.XMLHTTP");			//MSXML2以降用
		} catch (e) {
			try {
				return new ActiveXObject("Microsoft.XMLHTTP");	//旧MSXML用
			} catch (e2) {
				return null;
			}
		}
	}
	// IE 以外
	else if(window.XMLHttpRequest){
		return new XMLHttpRequest();
	}
	else {
		return null;
	}
}

/**
 * サーバ稼動している場合に遷移
 * @param siteName サイト名称
 */
function toSite(siteName) {
	// XMLHttpRequestオブジェクト生成
	var httpObj = createHttpRequest();
	if( httpObj == null ) {
		alert("A browser used is not supported.");
		return;
	}
	
	// 要求指定
	httpObj.open( "GET" , "Published.jsp?name=" + encodeURIComponent(siteName), true );
	
	//受信時に起動するイベント
	httpObj.onreadystatechange = function() {
		// 受信完了
		if (httpObj.readyState==4) {
			// コールバック
			var siteNo = httpObj.responseText;
			if ( siteNo != -1 && siteNo != -2 ) {
				location.href = "Result.jsp?type=rcdidx&idxtype=site&srchkey=" + siteNo;
			}
			else {
				alert("The server is temporarily unavailable.");
			}
			return;
		}
	}
	
	// 通信要求
	httpObj.send();
}
