package algo.util.statistics;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Combinatorics {
    static HashMap<Integer, BigInteger> cache = new HashMap<Integer,BigInteger>();

    // from: http://chaosinmotion.com/blog/?p=622
    public static BigInteger factorial(int n)
    {
        BigInteger ret;

        if (n == 0) return BigInteger.ONE;
        if (null != (ret = cache.get(n))) return ret;
        ret = BigInteger.valueOf(n).multiply(factorial(n-1));
        cache.put(n, ret);
        return ret;
    }
    
    public static BigInteger combine(int n, int k) {
    	return factorial(n).divide(factorial(k)).divide(factorial(n-k));
    }
    
    public static BigInteger permute(int n, int k) {
    	return factorial(n).divide(factorial(n-k));
    }
    
    public static BigInteger randomBigInt(BigInteger lower, BigInteger upper, Random r) {
    	return lower.add(randomBigInt(upper.subtract(lower), r));
    }
    
    public static BigInteger randomBigInt(BigInteger bound, Random r) {
    	BigInteger n;
    	do {
    		n = new BigInteger(bound.bitLength(), r); 
    	} while (n.compareTo(bound) > 0);
    	return n;
    }
    
    public static double randomDouble(double lowerBound, double upperBound, Random r) {
    	double d = r.nextDouble();
    	return lowerBound + (d * (upperBound - lowerBound));
    }
    
    public static Object randomElement(Object[] set, Map<Object, Double> densities) {
    	return randomElement(set, densities, new Random());
    }
    
    public static <T> T randomElement(T[] set, Map<T, Double> densities, Random r) {
    	double totalDensity = 0.0;
    	for(Object o : set) {
    		totalDensity += densities.get(o);
    	}
    	
    	double randomIndex = r.nextDouble() * totalDensity;
    	
    	double cumulativeDensity = 0.0;
    	int i = 0;
    	while(cumulativeDensity < randomIndex) {
    		double density = densities.get(set[i]);
    		cumulativeDensity += density;
    		i++;
    	}
    	
    	return set[i - 1];
    }
    
    public static <T> T[] randomSubset(T[] set, int subsetSize, Class<T> ct) {
    	return randomSubset(set, subsetSize, new Random(), ct);
    }
    
	public static <T> T[] randomSubset(T[] set, int subsetSize, Random r, Class<T> ct) {
		BigInteger orderSetSize = combine(set.length, subsetSize);
		BigInteger orderID = randomBigInt(orderSetSize, r);
		return getMthLexicographicCombination(set, subsetSize, orderID, ct);
	}
	
	public static <T> T[] getMthLexicographicPermutation(T[] set, BigInteger m, Class<T> ct) {
		return getMthLexicographicCombination(set, set.length, m, ct);
	}

	//from: http://msdn.microsoft.com/en-us/library/aa289166(v=vs.71).aspx
	public static <T> T[] getMthLexicographicCombination(T[] set, int k, BigInteger m, Class<T> ct) {
		int[] ans = new int[k];
		
		int a = set.length;
		int b = k;
		BigInteger x = (combine(set.length, k).subtract(BigInteger.ONE)).subtract(m); // x is the "dual" of m
		
		for (int i = 0; i < k; ++i) {
			ans[i] = largestV(a,b,x); // largest value v, where v < a and vCb < x    
			x = x.subtract(combine(ans[i],b));
			a = ans[i];
			b = b-1;
		}
		
		T[] c = (T[])Array.newInstance(ct, k);
		
		for (int i = 0; i < k; ++i) {
			ans[i] = (set.length-1) - ans[i];
			c[i] = set[ans[i]];
		}
		
		return c;
	}
	
	private static int largestV(int a, int b, BigInteger x) {
		int v = a - 1;
		while(combine(v,b).compareTo(x) > 0)
			--v;
		return v;
	}

}
