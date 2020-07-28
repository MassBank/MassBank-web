package massbank.web.quicksearch;

public class Search {
	public static class SearchResult {
		public final String accession;
		public final String recordTitle;
		public final int hitNumber;
		public final double hitScore;
		public final String ION_MODE;
		public final String formula;
		public final double exactMass;
		public SearchResult(String accession, String recordTitle, int hitNumber, double hitScore, String ION_MODE, String formula, double exactMass) {
			this.accession	= accession;
			this.recordTitle	= recordTitle;
			this.hitNumber	= hitNumber;
			this.hitScore	= hitScore;
			this.ION_MODE	= ION_MODE;
			this.formula		= formula;
			this.exactMass	= exactMass;
		}
	}
}
