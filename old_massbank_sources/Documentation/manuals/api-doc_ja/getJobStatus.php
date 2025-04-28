<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>getJobStatus メソッド</title>
<link rel="stylesheet" type="text/css" href="api_ref.css">
</head>
<body>
<span class="head">MassBank WEB-API リファレンス</span>
<hr>
<h1 class="method">getJobStatus  メソッド</h1>
<b>指定したジョブIDのステータスを取得します。</b><br>
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
ステータスを&nbsp;<span class="dtype">JobStatus</span>&nbsp;型（以下の構造体）で返します。<br>
<table border="1" width="850">
<tr><td>
&nbsp;&nbsp;-&nbsp;<b>status</b>&nbsp;:&nbsp;ステータス文字列&nbsp;[型]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;["Waiting":実行待ち&nbsp;,&nbsp;"Running":ジョブ実行中&nbsp;,&nbsp;"Completed":処理完了]<br>
<br>
&nbsp;&nbsp;-&nbsp;<b>statusCode</b>&nbsp;:&nbsp;ステータスコード[型]string<br>
&nbsp;&nbsp;&nbsp;&nbsp;["0":実行待ち&nbsp;,&nbsp;"1":ジョブ実行中&nbsp;,&nbsp;"2":処理完了]<br>
<br>
&nbsp;&nbsp;-&nbsp;<b>requestDate</b>&nbsp;:&nbsp;リクエスト受付け日時 [型]string<br>
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
