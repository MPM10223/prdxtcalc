package algo.neuralnetwork;

import java.nio.ByteBuffer;
import java.util.Arrays;

import sqlWrappers.SQLDatabase;

import junit.framework.Assert;

import algo.PredictiveModel;

import algo.util.search.IGeneticOrganism;

public class DiscretizedNeuralNetwork extends PredictiveModel implements IGeneticOrganism {
	
	protected FeedForwardNeuralNetwork nn;
	protected double[][] threshholds;
	
	public DiscretizedNeuralNetwork(int layerSize, double[][] inputToHiddenSynapses, double[] hiddenToOutputSynapses, double[][] threshholds) {
		this.nn = new FeedForwardNeuralNetwork(layerSize, inputToHiddenSynapses, hiddenToOutputSynapses);
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
	public double predict(double[] ivs) {
		return nn.predict(this.discretize(ivs));
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
	public void toDB(SQLDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromDB(SQLDatabase db, int modelID) {
		// TODO Auto-generated method stub
		
	}
}
