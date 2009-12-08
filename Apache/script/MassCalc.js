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
 * MassCalculator用スクリプト
 *
 * ver 1.0.0 2009.12.08
 *
 ******************************************************************************/

// フォーカスオブジェクト格納用
var focusObj = null;

/**
 * 初期フォーカス設定
 */
function initFocus() {
	document.forms[0].fom0.focus();
	return;
}

/**
 * 組成式を元として求めたmassをm/zに設定
 */
function setMZ() {
	for (var i=0; i<6; i++) {
		var mass = "";
		var fom = eval("document.forms[0].fom" + i + ".value");
		if (fom != "") {
			atomicArray = new Array();
			
			// 入力された組成式の前後の半角/全角スペースをトリム
			fom = fom.replace(/^[\s　]+|[\s　]+$/g, "");
			
			// 入力された組成式の全角文字を半角文字へ変換
			newFom = fom.replace(/[Ａ-Ｚａ-ｚ０-９]/g, toHalfChar);
			
			// 整形した値を再設定
			fomObj = eval("document.forms[0].fom" + i);
			fomObj.value = newFom;
			
			// 組成式から原子(原子記号+原子数)に分解した配列を取得
			atomicArray = getAtomicArray(newFom);
			
			// 原子(原子記号+原子数)配列から原子ごとのm/zを全て加算した値を取得
			mass = massCalc(atomicArray);
		}
		
		// 結果を入力フォームに設定
		mzObj = eval("document.forms[0].mz" + i);
		mzObj.value = mass;
	}
}

/**
 * 原子(原子記号+原子数)配列返却
 * @param fom 組成式(半角英数字)
 * @return atomicArray 組成式から求めた原子配列
 */
function getAtomicArray(fom) {
	
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
			mass = "invalid."
			return mass;
		}
	}
	
	// 原子ごとのmassを全て加算する
	for (i=0; i<massArray.length; i++) {
		mass = eval(mass + massArray[i]);
	}
	
	// 小数点以下の表示を6桁に合わせる（切り捨て、0埋め）
	mass += "";
	if (mass.indexOf(".") == -1) {
		mass += ".000000";
	}
	else {
		var tmpMass = mass.split(".");
		if (tmpMass[1].length > 6) {
			mass = tmpMass[0] + "." + tmpMass[1].substring(0, 6);
		}
		else {
			var zeroCnt = 6 - tmpMass[1].length;
			for (var i=0; i<zeroCnt; i++) {
				mass += "0";
			}
		}
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
	for (var i=0; i<6; i++ ) {
		f1["mz" + i].value = "";
		f1["fom" + i].value = "";
	}
	initFocus();
}

/**
 * キーダウンイベント処理
 */
function keyDownEvent() {
	focusObj = document.activeElement;
	var code = event.keyCode;
	if (code == 13 && focusObj.name != "calc" && focusObj.name != "clear") {
		// Enterキーでフォーカス変更実行（フォーカスがボタン以外の場合）
		document.forms[0].calc.focus();
	}
}

/**
 * キーアップイベント処理
 */
function keyUpEvent() {
	var code = event.keyCode;
	if (code == 27) {
		// Escキーでウィンドウクローズ
		window.close();
	}
	else if (code == 13 && focusObj.name != "calc" && focusObj.name != "clear") {
		// EnterキーでCalc実行（フォーカスがボタン以外の場合）
		document.getElementsByName("calc").item(0).click();
		focusObj.focus();
		focusObj = null;
	}
}
