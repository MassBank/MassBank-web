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

/**
 * MassCalculator
 */
function openMassCalc() {
	$("#MassCalc").fadeIn();
}
function closeMassCalc() {
	$("#MassCalc").fadeOut();
}
function resetMassCalc() {
	$(".Mass").val("");
	$(".Formula").val("");
	$(".Formula:eq(0)").focus();
}

$(document).ready(function() {
	$('#openMassCalc').click(function(){ openMassCalc(); return false; });
	$('#closeMassCalc').click(function(){ closeMassCalc(); return false; });
	//update mass according to formula
	$(".Formula").on("keyup", function(){
			var inputFormula = $(this).val();
			inputFormula = inputFormula.trim();
			inputFormula = getAtomicArray(inputFormula);
			mass = massCalc(inputFormula);
			if (mass > 0) {
				$(".Mass:eq("+$(".Formula").index(this)+")").val(mass);
			} else {
				$(".Mass:eq("+$(".Formula").index(this)+")").val("");
			}
        }  
    );
});

var atomicMass = {
	"H" : 1.0078250321,
	"He" : 4.0026032497,
	"Li" : 7.0160040,
	"Be" : 9.0121821,
	"B" : 11.0093055,
	"C" : 12.0,
	"N" : 14.0030740052,
	"O" : 15.9949146221,
	"F" : 18.99840320,
	"Ne" : 19.9924401759,
	"Na" : 22.98976967,
	"Mg" : 23.98504190,
	"Al" : 26.98153844,
	"Si" : 27.9769265327,
	"P" : 30.97376151,
	"S" : 31.97207069,
	"Cl" : 34.96885271,
	"Ar" : 35.96754628,
	"K" : 38.9637069,
	"Ca" : 39.9625912,
	"Sc" : 44.9559102,
	"Ti" : 47.9479471,
	"V" : 50.9439637,
	"Cr" : 51.9405119,
	"Mn" : 54.9380496,
	"Fe" : 55.9349421,
	"Co" : 58.9332002,
	"Ni" : 57.9353479,
	"Cu" : 62.9296011,
	"Zn" : 63.9291466,
	"Ga" : 68.925581,
	"Ge" : 73.9211782,
	"As" : 74.9215964,
	"Se" : 79.9165218,
	"Br" : 78.9183376,
	"Kr" : 83.911507,
	"Rb" : 84.9117893,
	"Sr" : 87.9056143,
	"Y" : 88.9058479,
	"Zr" : 95.908276,
	"Nb" : 92.9063775,
	"Mo" : 91.906810,
	"Tc" : 96.906365,
	"Ru" : 101.9043495,
	"Rh" : 102.905504,
	"Pd" : 105.903483,
	"Ag" : 106.905093,
	"Cd" : 113.9033581,
	"In" : 114.903878,
	"Sn" : 119.9021966,
	"Sb" : 120.9038180,
	"Te" : 129.9062228,
	"I" : 126.904468,
	"Xe" : 131.9041545,
	"Cs" : 132.905447,
	"Ba" : 137.905241,
	"La" : 138.906348,
	"Ce" : 139.905434,
	"Pr" : 140.907648,
	"Nd" : 141.907719,
	"Pm" : 144.912744,
	"Sm" : 151.919728,
	"Eu" : 152.921226,
	"Gd" : 157.924101,
	"Tb" : 158.925343,
	"Dy" : 163.929171,
	"Ho" : 164.930319,
	"Er" : 165.930290,
	"Tm" : 168.934211,
	"Yb" : 173.9388581,
	"Lu" : 174.9407679,
	"Hf" : 179.9465488,
	"Ta" : 180.947996,
	"W" : 183.9509326,
	"Re" : 186.9557508,
	"Os" : 191.961479,
	"Ir" : 192.962924,
	"Pt" : 194.964774,
	"Au" : 196.966552,
	"Hg" : 201.970626,
	"Tl" : 204.974412,
	"Pn" : 207.976636,
	"Bi" : 208.980383,
	"Po" : 209.982857,
	"At" : 209.987131,
	"Rn" : 222.0175705,
	"Fr" : 223.0197307,
	"Ra" : 226.0254026,
	"Ac" : 227.0277470,
	"Th" : 232.0380504,
	"Pa" : 231.0358789,
	"U" : 238.0507826,
	"Np" : 237.0481673,
	"Pu" : 239.0521565,
	"Am" : 243.0613727,
	"Cm" : 247.070347,
	"Bk" : 247.070299,
	"Cf" : 251.079580,
	"Es" : 252.082970,
	"Fm" : 257.095099
};

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
 * MassCalc（Formula to m/z）
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

