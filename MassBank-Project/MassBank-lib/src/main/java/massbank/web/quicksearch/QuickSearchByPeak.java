package massbank.web.quicksearch;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import massbank.GetConfig;
import massbank.web.quicksearch.Search;
import massbank.web.Database;
import massbank.web.SearchFunction;

public class QuickSearchByPeak implements SearchFunction {
	
	private HttpServletRequest request;
	
	public void getParameters(HttpServletRequest request) {
		this.request = request;
	}
	
	public ArrayList<String> search(Connection connection) {
		ArrayList<String> resList = new ArrayList<String>();
		Search search = new Search(this.request, connection);
		resList = search.getResult();
		return resList;
	}
}
