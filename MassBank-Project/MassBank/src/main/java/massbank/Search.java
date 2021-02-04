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

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet("/Search")
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(Search.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Preprocess request: load list of mass spectrometry information in JSP.
		try {
			GetInstInfo instInfo = new GetInstInfo(request);
			Map<String, List<String>> instrument_info = instInfo.getTypeGroup();
			String[] msInfo = instInfo.getMsAll();
			List<String> ms_info = Arrays.asList(msInfo); 
			
	        request.setAttribute("instrument_info", instrument_info);
	        request.setAttribute("ms_info", ms_info);
	        request.getRequestDispatcher("/Search.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Cannot obtain Instrument Information from DB", e);
        }
     }

}
