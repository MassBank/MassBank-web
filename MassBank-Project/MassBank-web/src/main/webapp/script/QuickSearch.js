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
 * QuickSearchPage用スクリプト
 *
 * ver 1.0.7 2009.12.10
 *
 ******************************************************************************/

/**
 * 初期フォーカス設定
 */
function initFocus() {
	if (document.forms[0].searchType[0].checked == true ) {
		document.forms[1].compound.focus();
	}
	return;
}

/**
 * フォーム初期化
 */
function resetForm() {
	var f1 = document.forms[1];

	// 入力フォーム初期化
	if ( document.forms[0].searchType[0].checked == true ) {
		f1.compound.value = "";
		f1.mz.value = "";
		f1.tol.value = "0.3";
		f1.formula.value = "";
		f1.op1.value = "and";
		f1.op2.value = "and";
		initFocus();
	}
}

/**
 * 検索種別切り替え
 */
function changeSearchType() {
	document.forms[0].target = "_self";
	document.forms[0].method = "post";
	document.forms[0].submit();
}

/**
 * サンプルデータ1挿入
 */
function insertExample1() {
	document.forms[1].qpeak.value =
		"273.096 22\n289.086 107\n290.118 14\n291.096 999\n"
	  + "292.113 162\n293.054 34\n579.169 37\n580.179 15\n";
}

/**
 * サンプルデータ2挿入
 */
function insertExample2() {
	document.forms[1].qpeak.value =
		"70 51; 71 13; 72 49; 73 999; 74 98;"
	  + "75 158; 76 21; 77 235; 78 21; 79 77;"
	  + "80 5; 81 3; 82 1; 83 1; 84 8;"
	  + "85 4; 86 28; 87 12; 88 22; 89 4;"
	  + "90 2; 91 5; 92 2; 94 14; 95 1;"
	  + "96 2; 97 1; 98 3; 99 3; 100 67;"
	  + "101 20; 102 19; 103 44; 104 6; 105 5;"
	  + "106 1; 107 26; 108 1; 109 1; 110 15;"
	  + "112 1; 113 2; 114 10; 115 13; 116 845;"
	  + "117 105; 118 40; 119 5; 126 1; 127 5;"
	  + "128 15; 129 3; 130 50; 131 23; 132 9;"
	  + "133 23; 134 58; 135 7; 136 2; 143 2;"
	  + "144 4; 145 1; 146 5; 147 183; 148 30;"
	  + "149 15; 150 2; 174 1; 184 12; 185 1;"
	  + "190 34; 191 8; 192 2; 199 2; 218 10";
	  + "219 2; 220 1;";
}

/**
 * 検索前処理
 */
function beforeSubmit() {
	// 全角スペース置換
	var tmpValue = document.forms[1].qpeak.value;
	tmpValue = tmpValue.replace(/　/g, " ");
	document.forms[1].qpeak.value = tmpValue;
}
