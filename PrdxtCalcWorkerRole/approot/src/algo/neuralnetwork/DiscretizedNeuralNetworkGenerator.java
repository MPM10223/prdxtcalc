package algo.neuralnetwork;

import java.nio.ByteBuffer;
import java.util.Arrays;

import junit.framework.Assert;


import algo.util.search.GeneratorUtility;
import algo.util.search.IOrganismGenerator;

public class DiscretizedNeuralNetworkGenerator implements IOrganismGenerator<DiscretizedNeuralNetwork> {
	
	protected int[] inputFeatures;
	
	protected double minimumSynapseWeight;
	protected double maximumSynapseWeight;
	
	protected double[][] inputThreshholdRanges;
	
	public static final int BYTES_PER_SEGMENT = 8;
	public static final int MAX_DISCRETIZATION_THRESHHOLDS = 4;
	
	public DiscretizedNeuralNetworkGenerator(int[] inputFeatures) {
		double[][] ranges = new double[inputFeatures.length][2];
		for(int i = 0; i < inputFeatures.length; i++) {
			ranges[i][0] = Double.NEGATIVE_INFINITY;
			ranges[i][1] = Double.POSITIVE_INFINITY;
		}
		
		this.initialize(inputFeatures, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, ranges);
	}
	
	public DiscretizedNeuralNetworkGenerator(int[] inputFeatures, double minimumSynapseWeight, double maximumSynapseWeight, double[][] inputThreshholdRanges) {
		this.initialize(inputFeatures, minimumSynapseWeight, maximumSynapseWeight, inputThreshholdRanges);
	}
	
	private void initialize(int[] inputFeatures, double minimumSynapseWeight, double maximumSynapseWeight, double[][] inputThreshholdRanges) {
		this.inputFeatures = inputFeatures;
		this.minimumSynapseWeight = minimumSynapseWeight;
		this.maximumSynapseWeight = maximumSynapseWeight;
		this.inputThreshholdRanges = inputThreshholdRanges;
	}

	@Override
	public DiscretizedNeuralNetwork generate(byte[] dna) {
		// DNA:
		// 1) N x N : input > hidden weights
		// 2) N		: hidden > output weights
		// 3) t x N	: discretization threshholds
		int dnaLength = this.getDNASize();
		
		if(dna.length != dnaLength) throw new IllegalArgumentException();
		
		int sectionOneLength = (int)Math.pow(this.getNetworkSize(), 2) * BYTES_PER_SEGMENT;
		int sectionTwoLength = this.getNetworkSize() * BYTES_PER_SEGMENT;
		int sectionThreeLength = this.getNetworkSize() * MAX_DISCRETIZATION_THRESHHOLDS * BYTES_PER_SEGMENT;
		
		ByteBuffer b = ByteBuffer.wrap(dna);
		
		byte[] ithWeightData = new byte[sectionOneLength];
		byte[] htoWeightData = new byte[sectionTwoLength];
		byte[] threshholdData = new byte[sectionThreeLength];
		
		b.get(ithWeightData);
		b.get(htoWeightData);
		b.get(threshholdData);
		
		Assert.assertFalse(b.hasRemaining());
		
		double[][] ithWeights = this.generateInputToHiddenWeights(ithWeightData);
		double[] htoWeights = this.generateHiddenToOutputWeights(htoWeightData);
		double[][] inputThreshholds = this.generateInputThreshholds(threshholdData);
		
		DiscretizedNeuralNetwork dnn = new DiscretizedNeuralNetwork(this.inputFeatures, this.getNetworkSize(), ithWeights, htoWeights, inputThreshholds);
		return dnn;
	}
	
	protected int getNetworkSize() {
		return this.inputFeatures.length;
	}

	protected double[][] generateInputToHiddenWeights(byte[] data) {
		// N x N
		if(data.length != ((int)Math.pow(this.getNetworkSize(), 2) * BYTES_PER_SEGMENT)) throw new IllegalArgumentException();
		
		return GeneratorUtility.generateDoubleMatrix(data, this.getNetworkSize(), this.minimumSynapseWeight, this.maximumSynapseWeight);
	}

	protected double[] generateHiddenToOutputWeights(byte[] data) {
		// N
		if(data.length != (this.getNetworkSize() * BYTES_PER_SEGMENT)) throw new IllegalArgumentException();
		
		return GeneratorUtility.generateDoubleArray(data, this.minimumSynapseWeight, this.maximumSynapseWeight);
	}
	
	protected double[][] generateInputThreshholds(byte[] data) {
		// t x N
		if(data.length != (this.getNetworkSize() * MAX_DISCRETIZATION_THRESHHOLDS * BYTES_PER_SEGMENT)) throw new IllegalArgumentException();
		
		double[][] t = new double[this.getNetworkSize()][];
		
		for(int i = 0; i < this.getNetworkSize(); i++) {
			byte[] chunk = Arrays.copyOfRange(data, i * MAX_DISCRETIZATION_THRESHHOLDS * BYTES_PER_SEGMENT, (i + 1) * MAX_DISCRETIZATION_THRESHHOLDS * BYTES_PER_SEGMENT);
			double[] th = GeneratorUtility.generateDoubleArray(chunk, this.inputThreshholdRanges[i][0], this.inputThreshholdRanges[i][1]);
			Arrays.sort(th);
			t[i] = th;
		}
		
		return t;
	}

	@Override
	public int getDNASize() {
		// DNA:
		// 1) N x N : input > hidden weights
		// 2) N		: hidden > output weights
		// 3) t x N	: discretization threshholds
		int bytesPerSegment = BYTES_PER_SEGMENT;
		int numSegments = (int)Math.pow(this.getNetworkSize(), 2) + (1 + MAX_DISCRETIZATION_THRESHHOLDS) * this.getNetworkSize();
		return bytesPerSegment * numSegments;
	}

}
