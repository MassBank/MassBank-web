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
 * Search by Peak ResultPage用スクリプト
 *
 * ver 1.0.2 2008.12.05
 *
 ******************************************************************************/

/**
 * Show Spectraボタンクリック
 */
function clickShowSpectra() {
	if ( submitFormCheck() ) {
		document.resultForm.action = "Display.jsp";
		document.resultForm.multi.click();
	}
}

/**
 * Spectrum Searchボタンクリック
 */
function submitSearchPage() {
	if ( submitFormCheck() ) {
		document.resultForm.action = "SearchPage.jsp";
		document.resultForm.submit();
	}
}

/**
 * チェックボックス状態取得
 * @param trId trタグid
 * @return チェック状態（true：チェック済、false：未チェック）
 */
function isCheck(trId) {
	if (trId == "") {
		return false;
	}
	else if ( !op && !ie && !ns4 && !ns6 ) {
		// 未サポートブラウザではチェックされていないと同じ状態を返却
		return false;
	}
	
	checkId = trId + "check";
	if (ns4) {									//NS4
		checkObj = document.layers[checkId];
	}
	else {										//OP,IE,NS6
		checkObj = document.getElementById(checkId);
	}
	return checkObj.checked;
}

/**
 * チェックボックスチェック（ノード）
 * @param trId trタグid
 * @param nodeNum ノード数（ヒット件数）
 */
function checkNode(trId, nodeNum) {
	if ( !op && !ie && !ns4 && !ns6 ) {
		return;
	}
	
	// チェックボックスid属性オブジェクト値生成
	checkId = trId + "check";
	if (ns4) {									//NS4
		checkObj = document.layers[checkId];
		trObj = document.layers[trId];
	}
	else {										//OP,IE,NS6
		checkObj = document.getElementById(checkId);
		trObj = document.getElementById(trId);
	}
	nodeCheck = checkObj.checked;
	
	// 背景色セット
	if ( nodeCheck ) {
		trObj.style.background = "#CCCCFF";
		
		// チェックボックスチェック（オール）チェック
		allCheck = true;
		for ( cnt=0; cnt<nodeNum; cnt++ ) {
			checkId = cnt + "check";
			if ( ns4 ) {								//NS4
				checkObj = document.layers[checkId];
			}
			else {										//OP,IE,NS6
				checkObj = document.getElementById(checkId);
			}
			
			if ( !checkObj.checked ) {
				allCheck = false;
				break;
			}
		}
		if ( allCheck ) {
			document.resultForm.chkAll.checked = true;
		}
	}
	else {
		trObj.style.background = "#E6E6FA";
		
		// チェックボックス（オール）のチェックをはずす
		if ( document.resultForm.chkAll.checked ) {
			document.resultForm.chkAll.checked = false;
		}
	}
}

/**
 * チェックボックス状態（オール）
 * @return チェック状態（true：check、false：uncheck）
 */
function checkAllState() {
	return document.resultForm.chkAll.checked;
}

/**
 * チェックボックスチェック（オール）呼び出し
 */
function callCheckAll() {
	allCheck = document.resultForm.chkAll.checked;
	if ( allCheck ) {
		document.resultForm.chkAll.checked = false;
	}
	else {
		document.resultForm.chkAll.checked = true;
	}
	checkAll();
}

/**
 * チェックボックスチェック（オール）
 */
function checkAll() {
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return;
	}
	
	check = document.resultForm.chkAll.checked;
	if ( check ) {
		bgcolor = "#99DDFF";
	}
	else {
		bgcolor = "#FFFFFF";
	}
	
	// ヒット件数が2件以上
	if ( document.resultForm.id.length != null ) {
		for ( cnt=0; cnt<document.resultForm.id.length; cnt++ ) {
			// チェクボックス
			document.resultForm.id[cnt].checked = check;
			
			// チェックボックスid属性オブジェクト取得
			idObj = document.resultForm.id[cnt].id;
			
			// trタグid属性オブジェクト値生成
			trId = idObj.substr( 0, idObj.indexOf("check") );
			if ( ns4 ) {								//NS4
				trObj = document.layers[trId];
			}
			else {										//OP,IE,NS6
				trObj = document.getElementById(trId);
			}
			// 背景色セット
			trObj.style.background = bgcolor;
		}
	}
	// ヒット件数が1件
	else {
		// チェックボックス
		document.resultForm.id.checked = check;
		// trタグid属性オブジェクト値生成
		trId = document.resultForm.id.id.substr(0, document.resultForm.id.id.indexOf("check"));
		if ( ns4 ) {								//NS4
			trObj = document.layers[trId];
		}
		else {										//OP,IE,NS6
			trObj = document.getElementById(trId);
		}
		
		// 背景色セット
		trObj.style.background = bgcolor;
	}
}

/**
 * フォーム送信前チェック
 */
function submitFormCheck() {
	if (document.resultForm.id.length != null) {
		// ヒット件数が2件以上
		for (cnt = 0; cnt < document.resultForm.id.length; cnt++){
			if (document.resultForm.id[cnt].checked) {
				return true;
			}
		}
	}
	else {
		// ヒット件数が1件
		if (document.resultForm.id.checked) {
			return true;
		}
	}
	alert("Please select one or more checkbox.");
	return false;
}

/**
 * 構造式表示ページを新規ウインドウにて開く
 * @param url ターゲットURL
 */
function popupMolView(url) {
	
	if ( ie ) {
		leftX = window.screenLeft + document.body.clientWidth - 250;
		topY =  window.screenTop;
	}
	else {
		leftX = window.screenX + document.body.clientWidth - 250;
		topY =  window.screenTop;
	}
	win = window.open(url, "MolView",
		'width=230,height=240,menubar=no,toolbar=no,scrollbars=no,status=no,left='
		 + leftX + ', top=' + topY + ',screenX=' + leftX + ',screenY=' + topY + '' );
	win.focus();
}

/**
 * マウスオーバー時のオブジェクトの背景色を変更する
 * @param obj 対象オブジェクト
 * @param color 変更する色
 * @param id チェックボックスID
 */
function overBgColor(obj, color, id) {
	checkState = isCheck(id);
	if (!checkState) {
		obj.style.background = color;
	}
	else {
		obj.style.background = "#CCCCFF";
	}
}

/**
 * マウスアウト時のオブジェクトの背景色を変更する
 * @param obj 対象オブジェクト
 * @param color 変更する色
 * @param id チェックボックスID
 */
function outBgColor(obj, color, id) {
	checkState = isCheck(id);
	if (!checkState) {
		obj.style.background = color;
	}
	else {
		obj.style.background = "#99DDFF";
	}
}

/**
 * 再検索処理
 */
function parameterResetting() {
	document.resultForm.action = "QuickSearch.jsp";
	document.resultForm.target = "_self";
	document.resultForm.submit();
	return false;
}
