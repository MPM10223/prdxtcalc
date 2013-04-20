package algo.neuralnetwork;

import org.junit.Assert;
import org.junit.Test;

public class NeuralNetworkTest {

	@Test
	public void testXOR() {
		
		int[] inputFeatures = new int[] {1, 2};
		Neuron[] inputNeurons = new Neuron[] { new Neuron(0), new Neuron(0) };
		Neuron[] hiddenNeurons = new Neuron[] { new Neuron(1.5), new Neuron(0.5) };
		double[][] inputToHiddenSynapses = new double[][] { { 1, 1 }, { 1, 1} };
		Neuron[] outputNeurons = new Neuron[] { new Neuron(0.5) };
		double[][] hiddenToOutputSynapses = new double[][] { { -1 }, { 1 } };
		
		NeuralNetwork ann = new NeuralNetwork(inputFeatures, inputNeurons, hiddenNeurons, inputToHiddenSynapses, outputNeurons, hiddenToOutputSynapses);
		
		double[] output;
		
		output = ann.processSignal(new double[] {0, 0});
		Assert.assertArrayEquals(new double[] {0} , output, 0.0);
		
		output = ann.processSignal(new double[] {0, 1});
		Assert.assertArrayEquals(new double[] {1} , output, 0.0);

		output = ann.processSignal(new double[] {1, 0});
		Assert.assertArrayEquals(new double[] {1} , output, 0.0);

		output = ann.processSignal(new double[] {1, 1});
		Assert.assertArrayEquals(new double[] {0} , output, 0.0);
	}
	
	@Test
	public void testOR() {
		
		int[] inputFeatures = new int[] {1, 2};
		Neuron[] inputNeurons = new Neuron[] { new Neuron(0), new Neuron(0) };
		Neuron[] hiddenNeurons = new Neuron[] { new Neuron(0.9) };
		double[][] inputToHiddenSynapses = new double[][] { { 1 }, { 1 } };
		Neuron[] outputNeurons = new Neuron[] { new Neuron(1.0) };
		double[][] hiddenToOutputSynapses = new double[][] { { 1 } };
		
		NeuralNetwork ann = new NeuralNetwork(inputFeatures, inputNeurons, hiddenNeurons, inputToHiddenSynapses, outputNeurons, hiddenToOutputSynapses);
		
		double[] output;
		
		output = ann.processSignal(new double[] {0, 0});
		Assert.assertArrayEquals(new double[] {0} , output, 0.0);
		
		output = ann.processSignal(new double[] {0, 1});
		Assert.assertArrayEquals(new double[] {1} , output, 0.0);

		output = ann.processSignal(new double[] {1, 0});
		Assert.assertArrayEquals(new double[] {1} , output, 0.0);

		output = ann.processSignal(new double[] {1, 1});
		Assert.assertArrayEquals(new double[] {1} , output, 0.0);
	}
	
	@Test
	public void testAND() {
		
		int[] inputFeatures = new int[] {1, 2};
		Neuron[] inputNeurons = new Neuron[] { new Neuron(0), new Neuron(0) };
		Neuron[] hiddenNeurons = new Neuron[] { new Neuron(1.5) };
		double[][] inputToHiddenSynapses = new double[][] { { 1 }, { 1 } };
		Neuron[] outputNeurons = new Neuron[] { new Neuron(0.5) };
		double[][] hiddenToOutputSynapses = new double[][] { { 1 } };
		
		NeuralNetwork ann = new NeuralNetwork(inputFeatures, inputNeurons, hiddenNeurons, inputToHiddenSynapses, outputNeurons, hiddenToOutputSynapses);
		
		double[] output;
		
		output = ann.processSignal(new double[] {0, 0});
		Assert.assertArrayEquals(new double[] {0} , output, 0.0);
		
		output = ann.processSignal(new double[] {0, 1});
		Assert.assertArrayEquals(new double[] {0} , output, 0.0);

		output = ann.processSignal(new double[] {1, 0});
		Assert.assertArrayEquals(new double[] {0} , output, 0.0);

		output = ann.processSignal(new double[] {1, 1});
		Assert.assertArrayEquals(new double[] {1} , output, 0.0);
	}
}
