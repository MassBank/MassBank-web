/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
 * Copyright (C) 2018 MassBank consortium
 * 
 * This file is part of MassBank.
 * 
 * MassBank is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 ******************************************************************************/


function masterclick(elmnt) {
	// copy the state of the master checkbox to all items
	$("."+elmnt.id).prop("checked", elmnt.checked);
}

function itemclick(elmnt) {
	var id = elmnt.classList[1];
	// if one item gets unchecked master should be uncheck
	if (elmnt.checked == false) {
	 	$("#"+id).prop("checked",false);
	}
	else {
		// if all items are checked master should be checked
		if ($("."+id).length == $("."+id+":checked").length) $("#"+id).prop("checked",true)
	}
}

function insertExample1() {
	$("#qpeak").val("273.096 22\n289.086 107\n290.118 14\n291.096 999\n"
		  + "292.113 162\n293.054 34\n579.169 37\n580.179 15\n");
}

function insertExample2() {
	$("#qpeak").val("70 51; 71 13; 72 49; 73 999; 74 98;"
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
	  + "190 34; 191 8; 192 2; 199 2; 218 10;"
	  + "219 2; 220 1;");
}

$(document).ready(function() {
	function updateStorage() {
		$checkboxes.each(function() {
			formValues[this.id] = this.checked;
		});
		localStorage.setItem("formValues", JSON.stringify(formValues));
	}
	// save all checkboxes in ms_information form, state will persist reload
	var $checkboxes = $("[name='ms_information'] :checkbox");
	// get last values from localStorage or load defaults if empty
	var formValues = JSON.parse(localStorage.getItem('formValues'));
	// load defaults if empty
	if (formValues == null) {
		formValues = {};
		$('#ESI').trigger('click');
		if ($('#MS2').length == 0) {
			$('#ms').trigger('click');
		}
		else {
			$('#MS2').trigger('click');
		}
		updateStorage();
	};
	// load checkboxes state from localStorage
	$.each(formValues, function(key, value) {
		$("#" + key).prop('checked', value);
	});
	// update checkbox state on each change
	$checkboxes.on("change", updateStorage);
	// change color in ms information section slightly
	var ms_information_column = $(".ms_information_column:odd").addClass("w3-light-grey");
	
	// change text on dropdowns in keyword search
	$(".searchop").on("change", function(event) {
		console.log(this.value);
		if (this.value == 'or') {
			$(this.form).find( "div.searchoptext" ).html('<b>OR</b>');
			$(this.form).find( "input[name^='op']" ).val(this.value);
		}
		else {
			$(this.form).find( "div.searchoptext" ).html('<b>AND</b>');
			$(this.form).find( "input[name^='op']" ).val(this.value);
		}

		//console.log($(this.form).find( "div.searchoptext" ).text());
	});
	
	// put form information of the query and ms_information form together and submit
	$(".query").on("submit", function(event) {
		event.preventDefault();
		// check if there are options selected
		if ($("input[name='inst']:checked").length == 0 || $("input[name='ms']:checked").length == 0) {
			$.alert({
				theme : 'supervan',
				title : 'Error',
				content : 'Please select at least one Instrument Type and MS Type!',
			});
			return;
		}
		var form = document.createElement("form");
		form.setAttribute("method",this.method);
		form.setAttribute("action",this.action);
		$([this,document.forms["ms_information"] ]).serializeArray().forEach(function(element) {
			//console.log("name:",element["name"]," value:",element["value"]);
			var hiddenField = document.createElement("input");
			hiddenField.setAttribute("type","hidden");
			hiddenField.setAttribute("name",element["name"]);
			hiddenField.setAttribute("value",element["value"]);
			form.appendChild(hiddenField);
		});
		document.body.appendChild(form);
		form.submit();
	});
	
	
	
});