<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Arrays"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.apache.commons.lang3.StringUtils"%>
<%@ page import="massbank.SearchDefaults"%>
<%@ include file="./Common.jsp"%>
<%
	final int NUM_FORMULA_STD = 6;
	final int NUM_FORMULA_ADV = 5;
	String searchOf = "peak";
	String searchBy = "mz";
	String relInte = SearchDefaults.relInte;
	String tol = SearchDefaults.tol;
	String ionMode = SearchDefaults.ionMode;
	String ionModeAdv = SearchDefaults.ionMode;
	String mode = "and";
	Map inputFormula = new HashMap();
	boolean isFirst = true;
	List<String> instGrpList = new ArrayList<String>();
	List<String> instTypeList = new ArrayList<String>();
	List<String> msTypeList = new ArrayList<String>();
	List<String> instGrpListAdv = new ArrayList<String>();
	List<String> instTypeListAdv = new ArrayList<String>();
	List<String> msTypeListAdv = new ArrayList<String>();
	Hashtable<String, String> params = new Hashtable<String, String>();
	int paramCnt = 0;
	Enumeration names = request.getParameterNames();
	if (names.hasMoreElements()) {
		isFirst = false;
	}
	while (names.hasMoreElements()) {
		String key = (String) names.nextElement();
		if (key.equals("inst_grp")) {
			String[] vals = request.getParameterValues(key);
			instGrpList = Arrays.asList(vals);
		} else if (key.equals("inst")) {
			String[] vals = request.getParameterValues(key);
			instTypeList = Arrays.asList(vals);
		} else if (key.equals("ms")) {
			String[] vals = request.getParameterValues(key);
			msTypeList = Arrays.asList(vals);
		} else if (key.equals("inst_grp_adv")) {
			String[] vals = request.getParameterValues(key);
			instGrpListAdv = Arrays.asList(vals);
		} else if (key.equals("inst_adv")) {
			String[] vals = request.getParameterValues(key);
			instTypeListAdv = Arrays.asList(vals);
		} else if (key.equals("ms_adv")) {
			String[] vals = request.getParameterValues(key);
			msTypeListAdv = Arrays.asList(vals);
		} else {
			String val = request.getParameter(key);
			if (key.equals("searchof"))
				searchOf = val;
			else if (key.equals("searchby"))
				searchBy = val;
			else if (key.equals("mode"))
				mode = val;
			else if (key.indexOf("formula") >= 0)
				inputFormula.put(key, val);
			else if (key.equals("int"))
				relInte = val;
			else if (key.equals("tol"))
				tol = val;
			else if (key.equals("ion"))
				ionMode = val;
			else if (key.equals("ion_adv"))
				ionModeAdv = val;
			else if (key.indexOf("mz") >= 0 || key.indexOf("op") >= 0) {
				params.put(key, val);
			}
		}
	}

	String type = "";
	if (searchBy.equals("mz")) {
		if (searchOf.equals("peak"))
			type = "peak";
		else
			type = "diff";
	} else {
		if (searchOf.equals("peak"))
			type = "product";
		else
			type = "neutral";
	}

	if (paramCnt > 0) {
		isFirst = false;
	}
	String instGrp = "";
	for (int i = 0; i < instGrpList.size(); i++) {
		instGrp += instGrpList.get(i);
		instGrp += ",";
	}
	instGrp = StringUtils.chop(instGrp);
	String instType = "";
	for (int i = 0; i < instTypeList.size(); i++) {
		instType += instTypeList.get(i);
		instType += ",";
	}
	instType = StringUtils.chop(instType);
	String msType = "";
	for (int i = 0; i < msTypeList.size(); i++) {
		msType += msTypeList.get(i);
		msType += ",";
	}
	msType = StringUtils.chop(msType);

	String instGrpAdv = "";
	for (int i = 0; i < instGrpListAdv.size(); i++) {
		instGrpAdv += instGrpListAdv.get(i);
		instGrpAdv += ",";
	}
	instGrpAdv = StringUtils.chop(instGrpAdv);
	String instTypeAdv = "";
	for (int i = 0; i < instTypeListAdv.size(); i++) {
		instTypeAdv += instTypeListAdv.get(i);
		instTypeAdv += ",";
	}
	instTypeAdv = StringUtils.chop(instTypeAdv);
	String msTypeAdv = "";
	for (int i = 0; i < msTypeListAdv.size(); i++) {
		msTypeAdv += msTypeListAdv.get(i);
		msTypeAdv += ",";
	}
	msTypeAdv = StringUtils.chop(msTypeAdv);

	//-------------------------------------
	// ポスト先
	//-------------------------------------
	String formAction = "Result.jsp";
	if (searchBy.equals("formula")) {
		formAction = "ResultAdv.jsp";
	}
%>
<html>
<head>

<link rel="stylesheet" type="text/css" href="css/Common.css">
<link rel="stylesheet" type="text/css" href="css/FormulaSuggest.css" />
<script type="text/javascript" src="script/Common.js"></script>
<script type="text/javascript" src="script/jquery.js"></script>
<script type="text/javascript" src="script/AtomicMass.js"></script>
<script type="text/javascript" src="script/PeakSearch.js"></script>

</head>
<body class="msbkFont backgroundImg cursorDefault"
	onload="loadCheck('<%=searchOf%>', '<%=searchBy%>');">
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td>
				<h1>Peak Search</h1>
			</td>
			<td align="right" class="font12px"><img
				src="image/bullet_link.gif" width="10" height="10">&nbsp;<b><a
					class="text" href="javascript:openMassCalc();">mass calculator</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<img src="image/bullet_link.gif" width="10" height="10">&nbsp;<b><a
					class="text" href="<%=MANUAL_URL%><%=PEAK_PAGE%>" target="_blank">user
						manual</a></b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
		</tr>
	</table>
	<iframe src="menu.jsp" width="860" height="30" frameborder="0"
		marginwidth="0" scrolling="no"></iframe>
	<hr size="1">

	<%
		/*↓ServerInfo.jspはプライマリサーバにのみ存在する(ファイルが無くてもエラーにはならない)*/
	%>

	<form name="form_query" method="post" action="<%=formAction%>"
		style="display: inline" onSubmit="doWait('Searching...')">
		<table border="0" cellpadding="0">
			<tr>
				<td width="90"><b>Search of</b></td>
				<td width="100"><input type="radio" name="searchof"
					value="peak" tabindex="1"
					onClick="return changeSearchType('peak','');"
					<%if (searchOf.equals("peak"))
				out.print(" checked");%>><b><i><span
							name="typeLbl" onclick="return changeSearchType('peak','')"><b>Peaks</b></span></i></b>
				</td>
				<td width="20">&nbsp;</td>
				<td width="170"><input type="radio" name="searchof"
					value="diff" tabindex="2"
					onClick="return changeSearchType('diff','');"
					<%if (searchOf.equals("diff"))
				out.print(" checked");%>><b><i><span
							name="typeLbl" onclick="return changeSearchType('diff','')">Peak&nbsp;Differences</span></i></b>
				</td>
			</tr>
			<tr>
				<td></td>
				<td id="underbar1" height="4"
					<%if (type.equals("peak")) {
				out.print(" bgcolor=\"OliveDrab\"");
			} else if (type.equals("product")) {
				out.print(" bgcolor=\"MidnightBlue\"");
			}%>></td>
				<td></td>
				<td id="underbar2" height="4"
					<%if (type.equals("diff")) {
				out.print(" bgcolor=\"DarkOrchid\"");
			} else if (type.equals("neutral")) {
				out.print(" bgcolor=\"DarkGreen\"");
			}%>></td>
			</tr>

			<input type="hidden" name="searchby" value="mz">
		</table>
		<hr size="1">

		<!--// Peak Search-->
		<%
			if (searchBy.equals("mz")) {
				out.println("\t\t<div id=\"standard\" class=\"showObj\">");
			} else {
				out.println("\t\t<div id=\"standard\" class=\"hidObj\">");
			}

			String mzLabel = "<i>m/z</i>";
			String allowImage = "<img src=\"image/arrow_peak.gif\" alt=\"\">";
			if (searchOf.equals("diff") && searchBy.equals("mz")) {
				mzLabel = "<i>m/z</i> Diff.";
				allowImage = "<img src=\"image/arrow_diff.gif\" alt=\"\">";
			}
		%>
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
					<table border="0" cellpadding="0" cellspacing="12" class="form-box">
						<tr>
							<th></th>
							<th id="mz"><%=mzLabel%></th>
							<th>Formula</th>
						</tr>
						<%
							final String[] logic = { "and", "or" };
							String lblLogic = logic[0].toUpperCase();
							String[] mz = new String[NUM_FORMULA_STD];
							String[] op = new String[NUM_FORMULA_STD];
							for (int i = 0; i < NUM_FORMULA_STD; i++) {
								String key = "mz" + String.valueOf(i);
								if (params.containsKey(key)) {
									mz[i] = (String) params.get(key);
									op[i] = (String) params.get("op" + String.valueOf(i));
								} else {
									mz[i] = "";
									op[i] = "";
								}
								out.println("\t\t\t\t\t\t\t<tr>");
								if (i == 0) {
									out.println("\t\t\t\t\t\t\t\t<td>");
									out.println("\t\t\t\t\t\t\t\t\t<select name=\"op0\" class=\"mzLogics\" tabindex=\"5\">");
									for (int j = 0; j < logic.length; j++) {
										out.print("\t\t\t\t\t\t\t\t\t\t<option value=\"" + logic[j] + "\"");
										if (logic[j].equals(op[0])) {
											out.print(" selected");
											lblLogic = logic[j].toUpperCase();
										}
										out.println(">" + logic[j].toUpperCase() + "</option>");
									}
									out.println("\t\t\t\t\t\t\t\t\t</select>");
									out.println("\t\t\t\t\t\t\t\t</td>");
								} else {
									out.println("\t\t\t\t\t\t\t\t<td align=\"right\"><span class=\"logic\">" + lblLogic
											+ "</span>&nbsp;</td>");
								}

								// m/z
								out.println("\t\t\t\t\t\t\t\t<td><input name=\"mz" + i + "\" type=\"text\" size=\"14\" value=\"" + mz[i]
										+ "\" class=\"Mass\" tabindex=\"" + (i + 5) + "\"></td>");

								// Formula
								out.println("\t\t\t\t\t\t\t\t<td>");
								out.println("\t\t\t\t\t\t\t\t\t<span id=\"arrow" + i + "\">" + allowImage + "</span>");
								out.println("\t\t\t\t\t\t\t\t\t<input name=\"fom" + i
										+ "\" type=\"text\" size=\"20\" value=\"\" class=\"Formula\" tabindex=\"" + (i + 11) + "\">");
								out.println("\t\t\t\t\t\t\t\t</td>");
								out.println("\t\t\t\t\t\t\t</tr>");
							}
						%>
						<tr>
							<td colspan="3" height="1"></td>
						</tr>
						<tr>
							<td colspan="3"><b>Rel.Intensity</b>&nbsp;<input name="int"
								type="text" size="10" value="<%=relInte%>" tabindex="17">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Tolerance</b>&nbsp;<input
								name="tol" type="text" size="10" value="<%=tol%>"
								tabindex="18"></td>
						</tr>
						<tr>
							<td colspan="3" align="right"><input type="button"
								name="reset" value="Reset" onClick="resetForm()"></td>
						</tr>
					</table> <br>
					<table>
						<tr>
							<td><input type="submit" value="Search"
								onclick="return checkSubmit(0);" class="search" tabindex="19">
								<input type="hidden" name="op1"
								value="<%=lblLogic.toLowerCase()%>"> <input
								type="hidden" name="op2" value="<%=lblLogic.toLowerCase()%>">
								<input type="hidden" name="op3"
								value="<%=lblLogic.toLowerCase()%>"> <input
								type="hidden" name="op4" value="<%=lblLogic.toLowerCase()%>">
								<input type="hidden" name="op5"
								value="<%=lblLogic.toLowerCase()%>"> <input
								type="hidden" name="sortKey" value="name"> <input
								type="hidden" name="sortAction" value="1"> <input
								type="hidden" name="pageNo" value="1"> <input
								type="hidden" name="exec" value=""></td>
						</tr>
					</table>
				</td>
				<td style="padding: 0px 15px;" valign="top"><jsp:include
						page="Instrument.jsp" flush="true">
						<jsp:param name="first" value="<%=isFirst%>" />
						<jsp:param name="inst_grp" value="<%=instGrp%>" />
						<jsp:param name="inst" value="<%=instType%>" />
						<jsp:param name="ms" value="<%=msType%>" />
						<jsp:param name="ion" value="<%=ionMode%>" />
					</jsp:include></td>
			</tr>
		</table>
		</div>

		<!--// Peak Search Advanced -->
		<%
			if (searchBy.equals("formula")) {
				out.println("\t\t<div id=\"advance\" class=\"showObj\">");
			} else {
				out.println("\t\t<div id=\"advance\" class=\"hidObj\">");
			}
		%>
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top">
					<table border="0" cellpadding="0" cellspacing="15" class="form-box">
						<tr>
							<td>
								<table border="0" cellpadding="0" cellspacing="0">
									<%
										String style = "bgProduct";
										String str = "Ion&nbsp;";
										if (searchBy.equals("formula") && searchOf.equals("diff")) {
											style = "bgNeutral";
											str = "Neutral&nbsp;Loss&nbsp;";
										}
										String condition = "<span class=\"logic\">AND</span>";
										if (mode.equals("or")) {
											condition = "<span class=\"logic\">OR</span>";
										} else if (mode.equals("seq")) {
											condition = "<img src=\"image/arrow_neutral.gif\">";
										}

										out.println("\t\t\t\t\t\t\t\t\t\t<tr><br>");
										for (int i = 1; i <= NUM_FORMULA_ADV; i++) {
											out.println("\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\" width=\"100\" id=\"advanceType" + i
													+ "\" class=\"" + style + "\">" + str + String.valueOf(i) + "</td>");
											if (i < NUM_FORMULA_ADV) {
												out.println("\t\t\t\t\t\t\t\t\t\t\t<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
											}
										}
										out.println("\t\t\t\t\t\t\t\t\t\t</tr>");

										out.println("\t\t\t\t\t\t\t\t\t\t<tr>");
										for (int i = 1; i <= NUM_FORMULA_ADV; i++) {
											out.println("\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">Formula</td>");
											if (i < NUM_FORMULA_ADV) {
												out.println("\t\t\t\t\t\t\t\t\t\t\t<td></td>");
											}
										}
										out.println("\t\t\t\t\t\t\t\t\t\t</tr>");

										out.println("\t\t\t\t\t\t\t\t\t\t<tr>");
										for (int i = 1; i <= NUM_FORMULA_ADV; i++) {
											String key = "formula" + String.valueOf(i);
											String val = "";
											if (inputFormula.containsKey(key)) {
												val = (String) inputFormula.get(key);
											}
											out.println("\t\t\t\t\t\t\t\t\t\t\t<td align=\"center\">");
											out.println("\t\t\t\t\t\t\t\t\t\t\t\t<input id=\"" + key + "\" class=\"FormulaSuggest\" name=\"" + key
													+ "\" type=\"text\" size=\"12\" value=\"" + val + "\" autocomplete=\"off\">");
											out.println("\t\t\t\t\t\t\t\t\t\t\t</td>");
											if (i < NUM_FORMULA_ADV) {
												out.println("\t\t\t\t\t\t\t\t\t\t\t<td id=\"cond" + String.valueOf(i)
														+ "\" width=\"26\" align=\"center\">" + condition + "</td>");
											}
										}
										out.println("\t\t\t\t\t\t\t\t\t\t</tr>");
										out.println("\t\t\t\t\t\t\t\t\t\t<tr height=\"50\">");
										out.println("\t\t\t\t\t\t\t\t\t\t\t<td colspan=\"7\">");

										String[] valMode = new String[] { "and", "or" };
										String[] strMode = new String[] { "AND", "OR" };
										if (searchBy.equals("formula") && searchOf.equals("diff")) {
											valMode = new String[] { "and", "seq" };
											strMode = new String[] { "AND", "SEQUENCE" };
										}
										for (int i = 0; i < valMode.length; i++) {
											out.print("\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"radio\" name=\"mode\" value=\"" + valMode[i]
													+ "\" onClick=\"chageMode(this.value)\"");
											if (mode.equals(valMode[i])) {
												out.print(" checked");
											}
											out.println("><b><span id=\"modeTxt" + i + "\">" + strMode[i] + "</span></b>&nbsp;&nbsp;&nbsp;");
										}
										out.println("\t\t\t\t\t\t\t\t\t\t\t</td>");
										out.println("\t\t\t\t\t\t\t\t\t\t\t<td colspan=\"4\" align=\"right\">");
										out.println(
												"\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"button\" name=\"reset\" value=\"Reset\" onClick=\"resetForm()\">");
										out.println("\t\t\t\t\t\t\t\t\t\t\t</td>");
										out.println("\t\t\t\t\t\t\t\t\t\t</tr>");
									%>
									<tr>
										<td>&nbsp;</td>
									</tr>
									<tr>
										<td colspan="9"><b><span class="fontNote">*
													The targets of Peak Search Advanced are only Keio and Riken
													data.</span></b></td>
									</tr>
									<tr>
										<td>&nbsp;</td>
									</tr>
								</table>
							</td>
						</tr>
					</table> <br>
					<table>
						<tr>
							<td valign="top"><input type="submit" value="Search"
								onclick="return checkSubmit(1);" class="search"> <input
								type="hidden" name="type" value="<%=type%>"></td>
						</tr>
					</table>
				</td>
				<td style="padding: 0px 15px;" valign="top" rowspan="2"><jsp:include
						page="Instrument.jsp" flush="true">
						<jsp:param name="first" value="<%=isFirst%>" />
						<jsp:param name="padv" value="true" />
						<jsp:param name="inst_grp_adv" value="<%=instGrpAdv%>" />
						<jsp:param name="inst_adv" value="<%=instTypeAdv%>" />
						<jsp:param name="ms_adv" value="<%=msTypeAdv%>" />
						<jsp:param name="ion_adv" value="<%=ionModeAdv%>" />
					</jsp:include></td>
			</tr>
		</table>
		</div>
		<div id="loaded"></div>
	</form>
</body>
</html>
