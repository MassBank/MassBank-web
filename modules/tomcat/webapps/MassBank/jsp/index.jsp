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
<%@ page import="massbank.MassBankEnv" %>
<%
	//-------------------------------------------
	// 設定ファイルから各種情報を取得
	//-------------------------------------------
	GetConfig conf = new GetConfig(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
	String siteLongName = conf.getSiteLongName()[0];	// サイト名取得
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<meta http-equiv="imagetoolbar" content="no">
<meta name="description" content="Mass Spectral DataBase">
<meta name="keywords" content="Mass,Spectral,Database,MassBank">
<meta name="revisit_after" content="10 seconds">
<title>MassBank | <%=siteLongName%> Mass Spectral DataBase</title>
<link rel="stylesheet" type="text/css" href="css/Common.css">
<link rel="stylesheet" type="text/css" href="css/Personal.css">
</head>
<body class="msbkFont" bgcolor="#ECECEC" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<div align="center" valign="top">

<table width="820" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<!--// left -->
		<td width="10" background="image/index_bg_left_001.gif"><img src="image/spacer.gif" alt="" width="10"></td>
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
						<img src="image/spacer.gif" width="1" height="10">
						<h2><font color="dimgray"><%=siteLongName%></font></h2>
						<!--// database -->
						<table width="700" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td>
									<div align="center">
										<table width="670" border="0" cellspacing="10" cellpadding="1" bgcolor="#FFFFFF">
											<tr>
												<!--// Spectrum Search -->
												<td width="2" bgcolor="blue"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="SearchPage.html" style="font-weight:bold;" class="bullet_link" target="_self">Spectrum Search</a></td>
														</tr>
														<tr>
															<td align="center"><A href="SearchPage.html" target="_self"><img src="img/search.gif" alt="Spectrum Search" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
												<td width="20" bgcolor="white"></td>
												<!--// Quick Search -->
												<td width="2" bgcolor="green"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="QuickSearch.html" style="font-weight:bold;" class="bullet_link" target="_self">Quick Search</a></td>
														</tr>
														<tr>
															<td align="center"><a href="QuickSearch.html" target="_self"><img src="img/quick.gif" alt="Quick Search" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
												<td width="20" bgcolor="white"></td>
												<!--// Substructure Search -->
												<td width="2" bgcolor="BlueViolet"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="StructureSearch.html" style="font-weight:bold;" class="bullet_link" target="_self">Substructure Search</a></td>
														</tr>
														<tr>
															<td align="center"><a href="StructureSearch.html" target="_self"><img src="img/substructure.gif" alt="Substructure Search" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
											</tr>
											<tr>
												<!--// Browse Page -->
												<td width="2" bgcolor="red"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="BrowsePage.html" style="font-weight:bold;" class="bullet_link" target="_self">Browse Page</a></td>
														</tr>
														<tr>
															<td align="center"><a href="BrowsePage.html" target="_self"><img src="img/browse.gif" alt="Browse Page" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
												<td width="20" bgcolor="white"></td>
												<!--// Peak Search -->
												<td width="2" bgcolor="gold"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="PeakSearch.html" style="font-weight:bold;" class="bullet_link" target="_self">Peak Search</a></td>
														</tr>
														<tr>
															<td align="center"><a href="PeakSearch.html" target="_self"><img src="img/peak.gif" alt="Peak Search Page" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
												<td width="20" bgcolor="white"></td>
												<!--// Spectral Browser -->
												<td width="2" bgcolor="Chocolate"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="PackageView.html" style="font-weight:bold;" class="bullet_link" target="_self">Spectral Browser</a></td>
														</tr>
														<tr>
															<td align="center"><a href="PackageView.html" target="_self"><img src="img/package.gif" alt="Spectral Browser" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
												<td width="20" bgcolor="white"></td>
											</tr>
											<tr>
												<!--// Record Index -->
												<td width="2" bgcolor="DeepPink"></td>
												<td align="center" bgcolor="#F1F1F1" style="border: 1px #EEE solid;">
													<table border="0" cellspacing="0" cellpadding="5" width="190">
														<tr>
															<td><a href="RecordIndex.html" style="font-weight:bold;" class="bullet_link" target="_self">Record Index</a></td>
														</tr>
														<tr>
															<td align="center"><a href="RecordIndex.html" target="_self"><img src="img/list.gif" alt="Record Index" width="170" border="0"></a></td>
														</tr>
													</table>
												</td>
											</tr>
											<tr>
												<td height="100" colspan="6">
													<!-- <li><a href="./mbadmin/">MassBank Administration Tool</a></li><br /><br /> -->
													<li><a href="./api/services/MassBankAPI?wsdl" target="_blank">WEB-API WSDL</a></li>
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
						<iframe src="./copyrightline.html" frameborder="0" marginwidth="0" marginheight="0" scrolling="no" style="width:400px; height:60px; padding-left:30px;"></iframe>
					</td>
					<td>
						<span class="version">system version 1.8.1</span>
					</td>
				</tr>
			</table>
		</td>
		<td width="10" background="image/index_bg_right_001.gif"><img src="image/spacer.gif" alt="" width="10"></td>
	</tr>
</table>
</div>
</body>
</html>
