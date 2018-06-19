package massbank.web.quicksearch;

import javax.servlet.http.HttpServletRequest;

import massbank.DatabaseManager;
import massbank.web.SearchFunction;
import massbank.web.quicksearch.Search.SearchResult;

public class QuickSearchByPeak implements SearchFunction<SearchResult[]> {

	private HttpServletRequest request;

	public void getParameters(HttpServletRequest request) {
		this.request = request;
	}

	public SearchResult[] search(DatabaseManager databaseManager) {
//		List<String> resList = new ArrayList<String>();
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
		SearchResult[] resList = search.getResult();
		return resList;
	}
}
