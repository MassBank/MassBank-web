<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *******************************************************************************
 *
 * インストーラ用トップページ
 *
 * ver 1.0.6 2012.02.29
 *
 ******************************************************************************/
%>
<%@ page import="massbank.GetConfig" %>
<%@ page import="massbank.Config" %>
<%
	//-------------------------------------------
	// 設定ファイルから各種情報を取得
	//-------------------------------------------
	GetConfig conf = new GetConfig(Config.get().BASE_URL());
	String siteLongName = Config.get().LongName();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="imagetoolbar" content="no">
<meta name="description" content="Mass Spectral DataBase">
<meta name="keywords" content="Mass,Spectral,Database,MassBank">
<meta name="hreflang" content="en">
<meta name="revisit_after" content="10 seconds">
<title>MassBank | <%=siteLongName%> Mass Spectral DataBase</title>
<link rel="stylesheet" type="text/css" href="css/Common.css">
<link rel="stylesheet" type="text/css" href="css/Personal.css">
<script type="text/javascript" src="script/Piwik.js"></script>
</head>
<body class="msbkFont" bgcolor="#ECECEC">
<div align="center">

<table width="820" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<!--// left -->
		<td width="10"><img src="image/spacer.gif" alt="spacer" width="10"></td>
		<td align="center" bgcolor="#FFFFFF">
			<!--// header -->
			<table width="800" border="0" cellspacing="0" cellpadding="0">
				<tr> 
					<td colspan="2"><img src="image/sub_logo.jpg" alt="MassBank" border="0" usemap="#Map"></td>
				</tr>
			</table>
			<table width="800" border="0" cellpadding="0" cellspacing="0" class="pageShadow">
				<tr>
					<td align="center">
						<img src="image/spacer.gif" alt="spacer" width="1" height="10">
						<h2><font color="dimgray"><%=siteLongName%></font></h2>
						<!--// database -->
						<table width="700" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td>
									<div align="center">
										<table width="670" border="0" cellspacing="10" cellpadding="1" bgcolor="#FFFFFF">
											<tr>
												<!-- Quick Search -->
												<td width="2" bgcolor="green"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="QuickSearch.jsp" style="font-weight:bold;" class="bullet_link" target="_self">Quick Search</a></td>
														</tr>
														<tr>
															<td align="center"><a href="QuickSearch.jsp" target="_self"><img src="image/quick.gif" alt="Quick Search" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
 												<td width="20" bgcolor="white"></td>
 												<!-- Peak Search -->
 												<td width="2" bgcolor="gold"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="PeakSearch.jsp" style="font-weight:bold;" class="bullet_link" target="_self">Peak Search</a></td>
														</tr>
														<tr>
															<td align="center"><a href="PeakSearch.jsp" target="_self"><img src="image/peak.gif" alt="Peak Search Page" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
												<!--// Record Index -->
												<td width="2" bgcolor="DeepPink"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="RecordIndex.jsp" style="font-weight:bold;" class="bullet_link" target="_self">Record Index</a></td>
														</tr>
														<tr>
															<td align="center"><a href="RecordIndex.jsp" target="_self"><img src="image/list.gif" alt="Record Index" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
											</tr>
											<tr>
												<td height="100" colspan="6">
													<ul>
														<li><a href="./api/services/MassBankAPI?wsdl" target="_blank">WEB-API WSDL</a></li>
													</ul>	
												</td>
											</tr>
											<tr>
												<td colspan="100">
												<b>Announcement<br><br></b>
												Dear customers,<br>
												The Java applet technology was deprecated. Therefore many services of MassBank are out of service (e.g. spectral search). The main services quick search, peak search, record index and record display are working properly. Our apologies for any inconvience.
												
												</td>
											</tr>
										</table>
									</div>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<br>
			<!--// footer -->
			<hr size="1">
			<table width="800" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td>
						<iframe src="copyrightline.html" frameborder="0" marginwidth="0" marginheight="0" scrolling="no" style="width:400px; height:60px; padding-left:30px;"></iframe>
					</td>
					<td>
						<span class="version">system version 1.8.1</span>
					</td>
				</tr>
			</table>
		</td>
		<td width="10"><img src="image/spacer.gif" alt="" width="10"></td>
	</tr>
</table>
</div>
</body>
</html>
