<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getPeak Method</title>
<link rel="stylesheet" type="text/css" href="./api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API References</span>
<hr>
<h1>getPeak Method</h1>
Get the peak data of MassBank records specified by Record IDs.<br>
<br>
<b>Parameter</b><br>
ids&nbsp;:&nbsp;MassBank IDs to get peak data. [type: array of string]<br><br>
<b>Response</b><br>
Array of Peak (the following structure)<br>
&nbsp;&nbsp;-&nbsp;id&nbsp;:&nbsp;MassBank IDs obtained. [type: string]<br>
&nbsp;&nbsp;-&nbsp;numPeaks&nbsp;:&nbsp;Number of peaks [type: int]<br>
&nbsp;&nbsp;-&nbsp;mzs&nbsp;:&nbsp;values of m/z [type: array of string]<br>
&nbsp;&nbsp;-&nbsp;intensities&nbsp;:&nbsp;values of intensity [type: array of string]<br>
<br><br>
</div>
<br>
PHP Sample Code<br>
<div class="src">
<pre>
<font color="green">// set parameters</font>
$ids = array("PR020001", "PR020002", "PR020003", "FU000001" );
$params = array("ids" => $ids );

<font color="green">// call method</font>
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
<font color="blue">try {</font>
	$res = $soap-><b>getPeak</b>( $params );
<font color="blue">}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}
</font>
<font color="green">// display obtained data</font>
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
Obtained Data (the above code is executed actually)<br>
<div class="res2">
<?php
// set parameters
$ids = array("PR020001", "PR020002", "PR020003", "FU000001" );
$params = array("ids" => $ids );

// call method
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
try {
	$res = $soap->getPeak( $params );
}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}

// display obtained data
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
