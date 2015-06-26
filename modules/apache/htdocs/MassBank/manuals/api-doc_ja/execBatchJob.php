<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>execBatchJob メソッド</title>
<link rel="stylesheet" type="text/css" href="api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API リファレンス</span>
<hr>
<h1 class="method">execBatchJob メソッド</h1>
<b>バッチ処理にて一括検索を実行します。</b><br>
<ul>
	<li>ジョブの実行状況は、&nbsp;<a href="getJobStatus.php">getJobStatus</a>&nbsp;メソッドにより取得できます。</li>
	<li>検索結果は、&nbsp;<a href="getJobResult.php">getJobResult</a>&nbsp;メソッドにより取得できます。getJobResult&nbsp;メソッドは、ジョブ実行状況を確認した上で呼び出してください。<br>なお、検索結果等の情報はサーバ側に数日間保持されます。
</li>
	<li>メールアドレスを指定することにより、検索結果をメールで受け取ることができます。メールには、<a href="MassBankResults.txt">テキスト形式</a>と<a href="MassBankResults.html">HTML形式</a>のファイルが<br>添付されます。クエリのスペクトルが大量にある場合は、添付ファイルのサイズが大きくなりますのでご注意ください。<br>
	添付ファイルサイズは、1000スペクトルをクエリにした場合には、テキスト形式が1.5MB、HTML形式が5MB程度になります。
	</li>
</ul>

<br>
<b>パラメータ</b><br>
<table border="1" width="850">
<tr>
<td><b>type</b></td><td>[型]string</td>
<td>
実行するジョブ種別の番号<br>
1:&nbsp;searchSpectrum
</td>
</tr>
<tr>
<td><b>mailAddress</b></td><td>[型]string</td><td>検索結果を受け取るメールアドレス（メール不要の場合は空文字にしてください）</td>
</tr>
<tr>
<td><b>queryStrings</b></td><td>[型]string 配列</td>
<td>クエリのスペクトル<br>
&nbsp;&nbsp;配列の1つの要素に1スペクトル分の情報を格納します。<i>m/z</i>&nbsp;とintensity&nbsp;のペアの繋がりを1行で記述してください。<br>
<br>
&nbsp;&nbsp;<b><i>m/z</i>(茶色)&nbsp;とintensity(紫色)&nbsp;の間に「,」カンマを入れたペアを「;」セミコロン区切りで記述してください。</b>
<br>
&nbsp;&nbsp;(例)&nbsp;<b><font color="maroon">58.500</font>,<font color="blueviolet">39604.0</font>; <font color="maroon">73.200</font>,<font color="blueviolet">14851.5</font>;</b><br>
<br>
&nbsp;&nbsp;<b>Name&nbsp;タグ(緑色)は検索結果の見出しになりますので、付与することを推奨します。</b>
<br>
&nbsp;&nbsp;(例)&nbsp;<b><font color="green">Name: Compound1;</font>&nbsp;<font color="maroon">58.500</font>,<font color="blueviolet">39604.0</font>; <font color="maroon">73.200</font>,<font color="blueviolet">14851.5</font></b>
<br>
&nbsp;&nbsp;&nbsp;※ Name&nbsp;タグを付けない場合は、システム側で自動付与します

</td>
</tr>
<td><b>instrumentTypes</b></td><td>[型]string 配列</td>
<td>
分析機器種別を指定する（複数指定可）。"all"を指定した場合は、すべての種別が検索対象になります。<br>
getInstrumentTypes メソッドにて取得した値以外は指定しないでください。<br>
</td>
</tr>
<tr>
<td><b>ionMode</b></td><td>[型]string</td>
<td>
イオン化モード<br>
"Positive", "Negative", "Both"のいずれか(大文字・小文字区別なし)を指定します。<br>
</td>
</tr>
<tr>
</tr>
</table>
<br><br>
<b>レスポンス</b><br>
ジョブIDを&nbsp;string&nbsp;型で返します。<br>
<br><br>
<b>例外</b><br>
エラーメッセージ<br>
"Invalid parameter : xxxxx" : パラメータが不正な場合
<br><br>
</div>
<hr color="silver">
<br>
■PHPソースコード<br>
<div class="src">
<pre>
<font color="green">// パラメータセット</font>
$mail = "hogehoge@massbank.jp";
$query = array(
	"Name:Sample1; 59.300,653466.0; 112.300,19802.0;",
	"Name:Sample2; 30.000,34653.5; 80.100,430693.5;",
	"Name:Sample3; 80.100,430693.5; 55.900,89109.0; 60.100,391089.5;"
);
$inst = array("all");
$ion = "Positive";
$params = array(
	"type" => "1", "mailAddress" => $mail,
	"queryStrings" => $query,
 	"instrumentTypes" => $inst, "ionMode" => $ion
);

$soap = new SoapClient('http://www.massbank.jp/api/services/MassBankAPI?wsdl');
<font color="green">// バッチ処理実行</font>
try {
	$res1 = $soap-><b>execBatchJob</b>( $params );
}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}

$job_id = $res1->return;

for ($lp = 0 ; $lp < 60; $lp++) {
	sleep(60);

	<font color="green">// ジョブステータス取得</font>
	try {
		$res2 = $soap-><b>getJobStatus</b>( array("jobId" => $job_id) );
	}
	catch (SoapFault $e) {
		echo $e->getMessage();
		return;
	}

	<font color="green">// ステータスが"処理完了"であるか？</font>
	$status_info = $res2->return;
	if ( $status_info->status == "Completed" ) {
		break;
	}
}

<font color="green">// 結果取得</font>
try {
	$res3 = $soap-><b>getJobResult</b>( array("jobId" => $job_id) );
}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}

<font color="green">// 結果表示</font>
$result_set_array = $res3->return;
foreach ($result_set_array as $result_set) {
	echo "&lt;table border=\"1\" width=\"800\">\n";
	echo "&lt;tr&gt;\n";
	echo "&lt;th colspan=\"5\" bgcolor=\"tan\"&gt;Name:" . $result_set->queryName . "&lt;/th&gt;\n";
	echo "&lt;/tr&gt;\n";
	$results = $result_set->results;
	if ( $result_set->numResults > 0 ) {
		for ($i = 0; $i &lt; $result_set->numResults; $i++) {
			echo "&lt;tr&gt;\n";
			echo "&lt;td&gt;" . $results[$i]->id . "&lt;/td&gt;";
			echo "&lt;td&gt;" . $results[$i]->title . "&lt;/td&gt;";
			echo "&lt;td&gt;" . $results[$i]->formula . "&lt;/td&gt;";
			echo "&lt;td&gt;" . $results[$i]->exactMass ."&lt;/td&gt;";
			echo "&lt;td&gt;" . $results[$i]->score ."&lt;/td&gt;\n";
			echo "&lt;/tr&gt;\n";
		}
		echo "&lt;/table&gt;\n";
		echo "&lt;br&gt;&lt;br&gt;\n";
	}
}
</font>
</pre>
</div>
<br>
<br>
■実行結果<br>
<table border="1" width="800">
<tr>
<th colspan="5" bgcolor="tan">Name:Sample1</th>
</tr>
<tr>
<td>KO007434</td><td>Glucosaminate; MS/MS; QqTOF; CE:30 V; [M+H]+</td><td>C6H13NO6</td><td>195.07429</td><td>0.332743216985</td>

</tr>
<tr>
<td>KO008793</td><td>Zalcitabine; MS/MS; QqTOF; CE:30 V; [M+H]+</td><td>C9H13N3O3</td><td>211.09569</td><td>0.329629198196</td>
</tr>
<tr>
<td>KO002907</td><td>N-Formylmethionine; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C6H11NO3S</td><td>177.04596</td><td>0.314935858280</td>
</tr>

<tr>
<td>KO004274</td><td>Zalcitabine; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C9H13N3O3</td><td>211.09569</td><td>0.310268851943</td>
</tr>
<tr>
<td>KO008794</td><td>Zalcitabine; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C9H13N3O3</td><td>211.09569</td><td>0.296979042792</td>
</tr>
<tr>

<td>KO007003</td><td>Cytosine arabinoside; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C9H13N3O5</td><td>243.08552</td><td>0.268674599679</td>
</tr>
<tr>
<td>KO003007</td><td>Glucosaminate; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C6H13NO6</td><td>195.07429</td><td>0.263307583821</td>
</tr>
<tr>
<td>KO003346</td><td>4-Methyl-5-thiazoleethanol; MS/MS; QqQ; CE:50 V; [M+H]+</td><td>C6H9NOS</td><td>143.04048</td><td>0.263085430179</td>

</tr>
<tr>
<td>KO004275</td><td>Zalcitabine; MS/MS; QqQ; CE:50 V; [M+H]+</td><td>C9H13N3O3</td><td>211.09569</td><td>0.237873574930</td>
</tr>
<tr>
<td>KO003006</td><td>Glucosaminate; MS/MS; QqQ; CE:30 V; [M+H]+</td><td>C6H13NO6</td><td>195.07429</td><td>0.233280373531</td>
</tr>

<tr>
<td>KO008795</td><td>Zalcitabine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C9H13N3O3</td><td>211.09569</td><td>0.224262623599</td>
</tr>
<tr>
<td>KO003345</td><td>4-Methyl-5-thiazoleethanol; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C6H9NOS</td><td>143.04048</td><td>0.205072254437</td>
</tr>
<tr>

<td>KO003656</td><td>Octopine; MS/MS; QqQ; CE:50 V; [M+H]+</td><td>C9H18N4O4</td><td>246.13281</td><td>0.159741861817</td>
</tr>
<tr>
<td>KO008530</td><td>Specitinomycin; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C14H24N2O7</td><td>332.15835</td><td>0.151279789599</td>
</tr>
<tr>
<td>KO007433</td><td>Glucosaminate; MS/MS; QqTOF; CE:20 V; [M+H]+</td><td>C6H13NO6</td><td>195.07429</td><td>0.144159570525</td>

</tr>
<tr>
<td>KO003698</td><td>Pantothenate; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C9H17NO5</td><td>219.11067</td><td>0.140839650663</td>
</tr>
<tr>
<td>KO008260</td><td>Propylthiouracil; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C7H10N2OS</td><td>170.05138</td><td>0.127438617559</td>
</tr>

<tr>
<td>KO002798</td><td>Daminozide; MS/MS; QqQ; CE:30 V; [M+H]+</td><td>C6H12N2O3</td><td>160.08479</td><td>0.125149708001</td>
</tr>
<tr>
<td>KO008185</td><td>Pantothenate; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C9H17NO5</td><td>219.11067</td><td>0.120844661943</td>
</tr>
<tr>

<td>KO003005</td><td>Glucosaminate; MS/MS; QqQ; CE:20 V; [M+H]+</td><td>C6H13NO6</td><td>195.07429</td><td>0.116666006930</td>
</tr>
</table>
<br><br>
<table border="1" width="800">
<tr>
<th colspan="5" bgcolor="tan">Name:Sample2</th>
</tr>
<tr>
<td>KO002405</td><td>(Aminomethyl)phosphonate; MS/MS; QqQ; CE:30 V; [M+H]+</td><td>CH6NO3P</td><td>111.00853</td><td>0.991818407760</td>

</tr>
<tr>
<td>KO002404</td><td>(Aminomethyl)phosphonate; MS/MS; QqQ; CE:20 V; [M+H]+</td><td>CH6NO3P</td><td>111.00853</td><td>0.676787141247</td>
</tr>
<tr>
<td>KO008101</td><td>Nornicotine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C9H12N2</td><td>148.10005</td><td>0.530833881721</td>
</tr>

<tr>
<td>KO006552</td><td>Anabasine; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C10H14N2</td><td>162.1157</td><td>0.394746543614</td>
</tr>
<tr>
<td>KO006553</td><td>Anabasine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C10H14N2</td><td>162.1157</td><td>0.379324536093</td>
</tr>
<tr>

<td>KO006551</td><td>Anabasine; MS/MS; QqTOF; CE:30 V; [M+H]+</td><td>C10H14N2</td><td>162.1157</td><td>0.357458431230</td>
</tr>
<tr>
<td>KO007801</td><td>4-Methyl-5-thiazoleethanol; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H9NOS</td><td>143.04048</td><td>0.245597772284</td>
</tr>
<tr>
<td>KO006517</td><td>2-Aminoethylphosphonate; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C2H8NO3P</td><td>125.02418</td><td>0.181088725776</td>

</tr>
<tr>
<td>KO008256</td><td>Pseudopelletierine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C9H15NO</td><td>153.11536</td><td>0.178134921798</td>
</tr>
<tr>
<td>KO007576</td><td>5-Hydroxylysine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H14N2O3</td><td>162.10044</td><td>0.171289255584</td>
</tr>

<tr>
<td>KO007446</td><td>Galactosamine 1-phosphate; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H14NO8P</td><td>259.0457</td><td>0.168056563156</td>
</tr>
<tr>
<td>KO007425</td><td>Glucosamine; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.163103503467</td>
</tr>
<tr>

<td>KO008690</td><td>Thiamine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C12H17N4OS</td><td>265.11231</td><td>0.159514608029</td>
</tr>
<tr>
<td>KO007575</td><td>5-Hydroxylysine; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H14N2O3</td><td>162.10044</td><td>0.148091014407</td>
</tr>
<tr>
<td>KO006518</td><td>2-Aminoethylphosphonate; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C2H8NO3P</td><td>125.02418</td><td>0.148044559626</td>

</tr>
<tr>
<td>KO007426</td><td>Glucosamine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.141770073616</td>
</tr>
<tr>
<td>KO007445</td><td>Galactosamine 1-phosphate; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H14NO8P</td><td>259.0457</td><td>0.141769257245</td>
</tr>

<tr>
<td>KO007580</td><td>Histidinol; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H11N3O</td><td>141.09021</td><td>0.139963012225</td>
</tr>
<tr>
<td>KO002997</td><td>Glucosamine; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.134523836788</td>
</tr>
<tr>

<td>KO007581</td><td>Histidinol; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H11N3O</td><td>141.09021</td><td>0.127913647792</td>
</tr>
</table>
<br><br>
<table border="1" width="800">
<tr>
<th colspan="5" bgcolor="tan">Name:Sample3</th>
</tr>
<tr>
<td>KO007425</td><td>Glucosamine; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.319847567416</td>

</tr>
<tr>
<td>KO002997</td><td>Glucosamine; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.316066578573</td>
</tr>
<tr>
<td>KO007426</td><td>Glucosamine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.290066849033</td>
</tr>

<tr>
<td>KO002987</td><td>Galactosamine; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.265270054120</td>
</tr>
<tr>
<td>KO007424</td><td>Glucosamine; MS/MS; QqTOF; CE:30 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.261544225901</td>
</tr>
<tr>

<td>KO003018</td><td>Galactosamine 1-phosphate; MS/MS; QqQ; CE:50 V; [M+H]+</td><td>C6H14NO8P</td><td>259.0457</td><td>0.249522588565</td>
</tr>
<tr>
<td>KO002996</td><td>Glucosamine; MS/MS; QqQ; CE:30 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.237198964530</td>
</tr>
<tr>
<td>KO003142</td><td>Histidinol; MS/MS; QqQ; CE:50 V; [M+H]+</td><td>C6H11N3O</td><td>141.09021</td><td>0.236523446883</td>

</tr>
<tr>
<td>KO007415</td><td>Galactosamine; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.233713562874</td>
</tr>
<tr>
<td>KO007945</td><td>Mannosamine; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.230281898003</td>
</tr>

<tr>
<td>KO007446</td><td>Galactosamine 1-phosphate; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H14NO8P</td><td>259.0457</td><td>0.223288641815</td>
</tr>
<tr>
<td>KO003484</td><td>Mannosamine; MS/MS; QqQ; CE:30 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.222155169076</td>
</tr>
<tr>

<td>KO007580</td><td>Histidinol; MS/MS; QqTOF; CE:40 V; [M+H]+</td><td>C6H11N3O</td><td>141.09021</td><td>0.218122614162</td>
</tr>
<tr>
<td>KO003141</td><td>Histidinol; MS/MS; QqQ; CE:40 V; [M+H]+</td><td>C6H11N3O</td><td>141.09021</td><td>0.217331552642</td>
</tr>
<tr>
<td>KO007416</td><td>Galactosamine; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.216573880670</td>

</tr>
<tr>
<td>KO007801</td><td>4-Methyl-5-thiazoleethanol; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H9NOS</td><td>143.04048</td><td>0.214173316494</td>
</tr>
<tr>
<td>KO007414</td><td>Galactosamine; MS/MS; QqTOF; CE:30 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.210917331400</td>
</tr>

<tr>
<td>KO007581</td><td>Histidinol; MS/MS; QqTOF; CE:50 V; [M+H]+</td><td>C6H11N3O</td><td>141.09021</td><td>0.210430186017</td>
</tr>
<tr>
<td>KO007579</td><td>Histidinol; MS/MS; QqTOF; CE:30 V; [M+H]+</td><td>C6H11N3O</td><td>141.09021</td><td>0.205380343969</td>
</tr>
<tr>

<td>KO007944</td><td>Mannosamine; MS/MS; QqTOF; CE:30 V; [M+H]+</td><td>C6H13NO5</td><td>179.07937</td><td>0.195042667845</td>
</tr>
</table>
<?php
/*
$mail = "ynihei@ttck.keio.ac.jp";
$query = array(
	"Name:Sample1; 59.300,653466.0; 112.300,19802.0;",
	"Name:Sample2; 30.000,34653.5; 80.100,430693.5;",
	"Name:Sample3; 80.100,430693.5; 55.900,89109.0; 60.100,391089.5;"
);
$inst = array("all");
$ion = "Positive";
$params = array(
	"type" => "1", "mailAddress" => $mail,
	"queryStrings" => $query,
 	"instrumentTypes" => $inst, "ionMode" => $ion
);

set_time_limit(0);
$soap = new SoapClient('http://localhost/api/services/MassBankAPI?wsdl');
try {
	$res1 = $soap->execBatchJob( $params );
	$job_id = $res1->return;

	for ($lp = 0 ; $lp < 60; $lp++) {
		sleep(60);
		$res2 = $soap->getJobStatus( array("jobId" => $job_id) );
		$status_info = $res2->return;

		if ( $status_info->status == "Completed" ) {
			break;
		}
	}

	$res3 = $soap->getJobResult( array("jobId" => $job_id) );
	$result_set_array = $res3->return;
	foreach ($result_set_array as $result_set) {
		echo "<table border=\"1\" width=\"800\">\n";
		echo "<tr>\n";
		echo "<th colspan=\"5\" bgcolor=\"tan\">Name:" . $result_set->queryName . "</th>\n";
		echo "</tr>\n";
		$results = $result_set->results;
		if ( $result_set->numResults > 0 ) {
			for ($i = 0; $i < $result_set->numResults; $i++) {
				echo "<tr>\n";
				echo "<td>" . $results[$i]->id . "</td>";
				echo "<td>" . $results[$i]->title . "</td>";
				echo "<td>" . $results[$i]->formula . "</td>";
				echo "<td>" . $results[$i]->exactMass ."</td>";
				echo "<td>" . $results[$i]->score ."</td>\n";
				echo "</tr>\n";
			}
		}
		echo "</table>\n";
		echo "<br><br>\n";
	}
}
catch (SoapFault $e) {
	echo $e->getMessage();
	return;
}
*/
?>
</body>
</html>
