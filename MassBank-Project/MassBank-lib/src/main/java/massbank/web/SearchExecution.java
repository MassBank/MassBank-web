package massbank.web;

import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import massbank.GetConfig;

public class SearchExecution {
	private Database database = null;
	
	private Connection connection = null;
	
	private HttpServletRequest request = null;
	
	private GetConfig conf = null;
	
	public SearchExecution(HttpServletRequest request, GetConfig conf) {
		this.database = new Database(); 
		this.connection = this.database.getConnection();
		this.request = request;
		this.conf = conf;
	}
	
	public ArrayList<String> exec(SearchFunction function) {
		function.getParameters(this.request);
		return function.search(this.connection);
	}
}
