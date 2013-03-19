package algo.util.search;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import junit.framework.Assert;

public abstract class GeneratorUtility {
	
	public static final int BYTES_PER_DOUBLE = 8;
	
	public static double generateDouble(byte[] data) {
		return generateDouble(data, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public static double generateDouble(byte[] data, double min, double max) {
		if(data.length != BYTES_PER_DOUBLE) throw new IllegalArgumentException();
		
		ByteBuffer b = ByteBuffer.wrap(data);
		
		double d;
		if(Double.isInfinite(min) || Double.isInfinite(max)) {
			d = b.getDouble();
			 
			if(Double.isNaN(d) || Double.isInfinite(d)) {
				d = 0.0;
			}
			
			if(d < min || d > max) {
				d = min + Math.abs(d % (max - min));
			}
			
		} else {
			long seed = b.getLong();
			Random r = new Random(seed);
			double x = r.nextDouble();
			d = min + (x * (max - min));
		}
				
		Assert.assertTrue(d >= min && d <= max);
		
		return d;
	}
	
	public static double[] generateDoubleArray(byte[] data) {
		return generateDoubleArray(data, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public static double[] generateDoubleArray(byte[] data, double min, double max) {
		if(data.length % BYTES_PER_DOUBLE != 0) throw new IllegalArgumentException();
		
		int n = data.length / BYTES_PER_DOUBLE;
		double[] d = new double[n];
		ByteBuffer b = ByteBuffer.wrap(data);
		
		for(int i = 0; i < n; i++) {
			byte[] doubleData = new byte[BYTES_PER_DOUBLE];
			b.get(doubleData);
			d[i] = generateDouble(doubleData, min, max);
			//d[i] = b.getDouble(i * BYTES_PER_DOUBLE);
		}
		
		return d;
	}
	
	public static double[][] generateDoubleMatrix(byte[] data, int d1) {
		return generateDoubleMatrix(data, d1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
		
	public static double[][] generateDoubleMatrix(byte[] data, int d1, double min, double max) {
		if(	data.length % d1 != 0 ||
			data.length / d1 % BYTES_PER_DOUBLE != 0) throw new IllegalArgumentException();
		
		int d2 = data.length / BYTES_PER_DOUBLE / d1;
		
		double[][] d = new double[d1][d2];
		
		for(int i = 0; i < d1; i++) {
			int startIndex = i * BYTES_PER_DOUBLE * d2;
			int endIndex = startIndex + (BYTES_PER_DOUBLE * d2);
			byte[] rowData = Arrays.copyOfRange(data, startIndex, endIndex);
			d[i] = generateDoubleArray(rowData, min, max);
		}
		
		return d;
	}

}
