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
 * Substructure Search用スクリプト
 *
 * ver 1.0.0 2010.03.24
 *
 ******************************************************************************/

var MOL_HEADER = "@data=";
var QUERY_NUM = 2;
var PEAK_NUM = 3;
var html = "";
var peak = new Array(PEAK_NUM);
var tol = "";
var PI_CHECK_VAL = new Array( "2", "1", "0" );
var piCheckNum = 0;
var boxGrp;
var boxInst;
var ionCheckNum;
var isKnapsack;

/**
 * エディタ画面を表示する
 * (クエリ→エディタに反映)
 */
function toEditor(isMassBank, index) {
	var ele, objApplet, appletName;
	if ( isMassBank ) {
		isKnapsack = false;
		ele = document.getElementById("tbl_queryM");
		objAppletQ = document.jme_queryM;
		appletName = "jme_editM";
	}
	else {
		isKnapsack = true;
		ele = document.getElementById("tbl_queryK");
		objAppletQ = document.jme_queryK;
		appletName = "jme_editK";
	}

	// 入力フィールドの内容を保持する
	storeFields();

	// クエリ側JME形式のデータを取得する
	var jme = objAppletQ[index].jmeFile();

	// クエリ側HTMLを保持する
	html = ele.innerHTML;

	// HTMLをエディタ画面に書き換える
	ele.innerHTML =
		 "&nbsp;<b>Query" + String(index+1) + "</b><br>"
		+ "<table>"
		+ "<tr>"
		+ "<td colspan='2'>"
		+ "<applet name='" + appletName + "' code='JME.class' archive='./applet/JME.jar' width='516' height='400'>"
		+ "</applet><br>"
		+ "</td>"
		+ "</tr>"
		+ "<tr>"
		+ "<td>"
		+ "<input type='button' value='OK' onClick='fromEditor(" + index + ");'>"
		+ "<input type='button' value='CANCEL' onClick='cancelEditor();'>"
		+ "</td>"
		+ "<td align='right'>"
		+ "<font style='font-size:11px'><a href='http://www.molinspiration.com/jme/' target='_blank'>JME Editor</a> courtesy of Peter Ertl, Novartis</font>"
		+ "</td>"
		+ "</tr>"
		+ "</table>";
	// エディタ側アプレット更新する
	if ( jme != "") {
		if ( isMassBank ) { objAppletE = document.jme_editM; }
		else              { objAppletE = document.jme_editK; }
		objAppletE.readMolecule(jme);
	}
}

/**
 * エディタの編集を完了する
 * （エディタ→クエリに反映）
 */
function fromEditor(index) {
	var ele, objApplet, objForm;

	// エディタ側の情報をmolfile形式で取得する
	if ( !isKnapsack ) { objApplet = document.jme_editM; }
	else               { objApplet = document.jme_editK; }
	var mol = objApplet.molFile();

	html = html.replace( "&gt;&gt; invalid data", "" );
	
	// HTMLをクエリ画面に書き戻す
	if ( !isKnapsack ) { ele = document.getElementById("tbl_queryM"); }
	else               { ele = document.getElementById("tbl_queryK"); }
	ele.innerHTML = html;

	// molfile情報をmoldataフィールドにセットする
	if ( !isKnapsack ) { objForm = document.form_queryM; }
	else               { objForm = document.form_queryK; }
	objForm.elements["moldata" + index].value = MOL_HEADER + mol;

	// 入力フィールドの内容を書き戻す
	restoreFields();

	// クエリの描画を更新する
	updateQuery();
}

/**
 * エディタの編集をキャンセルする
 */
function cancelEditor() {
	var ele;
	if ( !isKnapsack ) { ele = document.getElementById("tbl_queryM"); }
	else               { ele = document.getElementById("tbl_queryK"); }

	// HTMLをクエリ画面に書き戻す
	ele.innerHTML = html;
	
	// 入力フィールドの内容を書き戻す
	restoreFields();
	
	// クエリの描画を更新する
	updateQuery();
}

/**
 * クエリの描画を更新する
 */
function updateQuery() {
	var objForm, objApplet;
	if ( !isKnapsack ) {
		objForm = document.form_queryM;
		objApplet = document.jme_queryM;
	}
	else {
		objForm = document.form_queryK;
		objApplet = document.jme_queryK;
	}

	for ( i = 0; i < QUERY_NUM; i++ ) {
		objApplet[i].options('depict,border');
		mol = objForm.elements["moldata" + i].value;
		mol = mol.replace( MOL_HEADER, "" );
		if ( mol != "" ) {
			objApplet[i].readMolFile(mol);
		}
	}
}


/**
 * 入力フィールドの内容を保持する
 */
function storeFields() {
	var objForm;
	if ( !isKnapsack ) { objForm = document.form_queryM; }
	else               { objForm = document.form_queryK; }

	piCheckNum = objForm.elements.pi_check.selectedIndex;

	if ( !isKnapsack ) {
		for ( i = 0; i < PEAK_NUM; i++ ) {
			peak[i] = objForm.elements["mz" + i].value;
		}
		tol = objForm.elements["tol"].value;

		// Instrument Typeチェックボックス
		var obj1 = objForm.elements["inst_grp"];
		if ( obj1.length > 1 ) {
			boxGrp = new Array(obj1.length);
			for ( i = 0; i < obj1.length; i++ ) {
				boxGrp[i] = obj1[i].checked;
			}
		}
		else {
			boxGrp = new Array(1);
			boxGrp[0] = obj1.checked;
		}
	
		var obj2 = objForm.elements["inst"];
		if ( obj2.length > 1 ) {
			boxInst = new Array(obj2.length);
			for ( i = 0; i < obj2.length; i++ ) {
				boxInst[i] = obj2[i].checked;
			}
		}
		else {
			boxInst = new Array(1);
			boxInst[0] = obj2.checked;
		}
	
		// Ionization Modeラジオボタン
		var obj3 = objForm.elements["ion"];
		for ( i = 0; i < obj3.length; i++ ) {
			if ( obj3[i].checked ) {
				ionCheckNum = i;
				break;
			}
		}
	}
}

/**
 * 入力フィールドの内容を書き戻す
 */
function restoreFields() {
	var objForm;
	if ( !isKnapsack ) { objForm = document.form_queryM; }
	else               { objForm = document.form_queryK; }

	objForm.elements.pi_check[piCheckNum].selected = true;

	if ( !isKnapsack ) {
		for ( i = 0; i < PEAK_NUM; i++ ) {
			objForm.elements["mz" + i].value = peak[i];
		}
		objForm.elements["tol"].value = tol;

		// Instrument Typeチェックボックス
		var obj1 = objForm.elements["inst_grp"]
		if ( boxGrp.length == 1 ) {
			obj1.checked = boxGrp[0];
		}
		else {
			for ( i = 0; i < boxGrp.length; i++ ) {
				obj1[i].checked = boxGrp[i];
			}
		}
		var obj2 = objForm.elements["inst"]
		if ( boxInst.length == 1 ) {
			obj2.checked = boxInst[0];
		}
		else {
			for ( i = 0; i < boxInst.length; i++ ) {
				obj2[i].checked = boxInst[i];
			}
		}
		// Ionization Modeラジオボタン
		var obj3 = objForm.elements["ion"];
		obj3[ionCheckNum].checked = true;
	}
}

/**
 * Molfile読み込み画面を表示する
 */
function readMolfile(isMassBank, index) {
	var ele, objForm;
	if ( isMassBank ) { isKnapsack = false; }
	else              { isKnapsack = true;  }
	
	// 入力フィールドの内容を保持する
	storeFields();

	// クエリ側HTMLを保持する
	if ( !isKnapsack ) { ele = document.getElementById("tbl_queryM"); }
	else               { ele = document.getElementById("tbl_queryK"); }
	html = ele.innerHTML;
	
	var newHtml =
			"<b>Read Molfile</b><br>"
		  + "<form action='./StructureSearch.html' enctype='multipart/form-data' method='POST'>"
		  + "<input type='file' name='file' size='50'><br><br>"
		  + "<input type='submit' value='OK'>"
		  + "<input type='button' value='CANCEL' onClick='cancelEditor();'>"
		  + "<input type='hidden' name='num' value='" + String(index) + "'>";

	newHtml += "<input type='hidden' name='isSelectM' value='" + isMassBank + "'>";

	// 入力フィールドを隠しパラメータとしてセットする
	if ( !isKnapsack ) { objForm = document.form_queryM; }
	else               { objForm = document.form_queryK; }

	for ( i = 0; i < QUERY_NUM; i++ ) {
		var moldata = objForm.elements["moldata" + i].value;
		newHtml += "<input type='hidden' name='moldata" + String(i) + "' value='" + moldata + "'>";
	}
	
	newHtml += "<input type='hidden' name='pi_check' value='" + PI_CHECK_VAL[piCheckNum] + "'>";

	if ( !isKnapsack ) {
		for ( i = 0; i < PEAK_NUM; i++ ) {
			newHtml += "<input type='hidden' name='mz" + String(i) + "' value='" + peak[i] + "'>";
		}
		newHtml += "<input type='hidden' name='tol' value='" + tol + "'>";

		var obj1 = objForm.elements["inst_grp"];
		if ( obj1.length == 1 ) {
			if ( obj1.checked ) {
				newHtml += "<input type='hidden' name='inst_grp' value='" + obj1.value + "'>";
			}
		}
		else {
			for ( i = 0; i < obj1.length; i++ ) {
				if ( obj1[i].checked ) {
					newHtml += "<input type='hidden' name='inst_grp' value='" + obj1[i].value + "'>";
				}
			}
		}
		var obj2 = objForm.elements["inst"];
		if ( obj2.length == 1 ) {
			if ( obj2.checked ) {
				newHtml += "<input type='hidden' name='inst' value='" + obj2.value + "'>";
			}
		}
		else {
			for ( i = 0; i < obj2.length; i++ ) {
				if ( obj2[i].checked ) {
					newHtml += "<input type='hidden' name='inst' value='" + obj2[i].value + "'>";
				}
			}
		}
		var obj3 = objForm.elements["ion"];
		for ( i = 0; i < obj3.length; i++ ) {
			if ( obj3[i].checked ) {
				ionCheckNum = i;
				break;
			}
		}
		newHtml += "<input type='hidden' name='ion' value='" + obj3[ionCheckNum].value + "'>";
	}
	newHtml += "</form>";
	
	// HTMLをMolfile読み込み画面に書き換える
	ele.innerHTML = newHtml;
}

/**
 * クエリをクリアする
 */
function clearQuery(isMassBank, index) {
	var objForm;
	if ( isMassBank ) {
		objForm = document.form_queryM;
		objApplet = document.jme_queryM;
	}
	else {
		objForm = document.form_queryK;
		objApplet = document.jme_queryK;
	}
	objApplet[index].reset();
	objForm.elements["moldata" + index].value = "";
}

/**
 * タブ切替時の動作
 */
function changeTab(isSelectM) {
	isKnapsack = !isSelectM;
	updateQuery();
}


/**
 * Result.jspでのページ移動
 */
function changePageForStruct(page) {
	document.resultForm.action = location.href;
	document.resultForm.target = "_self";
	document.resultForm.page.value = page;
	document.resultForm.submit();
	return false;
}


/*
 * 検索ボタン押下時のチェック
 */
function checkSubmitForStruct() {
	if ( !isKnapsack ) {
		var isCheck = false;
		var obj = document.form_queryM["inst"];
		if ( obj.length > 1 ) {
			for ( i = 0; i < obj.length; i++ ) {
				if ( obj[i].checked ) {
					isCheck = true;
					break;
				}
			}
		}
		else {
			if ( obj.checked ) {
				isCheck = true;
			}
		}
		if ( !isCheck ) {
			alert( "Please select one or more checkboxs of the \"Instrument Type\"." );
			return false;
		}
	}
	return true;
}

/**
 * 部分構造検索パラメータ入力画面に戻る
 */
function prevStructSearch() {
	document.resultForm.action = "../StructureSearch.html";
	document.resultForm.target = "_self";
	document.resultForm.submit();
	return false;
}
