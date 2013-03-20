package algo.neuralnetwork;

import java.util.Arrays;

public class FeedForwardNeuralNetwork extends NeuralNetwork {
	
	public FeedForwardNeuralNetwork(int layerSize, double[][] inputToHiddenSynapses, double[] hiddenToOutputSynapses) {
		
		Neuron[] inputNeurons = new Neuron[layerSize];
		for(int i = 0; i < layerSize; i++) {
			inputNeurons[i] = new Neuron(new EchoActivationFunction()); // input layer echoes inputs forward
		}
		
		Neuron[] hiddenNeurons = new Neuron[layerSize];
		for(int i = 0; i < layerSize; i++) {
			hiddenNeurons[i] = new Neuron(new SigmoidActivationFunction()); // hidden layer uses the sigmoid activation function
		}
		
		Neuron[] outputNeurons = new Neuron[] { new Neuron(new SigmoidActivationFunction()) }; // output layer uses the sigmoid activation function as well
		
		double[][] hiddenToOutputSynapsesMatrix = new double[layerSize][1];
		for(int i = 0; i < layerSize; i++) {
			hiddenToOutputSynapsesMatrix[i][0] = hiddenToOutputSynapses[i];
		}
		
		this.initialize(inputNeurons, hiddenNeurons, inputToHiddenSynapses, outputNeurons, hiddenToOutputSynapsesMatrix);
	}
	
	public double processSignalToOutput(double[] inputs) {
		return this.processSignal(inputs)[0];
	}
	
	public int getLayerSize() {
		return this.numInputNeurons;
	}

	@Override
	public double predict(double[] ivs) {
		return this.processSignalToOutput(ivs);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		b.append(String.format("Input --> Hidden Synapses%n"));
		for(int i = 0; i < this.numInputNeurons; i++) {
			double[] ithInputSynapseWeights = new double[this.numHiddenNeurons];
			for(int j = 0; j < this.numHiddenNeurons; j++) {
				ithInputSynapseWeights[j] = this.graph.getEdgeWeight(this.getInputNeuronIndex(i), this.getHiddenNeuronIndex(j));
			}
			b.append(Arrays.toString(ithInputSynapseWeights));
			b.append(String.format("%n"));
		}

		b.append(String.format("%n"));
		b.append(String.format("Hidden --> Output Synapses%n"));
		
		for(int i = 0; i < this.numOutputNeurons; i++) {
			double[] ithOutputSynapseWeights = new double[this.numHiddenNeurons];
			for(int j = 0; j < this.numHiddenNeurons; j++) {
				ithOutputSynapseWeights[j] = this.graph.getEdgeWeight(this.getHiddenNeuronIndex(j), this.getOutputNeuronIndex(i));
			}
			b.append(Arrays.toString(ithOutputSynapseWeights));
			b.append(String.format("%n"));
		}
		
		return b.toString();
	}
}
