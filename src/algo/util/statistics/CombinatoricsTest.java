package algo.util.statistics;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

public class CombinatoricsTest {

	@Test
	public void testRandomBigInt_bound_0() {
		Random r = new Random();
		BigInteger b = Combinatorics.randomBigInt(new BigInteger(new byte[] {0}), r);
		Assert.assertEquals(0, b.intValue());
	}
	
	@Test
	public void testRandomBigInt_between_0_0() {
		Random r = new Random();
		BigInteger b = Combinatorics.randomBigInt(new BigInteger(new byte[] {0}), new BigInteger(new byte[] {0}), r);
		Assert.assertEquals(0, b.intValue());
	}
	
	@Test
	public void testRandomBigInt_between_1_1() {
		Random r = new Random();
		BigInteger b = Combinatorics.randomBigInt(new BigInteger(new byte[] {1}), new BigInteger(new byte[] {1}), r);
		Assert.assertEquals(1, b.intValue());
	}

	@Test
	public void testRandomElement() {
		Random r = new Random();
		Integer[] set = {0,1,2};
		HashMap<Integer,Double> densities = new HashMap<Integer, Double>(2);
		densities.put(0, 0.50);
		densities.put(1, 1.00);
		densities.put(2, 0.25);
		
		int zeroCount = 0;
		for(int i = 0; i < 1000; i++) {
			int e = Combinatorics.randomElement(set, densities, r);
			if(e == 0) zeroCount++;
		}
		
		Assert.assertTrue(zeroCount >= 250 && zeroCount <= 320);
	}
}
