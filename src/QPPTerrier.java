package qpp;

import qpp.OneQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.terrier.applications.batchquerying.TRECQuerying;
import org.terrier.querying.SearchRequest;
import org.terrier.querying.parser.MultiTermQuery;
import org.terrier.querying.parser.Query;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;

public class QPPTerrier extends TRECQuerying {

	public QPPTerrier(){
	}
		
	@SuppressWarnings("finally")
	public int getDocumentFrequency(String term){
        Lexicon<String> lex = index.getLexicon();
        LexiconEntry le = lex.getLexiconEntry(term);
        int docFreq = 0;
        try{
            docFreq = le.getDocumentFrequency();
        }
        catch(Exception e){
            logger.error("Could not get document frequency for term: " + term + " --- Resulted in Error:" +  e.getMessage());
        }
        finally{
            return docFreq;
        }	
	}

    public List<Integer> getTermFrequencies(String term){

        List<Integer> result = new ArrayList<Integer>();
        PostingIndex inv = index.getInvertedIndex();
        Lexicon<String> lex = index.getLexicon();
        LexiconEntry le = lex.getLexiconEntry(term);

        try{
            IterablePosting postings = inv.getPostings((BitIndexPointer) le);
            if(postings != null) {
                while (postings.next() != IterablePosting.EOL) {
                    result.add(postings.getFrequency());
                }
            }
        }
        catch(Exception e){
            logger.error("Term: " + term + " --- Resulted in Error:" +  e.getMessage());
        }
        return result;
    }

	public List<OneQuery> queryLoop(){
		querySource.reset();
		
        List<OneQuery> queries = new ArrayList<OneQuery>(); 
        
        while (querySource.hasNext()) {
            String query = querySource.next();
            String queryId = querySource.getQueryId();
            
            SearchRequest srq = queryingManager.newSearchRequest(queryId, query);
            queryingManager.runPreProcessing(srq);

            MultiTermQuery queryAfterPreprocessing = (MultiTermQuery) srq.getQuery();

            List<Query> terms = new ArrayList<Query>();
            queryAfterPreprocessing.getTermsOf(Query.class, terms, true);
            
            List<String> qterms = new ArrayList<String>();
            for(Query q: terms)
            	qterms.add(q.toString());
            
            queries.add(new OneQuery(queryId, query, qterms));
        }
        return queries;
	}
	
	public int getTotalNumberOfDocuments(){
        Lexicon<String> lex = index.getLexicon();
        return index.getCollectionStatistics().getNumberOfDocuments();
    }

	public long getTotalNumberOfTokens(){
        Lexicon<String> lex = index.getLexicon();
        return index.getCollectionStatistics().getNumberOfTokens();
    }

    public Set<Integer> getPostings(String term){
        Lexicon<String> lex = index.getLexicon();
        LexiconEntry le = lex.getLexiconEntry(term);

        PostingIndex inv = index.getInvertedIndex();
        Set<Integer> result = new HashSet<Integer>();
        try{
            IterablePosting postings = inv.getPostings((BitIndexPointer) le);
            if(postings != null){
                while (postings.next() != IterablePosting.EOL) {
                    result.add(postings.getId());
                }
            }
        }
       catch(Exception e){
            logger.error("Term: " + le + " --- Resulted in Error:" +  e.getMessage());
            return result;
        }
        return result;
    }

}
