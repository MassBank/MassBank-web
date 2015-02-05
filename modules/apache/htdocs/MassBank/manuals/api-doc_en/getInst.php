<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getInstrumentTypes Method</title>
<link rel="stylesheet" type="text/css" href="./api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API References</span>
<hr>
<h1>getInstrumentTypes Method</h1>
Get a list of the instrument types.<br>
<br>
<b>Parameter</b><br>
(none)<br>
<br>
<b>Response</b><br>
array of string<br>
<br><br>
</div>
<br>
PHP Sample Code<br>
<div class="src">
<pre>
// call method
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
$res = $soap-><b>getInstrumentTypes</b>();

// display obtained data
for ($i = 0 ; $i < count($res->return); $i++) {
	echo $res->return[$i] . "&lt;br&gt;";
}
</pre>
</div>
<br>
<br>
Obtained Data (the above code is excecuted actually)<br>
<div class="res1">
<?php
// call method
$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
$res = $soap->getInstrumentTypes();

// display obtained data
for ($i = 0 ; $i < count($res->return); $i++) {
	echo $res->return[$i] . "<br>\n";
}
?>
</div>
</body>
</html>
