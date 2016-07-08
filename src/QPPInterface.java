package qpp;

import java.util.Set;

public interface QPPInterface {
    
    //constructor 
    //public void QPP(); //{ 
        //getSpecificityPreRetrievalPredictors();
        //getRankSensitivityPreRetrievalPredictors();
        //getAmbiguityPreRetrievalPredictors();
        //getTermRelatednessPreRetrievalPredictors();
    //} 

    // Print Results
    //void printResults(PrintWriter pw, SearchRequest q);

    // Search related functions
    int getDocumentFrequency(String term);
    int getCollectionFrequency(String term);
    int getTotalNumberOfDocuments();
    long getTotalNumberOfTokens();
    Set<Integer> getPostings(String term);

    /*
    *  Calculates the Specificity group of pre-retrieval predictions. See Chapter 2.6 of Hauff's thesis.
    *
    */
    void getSpecificityPreRetrievalPredictors();

    // Specificity Pre-Retrieval QPP functions
    double getIDF(String term);
    double getICTF(String term);
    double getSCQ(String term);
    Set<Integer> getQS(String term);

    /*
    *  Calculates the Rank sensitivity group of pre-retrieval predictions. See Chapter 2.7 of Hauff's thesis.
    *
    */
    public void getRankSensitivityPreRetrievalPredictors();
    // Rank Sensitivity Pre-Retrieval QPP functions
    double getVar(String term);

    /*
    *  Calculates the Ambiguity group of pre-retrieval predictions. See Chapter 2.8 of Hauff's thesis.
    *
    */
    void getAmbiguityPreRetrievalPredictors();

    /*
    *  Calculates the Term relatedness group of pre-retrieval predictions. See Chapter 2.9 of Hauff's thesis.
    *
    */
    public void getTermRelatednessPreRetrievalPredictors();

    // Term Relatedness Pre-Retrieval QPP functions
    double getPMI(String term1, String term2);


}
