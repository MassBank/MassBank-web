package massbank.web.quicksearch;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;
import massbank.web.SearchFunction;

public class QuickSearchByPeak implements SearchFunction<List<String>> {

	private HttpServletRequest request;

	public void getParameters(HttpServletRequest request) {
		this.request = request;
	}

	public List<String> search(DatabaseManager databaseManager) {
		List<String> resList = new ArrayList<String>();
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
		Search search = new Search(this.request, databaseManager.getConnection());
		resList = search.getResult();
		return resList;
	}
}
