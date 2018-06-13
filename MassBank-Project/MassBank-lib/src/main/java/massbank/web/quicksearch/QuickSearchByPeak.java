package massbank.web.quicksearch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import massbank.web.quicksearch.Search;
import massbank.web.SearchFunction;

public class QuickSearchByPeak implements SearchFunction {

	private HttpServletRequest request;

	public void getParameters(HttpServletRequest request) {
		this.request = request;
	}

	public ArrayList<String> search(Connection connection) {
		ArrayList<String> resList = new ArrayList<String>();
//		System.out.println("############################################");
//		System.out.println(this.request);
//		System.out.println(this.request.getRequestURL());
//		System.out.println(this.request.getQueryString());
//		Map<String, String[]> parameterMap = request.getParameterMap();
//		for (Entry<String, String[]> e : parameterMap.entrySet()) {
//			String strKey = e.getKey().toUpperCase();
//			String[] strVal = e.getValue();
//			System.out.println(strKey + "\t" + Arrays.toString(strVal));
//		}
		Search search = new Search(this.request, connection);
		resList = search.getResult();
		return resList;
	}
}
