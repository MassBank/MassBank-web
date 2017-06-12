<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>searchPeak Method</title>
<link rel="stylesheet" type="text/css" href="./api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API References</span>
<hr>
<h1>searchPeak Method</h1>
Get the response equivalent to the "Peak Search" results.<br>
<br>
<b>Parameter</b><br>
<table border="1">
<tr>
<td>mzs</td><td>[type: array of string]</td><td>values of m/z of peaks</td>
</tr>
<tr>
<td>relativeIntensity</td><td>[type: string]</td><td>value of relative intensity of peaks</td>
</tr>
<tr>
<td>tolerance</td><td>[type: string]</td><td>Tolerance.</td> 
</tr>
<tr>
<td>cutoff</td><td>[type: string]</td><td>Ignore peaks whose intensity is not larger than the value of cutoff.</td>
</tr>
<tr>
<td>instrumentTypes</td><td>[type: array of string]</td>
<td>
Specify one or more instrument types. Not to restrict instrument types, specify "all".<br>
Do not specify the values obtained by getInstrumentTypes method.<br>
</td>
</tr>
<tr>
<td>ionMode</td><td>[type: string]</td>
<td>
Ionization mode<br>
Specify one of "Positive", "Negative" or "Both" (case is ignored)<br>
</td>
</tr>
<tr>
</tr>
<tr>
<td>maxNumResults</td><td>[type: int]</td><td>Maximum number of search results. "0" means unspecified and then all results are obtained.</td>
</tr>
</table>

<br><br>
<b>Response</b><br>
Array of SearchResult (the following structure)<br>
&nbsp;&nbsp;|<br>
&nbsp;&nbsp;+&nbsp;numResults&nbsp;:&nbsp;Number of search results [type: int]<br>
&nbsp;&nbsp;+&nbsp;Array of Result (the following structure)<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+&nbsp;id&nbsp;:&nbsp;MassBank ID [type: string]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;title&nbsp;:&nbsp;Record Title [type: string]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;formula&nbsp;:&nbsp;Molecular formula [type: string]<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+&nbsp;exactMass&nbsp;:&nbsp;Exact mass [type: string]<br>
<br><br>
</div>
<br>
PHP Sample Code<br>
<div class="src">
<pre>
<font color="green">// set parameters</font>
$mzs = array('80', '85');
$inte = "200";
$tol = "0.3";
$inst = array("all");
$ion = "Both";
$params = array(
	"mzs" => $mzs, "relativeIntensity" => $inte,   "tolerance" => $tol,
	"instrumentTypes" => $inst, "ionMode" => $ion, "maxNumResults" => 0
);

<font color="green">// call method</font>
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
<font color="blue">try {</font>
	$res = $soap->searchPeak( $params );
<font color="blue">}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}
</font>
<font color="green">// display obtained data</font>
$ret = $res->return;
echo "&lt;table&gt;\n";
for ($i = 0 ; $i < $ret->numResults; $i++) {
	$info = $ret->results[$i];
	echo "<tr>\n";
	echo "<td>$info->title</td>";
	echo "<td>$info->formula</td>";
	echo "<td>$info->exactMass</td>";
	echo "<td>$info->id</td>\n";
	echo "</tr>\n";
}
echo "&lt;/table&gt\n";
</pre>
</div>
<br>
<br>
Obtained Data (the above code is executed actually)<br>
<div class="res2">
<?php
// set parameters
$mzs = array('80', '85');
$inte = "200";
$tol = "0.3";
$inst = array("all");
$ion = "Both";
$params = array(
	"mzs" => $mzs, "relativeIntensity" => $inte,   "tolerance" => $tol,
	"instrumentTypes" => $inst, "ionMode" => $ion, "maxNumResults" => 0
);

// call method
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
try {
	$res = $soap->searchPeak( $params );
}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}

// display obtained data
$ret = $res->return;
echo "<table>\n";
for ($i = 0 ; $i < $ret->numResults; $i++) {
	$info = $ret->results[$i];
	echo "<tr>\n";
	echo "<td>$info->title</td>";
	echo "<td>$info->formula</td>";
	echo "<td>$info->exactMass</td>";
	echo "<td>$info->id</td>\n";
	echo "</tr>\n";
}
echo "</table>\n";
?>
</div>
</body>
</html>
