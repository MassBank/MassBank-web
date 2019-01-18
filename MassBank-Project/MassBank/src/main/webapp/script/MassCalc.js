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
 * ver 1.0.3 2010.12.20
 *
 ******************************************************************************/

/**
 * jQuery実行
 */
$(function() {
	$.fn.initFocus();
	$("*").exitMassCalc();
	$("input.fFormula").fmCalc();
	$("input.fMass").resultFocus();
	$("input.mMass").mfCalc();
	$("textarea.mFormula").resultFocus();
});

/**
 * フォーカス初期化
 */
$.fn.initFocus = function() {
	$("input[name='type']:radio:eq(0)").focus();
}

/**
 * Escキー押下時のWindowクローズ
 */
$.fn.exitMassCalc = function() {
	$(this).each(function() {
		$(this).keyup(function(e) {
			if ( e.keyCode == 27 ) {
				window.opener = window;			//FF対応
				window.close();
			}
		});
	});
}

/**
 * 計算結果領域クリック時処理
 */
$.fn.resultFocus = function() {
	$(this).click(function() {
		if ( $(this).get(0).tagName.toLowerCase() == "input" ) {
			$(this).select();
		}
		else if ( $(this).get(0).tagName.toLowerCase() == "textarea" ) {
//			$(this).select();
		}
	});
}

/**
 * リアルタイムMassCalc（m/z to formula）
 */
$.fn.mfCalc = function() {
	
	var jsonFiles = ["ion_mass.json", "nloss_mass.json"];		// 読み込み対象のJSONファイル
	var formulaList = [];										// JSONファイルから読込んだ組成式リスト
	var isInit = true;
	
	// 全組成式取得
	for (var i=0; i<jsonFiles.length; i++) {
		$.getJSON(
			jsonFiles[i],
			function(jsonData){
				if (i == 0) {
					formulaList = $.merge([], jsonData);
				}
				else {
					formulaList = $.merge(formulaList, jsonData);
				}
			}
		);
	}
	
	// フォーカス時
	$(this).focus(function(){
		if ( isInit ) {
			// 小数点以下を5桁以上に統一
			for (var i=0; i<formulaList.length; i++) {
				var formula = formulaList[i][0];
				var mass = formulaList[i][1];
				if (mass.indexOf(".") == -1) {
					mass += ".00000";
				}
				else {
					var tmpMass = mass.split(".");
					if (tmpMass[1].length <= 5) {
						var zeroCnt = 5 - tmpMass[1].length;
						for (var j=0; j<zeroCnt; j++) {
							mass += "0";
						}
					}
				}
				formulaList[i][1] = mass;
			}
			
			// 重複除去
			var chkStorage = {};
			var tmpList = [];
			for (var i=0; i<formulaList.length; i++) {
				var value = formulaList[i];
				if ( !(value in chkStorage) ) {
					chkStorage[value] = true;
					tmpList.push(value);
				}
			}
			formulaList = $.merge([], tmpList);
			formulaList.sort();
			isInit = false;
		}
	});
	
	// キーアップ時
	$(this).keyup(function(e) {
		var inputMz = $(this).val();
		var matchFormula = new Array();
		if ( inputMz != "" ) {
			// 入力されたm/zの前後の半角/全角スペースをトリム
			inputMz = inputMz.replace(/^[\s　]+|[\s　]+$/g, "");
			
			// 入力されたm/zの全角文字を半角文字へ変換
			inputMz = inputMz.replace(/[Ａ-Ｚａ-ｚ０-９．]/g, toHalfChar);
			
			for (var i in formulaList) {
				// マッチする値の抽出
				var formula = formulaList[i][0];
				var mass = formulaList[i][1];
				if (mass.indexOf(inputMz) == 0) {
					matchFormula.push(formula + " (" + mass + ")");
				}
				else if (inputMz.indexOf(mass) == 0) {
					matchFormula.push(formula + " (" + mass + ")");
				}
			}
			matchFormula.sort();
		}
		// 結果を入力フォームに設定
		if ( matchFormula.length > 0 || inputMz == "" ) {
			$("textarea.mFormula").val(matchFormula.join("\n"));
		}
		else {
			$("textarea.mFormula").val("-");
		}
	});
}

/**
 * 計算種別変更
 * @param type 種別
 */
function changeType(type) {
	if (type == "fm") {
		$("#fCalc").show();
		$("#mCalc").hide();
		$("input[name='type']:radio").val(["fm"]);
		$("input[name='type']:radio:eq(0)").focus();
		$("span[name='typeLbl']:eq(0)").css("text-decoration", "underline");
		$("span[name='typeLbl']:eq(1)").css("text-decoration", "none");
	}
	else {
		$("#fCalc").hide();
		$("#mCalc").show();
		$("input[name='type']:radio").val(["mf"]);
		$("input[name='type']:radio:eq(1)").focus();
		$("span[name='typeLbl']:eq(0)").css("text-decoration", "none");
		$("span[name='typeLbl']:eq(1)").css("text-decoration", "underline");
	}
}

