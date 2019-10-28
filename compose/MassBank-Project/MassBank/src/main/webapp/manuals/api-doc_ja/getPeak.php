<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getPeak メソッド</title>
<link rel="stylesheet" type="text/css" href="api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API リファレンス</span>
<hr>
<h1 class="method">getPeak メソッド</h1>
<b>ピークデータを取得します。</b><br>
<br>
<b>パラメータ</b><br>
<table border="1" width="850">
<tr>
<td><b>ids</b></td>
<td>[型]string 配列</td>
<td>取得するピークデータのMassBank IDを指定します</td>
</tr>
</table>
<br><br>
<b>レスポンス</b><br>
ピークデータを&nbsp;<span class="dtype">Peak</span>&nbsp;型（以下の構造体）の配列で返します。<br>
<table border="1" width="850">
<tr><td>
&nbsp;&nbsp;-&nbsp;<b>id</b>&nbsp;:&nbsp;取得したピークデータのMassBank ID [型]string<br>
&nbsp;&nbsp;-&nbsp;<b>numPeaks</b>&nbsp;:&nbsp;ピーク数 [型]int<br>
&nbsp;&nbsp;-&nbsp;<b>mzs</b>&nbsp;:&nbsp;m/z [型]string 配列<br>
&nbsp;&nbsp;-&nbsp;<b>intensities</b>&nbsp;:&nbsp;intensity [型]string 配列<br>
</td></tr>
</table>
<br>
<b>例外</b><br>
エラーメッセージ<br>
"MassBank Record is not found."&nbsp;:&nbsp;該当するレコードが１つもない（存在しないMassBank IDを指定している）場合
<br><br>
</div>
<hr color="silver">
<br>
■PHPソースコード<br>
<div class="src">
<pre>
<font color="green">// パラメータセット</font>
$ids = array("PR020001", "PR020002", "PR020003", "FU000001" );
$params = array("ids" => $ids );

<font color="green">// メソッド呼び出し</font>
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
<font color="blue">try {</font>
	$res = $soap-><b>getPeak</b>( $params );
<font color="blue">}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}
</font>
<font color="green">// 取得内容表示</font>
$ret = $res->return;
for ($i = 0 ; $i < count($ret); $i++) {
	echo "[" . $ret[$i]->id . "]&lt;br&gt;\n";
	echo "NUM_PEAK:" . $ret[$i]->numPeaks . "&lt;br&gt;\n";
	for ($j = 0 ; $j < count($ret[$i]->mzs); $j++) {
		$mz = $ret[$i]->mzs[$j];
		$inte = $ret[$i]->intensities[$j];
		echo "mz:$mz, inte:$inte&lt;br&gt;\n";
	}
	echo "&lt;br&gt;&lt;br&gt;\n";
}
</pre>
</div>
<br>
<br>
■取得内容(実際に取得しています)<br>
<div class="res2">
<?php
// パラメータセット
$ids = array("PR020001", "PR020002", "PR020003", "FU000001" );
$params = array("ids" => $ids );

// メソッド呼び出し
//$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
$soap = new SoapClient('http://localhost/api/services/MassBankAPI?wsdl');
try {
	$res = $soap->getPeak( $params );
}
catch(SoapFault $e) {
	echo $e->getMessage();
	return;
}

// 取得内容表示
$ret = $res->return;
for ($i = 0 ; $i < count($ret); $i++) {
	echo "[" . $ret[$i]->id . "]<br>\n";
	echo "NUM_PEAK:" . $ret[$i]->numPeaks . "<br>\n";
	for ($j = 0 ; $j < count($ret[$i]->mzs); $j++) {
		$mz = $ret[$i]->mzs[$j];
		$inte = $ret[$i]->intensities[$j];
		echo "mz:$mz, inte:$inte<br>\n";
	}
	echo "<br><br>\n";
}
?>
</div>
</body>
</html>
