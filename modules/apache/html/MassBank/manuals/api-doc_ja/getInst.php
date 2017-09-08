<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getInstrumentTypes メソッド</title>
<link rel="stylesheet" type="text/css" href="api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API リファレンス</span>
<hr>
<h1 class="method">getInstrumentTypes メソッド</h1>
<b>MassBankに登録されているデータの分析機器種別すべてを取得します。</b><br>
<br>
<b>パラメータ</b><br>
なし<br>
<br>
<b>レスポンス</b><br>
分析機器種別を&nbsp;string&nbsp;型の配列で返します。<br>
<br>
<hr color="silver">
<br>
■PHPソースコード<br>
<div class="src">
<pre>
<font color="green">// パラメータセット</font>
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
$res = $soap-><b>getInstrumentTypes</b>();

<font color="green">// 取得内容表示</font>
for ($i = 0 ; $i < count($res->return); $i++) {
	echo $res->return[$i] . "&lt;br&gt;";
}
</pre>
</div>
<br>
<br>
■取得内容(実際に取得しています)<br>
<div class="res1">
<?php
// メソッド呼び出し
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
//$soap = new SoapClient('http://localhost/api/services/MassBankAPI?wsdl');
$res = $soap->getInstrumentTypes();

// 取得内容表示
for ($i = 0 ; $i < count($res->return); $i++) {
	echo $res->return[$i] . "<br>\n";
}
?>
</div>
</body>
</html>
