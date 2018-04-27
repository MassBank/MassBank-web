package massbank.web.quicksearch;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import massbank.GetConfig;
import massbank.web.quicksearch.Search;
import massbank.web.Database;

public class QuickSearchByPeak {
	
	private Database database = null;
	
	private Connection connection = null;
	
	private HttpServletRequest request = null;
	
	private GetConfig conf = null;
	
	public QuickSearchByPeak(HttpServletRequest request, GetConfig conf) {
		this.database = new Database(); 
		this.connection = this.database.getConnection();
		this.request = request;
		this.conf = conf;
	}
	
	public ArrayList<String> exec() {
//		this.getParameters();
		return this.search();
	}
	
	public ArrayList<String> search() {
		ArrayList<String> resList = new ArrayList<String>();
		Search search = new Search(request, connection);
		resList = search.getResult();
		return resList;
	}
}
