package algo.neuralnetwork;

import sqlWrappers.SQLDatabase;
import algo.PredictiveModel;
import algo.util.graph.WeightedDirectedGraph;

public class NeuralNetwork extends PredictiveModel {

	protected WeightedDirectedGraph<Neuron> graph;
	
	protected int numInputNeurons;
	protected int numHiddenNeurons;
	protected int numOutputNeurons;
	
	public NeuralNetwork(int[] inputFeatures) {
		super(inputFeatures);
	}
	
	public NeuralNetwork(int[] inputFeatures, Neuron[] inputNeurons, Neuron[] hiddenNeurons, double[][] inputToHiddenSynapses, Neuron[] outputNeurons, double[][] hiddenToOutputSynapses) {
		super(inputFeatures);
		this.initialize(inputNeurons, hiddenNeurons, inputToHiddenSynapses, outputNeurons, hiddenToOutputSynapses);
	}
	
	protected void initialize (Neuron[] inputNeurons, Neuron[] hiddenNeurons, double[][] inputToHiddenSynapses, Neuron[] outputNeurons, double[][] hiddenToOutputSynapses) {
		
		this.numInputNeurons = inputNeurons.length;
		this.numHiddenNeurons = hiddenNeurons.length;
		this.numOutputNeurons = outputNeurons.length;
		
		int n = this.numInputNeurons + this.numHiddenNeurons + this.numOutputNeurons;
		
		Neuron[] nodes = new Neuron[n];
		
		for(int j = 0; j < this.numInputNeurons; j++) {
			nodes[this.getInputNeuronIndex(j)] = inputNeurons[j];
		}
		
		for(int j = 0; j < this.numHiddenNeurons; j++) {
			nodes[this.getHiddenNeuronIndex(j)] = hiddenNeurons[j];
		}
		
		for(int j = 0; j < this.numOutputNeurons; j++) {
			nodes[this.getOutputNeuronIndex(j)] = outputNeurons[j];
		}
		
		double[][] edges = new double[n][n];
		
		if(inputToHiddenSynapses.length != this.numInputNeurons) throw new IllegalArgumentException();
		for(int j = 0; j < this.numInputNeurons; j++) {
			if(inputToHiddenSynapses[j].length != this.numHiddenNeurons) throw new IllegalArgumentException();
		}
		
		for(int j = 0; j < this.numInputNeurons; j++) {
			for(int k = 0; k < this.numHiddenNeurons; k++) {
				edges[this.getInputNeuronIndex(j)][this.getHiddenNeuronIndex(k)] = inputToHiddenSynapses[j][k];
			}
		}
		
		if(hiddenToOutputSynapses.length != this.numHiddenNeurons) throw new IllegalArgumentException();
		for(int j = 0; j < this.numHiddenNeurons; j++) {
			if(hiddenToOutputSynapses[j].length != this.numOutputNeurons) throw new IllegalArgumentException();
		}
		
		for(int j = 0; j < this.numHiddenNeurons; j++) {
			for(int k = 0; k < this.numOutputNeurons; k++) {
				edges[this.getHiddenNeuronIndex(j)][this.getOutputNeuronIndex(k)] = hiddenToOutputSynapses[j][k];
			}
		}
		
		graph = new WeightedDirectedGraph<Neuron>(nodes, edges);
	}
	
	protected int getInputNeuronIndex(int index) {
		return index;
	}
	
	protected int getHiddenNeuronIndex(int index) {
		return this.numInputNeurons + index;
	}
	
	protected int getOutputNeuronIndex(int index) {
		return this.numInputNeurons + this.numHiddenNeurons + index;
	}
	
	public double[] processSignal(double[] inputs) {
		if(inputs.length != this.numInputNeurons) throw new IllegalArgumentException();
		
		double[] inputFires = new double[this.numInputNeurons];
		for(int i = 0; i < this.numInputNeurons; i++) {
			Neuron n = this.graph.getNodeAt(this.getInputNeuronIndex(i));
			inputFires[i] = n.process(new double[] { inputs[i] });
		}
		
		double[] hiddenFires = new double[this.numHiddenNeurons];
		for(int i = 0; i < this.numHiddenNeurons; i++) {
			// gather input signals
			double[] hiddenInput = new double[this.numInputNeurons];
			for(int j = 0; j < this.numInputNeurons; j++) {
				double amplification = graph.getEdgeWeight(this.getInputNeuronIndex(j), this.getHiddenNeuronIndex(i));
				hiddenInput[j] = inputFires[j] * amplification;
			}
			
			Neuron n = this.graph.getNodeAt(this.getHiddenNeuronIndex(i));
			hiddenFires[i] = n.process(hiddenInput);
		}
		
		double[] output = new double[this.numOutputNeurons];
		for(int i = 0; i < this.numOutputNeurons; i++) {
			// gather input signals
			double[] hiddenOutput = new double[this.numHiddenNeurons];
			for(int j = 0; j < this.numHiddenNeurons; j++) {
				double amplification = graph.getEdgeWeight(this.getHiddenNeuronIndex(j), this.getOutputNeuronIndex(i));
				hiddenOutput[j] = hiddenFires[j] * amplification;
			}
			
			for(int j = 0; j < this.numHiddenNeurons; j++) {
				output[i] += hiddenOutput[j];
			}
			
			Neuron o = this.graph.getNodeAt(this.getOutputNeuronIndex(i));
			output[i] = o.process(new double[] { output[i] });
		}
		
		return output;
	}
	
	public double getInputToHiddenSynapseWeight(int inputNeuronIndex, int hiddenNeuronIndex) {
		return graph.getEdgeWeight(this.getInputNeuronIndex(inputNeuronIndex), this.getHiddenNeuronIndex(hiddenNeuronIndex));
	}
	
	public double getHiddenToOutputSynapseWeight(int hiddenNeuronIndex, int outputNeuronIndex) {
		return graph.getEdgeWeight(this.getHiddenNeuronIndex(hiddenNeuronIndex), this.getOutputNeuronIndex(outputNeuronIndex));
	}
	
	@Override
	public int hashCode() {
		return (graph.hashCode() * this.numHiddenNeurons * this.numInputNeurons * this.numOutputNeurons) % Integer.MAX_VALUE;
	}

	@Override
	public int toDB(SQLDatabase db, int problemID, int algoID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double predict(double[] indexedInputs) {
		return this.processSignal(indexedInputs)[0];
	}

	@Override
	public void fromDB(SQLDatabase sqlDatabase, int modelID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getModelTypeID() {
		// TODO assign modelTypeID
		return -1;
	}
}
