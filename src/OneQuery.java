package qpp;

import java.util.List;

public class OneQuery{

	String queryId;
	String query;
	List<String> queryTerms;
	
	public OneQuery(String qid, String q, List<String> qts){
		this.queryId = qid;
		this.query = q;
		this.queryTerms = qts;
	}
}
