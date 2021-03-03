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
 * ResultPage用スクリプト
 *
 * ver 1.0.13 2011.07.22
 *
 ******************************************************************************/

/**
 * Show Spectraボタンクリック
 */
function clickShowSpectra() {
	document.resultForm.multi.click();
}

/**
 * Show Spectra 送信
 */
function submitShowSpectra() {
	if ( submitFormCheck() ) {
		document.resultForm.action = "Display.jsp";
		return true;
	}
	return false;
}

/**
 * Spectrum Search 送信
 */
function submitSearchPage() {
	if ( submitFormCheck() ) {
		document.resultForm.action = "SearchPage.jsp";
		document.resultForm.submit();
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
 * チェックボックスチェック（子ノード）
 * @param pTrId 親trタグid
 * @param cTrId 子trタグid
 * @param childNum 親ノード数
 * @param childNum 子ノード数
 */
function checkChild(pTrId, cTrId, parentNum, childNum) {
	if ( !op && !ie && !ns4 && !ns6 ) {
		return;
	}
	
	// 子ノードチェックボックス
	cCheckId = cTrId + "check";
	if (ns4) {									//NS4
		cCheckObj = document.layers[cCheckId];
		cTrObj = document.layers[cTrId];
	}
	else {										//OP,IE,NS6
		cCheckObj = document.getElementById(cCheckId);
		cTrObj = document.getElementById(cTrId);
	}
	childCheck = cCheckObj.checked;
	
	// 背景色セット
	if ( childCheck ) {
		cTrObj.style.background = "#CCCCFF";
		
		// チェックボックス（親ノード）チェック
		parentCheck = true;
		for ( cnt=0; cnt<childNum; cnt++ ) {
			cCheckId = cnt + pTrId + "check";
			if ( ns4 ) {								//NS4
				cCheckObj = document.layers[cCheckId];
			}
			else {										//OP,IE,NS6
				cCheckObj = document.getElementById(cCheckId);
			}
			
			if ( !cCheckObj.checked ) {
				parentCheck = false;
				break;
			}
		}
		
		if ( parentCheck ) {
			pCheckId = pTrId + "check";
			if (ns4) {									//NS4
				pCheckObj = document.layers[pCheckId];
				pTrObj = document.layers[pTrId];
			}
			else {										//OP,IE,NS6
				pCheckObj = document.getElementById(pCheckId);
				pTrObj = document.getElementById(pTrId);
			}
			pCheckObj.checked = true;
			pTrObj.style.background = "#99DDFF";
		}
		
		// チェックボックス（オール）チェック
		allCheck = true;
		for ( cnt=0; cnt<parentNum; cnt++ ) {
			pCheckId = "of" + cnt + "check";
			if ( ns4 ) {								//NS4
				pCheckObj = document.layers[pCheckId];
			}
			else {										//OP,IE,NS6
				pCheckObj = document.getElementById(pCheckId);
			}
			
			if ( !pCheckObj.checked ) {
				allCheck = false;
				break;
			}
		}
		if ( allCheck ) {
			document.resultForm.chkAll.checked = true;
		}
	}
	else {
		cTrObj.style.background = "#E6E6FA";
		
		// チェックボックス（親ノード）のチェックをはずす
		pCheckId = pTrId + "check";
		if (ns4) {									//NS4
			pCheckObj = document.layers[pCheckId];
			pTrObj = document.layers[pTrId];
		}
		else {										//OP,IE,NS6
			pCheckObj = document.getElementById(pCheckId);
			pTrObj = document.getElementById(pTrId);
		}
		
		if ( pCheckObj.checked ) {
			pCheckObj.checked = false;
			pTrObj.style.background = "#FFFFFF";
		}
		
		// チェックボックス（オール）のチェックをはずす
		if ( document.resultForm.chkAll.checked ) {
			document.resultForm.chkAll.checked = false;
		}
	}
}

/**
 * チェックボックスチェック（親ノード）
 * @param pTrId 親trタグid
 * @param parentNum 親ノード数
 * @param childNum 子ノード数
 */
function checkParent(pTrId, parentNum, childNum) {
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return;
	}
	
	// 親ノードチェックボックスチェック
	pCheckId = pTrId + "check";
	if (ns4) {									//NS4
		pCheckObj = document.layers[pCheckId];
		pTrObj = document.layers[pTrId];
	}
	else {										//OP,IE,NS6
		pCheckObj = document.getElementById(pCheckId);
		pTrObj = document.getElementById(pTrId);
	}
	parentCheck = pCheckObj.checked;
	
	// 背景色セット
	if ( parentCheck ) {
		pTrObj.style.background = "#CCCCFF";
		
		// チェックボックス（オール）のチェックをつける
		allCheck = true;
		for ( cnt=0; cnt<parentNum; cnt++ ) {
			pCheckId = "of" + cnt + "check";
			if ( ns4 ) {								//NS4
				pCheckObj = document.layers[pCheckId];
			}
			else {										//OP,IE,NS6
				pCheckObj = document.getElementById(pCheckId);
			}
			
			if ( !pCheckObj.checked ) {
				allCheck = false;
				break;
			}
		}
		
		if ( allCheck ) {
			document.resultForm.chkAll.checked = true;
		}
	}
	else {
		pTrObj.style.background = "#E6E6FA";
		
		// チェックボックス（オール）のチェックをはずす
		if ( document.resultForm.chkAll.checked ) {
			document.resultForm.chkAll.checked = false;
		}
	}
	
	
	// チェックボックス（子ノード）チェック
	for ( cnt=0; cnt<childNum; cnt++ ) {
		cCheckId = cnt + pTrId + "check";
		cTrId = cnt + pTrId;
		if ( ns4 ) {								//NS4
			cCheckObj = document.layers[cCheckId];
			cTrObj = document.layers[cTrId];
		}
		else {										//OP,IE,NS6
			cCheckObj = document.getElementById(cCheckId);
			cTrObj = document.getElementById(cTrId);
		}
		
		cCheckObj.checked = parentCheck;
		
		// 背景色セット
		if ( parentCheck ) {
			cTrObj.style.background = "#99DDFF";
		}
		else {
			cTrObj.style.background = "#FFFFFF";
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
	
	allCheck = document.resultForm.chkAll.checked;
	if ( allCheck ) {
		bgcolor = "#99DDFF";
	}
	else {
		bgcolor = "#FFFFFF";
	}
	
	// チェックボックス（親ノード）
	// ヒット件数が2件以上
	if ( document.resultForm.pid.length != null ) {
		for ( cnt=0; cnt<document.resultForm.pid.length; cnt++ ) {
			// チェクボックス
			document.resultForm.pid[cnt].checked = allCheck;
			
			pCheckObj = document.resultForm.pid[cnt].id;
			
			// trタグid属性オブジェクト値生成
			pTrId = pCheckObj.substr( 0, pCheckObj.indexOf("check") );
			if ( ns4 ) {								//NS4
				pTrObj = document.layers[pTrId];
			}
			else {										//OP,IE,NS6
				pTrObj = document.getElementById(pTrId);
			}
			// 背景色セット
			pTrObj.style.background = bgcolor;
		}
	}
	// ヒット件数が1件
	else {
		// チェックボックス
		document.resultForm.pid.checked = allCheck;
		// trタグid属性オブジェクト値生成
		pTrId = document.resultForm.pid.id.substr(0, document.resultForm.pid.id.indexOf("check"));
		if ( ns4 ) {								//NS4
			pTrObj = document.layers[pTrId];
		}
		else {										//OP,IE,NS6
			pTrObj = document.getElementById(pTrId);
		}
		
		// 背景色セット
		pTrObj.style.background = bgcolor;
	}
	
	
	// チェックボックス（子ノード）
	// ヒット件数が2件以上
	if ( document.resultForm.id.length != null ) {
		for ( cnt=0; cnt<document.resultForm.id.length; cnt++ ) {
			// チェクボックス
			document.resultForm.id[cnt].checked = allCheck;
			
			// チェックボックスid属性オブジェクト取得
			cCheckObj = document.resultForm.id[cnt].id;
			
			// trタグid属性オブジェクト値生成
			cTrId = cCheckObj.substr( 0, cCheckObj.indexOf("check") );
			if ( ns4 ) {								//NS4
				cTrObj = document.layers[cTrId];
			}
			else {										//OP,IE,NS6
				cTrObj = document.getElementById(cTrId);
			}
			// 背景色セット
			cTrObj.style.background = bgcolor;
		}
	}
	// ヒット件数が1件
	else {
		// チェックボックス
		document.resultForm.id.checked = allCheck;
		// trタグid属性オブジェクト値生成
		cTrId = document.resultForm.id.id.substr(0, document.resultForm.id.id.indexOf("check"));
		if ( ns4 ) {								//NS4
			cTrObj = document.layers[cTrId];
		}
		else {										//OP,IE,NS6
			cTrObj = document.getElementById(cTrId);
		}
		
		// 背景色セット
		cTrObj.style.background = bgcolor;
	}
}

/**
 * 全ツリー制御
 */
function allTreeCtrl() {
	
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return;
	}
	
	isOpen = false;
	if ( document.resultForm.treeCtrl.value.indexOf("Close") == -1 ) {
		image1 = "plus.png";
		image2 = "image/minus.png";
		imageTree1 = "treeline0.gif";
		imageTree2 = "image/treeline1.gif";
		type = "";
		document.resultForm.treeCtrl.value = "Close All Tree";
		isOpen = true;
	}
	else {
		image1 = "minus.png";
		image2 = "image/plus.png";
		imageTree1 = "treeline1.gif";
		imageTree2 = "image/treeline0.gif";
		type = "none";
		document.resultForm.treeCtrl.value = "Open All Tree";
		isOpen = false;
	}
	
	parentId = 0;
	while (true) {
		img = document.images[parentId++];
		if ( img == null ) {
			break;
		}
		// +-アイコン変更
		if ( img.src.indexOf(image1) >= 0 ) {
			img.src = image2;
		}
		// ツリー罫線の表示切替
		else if ( img.src.indexOf(imageTree1) >= 0 ) {
			img.src = imageTree2;
		}
	}
	
	//------------------------- IE4 IE5 IE6 NN4 NS6 ----
	// document.all             ○  ○  ○  ×  ×
	// document.layers          ×  ×  ×  ○  ×
	// document.getElementById  ×  ○  ○  ○  ○
	//--------------------------------------------------
	for ( i = 0; i < parentId; i++ ) {
		childId = i + "child";
		if (ns4) {											//NS4
			element = document.layers[childId];
		}
		else {												//OP,IE,NS6
			element = document.getElementById(childId);
		}
		if ( element == null ) {
			break;
		}
		element.style.display = type;
	}
	
	if ( !isOpen ) {
		location.href = "#";
	}
}

/**
 * 全ツリー制御状態取得
 * @return 全ツリー制御状態（-1：サポート外、0：CloseAll状態、1：OpenAll状態）
 */
function allTreeCtrlState() {
	if ( !op && !ie && !ns4 && !ns6 ) {
		return -1;
	}
	else if ( document.resultForm.treeCtrl.value.indexOf("Close") == -1 ) {
		return 0;
	}
	else {
		return 1;
	}
}

/**
 * ツリー展開/非展開
 * @param parentId 親ツリーID
 */
function treeMenu(parentId) {
	
	if ( !op && !ie && !ns4 && !ns6 ) {
		alert("Your browser is not supported.");
		return;
	}
	
	imageP = "plus.png";
	imageM = "minus.png";
	imgName = parentId + "img";
	// アイコン変更
	imgBefore = document.images[imgName];
	if ( imgBefore.src.indexOf(imageP) >= 0 ) {
		imgAfter = imageM;
	}
	else {
		imgAfter = imageP;
	}
	imgBefore.src = "image/" + imgAfter;
	
	childId = parentId + "child";
	if (ns4) {											//NS4
		element = document.layers[childId];
	}
	else {												//OP,IE,NS6
		element = document.getElementById(childId);
	}
	if ( element.style.display == "none" ) {
		element.style.display = "";
	}
	else {
		element.style.display = "none";
	}

	// ツリー罫線の表示切替
	image0 = "treeline0.gif";
	image1 = "treeline1.gif";
	imgName = parentId + "treeimg";
	imgBefore = document.images[imgName];
	if ( imgBefore.src.indexOf(image0) >= 0 ) {
		imgAfter = image1;
	}
	else {
		imgAfter = image0;
	}
	imgBefore.src = "image/" + imgAfter;
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
 * レコードソート
 * @param currentSortKey 現在のソートキー
 * @param nextSortKey ソートするソートキー
 */
function recSort(currentSortKey, nextSortKey) {
	
	// 表示ページを最初のページに戻す
	document.resultForm.pageNo.value = 1;
	
	// 「sortKey.value」と「sortAction.value」に設定する値はResultListの定数値を参照
	document.resultForm.sortKey.value = nextSortKey;
	
	if (currentSortKey == nextSortKey) {
		switch(parseInt(document.resultForm.sortAction.value)) {
			case 1:		// 昇順ソート状態の場合
				document.resultForm.sortAction.value = -1;
				break;
			case -1:	// 降順ソート状態の場合
				document.resultForm.sortAction.value = 1;
				break;
		}
	}
	else {
		// 昇順ソート
		document.resultForm.sortAction.value = 1;
	}
	document.resultForm.exec.value = "sort";
	url = location.href;
	url = url.split("?")[0];
	url = url.split("#")[0];
	document.resultForm.action = url;
	document.resultForm.target = "_self";
	document.resultForm.submit();
}

/**
 * ページ変更
 * Quick Search Results、Peak Search Results、Peak Difference Resultsのページ変更で必ず呼ばれる
 * Record List Resultsでは右クリックメニューによるページ変更時のみ呼ばれる
 * @param type 種別
 * @param nextPage 表示するページ
 */
function changePage(type, nextPage) {
	url = location.href;
	url = url.split("?")[0];
	url = url.split("#")[0];
	if (type == "rcdidx") {
		url += "?sortAction=" + document.resultForm.sortAction.value
				 + "&pageNo=" + nextPage
				 + "&exec=page"
				 + "&sortKey=" + document.resultForm.sortKey.value
				 + "&totalPageNo=" + document.resultForm.totalPageNo.value
				 + "&type=" + document.resultForm.type.value
				 + "&srchkey=" + document.resultForm.srchkey.value
				 + "&idxtype=" + document.resultForm.idxtype.value;
		location.assign(url);
		return true;
	}
	else {
		document.resultForm.pageNo.value = nextPage;
		document.resultForm.exec.value = "page";
		document.resultForm.action = url;
		document.resultForm.target = "_self";
		document.resultForm.submit();
		return false;
	}
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
 * 検索結果の現在ページを取得
 * @return 現在ページ
 */
function getPageNo() {
	return document.resultForm.pageNo.value;
}

/**
 * 検索結果の総ページを取得
 * @return 総ページ
 */
function getTotalPageNo() {
	return document.resultForm.totalPageNo.value;
}

/**
 * 種別取得
 * @return 種別
 */
function getType() {
	return document.resultForm.type.value;
}

/**
 * 再検索処理
 * @param リクエスト種別
 */
function parameterResetting(type) {
	if (type == "peak" || type == "diff") {
		document.resultForm.action = "PeakSearch.jsp";
	}
	else if (type == "quick") {
		document.resultForm.action = "QuickSearch.jsp";
	}
	else if (type == "rcdidx") {
		document.resultForm.action = "RecordIndex.jsp";
	}
	else if (type == "struct") {
		document.resultForm.action = "StructureSearch.jsp";
	}
	document.resultForm.target = "_self";
	document.resultForm.submit();
	return false;
}

/**
 * SubstructureSearchパラメータ入力画面に戻る
 */
function prevStructSearch() {
	document.resultForm.action = "StructureSearch.jsp";
	document.resultForm.target = "_self";
	document.resultForm.submit();
	return false;
}

/**
 * PeakSearchパラメータ入力画面に戻る
 */
function prevPeakSearchAdv() {
	document.resultForm.action = "PeakSearch.jsp";
	document.resultForm.target = "_self";
	document.resultForm.submit();
	return false;
}
