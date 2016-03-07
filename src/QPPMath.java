package qpp;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class QPPMath {

    // Internal math functions 
    public static double sum(double[] a){
        return new Sum().evaluate(a, 0, a.length);
    }
    
    public static double max(double[] a){
        return new Max().evaluate(a, 0, a.length);
    }

    public static double standardDeviation(double[] a){
        return new StandardDeviation().evaluate(a, 0, a.length);
    }
    
    public static double log2(double v){
        return Math.log(v) / Math.log(2.0);
    }

    public static double log1p(double v){return Math.log1p(v);}

    public static double log(double v){return Math.log(v);}

    public static double mean(double[] a){
    	return new Mean().evaluate(a, 0, a.length);
    }

    public static double variance(double [] a){ return new Variance(false).evaluate(a, 0, a.length);}
}
