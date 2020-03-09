<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getRecordInfo メソッド</title>
<link rel="stylesheet" type="text/css" href="api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API リファレンス</span>
<hr>
<h1 class="method">getRecordInfo メソッド</h1>
<b>MassBankレコード情報（ピークデータ、分析条件等すべて含む）を取得します。</b><br>
<br>
<b>パラメータ</b><br>
<table border="1" width="850">
<tr>
<td><b>ids</b></td>
<td>[型]string 配列</td>
<td>取得するレコードのMassBank IDを指定します。</td>
</tr>
</table>
<br><br>
<b>レスポンス</b><br>
MassBankレコード情報を&nbsp;<span class="dtype">RecordInfo</span>&nbsp;型（以下の構造体）の配列で返します。<br>
<table border="1" width="850">
<tr><td>
&nbsp;&nbsp;-&nbsp;<b>id</b>&nbsp;:&nbsp;取得したレコード情報のMassBank&nbsp;ID&nbsp;[型]string<br>
&nbsp;&nbsp;-&nbsp;<b>info</b>&nbsp;:&nbsp;レコード情報&nbsp;[型]string<br>
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
$ids = array("KOX00001", "KOX00002", "TY000040", "FU000001" );
$params = array("ids" => $ids );

<font color="green">// メソッド呼び出し</font>
//$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
$soap = new SoapClient('http://localhost/api/services/MassBankAPI?wsdl');
<font color="blue">try {</font>
	$res = $soap-><b>getRecordInfo</b>( $params );
<font color="blue">}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}
</font>
<font color="green">// 取得内容表示</font>
$ret = $res->return;
for ($i = 0 ; $i < count($ret); $i++) {
	$info = str_replace("\n", "&lt;br&gt", $ret[$i]->info );
	echo "[" . $ret[$i]->id . "]&lt;br&gt;\n";
	echo $info . "&lt;br&gt;&lt;br&gt;\n";
}
</pre>
</div>
<br>
<br>
■取得内容(実際に取得しています)<br>
<div class="res2">
<?php
// パラメータセット
$ids = array("KOX00001", "KOX00002", "TY000040", "FU000001" );
$params = array("ids" => $ids );

// メソッド呼び出し
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
try {
	$res = $soap->getRecordInfo( $params );
}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}

// 取得内容表示
$ret = $res->return;
for ($i = 0 ; $i < count($ret); $i++) {
	$info = str_replace("\n", "<br>", $ret[$i]->info );
	echo "[" . $ret[$i]->id . "]<br>\n";
	echo $info . "<br><br>\n";
}
?>
</div>
</body>
</html>
