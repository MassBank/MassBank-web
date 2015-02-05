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
 * 分子式入力の補完を行うスクリプト
 *
 * ver 1.0.3 2011.07.22
 *
 ******************************************************************************/
$(function(){
	$("input.FormulaSuggest").FormulaSuggest();
});

$.fn.FormulaSuggest = function(options){
	// オプション
	var c = $.extend({
		json: "ion_mass.json",
		top: 2,
		left: 0,
		close: "Close"
	},options);

	var allList = [];
	var myself;

	// keyUp時に行う
	function keyUpMatching(elmApId, target){
		$(elmApId).empty();
		var tval = target.val();

		// 未入力または、"C"の1文字のみの入力の場合は、表示しない
		if ( tval == "" || tval == "C" || tval.match(/[^A-Za-z0-9\.]/g) ) {
			$(elmApId).hide();
			return;
		}

		// リストに表示する値が無い場合は表示しない
		if ( allList.length == 0 ) {
			$(elmApId).hide();
			return;
		}

		var matchData = new Array();
		var reg = new RegExp("^" + tval,"i");
		for ( var i = 0; i < allList.length; i++ ){
			// 式での入力の場合
			if ( tval.match(/[^0-9\.]/g) ) {
				val = allList[i][0];
			}
			// 数値のみの入力の場合
			else {
				val = allList[i][1];
			}
			if ( val.match(reg) ) {
				matchData.push( "<font color=yellow>" + allList[i][0] + "</font> (" + allList[i][1] + ")");
			}
		}
		// 入力した値にマッチする値が無い場合は表示しない
		if ( matchData.length == 0 ) {
			$(elmApId).hide();
		}
		else {
			setList(elmApId, matchData, self);
		}
	}

	function setList(elmApId, data, self) {
		$(elmApId).append("<span class='close' title='" + c.close + "'>" + c.close + "</span>");
		for ( var i = 0; i < data.length; i++ ) {
			// データを全て書き出す
//			$("ul",elmApId).append("<li><a href='#'>" + data[i] + "</a></li>");
			$(elmApId).append("<a href='#'>" + data[i] + "</a>");
		}

		// 値をクリックした場合
		$("a",elmApId).click(function(){
			var sval = $(this).text();
			var item = sval.split(" ");
			myself.val(item[0]);
			$(elmApId).hide();
			return false;
		});

		// 閉じるボタンをクリックした場合
		$("span.close",elmApId).click(function(){
			$(elmApId).hide();
		});
	}
	
	// 位置を決めて表示する - elmApId用
	$.fn.setPosAndShow = function(pos,self,bgcolor){
		$(this).css({
			position: "absolute",
			top: pos.top + self.attr("offsetHeight") + c.top,
			left: pos.left + c.left,
			background: bgcolor
		}).show();
	}
	
	$(this).each(function(){
		// よく使う変数
		var self = $(this);
		var id = self.attr("id");
		var cl = "FormulaSuggest";
		var apId = id + "_" + cl;
		var elmApId = "#" + apId;
		var prevVal;
		var fname = "ion_mass.json";
		var bgcolor = "navy";
		if ( document.forms[0].searchof[0].checked ) {
			fname = "ion_mass.json";
			bgcolor = "navy";
		}
		else if ( document.forms[0].searchof[1].checked ) {
			fname = "nloss_mass.json";
			bgcolor = "darkgreen";
		}
		
		// 検索種別ラジオボタン変更時
		$('input[name="searchof"]:radio').change(function(){
			$(elmApId).empty();
			$(elmApId).hide();
			if ( $('input[name="searchof"]:checked').val() == "peak" ) {
				fname = "ion_mass.json";
				bgcolor = "navy";
			}
			else if ( $('input[name="searchof"]:checked').val() == "diff" ) {
				fname = "nloss_mass.json";
				bgcolor = "darkgreen";
			}
			$.getJSON(
				fname,
				function(jdata){
				allList = jdata;
			});
		});
		$('input[name="searchby"]:radio').change(function(){
			$(elmApId).empty();
			$(elmApId).hide();
		});
		
		// 検索種別ラベルクリック時
		$('span[name="typeLbl"]').click(function(){
			$(elmApId).empty();
			$(elmApId).hide();
			if ( $('input[name="searchof"]:checked').val() == "peak" ) {
				fname = "ion_mass.json";
				bgcolor = "navy";
			}
			else if ( $('input[name="searchof"]:checked').val() == "diff" ) {
				fname = "nloss_mass.json";
				bgcolor = "darkgreen";
			}
			$.getJSON(
				fname,
				function(jdata){
				allList = jdata;
			});
		});
		
		// input.incSearchフォーカス時
		$(this).focus(function(){
			pos = self.offset();
			$("div." + cl).hide();
			myself = self;
			
			// ページロード後初めてselfをクリックした場合
			if ( !$(elmApId) || $(elmApId).length == 0 ) {
				$("body").append("<div id='" + apId + "'></div>");
				$("span.close",elmApId).click(function(){ $(elmApId).hide(); });
				$(elmApId).addClass(cl).setPosAndShow(pos, self, bgcolor);
				$(elmApId).hide();
				$.getJSON(
					fname,
					function(jdata){
						allList = jdata;
					}
				);
			}
			// すでにelmApIdが生成されている場合
			if ( $(elmApId) && $(elmApId).length > 0 ) {
				$(elmApId).setPosAndShow(pos, self, bgcolor);
				keyUpMatching(elmApId, self);
			}
		}).keyup(function(e){		// Up時にmatchingで評価
			if ( e.keyCode == 27 ) {		// esc(27)を押した場合は隠す
				$(elmApId).hide();
			}
			else {
				if ( self.val() != prevVal ) {
					$(elmApId).show();
				}
				keyUpMatching(elmApId, self);
				prevVal = self.val();
			}
		});
		
		// bodyタグ内でのキーUpイベント
		$("body").keyup(function(e){
			if ( e.keyCode == 27 ) {		// esc(27)を押した場合は隠す
				$(elmApId).hide();
			}
		});
	});
}
