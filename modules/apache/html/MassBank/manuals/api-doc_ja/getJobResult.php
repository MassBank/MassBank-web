<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getJobResult メソッド</title>
<link rel="stylesheet" type="text/css" href="api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API リファレンス</span>
<hr>
<h1 class="method">getJobResult メソッド</h1>
<b>指定したジョブIDの結果を取得します。</b><br>
<br>
<b>パラメータ</b><br>
<table border="1" width="850">
<tr>
<td><b>jobId</b></td><td>[型]string</td><td>ジョブID<br>
</td>
</tr>
</table>
<br><br>
<b>レスポンス</b><br>
検索結果を&nbsp;<span class="dtype">ResultSet</span>&nbsp;型（以下の構造体）の配列で返します。<br>
<table border="1" width="850">
<tr><td>
&nbsp;&nbsp;-&nbsp;<b>queryName</b>&nbsp;:&nbsp;クエリ名&nbsp;[型]string<br>
&nbsp;&nbsp;-&nbsp;<b>numResults</b>&nbsp;:&nbsp;検索結果数&nbsp;[型]int&nbsp*1<br>
&nbsp;&nbsp;-&nbsp;<b>results</b>&nbsp;:&nbsp;ヒットしたレコードの情報&nbsp;&nbsp;[型]<span class="dtype">Result</span>&nbsp;型（以下の構造体）の配列<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;｜<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├&nbsp;<b>id</b>&nbsp;:&nbsp;MassBank ID [型]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;｜&nbsp;<b>title</b>&nbsp;:&nbsp;レコードタイトル [型]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;｜&nbsp;<b>formula</b>&nbsp;:&nbsp;分子式 [型]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;｜&nbsp;<b>exactMass</b>&nbsp;:&nbsp;精密質量 [型]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└&nbsp;<b>score</b>&nbsp;:&nbsp;スコア [型]string<br>
<br>
&nbsp;&nbsp;*1:&nbsp;不正な形式であったクエリについては、<b>numResults</b> は "-1" にセットされます。
</td></tr>
</table>
<br><br>
<b>例外</b><br>
エラーメッセージ<br>
"Job not found" : 指定されたIDのジョブが存在しない場合 
<br><br>
</div>
<hr color="silver">
<br>
■PHPソースコード<br>
<a href="execBatchJob.php">execBatchJob</a>&nbsp;メソッドを参照してください。
</body>
</html>
