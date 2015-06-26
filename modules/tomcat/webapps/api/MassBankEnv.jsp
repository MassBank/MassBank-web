<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (c) 2011 MassBank Project
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
 * MassBank環境変数一覧表示
 *
 * ver 1.0.0 2011.12.21
 *
 ******************************************************************************/
%>
<%@ page import="massbank.MassBankEnv" %>
<%
out.println(MassBankEnv.KEY_LOCAL_URL + ": " + MassBankEnv.get(MassBankEnv.KEY_LOCAL_URL) + "<br>");
out.println(MassBankEnv.KEY_BASE_URL + ": " + MassBankEnv.get(MassBankEnv.KEY_BASE_URL) + "<br>");
out.println(MassBankEnv.KEY_SUB_URL + ": " + MassBankEnv.get(MassBankEnv.KEY_SUB_URL) + "<br>");
out.println(MassBankEnv.KEY_SUB_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_SUB_PATH) + "<br>");
out.println(MassBankEnv.KEY_APACHE_DOCROOT_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_APACHE_DOCROOT_PATH) + "<br>");
out.println(MassBankEnv.KEY_APACHE_APPROOT_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_APACHE_APPROOT_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_DOCROOT_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_DOCROOT_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_TEMP_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_TEMP_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_APPROOT_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPROOT_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_APPJSP_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPJSP_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_APPADMIN_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPADMIN_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_APPEXT_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPEXT_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_APPPSERV_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPPSERV_PATH) + "<br>");
out.println(MassBankEnv.KEY_TOMCAT_APPTEMP_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPTEMP_PATH) + "<br>");
out.println(MassBankEnv.KEY_MASSBANK_CONF_URL + ": " + MassBankEnv.get(MassBankEnv.KEY_MASSBANK_CONF_URL) + "<br>");
out.println(MassBankEnv.KEY_MASSBANK_CONF_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_MASSBANK_CONF_PATH) + "<br>");
out.println(MassBankEnv.KEY_ADMIN_CONF_URL + ": " + MassBankEnv.get(MassBankEnv.KEY_ADMIN_CONF_URL) + "<br>");
out.println(MassBankEnv.KEY_ADMIN_CONF_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_ADMIN_CONF_PATH) + "<br>");
out.println(MassBankEnv.KEY_DATAROOT_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_DATAROOT_PATH) + "<br>");
out.println(MassBankEnv.KEY_ANNOTATION_PATH  + ": " + MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH) + "<br>");
out.println(MassBankEnv.KEY_MOLFILE_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH) + "<br>");
out.println(MassBankEnv.KEY_MOLFILE_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH) + "<br>");
out.println(MassBankEnv.KEY_PROFILE_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_PROFILE_PATH) + "<br>");
out.println(MassBankEnv.KEY_GIF_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_GIF_PATH) + "<br>");
out.println(MassBankEnv.KEY_GIF_SMALL_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_GIF_SMALL_PATH) + "<br>");
out.println(MassBankEnv.KEY_GIF_LARGE_PATH + ": " + MassBankEnv.get(MassBankEnv.KEY_GIF_LARGE_PATH) + "<br>");
out.println(MassBankEnv.KEY_PRIMARY_SERVER_URL + ": " + MassBankEnv.get(MassBankEnv.KEY_PRIMARY_SERVER_URL) + "<br>");
out.println(MassBankEnv.KEY_DB_HOST_NAME + ": " + MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME) + "<br>");
out.println(MassBankEnv.KEY_DB_MASTER_NAME + ": " + MassBankEnv.get(MassBankEnv.KEY_DB_MASTER_NAME) + "<br>");
out.println(MassBankEnv.KEY_BATCH_SMTP + ": " + MassBankEnv.get(MassBankEnv.KEY_BATCH_SMTP) + "<br>");
out.println(MassBankEnv.KEY_BATCH_NAME + ": " + MassBankEnv.get(MassBankEnv.KEY_BATCH_NAME) + "<br>");
out.println(MassBankEnv.KEY_BATCH_FROM + ": " + MassBankEnv.get(MassBankEnv.KEY_BATCH_FROM) + "<br>");
%>
