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
 * 化学構造式プレビュー表示
 *
 * ver 1.0.0 2010.08.26
 *
 ******************************************************************************/

/**
 * 化学構造式プレビュー表示スクリプト
 * jqueryライブラリを利用してマウスオーバーで化学構造式のプレビューを表示する
 *
 */
this.structurePreview = function(){
	// 表示位置設定
	xOffset = 10;
	yOffset = 10;
	
	// 表示処理
	$("a.preview_structure").hover(function(e){
		this.t = this.title;
		this.title = "";	
		var c = (this.t != "") ? "<br/>" + this.t : "";
		$("body").append("<p id='preview_structure'><img src='"+ this.href +"' alt='Image loading...' />"+ c +"</p>");
		$("#preview_structure")
			//.css("top",(e.pageY - yOffset + ( ((e.pageY - yOffset - window.innerHeight) < 0) ? -(e.pageY - yOffset - window.innerHeight) : 0 ) ) + "px")
			.css("top",(e.pageY - yOffset) + "px")
			.css("left",(e.pageX + xOffset) + "px")
			.fadeIn("fast");
	},
	function(){
		this.title = this.t;
		$("#preview_structure").remove();
	});	
	$("a.preview_structure").mousemove(function(e){
		$("#preview_structure")
			.css("top",(e.pageY - yOffset) + "px")
			.css("left",(e.pageX + xOffset) + "px");
	});
};

/**
 * 化学構造式表示スクリプト呼び出し
 * starting the script on page load
 */
$(document).ready(function(){
	structurePreview();
});
