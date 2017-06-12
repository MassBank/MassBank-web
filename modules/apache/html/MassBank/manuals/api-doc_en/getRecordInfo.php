<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getRecordInfo Method</title>
<link rel="stylesheet" type="text/css" href="./api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API References</span>
<hr>
<h1>getRecordInfo Method</h1>
Get the data of MassBank records specified by Record IDs.<br>
<br>
<b>Parameter</b><br>
ids&nbsp;:&nbsp;MassBank IDs of records to get. [type: array of string]
<br><br>
<b>Response</b><br>
Array of RecordInfo (the following structure)<br>
&nbsp;&nbsp;-&nbsp;id&nbsp;:&nbsp;MassBank IDs of obtained records [type: string]<br>
&nbsp;&nbsp;-&nbsp;info&nbsp;:&nbsp;Record information [type: string]<br>
<br><br>
</div>
<br>
PHP Sample Code<br>
<div class="src">
<pre>
<font color="green">// set parameters</font>
$ids = array("KOX00001", "KOX00002", "TY000040", "FU000001" );
$params = array("ids" => $ids );

<font color="green">// call method</font>
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
<font color="blue">try {</font>
	$res = $soap-><b>getRecordInfo</b>( $params );
<font color="blue">}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}
</font>
<font color="green">// display obtained data</font>
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
Obtained Data (the above code is executed actually)<br>
<div class="res2">
<?php
// set parameters
$ids = array("KOX00001", "KOX00002", "TY000040", "FU000001" );
$params = array("ids" => $ids );

// call method
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
$res = $soap->getRecordInfo( $params );

// display obtained data
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
