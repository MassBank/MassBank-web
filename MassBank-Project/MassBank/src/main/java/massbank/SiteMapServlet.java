/*******************************************************************************
 * Copyright (C) 2017 MassBank consortium
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redfin.sitemapgenerator.SitemapIndexGenerator;
import com.redfin.sitemapgenerator.WebSitemapGenerator;

/**
 * 
 * @author rmeier 
 * This servlet generates dynamic sitemap.xml files. It serves a index
 * at /sitemap_index.xml and the actual sitemaps at /sitemap/sitemap*.xml
 *
 */
@WebServlet({"/sitemap_index.xml","/sitemap/*"})
public class SiteMapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(SiteMapServlet.class);
	
	public void init() throws ServletException {
		try {
			logger.trace(getServletContext().getAttribute(ServletContext.TEMPDIR));
			File tmpdir = (File)getServletContext().getAttribute(ServletContext.TEMPDIR);
			for (File file : tmpdir.listFiles()) {
				if (file.getName().matches("sitemap.*\\.xml$")) {
					file.delete();
				}
			}
			
			// add URL here
			WebSitemapGenerator wsg = new WebSitemapGenerator(Config.get().SitemapBaseURL(), tmpdir);
			for (int i = 0; i < 60000; i++) wsg.addUrl(Config.get().SitemapBaseURL()+ "/doc"+i+".html");
			
			
			// write new sitemaps
			List<File> sitemaps=wsg.write();
			// write sitemap index
			SitemapIndexGenerator sig = new SitemapIndexGenerator(Config.get().SitemapBaseURL(), new File(tmpdir,"sitemap_index.xml"));
			for (File sitemap: sitemaps) {
				sig.addUrl(Config.get().SitemapBaseURL()+"/sitemap/"+sitemap.getName());
			}
			sig.write();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.trace("getPathInfo: " + request.getPathInfo());
		logger.trace("getServletPath: " + request.getServletPath());
		logger.trace("getRequestURI: " + request.getRequestURI() );
		
		File sitemap; 
		
		if ((request.getPathInfo() == null) && "/sitemap_index.xml".equals(request.getServletPath())) {
			sitemap=new File((File)getServletContext().getAttribute(ServletContext.TEMPDIR), "sitemap_index.xml");
			if (!sitemap.exists()) {
				// send 404 if index is missing
	            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
	            return;
	        }
		} else if (request.getServletPath().equals("/sitemap")) {
			sitemap=new File((File)getServletContext().getAttribute(ServletContext.TEMPDIR), request.getPathInfo());
			if (!sitemap.exists()) {
				// send 404 if index is missing
	            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
	            return;
	        }
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
		}
		response.reset();
        response.setContentType("application/xml;charset=UTF-8");
        // write content to response
        Files.copy(sitemap.toPath(), response.getOutputStream());
    }

	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
