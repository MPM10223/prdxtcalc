package algo.util.search;

import junit.framework.Assert;

import org.junit.Test;

public class GeneratorUtilityTest {

	@Test
	public void test_generateDouble() {
		/*
		long l = Double.doubleToLongBits(Double.MAX_VALUE);
		
		byte[] tmp = new byte[8];
		ByteBuffer b = ByteBuffer.wrap(tmp);
		b.putLong(l);
		
		System.out.println(Arrays.toString(tmp));
		*/
		
		Assert.assertEquals(0.0, 				GeneratorUtility.generateDouble(	new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 } ));
		Assert.assertEquals(Double.MAX_VALUE, 	GeneratorUtility.generateDouble(	new byte[] { 127, -17, -1, -1, -1, -1, -1, -1} ));
		Assert.assertEquals(-Double.MAX_VALUE, 	GeneratorUtility.generateDouble(	new byte[] { -1, -17, -1, -1, -1, -1, -1, -1} ));
	}

	@Test
	public void test_generateDouble_bounded() {
		Assert.assertEquals(6.0, GeneratorUtility.generateDouble(new byte[] { 127, 127, 1, 127, 1, 127, 17, 127}, -10, 10));
	}
	
	@Test
	public void test_generateDoubleArray() {
		org.junit.Assert.assertArrayEquals(new double[] { 0.0 }, GeneratorUtility.generateDoubleArray(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }), 0.0);
		org.junit.Assert.assertArrayEquals(new double[] { 0.0, Double.MAX_VALUE }, GeneratorUtility.generateDoubleArray(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 127, -17, -1, -1, -1, -1, -1, -1 }), 0.0);
	}

	@Test
	public void test_generateDoubleMatrix_1x1() {
		byte[] input = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		
		double[][] output = GeneratorUtility.generateDoubleMatrix(input, 1);
		
		Assert.assertEquals(1, output.length);
		org.junit.Assert.assertArrayEquals(new double[] { 0.0 }, output[0], 0.0);
	}
	
	@Test
	public void test_generateDoubleMatrix_2x2() {
		byte[] input = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1,   0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 2,   0, 0, 0, 0, 0, 0, 0, 3 };
		
		double[][] output = GeneratorUtility.generateDoubleMatrix(input, 2);
		
		Assert.assertEquals(2, output.length);
		org.junit.Assert.assertArrayEquals(new double[] { 4.9E-324, 0.0 } , output[0], 0.0);
		org.junit.Assert.assertArrayEquals(new double[] { 1.0E-323, 1.5E-323 } , output[1], 0.0);
	}

	@Test
	public void test_generateDoubleMatrix_1x4() {
		byte[] input = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1,   0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 2,   0, 0, 0, 0, 0, 0, 0, 3 };
		
		double[][] output = GeneratorUtility.generateDoubleMatrix(input, 1);
		
		Assert.assertEquals(1, output.length);
		org.junit.Assert.assertArrayEquals(new double[] { 4.9E-324, 0.0, 1.0E-323, 1.5E-323 } , output[0], 0.0);
	}
	
	@Test
	public void test_generateDoubleMatrix_4x1() {
		byte[] input = new byte[] { 0, 0, 0, 0, 0, 0, 0, 1,   0, 0, 0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0, 0, 0, 2,   0, 0, 0, 0, 0, 0, 0, 3 };
		
		double[][] output = GeneratorUtility.generateDoubleMatrix(input, 4);
		
		Assert.assertEquals(4, output.length);
		org.junit.Assert.assertArrayEquals(new double[] { 4.9E-324 } , output[0], 0.0);
		org.junit.Assert.assertArrayEquals(new double[] { 0.0 } , output[1], 0.0);
		org.junit.Assert.assertArrayEquals(new double[] { 1.0E-323 } , output[2], 0.0);
		org.junit.Assert.assertArrayEquals(new double[] { 1.5E-323 } , output[3], 0.0);
		
	}


}
