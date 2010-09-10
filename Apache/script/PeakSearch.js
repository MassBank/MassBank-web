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
 * PeakSearch用スクリプト
 *
 * ver 1.0.7 2010.09.09
 *
 ******************************************************************************/

// ブラウザ判別用変数
var op = (window.opera) ? 1 : 0;								//OP
var ie = (!op && document.all) ? 1 : 0;							//IE
var ns4 = (document.layers) ? 1 : 0;							//NS4
var ns6 = (document.getElementById&&!document.all) ? 1 : 0;		//NS6

/**
 * ページロード時表示チェック
 * @param reqType リクエスト種別
 */
function loadCheck(reqType) {
	var f1 = document.forms[0];
	var checkedType = "";
	if (f1.type[0].checked) {
		checkedType = "peak";
	}
	else if (f1.type[1].checked) {
		checkedType = "diff";
	}
	else if (f1.type[2].checked) {
		checkedType = "product";
	}
	else if (f1.type[3].checked) {
		checkedType = "neutral";
	}
	
	if (reqType == checkedType) {
		initFocus();
	}
	else {
		changeSearchType(checkedType);
	}
}

/**
 * 初期フォーカス設定
 */
function initFocus() {
	// フォーカス可能であればフォーカスを当てる
	try { document.forms[0].mz0.focus(); } catch(e) {}
	return;
}

/**
 * 検索種別変更
 * @param reqType リクエスト種別
 */
function changeSearchType(reqType) {
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return false;
	}
	
	var elementLd = null;
	var elementMz = null;
	var elementSt = null;
	var elementAd = null;
	if (ns4) {											//NS4
		elementLd = document.layers["loaded"];
		elementSt = document.layers["standard"];
		elementAd = document.layers["advance"];
	}
	else {												//OP,IE,NS6
		elementLd = document.getElementById("loaded");
		elementSt = document.getElementById("standard");
		elementAd = document.getElementById("advance");
	}
	
	// ページロード済みチェック
	if (elementLd == null) return false;
	var color1 = "OliveDrab";
	var color2 = "DarkOrchid";
	var color3 = "MidnightBlue";
	var color4 = "DarkGreen";
	if( reqType == document.forms[0].type[0].value || reqType == "") {
		document.forms[0].type[0].checked = true;
		var elementMz = document.getElementById( "mz" );
		elementMz.innerHTML = "<i>m/z</i>";
		color2 = "White";
		color3 = "White";
		color4 = "White";
		elementSt.className = "showObj";
		elementAd.className = "hidObj";
		document.forms[0].action = "./jsp/Result.jsp";
		changeStandard("peak");
	}
	else if ( reqType == document.forms[0].type[1].value ) {
		document.forms[0].type[1].checked = true;
		var elementMz = document.getElementById( "mz" );
		elementMz.innerHTML = "<i>m/z</i>&nbsp;Diff.";
		color1 = "White";
		color3 = "White";
		color4 = "White";
		elementSt.className = "showObj";
		elementAd.className = "hidObj";
		document.forms[0].action = "./jsp/Result.jsp";
		changeStandard("diff");
	}
	else if ( reqType == document.forms[0].type[2].value ) {
		document.forms[0].type[2].checked = true;
		color1 = "White";
		color2 = "White";
		color4 = "White";
		elementSt.className = "hidObj";
		elementAd.className = "showObj";
		document.forms[0].action = "./jsp/ResultAdv.jsp";
		resetForm();
		changeAdvance("product", "and");
	}
	else if ( reqType == document.forms[0].type[3].value ) {
		document.forms[0].type[3].checked = true;
		color1 = "White";
		color2 = "White";
		color3 = "White";
		elementSt.className = "hidObj";
		elementAd.className = "showObj";
		document.forms[0].action = "./jsp/ResultAdv.jsp";
		resetForm();
		changeAdvance("neutral", "and");
	}
	
	document.getElementById( "underbar1" ).bgColor = color1;
	document.getElementById( "underbar2" ).bgColor = color2;
	document.getElementById( "underbar3" ).bgColor = color3;
	document.getElementById( "underbar4" ).bgColor = color4;
	
	// フォーカス初期化
	initFocus();
}

/**
 * Peak、PeakDifference検索用
 * @param reqType リクエスト種別
 */
function changeStandard(reqType) {
	var arrowObj = null
	for (cnt=0; cnt<6; cnt++) {
		if (ns4) {											//NS4
			arrowObj = document.layers["arrow" + cnt];
		}
		else {												//OP,IE,NS6
			arrowObj = document.getElementById("arrow" + cnt);
		}
		
		if (reqType == "peak") {
			arrowObj.innerHTML = "<img src=\"image/arrow_peak.gif\" alt=\"\">";
		}
		else if (reqType == "diff") {
			arrowObj.innerHTML = "<img src=\"image/arrow_diff.gif\" alt=\"\">";
		}
	}
}

/**
 * ProductIon、NeutralLoss検索用
 * @param reqType リクエスト種別
 * @param mode 検索条件
 */
function changeAdvance(reqType, mode) {
	// 検索種別ラベル設定
	var typeLblObj = null;
	var typeTxt = "Product&nbsp;Ion&nbsp;";
	var typeClass = "bgProduct";
	if (reqType == "neutral") {
		typeTxt = "Neutral&nbsp;Loss&nbsp;";
		typeClass = "bgNeutral";
	}
	for (cnt=1; cnt<6; cnt++) {
		if (ns4) {											//NS4
			typeLblObj = document.layers["advanceType" + cnt];
		}
		else {												//OP,IE,NS6
			typeLblObj = document.getElementById("advanceType" + cnt);
		}
		
		typeLblObj.innerHTML = typeTxt + cnt;
		typeLblObj.className = typeClass;
	}
	
	// モードラジオボタンテキスト変更
	var modeTxtObj = null;
	if (ns4) {											//NS4
		modeTxtObj = document.layers["modeTxt1"];
	}
	else {												//OP,IE,NS6
		modeTxtObj = document.getElementById("modeTxt1");
	}
	document.forms[0].mode[0].value = "and";
	document.forms[0].mode[1].value = "or";
	modeTxtObj.innerHTML = "OR";
	if (reqType == "neutral") {
		document.forms[0].mode[0].value = "and";
		document.forms[0].mode[1].value = "seq";
		modeTxtObj.innerHTML = "SEQUENCE";
	}
	
	// モードラジオボタン選択設定
	if (mode == "and") {
		document.forms[0].mode[0].checked = true;
		document.forms[0].mode[1].checked = false;
	}
	else {
		document.forms[0].mode[0].checked = false;
		document.forms[0].mode[1].checked = true;
	}
	chageMode(mode);
}
/**
 * ProductIon、NeutralLoss用検索条件変更
 * @param modeValue 
 */
function chageMode(modeValue) {
	if ( modeValue == "seq" ) {
		val = "<img src=\"./image/arrow_neutral.gif\">"
	}
	else if ( modeValue == "and" ) {
		val = "<b class=\"logic\">AND</b>";
	}
	else if ( modeValue == "or" ) {
		val = "<b class=\"logic\">OR</b>";
	}
	for ( i = 1; i <= 4; i++ ) {
		ele = document.getElementById( "cond"+ String(i) );
		ele.innerHTML = val;
	}
}

/**
 * 組成式を元として求めたmassをm/zに設定
 * @param index 条件インデックス
 * @param fom 組成式(ユーザ入力値)
 */
function setMZ(index, fom) {
	mass = "";
	
	if (fom != "") {
		atomicArray = new Array();
		
		// 入力された組成式の前後の半角/全角スペースをトリム
		fom = fom.replace(/^[\s　]+|[\s　]+$/g, "");
		
		// 入力された組成式の全角文字を半角文字へ変換
		newFom = fom.replace(/[Ａ-Ｚａ-ｚ０-９]/g, toHalfChar);
		
		// 組成式から原子(原子記号+原子数)に分解した配列を取得
		atomicArray = getAtomicArray(newFom);
		
		// 原子(原子記号+原子数)配列から原子ごとのm/zを全て加算した値を取得
		mass = massCalc(atomicArray);
	}
	
	// 結果を入力フォームに設定
	mzObj = eval("document.forms[0].mz" + index);
	mzObj.value = mass;
}

/**
 * 原子(原子記号+原子数)配列返却
 * @param newFom 組成式(半角英数字)
 * @return atomicArray 組成式から求めた原子配列
 */
function getAtomicArray(newFom) {
	
	atomicArray = new Array();
	nextChar = "";
	subStrIndex = 0;
	endChrFlag = 0;
	
	// 入力値を適切な場所で区切り原子(原子記号+原子数)を配列に格納する
	for (i=0; i<newFom.length; i++) {
		
		if ((i+1) < newFom.length) {
			nextChar = newFom.charAt(i+1);
		} else {
			endChrFlag = 1;
		}
		
		// 次の文字がない場合または、次の文字が大文字の英字の場合は区切る
		if (endChrFlag == 1 || nextChar.match(/[A-Z]/)) {
			atomicArray.push(newFom.substring(subStrIndex,i+1));
			subStrIndex = i+1;
		}
	}
	
	return atomicArray;
}

/**
 * 組成式を元にmassを計算
 * @param atomicArray 原子(原子記号+原子数)配列
 * @return mass 組成式から求めたmass
 */
function massCalc(atomicArray) {
	mass = "";
	massArray = new Array();
	for (i=0; i<atomicArray.length; i++) {
		atom = "";
		atomNum = 0;
		subStrIndex = 0;
		atomNumFlag = 0;
		
		// 原子を原子記号と原子数に分ける
		for (j=0; j<atomicArray[i].length; j++) {
			if (atomicArray[i].search(/[0-9]/) != -1) {
				subStrIndex = atomicArray[i].search(/[0-9]/);
				atomNumFlag = 1;
				break;
			}
		}
		
		// 原子記号に対する原子数がない場合
		if (!atomNumFlag) {
			atomNum = 1;
			atomicArray[i] = atomicArray[i].replace(/ /g, "");
			atomicArray[i] = atomicArray[i].replace(/　/g, "");
			subStrIndex = atomicArray[i].length;
		}
		
		// 原子数の前まで、または文字列の最後までを原子記号とする
		atom = atomicArray[i].substring(0,subStrIndex);
		
		// 原子記号に対する原子数がある場合
		if (atomNumFlag) {
			atomNum = atomicArray[i].substr(subStrIndex);
		}
		
		// 入力チェック
		if (atomicMass[atom]) {
			// 原子記号の質量と原子数を乗算したものを原子ごとのmass配列に格納
			massArray[i] = atomicMass[atom] * atomNum;
		} else {
			// 原子質量配列に該当するものがない場合は入力エラー
			alert("Formula is not found.");
			return mass;
		}
	}
	
	// 原子ごとのmassを全て加算する
	for (i=0; i<massArray.length; i++) {
		mass = eval(mass + massArray[i]);
	}
	
	// 小数第6位を四捨五入
	mass = "" + (Math.round(mass * 100000) / 100000);
	
	// 小数点以下が0の場合も表示
	if (mass.indexOf(".") == -1) {
		mass = mass + ".00000";
	}
	
	return mass;
}

/**
 * 全角->半角変換
 * @param LargeChar 変換対象となる全角1字
 * @return hanChr 半角文字
 */
function toHalfChar(LargeChar) {
	hanChr = "";
	hanStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
			 "abcdefghijklmnopqlstuvwxyz" +
			 "0123456789";
	zenStr = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" +
			 "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ" +
			 "０１２３４５６７８９";
	index = zenStr.indexOf(LargeChar);
	hanChr = hanStr.charAt(index);
	
	return hanChr;
}

/**
 * フォーム初期化
 */
function resetForm() {
	var f1 = document.forms[0];
	var reqType = "";
	if (f1.type[0].checked || f1.type[1].checked) {
		for ( i=0; i<6; i++ ) {
			if ( i>0 ) {
				f1["op" + i].value = "and";
			}
			f1["mz" + i].value = "";
			f1["fom" + i].value = "";
		}
		f1.int.value = "100";
		f1.tol.value = "0.3";
		if (f1.type[0].checked) {
			reqType = "peak";
		}
		else if (f1.type[1].checked) {
			reqType = "diff";
		}
	
		initFocus(reqType);
	}
	else if (f1.type[2].checked || f1.type[3].checked) {
		for ( i=1; i<6; i++ ) {
			f1["formula" + i].value = "";
		}
		if (f1.type[2].checked) {
			reqType = "product";
		}
		else if (f1.type[3].checked) {
			reqType = "neutral";
		}
		f1.mode[0].checked = true;
		f1.mode[1].checked = false;
		changeAdvance(reqType, "and");
	}
}
