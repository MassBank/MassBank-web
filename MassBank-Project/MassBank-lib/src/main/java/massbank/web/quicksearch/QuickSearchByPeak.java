package massbank.web.quicksearch;

import javax.servlet.http.HttpServletRequest;

import massbank.db.DatabaseManager;
import massbank.web.SearchFunction;
import massbank.web.quicksearch.Search.SearchResult;

public class QuickSearchByPeak implements SearchFunction<SearchResult[]> {

	private HttpServletRequest request;

	public void getParameters(HttpServletRequest request) {
		this.request = request;
	}

	public SearchResult[] search(DatabaseManager databaseManager) {
		Search search = new Search(this.request, databaseManager.getConnection());
		SearchResult[] resList = search.getResult();
		return resList;
	}
}
