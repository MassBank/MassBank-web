/*******************************************************************************
 * Copyright (C) 2021 MassBank consortium
 * 
 * This file is part of MassBank.
 * 
 * MassBank is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * package massbank;
 * 
 ******************************************************************************/
package massbank;

import java.util.Enumeration;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author rmeier 
 * This servlet shows a generic error page indicating that the
 * requested record can not be found. It supports three optional
 * parameters:
 * id - the ACCESSION of the requested record
 * error - an additional error message
 *
 */
@WebServlet("/NoRecordPage")
public class NoRecordPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(NoRecordPage.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		// preprocess request parameter
		// get parameters
		// http://localhost/MassBank/NoRecordPage?id=XXX00001&error=Some error
		try {
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				String val = (String) request.getParameter( key );
				switch(key){
					case "id": request.setAttribute("accession", val); break;
					case "error": request.setAttribute("error", val); break;
					default: logger.warn("unused argument '" + key + "=" + val + "'.");
				}
			}
			request.getRequestDispatcher("/NoRecordPage.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Cannot load NoRecordPage", e);
        }
     }

}
