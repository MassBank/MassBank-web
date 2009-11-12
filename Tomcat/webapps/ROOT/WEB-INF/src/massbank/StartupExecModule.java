/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
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
 * Tomcat起動時に実行モジュールを起動するサーブレット
 *
 * ver 1.0.1 2008.12.05
 *
 ******************************************************************************/
package massbank;

import javax.servlet.*;
import javax.servlet.http.*;
import massbank.admin.CmdExecute;
import massbank.admin.CmdResult;
import java.util.logging.*;

public class StartupExecModule extends HttpServlet {
	private Process process = null;

	/**
	 * サービス初期処理を行う
	 */
	public void init() throws ServletException {
		if ( getInitParameter("path") == null ) {
			return;
		}
		String cmd = getInitParameter("path").trim();
		if ( !isProcess(cmd) ) {
			try {
				process = Runtime.getRuntime().exec(cmd);
			}
			catch (Exception e) {
				Logger.global.severe( e.toString() );
			}
		}
	}

	public void service(HttpServletRequest req, HttpServletResponse res) {
	}

	/**
	 * サービス終了処理を行う
	 */
	public void destroy() {
		if ( process != null ) {
			process.destroy();
		}
	}

	/**
	 * プロセス生存確認
	 */
	private boolean isProcess(String cmd) {
		String[] pscmd =  new String[]{ "/bin/sh", "-c", "ps ax | grep " + cmd + " | grep -v grep" };
		CmdResult res = new CmdExecute().exec(pscmd);
		String stderr = res.getStderr();
		String stdout = res.getStdout();
		if ( !stderr.equals("") ) {
			Logger.global.warning( stderr );
		}
		if ( stdout.equals("") ) {
			return false;
		}
		return true;
	}
}
