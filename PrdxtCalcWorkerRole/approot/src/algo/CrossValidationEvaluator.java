package algo;

import sqlWrappers.SQLDatabase;
import algo.util.dao.ILog;
import algo.util.search.IFitnessFunction;

public class CrossValidationEvaluator implements IFitnessFunction<Algorithm<PredictiveModel>> {
	
	protected CrossValidationEvaluatorDAO dao;
	protected ILog log;
	protected int numFolds;
	protected int problemID;
	protected int algorithmID;
	protected int modelID;

	public CrossValidationEvaluator(ILog log, SQLDatabase db, String dataTable, String[] ivColumns, int[] ivFeatureIDs, String dvColumn, String idColumn, int problemID, int algorithmID, int modelID) {
		this(log, db, dataTable, ivColumns, ivFeatureIDs, dvColumn, idColumn, 5, problemID, algorithmID, modelID);
	}
	
	public CrossValidationEvaluator(ILog log, SQLDatabase db, String dataTable, String[] ivColumns, int[] ivFeatureIDs, String dvColumn, String idColumn, int numFolds, int problemID, int algorithmID, int modelID) {
		this(log, new CrossValidationEvaluatorDAO(db, dataTable, ivColumns, ivFeatureIDs, dvColumn, idColumn, problemID, algorithmID, modelID), numFolds);
	}
	
	public CrossValidationEvaluator(ILog log, CrossValidationEvaluatorDAO dao, int numFolds) {
		super();
		this.log = log;
		this.numFolds = numFolds;
		this.dao = dao;
	}
	
	public ILog getLog() {
		return log;
	}

	public void setLog(ILog log) {
		this.log = log;
	}

	@Override
	public double evaluate(Algorithm<PredictiveModel> subject) {
		
		double aggAccuracy = 0.0;
		int numObservations = 0;
		
		// 1. break the training data into random folds
		dao.assignFolds(numFolds);
		
		log.logStepSequenceStarted(this.numFolds);
		
		// 2. for each fold
		for(int i = 0; i < this.numFolds; i++) {
		
			log.logMessage(String.format("Building model on all but fold %d...", i));
			
			// run the algorithm on all data but this fold
			PredictiveModel m = subject.buildModel(new AlgorithmDAO(dao.getDb(), dao.getFoldTable(), dao.getIvColumns(), dao.getIvFeatureIDs(), dao.getDvColumn(), dao.getIdColumn(), String.format("foldID <> %d", i)));
			
			log.logMessage(String.format("Evaluating on fold %d...", i));
			
			// evaluate the algorithm on this fold
			SQLTestDataEvaluator<PredictiveModel> e = new SQLTestDataEvaluator<PredictiveModel>(
					dao.getDb()
					, dao.getFoldTable() // evaluation pool
					, dao.getIdColumn()
					, dao.getIvColumns()
					, dao.getIvFeatureIDs()
					, dao.getDvColumn()
					, String.format("foldID = %d", i) // evaluate based on this fold alone
					, (dao.getDVIsBinary() ? EvaluationType.BOOLEAN : EvaluationType.CONTINUOUS_R2)); //TODO: discrete non-binary
			double accuracy = e.evaluate(m);
			int n = dao.getFoldSize(i);
			
			log.logMessage(String.format("Fold %d (%d): accuracy %f", i, n, accuracy));
			log.logStepCompleted();
			
			aggAccuracy += (accuracy * n);
			numObservations += n;
		}
		
		log.logStepSequenceCompleted();
		
		// 3. aggregate performance
		return aggAccuracy / numObservations;
	}

	@Override
	public Observation[] getPerformanceDetails(Algorithm<PredictiveModel> subject) {
		throw new RuntimeException("Performance details not available for Cross-Validation");
	}

}
