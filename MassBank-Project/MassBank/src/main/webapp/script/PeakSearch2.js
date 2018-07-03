

/**
 * リアルタイムMassCalc
 */
$.fn.massCalc = function() {
	
	$(this).each(function() {
		
		var prevFormula = "";	// 入力前の値を保持
		var targetIndex = 0;	// 入力対象のインデックスを保持
		
		// キーダウン時
		$(this).keydown(function(e) {
			prevFormula = $(this).val();
			targetIndex = $("input.Formula").index(this);
		});
		
		// キーアップ時
		$(this).keyup(function(e) {
			var inputFormula = $(this).val();
			if ( prevFormula == inputFormula ) {
				return;
			}
			var mass = "";
			if ( inputFormula != "" ) {
				var atomicArray = new Array();
				
				// 入力された組成式の前後の半角/全角スペースをトリム
				inputFormula = inputFormula.replace(/^[\s　]+|[\s　]+$/g, "");
				
				// 入力された組成式の全角文字を半角文字へ変換
				inputFormula = inputFormula.replace(/[Ａ-Ｚａ-ｚ０-９]/g, toHalfChar);
				
				// 組成式から原子(原子記号+原子数)に分解した配列を取得
				atomicArray = getAtomicArray(inputFormula);
				
				// 原子(原子記号+原子数)配列から原子ごとのm/zを全て加算した値を取得
				mass = massCalc(atomicArray);
			}
			
			// 結果を入力フォームに設定
			$("input.Mass:eq(" + targetIndex + ")").val(mass);
		});
	});
}

/**
 * 原子(原子記号+原子数)配列返却
 * @param formula 組成式(半角英数字)
 * @return atomicArray 組成式から求めた原子配列
 */
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
 * 組成式を元にmassを計算
 * @param atomicArray 原子(原子記号+原子数)配列
 * @return mass 組成式から求めたmass
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

/**
 * 全角->半角変換
 * @param fullChar 変換対象となる全角1字
 * @return 半角文字
 */
function toHalfChar(fullChar) {
	var halfChr = "";
	var halfCharList = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
					   "abcdefghijklmnopqlstuvwxyz" +
					    "0123456789";
	var fullCharList = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" +
					   "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ" +
					   "０１２３４５６７８９";
				       "ABCD"
	var index = fullCharList.indexOf(fullChar);
	
	halfChr = halfCharList.charAt(index);
	
	return halfChr;
}

/**
 * フォーム初期化
 */


function Test() {
	alert(getAtomicArray(massCalc('CH4')));
}
