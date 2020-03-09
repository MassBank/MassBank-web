<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>searchSpectrum Method</title>
<link rel="stylesheet" type="text/css" href="api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API References</span>
<hr>
<h1 class="method">searchSpectrum Method</h1>
<b>Get the response equivalent to the "Spectrum Search" results.</b><br>
<br>
<b>Parameter</b><br>
<table border="1" width="850">
<tr>
<td><b>mzs</b></td><td>[type]array of string</td><td>values of m/z of peaks</td>
</tr>
<tr>
<td><b>intensities</b></td><td>[type]array of string</td><td>values of intensity of peaks</td>
</tr>
<tr>
<td><b>unit</b></td><td>[type]string</td><td>Unit of tolerance. Specify "unit" or "ppm". (Default: "unit")</td>
</tr>
<tr>
<td><b>tolerance</b></td><td>[type]string</td><td>Tolerance of values of m/z of peaks. (Default: 0.3 unit or 50 ppm)</td> 
</tr>
<tr>
<td><b>cutoff</b></td><td>[type]string</td><td>Ignore peaks whose intensity is not larger than the value of cutoff. (Default: 50)</td>
</tr>
<tr>
<td><b>instrumentTypes</b></td><td>[type]array of string</td>
<td>
Specify one or more instrument types. Not to restrict instrument types, specify "all".<br>
Do not specify other than the values obtained by getInstrumentTypes method.<br>
</td>
</tr>
<tr>
<td><b>ionMode</b></td><td>[type]string</td>
<td>
Ionization mode<br>
Specify one of "Positive", "Negative" or "Both" (case is ignored)<br>
</td>
</tr>
<tr>
</tr>
<tr>
<td><b>maxNumResults</b></td><td>[type]int</td><td>Maximum number of search results. "0" means unspecified and then all results are obtained.</td>
</tr>
</table>

<br><br>
<b>Response</b><br>
Array of &nbsp;<span class="dtype">SearchResult</span>&nbsp;(the following structure)<br>
<table border="1" width="850">
<tr><td>
&nbsp;&nbsp;-&nbsp;<b>numResults</b>&nbsp;:&nbsp;Number of search results&nbsp;[type]int<br>
&nbsp;&nbsp;-&nbsp;<b>results</b>&nbsp;:&nbsp;Inforamation of search results&nbsp;&nbsp;Array of <span class="dtype">Result</span>&nbsp;(the following structure)<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+&nbsp;<b>id</b>&nbsp;:&nbsp;MassBank ID [type]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+&nbsp;<b>title</b>&nbsp;:&nbsp;Record title [type]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;<b>formula</b>&nbsp;:&nbsp;Molecular formula [type]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;<b>exactMass</b>&nbsp;:&nbsp;Exact mass [type]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+&nbsp;<b>score</b>&nbsp;:&nbsp;Score [type]string<br>
</td></tr>
</table>
<br>
<b>Exception</b><br>
Error Message<br>
"Invalid parameter : xxxxx"&nbsp;:&nbsp;when an invalid parameter is specified
<br><br>
</div>
<hr color="silver">
<br>
PHP Sample Code<br>
<div class="src">
<pre>
<font color="green">// set parameters</font>
$mzs = array('273.096', '289.086', '290.118', '291.096', '292.113', '579.169', '580.179');
$inte = array('300', '300', '300', '300', '300', '300', '300' );
$unit = "";		<font color="green">// unit is optional (default: "unit")</font>
$tol = "";			<font color="green">// tolerance is optional (default: "0.3")</font>
$cutoff = "";		<font color="green">// cutoff is optional (default: "50")</font>
$inst = array("all");
$ion = "Positive";
$params = array(
	"mzs" => $mzs, "intensities" => $inte,   "unit" => $unit,
	"tolerance" => $tol, "cutoff" => $cutoff, "instrumentTypes" => $inst,
	"ionMode" => $ion, "maxNumResults" => 20
);

<font color="green">// call method</font>
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
<font color="blue">try {</font>
	$res = $soap-><b>searchSpectrum</b>( $params );
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
	echo "&lt;tr&gt\n";
	echo "&lt;td&gt$info->score&lt;/td&gt";
	echo "&lt;td&gt$info->title&lt;/td&gt";
	echo "&lt;td&gt$info->formula&lt;/td&gt";
	echo "&lt;td&gt$info->exactMass&lt;/td&gt";
	echo "&lt;td&gt$info->id&lt;/td&gt\n";
	echo "&lt;/tr&gt\n";
}
echo "&lt;/table&gt\n";
</pre>
</div>
<br>
<br>
Obtained Data (the above code is executed actually)<br>
<div class="res2">
<?php
$mzs = array('273.096', '289.086', '290.118', '291.096', '292.113', '579.169', '580.179');
$inte = array('300', '300', '300', '300', '300', '300', '300' );
$unit = "unit";
$tol = "0.3";
$cutoff = "50";
$inst = array("all");
$ion = "Positive";
$params = array(
	"mzs" => $mzs, "intensities" => $inte,   "unit" => $unit,
	"tolerance" => $tol, "cutoff" => $cutoff, "instrumentTypes" => $inst,
	"ionMode" => $ion, "maxNumResults" => 20
);


$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
try {
	$res = $soap->searchSpectrum( $params );
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
	echo "<td>$info->score</td>";
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
