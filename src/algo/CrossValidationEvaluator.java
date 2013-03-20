package algo;

import algo.util.search.IFitnessFunction;

public class CrossValidationEvaluator implements IFitnessFunction<Algorithm<PredictiveModel>> {
	
	protected CrossValidationEvaluatorDAO dao;
	protected int numFolds;
	protected int problemID;
	protected int algorithmID;
	protected int modelID;

	public CrossValidationEvaluator(String dataTable, String dvColumn, int problemID, int algorithmID, int modelID) {
		this(dataTable, dvColumn, 5, problemID, algorithmID, modelID);
	}
	
	public CrossValidationEvaluator(String dataTable, String dvColumn, int numFolds, int problemID, int algorithmID, int modelID) {
		this(new CrossValidationEvaluatorDAO(dataTable, dvColumn, problemID, algorithmID, modelID), numFolds);
	}
	
	public CrossValidationEvaluator(CrossValidationEvaluatorDAO dao, int numFolds) {
		super();
		this.numFolds = numFolds;
		this.dao = dao;
	}
	
	@Override
	public double evaluate(Algorithm<PredictiveModel> subject) {
		
		double aggAccuracy = 0.0;
		int numObservations = 0;
		
		// 1. break the training data into random folds
		dao.assignFolds(numFolds);
		
		// 2. for each fold
		for(int i = 0; i < this.numFolds; i++) {
		
			// run the algorithm on all data but this fold
			PredictiveModel m = subject.buildModel(new AlgorithmDAO(dao.getDataTable(), dao.getDvColumn(), String.format("foldID <> %d", i)));
			
			// evaluate the algorithm on this fold
			SQLTestDataEvaluator<PredictiveModel> e = new SQLTestDataEvaluator<PredictiveModel>(
					dao.getDb()
					, dao.getFoldTable() // evaluation pool
					, dao.getIvColumns()
					, dao.getDvColumn()
					, String.format("foldID = %d", i)
					, null
					, (dao.getDVIsBinary() ? EvaluationType.BOOLEAN : EvaluationType.CONTINUOUS_R2)); //TODO: discrete non-binary
			double accuracy = e.evaluate(m);
			int n = dao.getFoldSize(i);
			
			aggAccuracy += (accuracy * n);
			numObservations += n;
		}
			
		// 3. aggregate performance
		return aggAccuracy / numObservations;
	}

	@Override
	public Observation[] getPerformanceDetails(Algorithm<PredictiveModel> subject) {
		throw new RuntimeException("Performance details not available for Cross-Validation");
	}

}
