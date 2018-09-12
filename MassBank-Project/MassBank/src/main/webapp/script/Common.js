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
 * MassBank共通スクリプト
 *
 * ver 1.0.15 2013.01.18
 *
 ******************************************************************************/

// ブラウザ判別用変数
var op  = (window.opera) ? 1 : 0;								// OP
var ie  = (!op && document.all) ? 1 : 0;						// IE
var ns4 = (document.layers) ? 1 : 0;							// NS4
var ns6 = (document.getElementById&&!document.all) ? 1 : 0;		// NS6

/**
 * Cookie情報設定
 * @param isCookieConf massbank.confでのクッキー有効無効フラグ
 * @param cookiename クッキー名
 * @param keyInstGrp キー名
 * @param keyInst キー名
 * @param keyMs キー名
 * @param keyIon キー名
 * @param isAdv PeakSearchAdvancedフラグ
 */
function setCookie(isCookieConf, cookieName, keyInstGrp, keyInst, keyMs, keyIon, isAdv) {
	var addStr = "";
	if ( isAdv == 1 ) {
		addStr = "_adv";
	}
	
	// ブラウザのクッキー許可の場合
	if (window.navigator.cookieEnabled) {
		
		// クッキーに設定する値
		var cookieInstGrp = keyInstGrp + "=";
		var cookieInst = keyInst + "=";
		var cookieMs = keyMs + "=";
		var cookieIon = keyIon + "=";
		
		var instGrpVals = document.getElementsByName("inst_grp" + addStr);
		for (var i=0; i<instGrpVals.length; i++) {
			if (instGrpVals[i].checked) {
				cookieInstGrp += instGrpVals[i].value + ",";
			}
		}
		if (cookieInstGrp.substring(cookieInstGrp.length - 1) == ",") {
			cookieInstGrp = cookieInstGrp.substring(0, cookieInstGrp.length - 1);
		}
		
		var instVals = document.getElementsByName("inst" + addStr);
		for (var i=0; i<instVals.length; i++) {
			if (instVals[i].checked) {
				cookieInst += instVals[i].value + ",";
			}
		}
		if (cookieInst.substring(cookieInst.length - 1) == ",") {
			cookieInst = cookieInst.substring(0, cookieInst.length - 1);
		}
		
		var msVals = document.getElementsByName("ms" + addStr);
		for (var i=0; i<msVals.length; i++) {
			if (msVals[i].checked) {
				cookieMs += msVals[i].value + ",";
			}
		}
		if (cookieMs.substring(cookieMs.length - 1) == ",") {
			cookieMs = cookieMs.substring(0, cookieMs.length - 1);
		}
		
		var ionVals = document.getElementsByName("ion" + addStr);
		for (var i=0; i<ionVals.length; i++) {
			if (ionVals[i].checked) {
				cookieIon += ionVals[i].value;
				break;
			}
		}
		var cookieVal = cookieInstGrp + ";" + cookieInst + ";" + cookieMs + ";" + cookieIon;
		
		// 有効期限計算
		var date = new Date();
		var time = date.getTime();
		if ( isCookieConf) {
			// 有効期限を30日に設定
			time += (30 * 24 * 60 * 60 * 1000);
		} else {
			// 有効期限を過去に設定
			time -= 30;
		}
		// 有効期限をグリニッジ標準時に変換
		date.setTime(time);
		var gmtTime = date.toGMTString();
		
		// Cookie設定
		document.cookie = cookieName + "=" + escape(cookieVal) + ";expires=" + gmtTime;
	}
}

/**
 * 要素のclass属性の動的切り替え
 * @param elementId ID
 * @param className1 初期値のクラス名
 * @param className2 切り替えクラス名
 */
function switchClass(elementId, className1, className2) {
	
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return;
	}
	if (ns4) {											//NS4
		element = document.layers[elementId];
	}
	else {												//OP,IE,NS6
		element = document.getElementById(elementId);
	}
	if ( element.className == className1 ) {
		element.className = className2;
	}
	else {
		element.className = className1;
	}
}

/**
 * 同グループのチェックボックスを全てON/OFFにする
 * @param key
 * @param num
 * @param isAdv PeakSearchAdvancedフラグ
 */
function selBoxGrp(key, num, isAdv) {
	var addStr = "";
	if ( isAdv == 1 ) {
		addStr = "adv_";
	}
	var isCheck = document.getElementById("inst_grp_" + addStr + key).checked;
	for ( i = 0; i < num; i++ ) {
		id = "inst_" + addStr + key + String(i);
		obj = document.getElementById(id);
		obj.checked = isCheck;
	}
}

/**
 * 同グループ全てのチェックボックスがONまたはOFFになった場合の制御
 * @param key
 * @param num
 * @param isAdv PeakSearchAdvancedフラグ
 */
function selBoxInst(key, num, isAdv) {
	var addStr = "";
	if ( isAdv == 1 ) {
		addStr = "adv_";
	}
	var allOn = true;
	for ( i = 0; i < num; i++ ) {
		id = "inst_" + addStr + key + String(i);
		obj1 = document.getElementById(id);
		if ( !obj1.checked ) {
			allOn = false;
			break;
		}
	}
	obj2 = document.getElementById("inst_grp_" + addStr + key);
	if ( allOn ) {
		obj2.checked = true;
	}
	else {
		obj2.checked = false;
	}
}

/**
 * MS TypeチェックボックスのON/OFF（All）
 * @param num
 * @param isAdv PeakSearchAdvancedフラグ
 */
function selAllMs(num, isAdv) {
	var addStr = "";
	if ( isAdv == 1 ) {
		addStr = "adv_";
	}
	var isCheck = document.getElementById("ms_" + addStr + "MS0").checked;
	for ( i=1; i<=num; i++ ) {
		id = "ms_" + addStr + "MS" + String(i);
		obj = document.getElementById(id);
		obj.checked = isCheck;
	}
}

/**
 * MS TypeチェックボックスON/OFF
 * @param num
 * @param isAdv PeakSearchAdvancedフラグ
 */
function selMs(num, isAdv) {
	var addStr = "";
	if ( isAdv == 1 ) {
		addStr = "adv_";
	}
	var isAllCheck = true;
	for ( i=1; i<=num; i++ ) {
		id = "ms_" + addStr + "MS" + String(i);
		obj = document.getElementById(id);
		if ( !obj.checked ) {
			isAllCheck = false;
			break;
		}
	}
	document.getElementById("ms_" + addStr + "MS0").checked = isAllCheck;
}

/**
 * ファイル拡張子のチェック
 * @param path ファイルの絶対パス
 */
function checkFileExtention(path) {
	var file;
	var ext;
	var invalidList = new Array(
							"xls", "xlsx", "doc", "docx", "ppt", "pptx", "pdf",
							"bmp", "jpg", "gif", "png", "cab", "lzh", "tar", "zip",
							"exe", "wma", "aac", "mp3");
	
	if (path == "") {
		alert("No file.");
		return false;
	}
	
	// ファイル名取得
	file = path.substring(path.lastIndexOf('/', path.length) + 1);
	file = file.substring(file.lastIndexOf('\\', file.length) + 1);
	// 拡張子取得
	ext = file.substring(file.lastIndexOf('.', file.length) + 1);
	
	for (var i=0; i<invalidList.length; i++) {
		if (invalidList[i] == ext.toLowerCase()) {
			alert("The extension is illegal.");
			return false;
		}
	}
	return true;
}

/*
 * 検索ボタン押下時のチェック
  * @param isAdv PeakSearchAdvancedフラグ
 */
function checkSubmit( isAdv ) {
	var addStr = "";
	if ( isAdv == 1 ) {
		addStr = "_adv";
	}
	
	// Instrument Type check
	var isInstCheck = false;
	var instObj = document.form_query["inst" + addStr];
	if ( instObj.length > 1 ) {
		for ( i = 0; i < instObj.length; i++ ) {
			if ( instObj[i].checked ) {
				isInstCheck = true;
				break;
			}
		}
	}
	else {
		if ( instObj.checked ) {
			isInstCheck = true;
		}
	}
	
	// MS Type check
	var isMsCheck = false;
	var msObj = document.form_query["ms" + addStr];
	if ( msObj.length > 1 ) {
		for ( i = 0; i < msObj.length; i++ ) {
			if ( msObj[i].checked ) {
				isMsCheck = true;
				break;
			}
		}
	}
	else {
		if ( msObj.checked ) {
			isMsCheck = true;
		}
	}
	
	if ( !isInstCheck && !isMsCheck ) {
		alert( "Please select one or more checkboxs of the \"Instrument Type\" and \"MS Type\"." );
		return false;
	}
	else if ( !isInstCheck ) {
		alert( "Please select one or more checkboxs of the \"Instrument Type\"." );
		return false;
	}
	else if ( !isMsCheck ) {
		alert( "Please select one or more checkboxs of the \"MS Type\"." );
		return false;
	}
	return true;
}

/**
 * 処理中画面表示
 * onSubmit()などから呼び出す。
 * 正しくは処理中に画面を表示するのではなく、
 * 呼び出しもとの画面にdivタグで指定したテーブルを一番手前に表示する。
 * また、暗黙的に受け取った引数（可変長引数）の第一引数に値が存在すれば、
 * その値を処理中画面の中央に表示する。
 */
function doWait() {
	
	// 可変長引数から表示文字列取得
	var msg = (arguments[0] != null) ? arguments[0] : "please wait...";
	
	var objBody = document.body;
	var objDiv = document.createElement("div");
	objDiv.setAttribute("id", "wait");
	
	// キーイベント無効（SF未対応）
	if ( document.addEventListener ) {
		document.addEventListener( 'keydown', function(e){e.preventDefault();}, false);
	} else if (document.attachEvent) {
		document.attachEvent("onkeydown", function(){window.event.keyCode=0;return false;});
	}
	
	// スクロール位置保持およびスクロールバー非表示
	var sLeft = document.documentElement.scrollLeft || document.body.scrollLeft;
	var sTop = document.documentElement.scrollTop || document.body.scrollTop;
	objBody.style.overflowY = "hidden";
	objBody.style.overflowX = "hidden";
	
	// ブラウザの表示領域取得
	var w = document.body.clientWidth + "px";
	var h = document.body.clientHeight + "px";
	
	// イメージファイル相対パス取得
	var relPath = "./";
	var url = location.href;
	if ( url.indexOf("/jsp") != -1 || url.indexOf("/mbadmin") != -1 || url.indexOf("/extend") != -1 ) {
		relPath = "../";
	}
	
	// divタグエレメント作成および追加
	with(objDiv.style){
		backgroundColor = "#FFFFFF";
		filter = "alpha(opacity=80)";	// 透明度（IE）
		opacity = 0.8;								// 透明度（FF、SF）
		position = "absolute";
		left = sLeft + "px";
		top = sTop + "px";
		cursor = "wait";
		zIndex = 9999;
		width = w
		height = h;
	}
	objDiv.innerHTML = [
		"<table width='" + w + "' height='" + h + "' border='0' cellspacing='0' cellpadding='0' onSelectStart='return false;' onMouseDown='return false;'>",
		"<tr>",
		"<td align='center' valign='middle'><b><i><font size='+3'>" + msg + "</font></i></b>&nbsp;&nbsp;<img src='" + relPath + "image/wait.gif' alt=''></td>",
		"</tr>",
		"</table>"
	].join("\n");
	objBody.appendChild(objDiv);
}

/**
 * 構造式拡大表示
 * @param url ターゲットURL
 */
function expandMolView(url) {
	win = window.open(url, "ExpandMolView",
		'width=436,height=436,menubar=no,toolbar=no,scrollbars=no,status=no,location=yes,resizable=yes,left=0,top=0,screenX=0,screenY=0');
	win.blur();	// chrome対応
	win.focus();
}


/**
 * MassCalculator表示
 */
function openMassCalc() {
	var url = location.href;
	if ( url.indexOf("/index") != -1 ) {
		url = url.substring(0, url.indexOf("/index") + 1);
	}
	else if ( url.indexOf("/ja") != -1 ) {
		url = url.substring(0, url.indexOf("/ja") + 1);
	}
	else if ( url.indexOf("/en") != -1 ) {
		url = url.substring(0, url.indexOf("/en") + 1);
	}
	else {
		url = url.substring(0, url.indexOf("/jsp") + 1);
	}
	url += "MassCalc.jsp";
	if ( ie ) {
		leftX = window.screenLeft + document.body.clientWidth - 350;
		topY =  window.screenTop + 20;
	}
	else {
		leftX = window.screenX + document.body.clientWidth - 350;
		topY =  window.screenTop + 20;
	}
	win = window.open(url, "MassCalc",
		'width=380,height=380,menubar=no,toolbar=no,scrollbars=no,status=no,left=' + leftX + ',top=' + topY + ',screenX=' + leftX + ',screenY=' + topY);
	win.focus();
}
