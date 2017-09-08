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
 * ResultMenu用スクリプト
 *
 * ver 1.0.5 2010.01.08
 *
 ******************************************************************************/

var op = (window.opera) ? 1 : 0;								//OP
var ie = (!op && document.all) ? 1 : 0;							//IE
var ns4 = (document.layers) ? 1 : 0;							//NS4
var ns6 = (document.getElementById&&!document.all) ? 1 : 0;		//NS6

var isPrevDisp = false;		// 1つ前のメニュー表示フラグ
var isPrevClose = true;		// 1つ前の非表示フラグ

document.oncontextmenu = function(ev) { return dispRightMenu(ev, false); }
document.onmousedown = function(ev) { closeRightMenu(ev, true); }
document.onkeydown = function(ev) { keyEvent(ev); }

/**
 * オリジナル右クリックメニュー表示
 * @param ev イベント
 * @param isDisp メニュー表示フラグ
 * @return デフォルト右クリックメニュー表示フラグ（ture：表示、false：非表示）
 */
function dispRightMenu(ev, isDisp) {
	if ( document.resultForm == null ) {
		return true;
	}
	
	ele = document.getElementById("menu");
	
	if ( ie ) {					// IE
		ele.style.left = window.event.clientX + document.body.scrollLeft;
		ele.style.top  = window.event.clientY + document.body.scrollTop;
	}
	else if ( ns6 ){			// NS6, FF
		ele.style.left = ev.clientX + window.pageXOffset;
		ele.style.top  = ev.clientY + window.pageYOffset;
	}
	
	// マウスイベント用HTMLプロパティ生成
	if ( isDisp ) {
		openTreeProperty = "";
		closeTreeProperty = "";
		switch ( allTreeCtrlState() ) {
			case 1:
				openTreeProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
								 + " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
				closeTreeProperty = " class='menuItem1' onClick='closeRightMenu(event, true); allTreeCtrl();' onMouseDown='closeRightMenu(event, false);'"
								  + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
				break;
			case 0:
				openTreeProperty = " class='menuItem1' onClick='closeRightMenu(event, true); allTreeCtrl();' onMouseDown='closeRightMenu(event, false);'"
								 + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
				closeTreeProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
								  + " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
				break;
			case -1:
				openTreeProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
								 + " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
				closeTreeProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
								  + " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
				break;
		}
		
		checkProperty = "";
		unCheckProperty = "";
		if ( checkAllState() ) {
			checkProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
						  + " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
			unCheckProperty = " class='menuItem1' onClick='closeRightMenu(event, true); callCheckAll();' onMouseDown='closeRightMenu(event, false);'"
							+ " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
		}
		else {
			checkProperty = " class='menuItem1' onClick='closeRightMenu(event, true); callCheckAll();' onMouseDown='closeRightMenu(event, false);'"
						  + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
			unCheckProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
							+ " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
		}
		
		refType = getType();
		pageNo = parseInt(getPageNo());
		totalPageNo = parseInt(getTotalPageNo());
		topProperty = "";
		endProperty = "";
		nextProperty = "";
		prevProperty = "";
		if ( pageNo <= 1 ) {
			topProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
						+ " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
			prevProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
						 + " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
		}
		else {
			topProperty = " class='menuItem1' onClick='closeRightMenu(event, true); changePage(\"" + refType + "\", 1)' onMouseDown='closeRightMenu(event, false);'"
						+ " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
			prevProperty = " class='menuItem1' onClick='closeRightMenu(event, true); changePage(\"" + refType + "\", " + (pageNo-1) + ")' onMouseDown='closeRightMenu(event, false);'"
						 + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
		}
		
		if ( pageNo >= totalPageNo ) {
			endProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
						+ " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
			nextProperty = " class='menuItem2' onClick='closeRightMenu(event, false);' onMouseDown='closeRightMenu(event, false);'"
						 + " onMouseOver='changeBgColor(this, true, false)' onMouseOut='changeBgColor(this, false, false)'";
		}
		else {
			endProperty = " class='menuItem1' onClick='closeRightMenu(event, true); changePage(\"" + refType + "\", " + totalPageNo + ")' onMouseDown='closeRightMenu(event, false);'"
						+ " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
			nextProperty = " class='menuItem1' onClick='closeRightMenu(event, true); changePage(\"" + refType + "\", " + (pageNo+1) + ")' onMouseDown='closeRightMenu(event, false);'"
						 + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'";
		}
		
		
		// メニューHTML生成
		ele.innerHTML = "<table class='cursorDefault' width='100%' border='0' cellspacing='0' cellpadding='1' onselectstart='return false' onContextMenu='return dispRightMenu(event, true)'>"
				  + "<tr>"
				  + "<td nowrap" + openTreeProperty + ">Open All Tree</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap" + closeTreeProperty + ">Close All Tree</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td colspan='2'><hr></td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap class='menuItem1' onClick='closeRightMenu(event, true); clickShowSpectra();' onMouseDown='closeRightMenu(event, false);'"
				  + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'>Multiple Display</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap class='menuItem1' onClick='closeRightMenu(event, true); submitSearchPage();' onMouseDown='closeRightMenu(event, false);'"
				  + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'>Spectrum Search</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td colspan='2'><hr></td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap" + checkProperty + ">Check All</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap" + unCheckProperty + ">UnCheck All</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td colspan='2'><hr></td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap class='menuItem1' onClick='closeRightMenu(event, true); parameterResetting(\"" + refType + "\");' onMouseDown='closeRightMenu(event, false);'"
				  + " onMouseOver='changeBgColor(this, true, true)' onMouseOut='changeBgColor(this, false, true)'>Edit / Resubmit Query</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td colspan='2'><hr></td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap" + topProperty + ">First Page</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap" + prevProperty + ">Prev page</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap" + nextProperty + ">Next Page</td>"
				  + "</tr>"
				  + "<tr>"
				  + "<td nowrap" + endProperty + ">Last Page</td>"
				  + "</tr>"
				  + "</table>";
	}
	
	// テーブル内クリック時
	if (isDisp) {
		if ( ele.style.width < 200 ) {
			ele.style.width = 200;
		}
		ele.style.visibility = "visible";
		isPrevDisp = isDisp;
		return false;
	}
	// テーブル外クリック時
	else {
		if ( !isPrevDisp ) {
			ele.style.visibility = "hidden";
			return true;
		}
		else {
			isPrevDisp = false;
			return false;
		}
	}
}

/**
 * オリジナル右クリックメニュー非表示
 * @param ev イベント
 * @param isClose 非表示フラグ
 */
function closeRightMenu(ev, isClose) {
	if ( !isClose ) {
		isPrevClose = isClose;
	}
	else {
		if ( !isPrevClose ) {
			isPrevClose = isClose;
		}
		else {
			ele = document.getElementById("menu");
			ele.style.visibility = "hidden";
		}
	}
}

/**
 * オブジェクトの背景色とフォント色を変更する
 * @param obj 対象オブジェクト
 * @param isMouse マウス状態（true：マウスオーバー、false：マウスアウト）
 * @param isState 使用可否（true：使用可、false：使用不可）
 */
function changeBgColor(obj, isMouse, isState) {
	if ( isMouse ) {
		obj.style.background = "#93A070";
		obj.style.color = "#FFFFFF";
	}
	else {
		obj.style.background = "#FFCCCC";
		obj.style.color = "#000000";
	}
	
	// 使用不可の場合
	if ( !isState ) {
		obj.style.color = "#ACA899";
	}
}

/**
 * キーイベント処理
 * @param イベント
 */
function keyEvent(ev) {
	if ( ie ) {
		if ( window.event.keyCode == 27 ) {
			closeRightMenu(ev, true);
		}
	}
	else {
		if ( ev.keyCode == 27 ) {
			closeRightMenu(ev, true);
		}
	}
}
