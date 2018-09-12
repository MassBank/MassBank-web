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
 * ver 1.0.10 2011.08.02
 *
 ******************************************************************************/

// ブラウザ判別用変数
var op = (window.opera) ? 1 : 0;								//OP
var ie = (!op && document.all) ? 1 : 0;							//IE
var ns4 = (document.layers) ? 1 : 0;							//NS4
var ns6 = (document.getElementById&&!document.all) ? 1 : 0;		//NS6

/**
 * jQuery実行
 */
$(function() {
	$("select.mzLogics").logicSelect();
	$("input.Formula").massCalc();
});

/**
 * ページロード時表示チェック
 * @param reqType リクエスト種別
 */
function loadCheck(searchof, searchby) {
	changeSearchType(searchof, searchby);
}

/**
 * 初期フォーカス設定
 */
function initFocus() {
	var f1 = document.forms[0];
	try {
		if (f1.searchby[0].checked) {
			f1.mz0.focus();
		}
	} catch(e) {}
	return;
}

/**
 * 検索種別変更
 * @param reqType リクエスト種別
 */
function changeSearchType(searchof, searchby) {
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return false;
	}
	
	var elementLd = null;
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
	var color2 = "White";
	var color3 = "OliveDrab";
	var color4 = "White";
	var isOfPeak = false;
	var f1 = document.forms[0];
	
	if ( searchof == "peak" ) isOfPeak = true;
	else if ( searchof == "diff" ) isOfPeak = false;
	else isOfPeak = f1.searchof[0].checked;
	
	var isByMz = false;
	if ( searchby == "mz" ) isByMz = true;
	else if ( searchby == "formula" ) isByMz = false;
	else {
		if ( !!document.forms[0].searchby[0] ) {
			isByMz = document.forms[0].searchby[0].checked;
		}
		else {
			isByMz = true;
		}
	}
	
	// ラベルクリック時にラジオボタンにチェックを入れるため
	if ( isOfPeak ) { f1.searchof[0].checked = true; }
	else            { f1.searchof[1].checked = true; }
	if ( isByMz )   { if ( !!f1.searchby[0] ) { f1.searchby[0].checked = true; } }
	else            { if ( !!f1.searchby[1] ) { f1.searchby[1].checked = true; } }
	
	var elementMz = document.getElementById( "mz" );
	if ( isByMz ) {
		elementSt.className = "showObj";
		elementAd.className = "hidObj";
		f1.action = "Result.jsp";
		if ( isOfPeak ) {
			f1.type.value = "peak";
			elementMz.innerHTML = "<i>m/z</i>";
			changeStandard("peak");
			color1 = "OliveDrab";
			color2 = "White";
			color3 = "OliveDrab";
			color4 = "White";
		}
		else {
			f1.type.value = "diff";
			elementMz.innerHTML = "<i>m/z</i>&nbsp;Diff.";
			changeStandard("diff");
			color1 = "White";
			color2 = "DarkOrchid";
			color3 = "DarkOrchid";
			color4 = "White";
		}
	}
	else {
		var elementCondInstTitle = document.getElementById( "condInstTitleAdv" );
		var elementCondMsTitle = document.getElementById( "condMsTitleAdv" );
		var elementCondIonTitle = document.getElementById( "condIonTitleAdv" );
		elementSt.className = "hidObj";
		elementAd.className = "showObj";
		f1.action = "ResultAdv.jsp";
		if ( isOfPeak ) {
			elementCondInstTitle.className = "cond-title-product";
			elementCondMsTitle.className = "cond-title-product";
			elementCondIonTitle.className = "cond-title-product";
			f1.type.value = "product";
			changeAdvance("product", "and");
			color1 = "Navy";
			color2 = "White";
			color3 = "White";
			color4 = "Navy";
		}
		else {
			elementCondInstTitle.className = "cond-title-neutral";
			elementCondMsTitle.className = "cond-title-neutral";
			elementCondIonTitle.className = "cond-title-neutral";
			f1.type.value = "neutral";
			changeAdvance("neutral", "and");
			color1 = "White";
			color2 = "DarkGreen";
			color3 = "White";
			color4 = "DarkGreen";
		}
	}
	
	document.getElementById( "underbar1" ).bgColor = color1;
	document.getElementById( "underbar2" ).bgColor = color2;
	if ( !!document.getElementById( "underbar3" ) ) { document.getElementById( "underbar3" ).bgColor = color3; }
	if ( !!document.getElementById( "underbar4" ) ) { document.getElementById( "underbar4" ).bgColor = color4; }
	
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
 * Ion、NeutralLoss検索用
 * @param reqType リクエスト種別
 * @param mode 検索条件
 */
function changeAdvance(reqType, mode) {
	// 検索種別ラベル設定
	var typeLblObj = null;
	var typeTxt = "Ion&nbsp;";
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
		val = "<span class=\"logic\">AND</span>";
	}
	else if ( modeValue == "or" ) {
		val = "<span class=\"logic\">OR</span>";
	}
	for ( i = 1; i <= 4; i++ ) {
		ele = document.getElementById( "cond"+ String(i) );
		ele.innerHTML = val;
	}
}

/**
 * 条件選択
 */
$.fn.logicSelect = function() {
	$(this).change(function() {
		var logicText = $("select.mzLogics option:selected").text();
		$("span.logic:visible").text(logicText);
		var logicVal = $("select.mzLogics option:selected").val();
		$("input[name='op1']:hidden,input[name='op2']:hidden,input[name='op3']:hidden,input[name='op4']:hidden,input[name='op5']:hidden").val(logicVal);
	});
}

/**
 * リアルタイムMassCalc
 */
$.fn.massCalc = function() {
	
	$(this).each(function() {
		
		var prevFormula = "";	// 入力前の値を保持
		var targetIndex = 0;	// 入力対象のインデックスを保持
		
		// キーダウン時
		$(this).keydown(function(e) {
			prevFormula = $(this).val();
			targetIndex = $("input.Formula").index(this);
		});
		
		// キーアップ時
		$(this).keyup(function(e) {
			var inputFormula = $(this).val();
			if ( prevFormula == inputFormula ) {
				return;
			}
			var mass = "";
			if ( inputFormula != "" ) {
				var atomicArray = new Array();
				
				// 入力された組成式の前後の半角/全角スペースをトリム
				inputFormula = inputFormula.replace(/^[\s　]+|[\s　]+$/g, "");
				
				// 入力された組成式の全角文字を半角文字へ変換
				inputFormula = inputFormula.replace(/[Ａ-Ｚａ-ｚ０-９]/g, toHalfChar);
				
				// 組成式から原子(原子記号+原子数)に分解した配列を取得
				atomicArray = getAtomicArray(inputFormula);
				
				// 原子(原子記号+原子数)配列から原子ごとのm/zを全て加算した値を取得
				mass = massCalc(atomicArray);
			}
			
			// 結果を入力フォームに設定
			$("input.Mass:eq(" + targetIndex + ")").val(mass);
		});
	});
}

/**
 * 原子(原子記号+原子数)配列返却
 * @param formula 組成式(半角英数字)
 * @return atomicArray 組成式から求めた原子配列
 */
function getAtomicArray(formula) {
	
	var atomicArray = new Array();
	var nextChar = "";
	var subStrIndex = 0;
	var endChrFlag = 0;
	
	// 入力値を適切な場所で区切り原子(原子記号+原子数)を配列に格納する
	for (i=0; i<formula.length; i++) {
		
		if ((i+1) < formula.length) {
			nextChar = formula.charAt(i+1);
		} else {
			endChrFlag = 1;
		}
		
		// 次の文字がない場合または、次の文字が大文字の英字の場合は区切る
		if (endChrFlag == 1 || nextChar.match(/[A-Z]/)) {
			atomicArray.push(formula.substring(subStrIndex,i+1));
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
	var mass = "";
	var massArray = new Array();
	for (i=0; i<atomicArray.length; i++) {
		var atom = "";
		var atomNum = 0;
		var subStrIndex = 0;
		var atomNumFlag = 0;
		
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
			mass = "-";
			return mass;
		}
	}
	
	// 原子ごとのmassを全て加算する
	for (i=0; i<massArray.length; i++) {
		mass = eval(mass + massArray[i]);
	}
	if (mass.toString() == "NaN") {
		mass = "-";
		return mass;
	}
	
	// 小数点以下の表示を5桁に合わせる（切り捨て、0埋め）
	mass += "";
	if (mass.indexOf(".") == -1) {
		mass += ".00000";
	}
	else {
		var tmpMass = mass.split(".");
		if (tmpMass[1].length > 5) {
			mass = tmpMass[0] + "." + tmpMass[1].substring(0, 5);
		}
		else {
			var zeroCnt = 5 - tmpMass[1].length;
			for (var i=0; i<zeroCnt; i++) {
				mass += "0";
			}
		}
	}
	
	return mass;
}

/**
 * 全角->半角変換
 * @param fullChar 変換対象となる全角1字
 * @return 半角文字
 */
function toHalfChar(fullChar) {
	var halfChr = "";
	var halfCharList = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
					   "abcdefghijklmnopqlstuvwxyz" +
					    "0123456789";
	var fullCharList = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" +
					   "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ" +
					   "０１２３４５６７８９";
	var index = fullCharList.indexOf(fullChar);
	halfChr = halfCharList.charAt(index);
	
	return halfChr;
}

/**
 * フォーム初期化
 */
function resetForm() {
	var f1 = document.forms[0];
	var reqType = "";
	if (!!!f1.searchby[0] || f1.searchby[0].checked) {
		for ( i=0; i<6; i++ ) {
			if ( i>0 ) {
				f1["op" + i].value = "and";
			}
			f1["mz" + i].value = "";
			f1["fom" + i].value = "";
		}
		f1.int.value = "100";
		f1.tol.value = "0.3";
		if (f1.searchof[0].checked) {
			reqType = "peak";
		}
		else if (f1.searchof[1].checked) {
			reqType = "diff";
		}
	}
	else if (f1.searchby[1].checked) {
		for ( i=1; i<6; i++ ) {
			f1["formula" + i].value = "";
		}
		if (f1.searchof[0].checked) {
			reqType = "product";
		}
		else if (f1.searchof[1].checked) {
			reqType = "neutral";
		}
		f1.mode[0].checked = true;
		f1.mode[1].checked = false;
		changeAdvance(reqType, "and");
	}
	initFocus();
}
