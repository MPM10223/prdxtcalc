package algo.neuralnetwork;

import java.util.HashMap;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;


public class DiscretizedNeuralNetworkTest {

	@Test
	public void test_discretize() {
		int[] inputFeatures = new int[] {1, 2};
		double[][] threshholds = new double[][] { { 1, 2 } };
		DiscretizedNeuralNetwork dnn = new DiscretizedNeuralNetwork(inputFeatures, 1, new double[][] { { 1 } }, new double[] {1}, threshholds);
		Assert.assertEquals(0.0, dnn.discretize(new double[] { 0.5 } )[0]);
		Assert.assertEquals(1.0, dnn.discretize(new double[] { 1.5 } )[0]);
		Assert.assertEquals(2.0, dnn.discretize(new double[] { 2.5 } )[0]);
	}
	
	@Test
	public void test_roundtripDNA_One() {
		DiscretizedNeuralNetworkGenerator g = new DiscretizedNeuralNetworkGenerator(new int[] {1,2,3,4,5,6,7,8,9,10,11,12});
		
		byte[] dna = new byte[g.getDNASize()];
		dna[0] = 1;
		
		DiscretizedNeuralNetwork nn = g.generate(dna);
		org.junit.Assert.assertArrayEquals(dna, nn.getDNA());
	}
	
	@Test
	public void test_roundtripDNA_rand() {
		DiscretizedNeuralNetworkGenerator g = new DiscretizedNeuralNetworkGenerator(new int[] {1,2,3,4,5,6,7,8,9,10,11,12});
		
		byte[] dna = new byte[g.getDNASize()];
		Random r = new Random(781789653);
		r.nextBytes(dna);
		
		DiscretizedNeuralNetwork nn = g.generate(dna);
		org.junit.Assert.assertArrayEquals(dna, nn.getDNA());
	}
	
	@Test
	public void test_processSignal_Excel() {
		
		int[] inputFeatures = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
		double[][] ithSynapses = new double[12][12];
		for(int i = 0; i < 12; i++) {
			for(int j = 0; j < 12; j++) {
				ithSynapses[i][j] = ((i + 1 + j + 1) % 10) - 5;
			}
		}
		
		double[] htoSynapses = new double[12];
		for(int i = 0; i < 12; i++) {
			htoSynapses[i] = ((2 * (i + 1)) % 10) - 5;
		}
		
		double[][] inputThreshholds = new double[12][4];
		for(int i = 0; i < 12; i++) {
			inputThreshholds[i][0] = -20;
			inputThreshholds[i][1] = -5;
			inputThreshholds[i][2] = 5;
			inputThreshholds[i][3] = 20;
		}
		
		DiscretizedNeuralNetwork dnn = new DiscretizedNeuralNetwork(inputFeatures, 12, ithSynapses, htoSynapses, inputThreshholds);
		
		//double[] inputs = new double[] {0, -50, 50, -10, 10, 0, -50, 50, -10, 10, 7, -7};
		HashMap<Integer, Double> inputs = new HashMap<Integer, Double>(12);
		inputs.put(1, 0.0);
		inputs.put(2, -50.0);
		inputs.put(3, 50.0);
		inputs.put(4, -10.0);
		inputs.put(5, 10.0);
		inputs.put(6, 0.0);
		inputs.put(7, -50.0);
		inputs.put(8, 50.0);
		inputs.put(9, -10.0);
		inputs.put(10, 10.0);
		inputs.put(11, 7.0);
		inputs.put(12, -7.0);
		
		double prediction = dnn.predict(inputs);
		Assert.assertEquals(0.13183917638641454, prediction);
		
	}
}
