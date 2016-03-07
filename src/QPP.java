package qpp;

import java.io.PrintWriter;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

public class QPP implements QPPInterface {
	
	/** The logger used */
	protected static final Logger logger = Logger.getLogger(QPP.class);

	protected PrintWriter qppResultFile;
	
	// Specificity (Pre-retrieval QPPs)
	protected Map<String, Double> avgQL;   // Averaged Query Length
	protected Map<String, Double> avgIDF;  // Averaged Inverse Document Frequency
	protected Map<String, Double> avgICTF; // Maximum Inverse Document Frequency
	protected Map<String, Double> maxIDF;  // Standard Deviation of IDF
	protected Map<String, Double> devIDF;  // Averaged Inverse Collection Term Frequency
    //
	protected Map<String, Double> SCS;     // Simplified Clarity Score
	protected Map<String, Double> sumSCQ;  // Summed Collection Query Similarity
	protected Map<String, Double> avgSCQ;  // Averaged Collection Query Similarity
	protected Map<String, Double> maxSCQ;  // Maximum Collection Query Similarity
	protected Map<String, Double> QS;      // Query Scope

    // RankSensitivity (Pre-retrieval QPPs)
    protected Map<String, Double> sumVar;  // Summed Term Weight Variability (SumVAR)
    protected Map<String, Double> avgVar;  // Averaged Term Weight Variability (AvVAR)
    protected Map<String, Double> maxVar;  // Maximum Term Weight Variability (MaxVAR)

    // Ambiguity (Pre-retrieval QPPs)
    // Collection based ambiguity
    protected Map<String, Double> avgQC;   // Averaged Query Term Coherence (AvQC)                            //TODO: implement
    protected Map<String, Double> avgQCG;  // Averaged Query Term Coherence with Global Constraint (AvQCG)    //TODO: implement
    // External source based (WordNet)
    protected Map<String, Double> avgP;    // Averaged Polysemy (AvP)                                         //TODO: implement
    protected Map<String, Double> avgNP;   // Averaged Noun Polysemy (AvNP)                                   //TODO: implement

    // Term Relatedness (Pre-retrieval QPPs)
    protected Map<String, Double> avgPMI;  // Averaged Pointwise Mutual Information (AvPMI)
    protected Map<String, Double> maxPMI;  // Maximum Pointwise Mutual Information (MaxPMI)
    protected Map<String, Double> avgPath; // Averaged Path Length (AvPath) [124]                   //TODO: implement
    protected Map<String, Double> avgLesk; // Averaged Lesk Relatedness (AvLesk) [15]               //TODO: implement
    protected Map<String, Double> avgVP;   // Averaged Vector Pair Relatedness (AvVP) [116].        //TODO: implement
    
    QPPTerrier se;
    List<OneQuery> queries;
    int totalNumberOfDocuments;
    long totalNumberOfTokens; 
    
    //constructor 
    public QPP(){ 
    	
    	se = new QPPTerrier();
    	queries = se.queryLoop();
    	
    	this.totalNumberOfDocuments = se.getTotalNumberOfDocuments();
    	this.totalNumberOfTokens = se.getTotalNumberOfTokens();
    	
    	avgQL = new HashMap<String, Double>(queries.size());
    	avgIDF = new HashMap<String, Double>(queries.size());
    	avgICTF = new HashMap<String, Double>(queries.size());
    	maxIDF = new HashMap<String, Double>(queries.size());
    	devIDF = new HashMap<String, Double>(queries.size());
    	SCS = new HashMap<String, Double>(queries.size());
    	sumSCQ = new HashMap<String, Double>(queries.size());
    	avgSCQ = new HashMap<String, Double>(queries.size());
    	maxSCQ = new HashMap<String, Double>(queries.size());
    	QS = new HashMap<String, Double>(queries.size());

        sumVar = new HashMap<String, Double>(queries.size());
        avgVar = new HashMap<String, Double>(queries.size());
        maxVar = new HashMap<String, Double>(queries.size());

        avgPMI = new HashMap<String, Double>(queries.size());
        maxPMI = new HashMap<String, Double>(queries.size());

        getPreRetreivalPredictors();

    }

    public void getPreRetreivalPredictors(){
        getSpecificityPreRetrievalPredictors();
        getRankSensitivityPreRetrievalPredictors();
        getAmbiguityPreRetrievalPredictors();
        getTermRelatednessPreRetrievalPredictors();
    }
  
    
	@Override
	public int getDocumentFrequency(String term) {
		return se.getDocumentFrequency(term);
	}

	@Override
	public int getTotalNumberOfDocuments() {
		return this.totalNumberOfDocuments;
	}

	@Override
	public long getTotalNumberOfTokens() {
		return this.totalNumberOfTokens;
	}

	@Override
	public Set<Integer> getPostings(String term) {
		return se.getPostings(term);
	}

	
	@Override
	public void getSpecificityPreRetrievalPredictors() {
		
		logger.info("Processing Specificity measures");
		for(OneQuery query: queries){
			List<String> terms = query.queryTerms;
			
			double[] idfs = new double[terms.size()];
			double[] ictf = new double[terms.size()];
			double[] scqs = new double[terms.size()];
			double[] termLength = new double[terms.size()];
			Set<Integer> setOfPostings = new HashSet<Integer>();
			
			logger.info("QueryId:" + query.queryId + " - Query:" + query.query + " AfterProcess:" + terms);
			
			for(int i = 0; i < terms.size(); i++){
				String term = terms.get(i);
				idfs[i] = getIDF(term);
            	ictf[i] = getICTF(term);
            	scqs[i] = getSCQ(term);
            	termLength[i] = term.length();
            	setOfPostings.addAll(getQS(term));
			}
		
			avgQL.put(query.queryId, QPPMath.mean(termLength));
			avgIDF.put(query.queryId, QPPMath.mean(idfs));
			avgICTF.put(query.queryId, QPPMath.mean(ictf));
			maxIDF.put(query.queryId, QPPMath.max(idfs));
			devIDF.put(query.queryId, QPPMath.standardDeviation(idfs));
			SCS.put(query.queryId, (QPPMath.log2(1.0/terms.size()) + avgICTF.get(query.queryId)));
			sumSCQ.put(query.queryId, QPPMath.sum(scqs));
			avgSCQ.put(query.queryId, QPPMath.mean(scqs));
			maxSCQ.put(query.queryId, QPPMath.max(scqs));
			QS.put(query.queryId, -1. * QPPMath.log(1. * setOfPostings.size() / this.getTotalNumberOfDocuments()));

		    logger.info("Maximum Number of documents returned by query: " + setOfPostings.size());
		}
	}

	@Override
	public double getIDF(String term) {
        int docFreq = this.getDocumentFrequency(term);
        if (docFreq > 0)
            return QPPMath.log(1. * this.getTotalNumberOfDocuments()) - QPPMath.log(1. * docFreq);
        return 0.0;
	}

	@Override
	public double getICTF(String term) {
		int docFreq = this.getDocumentFrequency(term);
	    if (docFreq > 0)
	    	return QPPMath.log2(this.getTotalNumberOfTokens()) - QPPMath.log2(docFreq);
	    return 0.0;
	}

	@Override
	public double getSCQ(String term) {
        int docFreq = this.getDocumentFrequency(term);
        if (docFreq > 0){
            double t1 = 1. + QPPMath.log(this.getTotalNumberOfTokens());
            double t2 = QPPMath.log(1. + 1. * this.getTotalNumberOfDocuments() / docFreq);
            return t1 * t2;
        }
        return 0.0;
	}

	@Override
	public Set<Integer> getQS(String term) {
		return se.getPostings(term);
	}

	@Override
	public void getRankSensitivityPreRetrievalPredictors() {
        logger.info("Processing Rank Sensitivity measures");
        for(OneQuery query: queries){
            List<String> terms = query.queryTerms;

            double[] vars = new double[terms.size()];
            logger.info("QueryId:" + query.queryId + " - Query:" + query.query + " AfterProcess:" + terms);

            for(int i = 0; i < terms.size(); i++){
                String term = terms.get(i);
                vars[i] = getVar(term);
            }

            sumVar.put(query.queryId, QPPMath.sum(vars));
            avgVar.put(query.queryId, QPPMath.mean(vars));
            maxVar.put(query.queryId, QPPMath.max(vars));
        }
	}

	@Override
	public double getVar(String term) {

        List<Integer> termFrequencies = se.getTermFrequencies(term);
		int numberOfDocuments = this.getTotalNumberOfDocuments();
		int documentFreq = this.getDocumentFrequency(term);
        if(documentFreq == 0)
            return 0.0;

        double idf = QPPMath.log1p(numberOfDocuments / documentFreq);

        List<Double> ws = new ArrayList<Double>();
        for(int freq: termFrequencies){
            double w = 1.0 + QPPMath.log(freq) * idf;
            ws.add(w);
        }
        Double[] ds = ws.toArray(new Double[ws.size()]);
        double[] d = ArrayUtils.toPrimitive(ds);

        return QPPMath.variance(d);
	}

	@Override
	public void getAmbiguityPreRetrievalPredictors() {
		// TODO Auto-generated method stub
	}

	@Override
	public void getTermRelatednessPreRetrievalPredictors() {
        logger.info("Processing Rank Sensitivity measures");
        for(OneQuery query: queries){
            List<String> terms = query.queryTerms;

            double[] pmis = new double[ (terms.size() * (terms.size() + 1)) / 2];
            logger.info("QueryId:" + query.queryId + " - Query:" + query.query + " AfterProcess:" + terms);

            int lastLoop = 0;
            for(int i = 0; i < terms.size(); i++){
                String ti = terms.get(i).toString();
                for(int j = i; j < terms.size(); j++) {
                    String tj = terms.get(j).toString();
                    double pmi = getPMI(ti, tj);
                    pmis[lastLoop + j - i] = pmi;
                }
                lastLoop = lastLoop + terms.size() - i;
            }
            avgPMI.put(query.queryId, QPPMath.mean(pmis));
            maxPMI.put(query.queryId, QPPMath.max(pmis));
        }
	}

	@Override
	public double getPMI(String term1, String term2) {
        int documents =  this.getTotalNumberOfDocuments();
        if(documents == 0)
            return 0.0;
        double freq1 = 1.0 * getDocumentFrequency(term1) / documents;
        double freq2 = 1.0 * getDocumentFrequency(term2) / documents;

        Set<Integer> intersection = getPostings(term1);
        intersection.retainAll(getPostings(term2));
        double freqIntersect = 1.0 * intersection.size() / documents;

        if(freq1 > 0 && freq2 > 0 && freqIntersect > 0)
            return QPPMath.log2( freqIntersect / (freq1 * freq2) );
        else
            return 0.0;
    }

	
	public void printResults(PrintWriter out){

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");

        String[] FILE_HEADER = {"qid","avgQL","avgIDF","avgICTF","maxIDF","devIDF","SCS", "sumSCQ","avgSCQ",
                "maxSCQ","QS", "sumVar", "avgVar", "maxVar", "avgPMI", "maxPMI"};

        CSVPrinter csvFilePrinter = null;
        try{
            csvFilePrinter = new CSVPrinter(out, csvFileFormat);
            csvFilePrinter.printRecord(FILE_HEADER);
		    for(OneQuery query: queries){
    			String qid = query.queryId;
                List record = new ArrayList<>();

                record.add(qid);
                // Specificity
                record.add(avgQL.get(qid));
                record.add(avgIDF.get(qid));
                record.add(avgICTF.get(qid));
                record.add(maxIDF.get(qid));
                record.add(devIDF.get(qid));
                record.add(SCS.get(qid));
                record.add(sumSCQ.get(qid));
                record.add(avgSCQ.get(qid));
                record.add(maxSCQ.get(qid));
                record.add(QS.get(qid));
                // Rank Sensitivity
                record.add(sumVar.get(qid));
                record.add(avgVar.get(qid));
                record.add(maxVar.get(qid));
                // Term Relatedness
                record.add(avgPMI.get(qid));
                record.add(maxPMI.get(qid));

                csvFilePrinter.printRecord(record);
	    	}
        } catch (Exception e) {
            logger.error("Error in CsvFileWriter:" + e.getMessage() );
        } finally {
            try {
                out.flush();
                out.close();
                csvFilePrinter.close();
            } catch (Exception e) {
                logger.error("Error while flushing/closing fileWriter/csvPrinter." + e.getMessage());
            }
        }
	}
	
	public static void main(String[] args) {
		QPP qpp = new QPP();
		qpp.printResults(new PrintWriter(System.out));
		System.out.println("DONE!");
	}

}
