package algo.neuralnetwork;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

import junit.framework.Assert;

import algo.PredictiveModel;

import algo.util.dao.SQLInsertBuffer;
import algo.util.search.IGeneticOrganism;

public class DiscretizedNeuralNetwork extends PredictiveModel implements IGeneticOrganism {
	
	protected FeedForwardNeuralNetwork nn;
	protected double[][] threshholds;
	
	public DiscretizedNeuralNetwork() {
		super();
	}
	
	public DiscretizedNeuralNetwork(int[] inputFeatures, int layerSize, double[][] inputToHiddenSynapses, double[] hiddenToOutputSynapses, double[][] threshholds) {
		super(inputFeatures);
		this.initialize(layerSize, inputToHiddenSynapses, hiddenToOutputSynapses, threshholds);
	}
	
	private void initialize(int layerSize, double[][] inputToHiddenSynapses, double[] hiddenToOutputSynapses, double[][] threshholds) {
		this.nn = new FeedForwardNeuralNetwork(this.inputFeatures, layerSize, inputToHiddenSynapses, hiddenToOutputSynapses);
		this.threshholds = threshholds;
	}
	
	public double[][] getThreshholds() {
		return threshholds;
	}

	public void setThreshholds(double[][] threshholds) {
		this.threshholds = threshholds;
	}

	public double processSignal(double[] inputs) {
		if(inputs.length != threshholds.length) throw new IllegalArgumentException();
		
		double[] discretizedInputs = this.discretize(inputs);
		return nn.processSignalToOutput(discretizedInputs);
	}

	public double[] discretize(double[] inputs) {
		if(inputs.length != threshholds.length) throw new IllegalArgumentException();
		
		double[] discretized = new double[inputs.length];
		for(int i = 0; i < inputs.length; i++) {
			int j;
			for(j = 0; j < threshholds[i].length; j++) {
				if(inputs[i] < threshholds[i][j]) {
					break;
				}
			}
			discretized[i] = j;
		}
		
		return discretized;
	}
	
	@Override
	public int hashCode() {
		double product = 1;
		for(int i = 0; i < threshholds.length; i++) {
			for(int j = 0; j < threshholds[i].length; j++) {
				product *= threshholds[i][j];
			}
		}
		
		return (nn.hashCode() * (int)(Math.round(product) % Integer.MAX_VALUE)) % Integer.MAX_VALUE;
	}

	@Override
	public byte[] getDNA() {
		// DNA:
		// 1) N x N : input > hidden weights
		// 2) N		: hidden > output weights
		// 3) t x N	: discretization threshholds
		int n = threshholds.length;
		
		int sectionOneLength = (int)Math.pow(n, 2) * DiscretizedNeuralNetworkGenerator.BYTES_PER_SEGMENT;
		int sectionTwoLength = n * DiscretizedNeuralNetworkGenerator.BYTES_PER_SEGMENT;
		int sectionThreeLength = n * DiscretizedNeuralNetworkGenerator.MAX_DISCRETIZATION_THRESHHOLDS * DiscretizedNeuralNetworkGenerator.BYTES_PER_SEGMENT;
		
		byte[] data = new byte[sectionOneLength + sectionTwoLength + sectionThreeLength];
		
		ByteBuffer b = ByteBuffer.wrap(data);
		
		// 1) N x N : input > hidden weights
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				double w = nn.getInputToHiddenSynapseWeight(i, j);
				b.putDouble(w);
			}
		}
		
		// 2) N		: hidden > output weights
		for(int i = 0; i < n; i++) {
			double w = nn.getHiddenToOutputSynapseWeight(i, 0);
			b.putDouble(w);
		}
		
		// 3) t x N	: discretization threshholds
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < DiscretizedNeuralNetworkGenerator.MAX_DISCRETIZATION_THRESHHOLDS; j++) {
				double t = threshholds[i][j];
				b.putDouble(t);
			}
		}
		
		Assert.assertTrue(!b.hasRemaining());
		
		return data;
	}

	@Override
	protected double predict(double[] indexedInputs) {
		return nn.predict(this.discretize(indexedInputs));
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		b.append(String.format("Discretization Threshholds:%n"));
		for(int i = 0; i < this.threshholds[0].length; i++) {
			double[] ithThreshholds = new double[this.threshholds.length];
			for(int j = 0; j < this.threshholds.length; j++) {
				ithThreshholds[j] = this.threshholds[j][i];
			}
			b.append(Arrays.toString(ithThreshholds));
			b.append(String.format("%n"));
		}
		
		b.append(String.format("%n"));

		b.append(this.nn.toString());
		
		return b.toString();
	}

	@Override
	public int toDB(SQLDatabase db, int problemID, int algoID) {
		int modelID = super.toDB(db, problemID, algoID);
		
		int n = nn.getLayerSize();
		
		// 1) N x N : input > hidden weights
		SQLInsertBuffer b = new SQLInsertBuffer(db, "dnn_inputToHiddenSynapses", new String[] {"[modelID]","[inputSynapseIndex]","[hiddenSynapseIndex]","[weight]"}); 
		b.startBufferedInsert((int)Math.pow(n, 2));
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				b.insertRow(new String[] {String.valueOf(modelID), String.valueOf(i), String.valueOf(j), String.valueOf(nn.getInputToHiddenSynapseWeight(i, j))});
			}
		}
		b.finishBufferedInsert();
		
		// 2) N		: hidden > output weights
		b = new SQLInsertBuffer(db, "dnn_hiddenToOutputSynapses", new String[] {"[modelID]","[synapseIndex]","[weight]"}); 
		b.startBufferedInsert(n);
		
		for(int i = 0; i < n; i++) {
			b.insertRow(new String[] {String.valueOf(modelID), String.valueOf(i), String.valueOf(nn.getHiddenToOutputSynapseWeight(i, 0))});
		}
		b.finishBufferedInsert();
		
		// 3) t x N	: discretization threshholds
		b = new SQLInsertBuffer(db, "dnn_discretizationThreshholds", new String[] {"[modelID]","[inputIndex]","[threshholdIndex]","[boundary]"}); 
		b.startBufferedInsert(nn.getLayerSize() * DiscretizedNeuralNetworkGenerator.MAX_DISCRETIZATION_THRESHHOLDS);
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < DiscretizedNeuralNetworkGenerator.MAX_DISCRETIZATION_THRESHHOLDS; j++) {
				double t = threshholds[i][j];
				b.insertRow(new String[] {String.valueOf(modelID), String.valueOf(i), String.valueOf(j), String.valueOf(t)});
			}
		}
		
		b.finishBufferedInsert();
		
		return modelID;
	}

	@Override
	public void fromDB(SQLDatabase db, int modelID) {
		super.fromDB(db, modelID);
	
		int layerSize;
		double[][] inputToHiddenSynapses;
		double[] hiddenToOutputSynapses;
		double[][] threshholds;
		
		// 1) N x N : input > hidden weights (dnn_inputToHiddenSynapses)
		String sql = String.format("SELECT inputSynapseIndex, hiddenSynapseIndex, weight FROM dnn_inputToHiddenSynapses WHERE modelID = %d", modelID);
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		layerSize = results.size();
		
		inputToHiddenSynapses = new double[layerSize][layerSize];
		for(Map<String,String> row : results) {
			int inputSynapseIndex = Integer.parseInt(row.get("inputSynapseIndex"));
			int hiddenSynapseIndex = Integer.parseInt(row.get("hiddenSynapseIndex"));
			double weight = Double.parseDouble(row.get("weight"));
			inputToHiddenSynapses[inputSynapseIndex][hiddenSynapseIndex] = weight; 
		}
		
		// 2) N		: hidden > output weights (dnn_hiddenToOutputSynapses)
		sql = String.format("SELECT synapseIndex, weight FROM dnn_hiddenToOutputSynapses WHERE modelID = %d", modelID);
		results = db.getQueryRows(sql);
		
		hiddenToOutputSynapses = new double[layerSize];
		for(Map<String,String> row : results) {
			int synapseIndex = Integer.parseInt(row.get("synapseIndex"));
			double weight = Double.parseDouble(row.get("weight"));
			hiddenToOutputSynapses[synapseIndex] = weight; 
		}
		
		// 3) t x N	: discretization threshholds (dnn_discretizationThreshholds)
		sql = String.format("SELECT inputIndex, threshholdIndex, boundary FROM dnn_discretizationThreshholds WHERE modelID = %d", modelID);
		results = db.getQueryRows(sql);
		
		threshholds = new double[layerSize][DiscretizedNeuralNetworkGenerator.MAX_DISCRETIZATION_THRESHHOLDS];
		for(Map<String,String> row : results) {
			int inputIndex = Integer.parseInt(row.get("inputIndex"));
			int threshholdIndex = Integer.parseInt(row.get("threshholdIndex"));
			double boundary = Double.parseDouble(row.get("boundary"));
			threshholds[inputIndex][threshholdIndex] = boundary; 
		}
		
		this.initialize(layerSize, inputToHiddenSynapses, hiddenToOutputSynapses, threshholds);
	}

	@Override
	public int getModelTypeID() {
		return 1; //TODO: make this cleaner, e.g. use an enum
	}
}
