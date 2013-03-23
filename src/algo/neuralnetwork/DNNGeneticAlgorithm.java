package algo.neuralnetwork;

import algo.Algorithm;
import algo.AlgorithmDAO;
import algo.EvaluationType;
import algo.SQLTestDataEvaluator;
import algo.util.search.GeneticSearch;

public class DNNGeneticAlgorithm extends Algorithm<DiscretizedNeuralNetwork> {

	private int numOrganisms;
	private int maxTrials;
	private double minMutationRate;
	private double maxMutationRate;
	private double minCrossoverRate;
	private double maxCrossoverRate;
	private Class<DiscretizedNeuralNetwork> ct;
	
	private DiscretizedNeuralNetwork solution;

	public DNNGeneticAlgorithm() {
		//TODO: verify that these are good defaults
		this(100, 10, 0.1, 0.4, 0.2, 0.6, DiscretizedNeuralNetwork.class);
	}
	
	public DNNGeneticAlgorithm(
			int numOrganisms, int maxTrials, double minMutationRate,
			double maxMutationRate, double minCrossoverRate,
			double maxCrossoverRate, Class<DiscretizedNeuralNetwork> ct) {
		super();
		this.numOrganisms = numOrganisms;
		this.maxTrials = maxTrials;
		this.minMutationRate = minMutationRate;
		this.maxMutationRate = maxMutationRate;
		this.minCrossoverRate = minCrossoverRate;
		this.maxCrossoverRate = maxCrossoverRate;
		this.ct = ct;
	}
	
	@Override
	public DiscretizedNeuralNetwork buildModel(AlgorithmDAO dao) {
		
		int networkSize = dao.getIvColumns().length;
		
		DiscretizedNeuralNetworkGenerator generator = new DiscretizedNeuralNetworkGenerator(networkSize);
		EvaluationType et = (dao.getDVIsBinary() ? EvaluationType.BOOLEAN : EvaluationType.CONTINUOUS_R2); // TODO: detect & support discrete non-binary
		SQLTestDataEvaluator<DiscretizedNeuralNetwork> fitness = new SQLTestDataEvaluator<DiscretizedNeuralNetwork>(dao.getDb(), dao.getDataTable(), dao.getIvColumns(), dao.getDvColumn(), et); 
		
		GeneticSearch<DiscretizedNeuralNetwork> s = new GeneticSearch<DiscretizedNeuralNetwork>(generator, fitness, numOrganisms, maxTrials, minMutationRate, maxMutationRate, minCrossoverRate, maxCrossoverRate, ct); 
		this.solution = s.search();
		return this.solution;
	}

}
