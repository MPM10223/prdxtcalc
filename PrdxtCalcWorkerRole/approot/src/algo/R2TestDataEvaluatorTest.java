package algo;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

public class R2TestDataEvaluatorTest {

	@Test
	public void testSpeed() {
		int n = 100000;
		int k = 10;
		
		Observation[] o = new Observation[n];
		int[] features = new int[k];
		Random r = new Random(781789653);
		
		for(int i = 0; i < n; i++) {
			HashMap<Integer, Double> ivs = new HashMap<Integer, Double>(k);
			for(int j = 0; j < k; j++) {
				features[j] = j;
				ivs.put(j, r.nextDouble());
			}
			
			double dv = r.nextDouble();
			
			Observation x = new Observation(String.valueOf(i), ivs, dv);
			
			o[i] = x;
		}
		
		R2TestDataEvaluator e = new R2TestDataEvaluator(o);
		
		MockModel m = new MockModel(features);
		
		double score = e.evaluate(m);
		
		Assert.assertEquals(-2.997571234193931, score);
	}
	
	private class MockModel extends PredictiveModel {

		public MockModel(int[] features) {
			super(features);
		}
		
		@Override
		public int getModelTypeID() {
			return -1;
		}

		@Override
		public boolean getPrefersBatchPrediction() {
			return false;
		}

		@Override
		protected double predict(double[] indexedInputs) {
			return 0;
		}
	}

}
