import java.util.Map;
import java.util.Vector;

import algo.Algorithm;
import algo.AlgorithmDAO;
import algo.CrossValidationEvaluator;
import algo.Observation;
import algo.PredictiveModel;

import dao.JobRunnerDAO;
import dao.ProblemDefinition;


public abstract class JobRunner {
	
	/**
	 * @param server
	 * @param database
	 * @param jobqtable
	 * @param username
	 * @param password
	 */
	protected static JobRunnerDAO dao;
	
	public static void main(String[] args) {
		
		if(args.length != 6)
			throw new RuntimeException();
		
		String server = args[0];
		String port = args[1];
		String database = args[2];
		String jobQtable = args[3];
		String username = args[4];
		String password = args[5];
		
		if(JobRunner.dao == null) JobRunner.dao = new JobRunnerDAO(server, port, database, jobQtable, username, password);
		
		Map<String,String> pop = dao.popJobQueue();
		
		if(pop != null) {
			int jobID = Integer.parseInt(pop.get("jobID"));
			
			try {
				
				String jobTypeID = pop.get("jobTypeID");
				JobType jobType = JobType.fromInt(Integer.parseInt(jobTypeID)); //JobType.valueOf(jobTypeID);
				
				String[] jobArgs = pop.get("args").split(" ");
				
				dao.logJobStarted(jobID);
				
				switch(jobType) {
				case BUILD_MODEL:
					
					// args usage: problemID algorithmID 
					if(jobArgs.length != 2) throw new RuntimeException();
					int problemID = Integer.parseInt(jobArgs[0]);
					int algorithmID = Integer.parseInt(jobArgs[1]);
					
					buildModel(problemID, algorithmID);
					
					break;
									
				case EVALUATE_MODEL:
					
					// args usage: problemID algorithmID modelID
					if(jobArgs.length != 3) throw new RuntimeException();
					problemID = Integer.parseInt(jobArgs[0]);
					algorithmID = Integer.parseInt(jobArgs[1]);
					int modelID = Integer.parseInt(jobArgs[2]);
					
					evaluateAlgorithm(problemID, algorithmID, modelID);
					
					break;
					
				case APPLY_MODEL:
					
					// args usage: modelID modelInputSetID
					if(jobArgs.length != 2) throw new RuntimeException();
					modelID = Integer.parseInt(jobArgs[0]);
					int applyModelRunID = Integer.parseInt(jobArgs[1]);
					
					applyModel(modelID, applyModelRunID);
					
					break;
					
				default:
					throw new RuntimeException(String.format("Unsupported jobTypeID: %d", jobTypeID));
				}
				
				dao.logJobCompleted(jobID);
		
			} catch(Exception e) {
				//dao.logJobFailed(jobID, e);
				throw new RuntimeException(e);
			}
		}
		
		// TODO, put this in a loop
	}

	protected static void buildModel(int problemID, int algorithmID) {
		
		Algorithm<? extends PredictiveModel> a = getAlgorithmFromID(algorithmID);
		ProblemDefinition problemData = dao.getProblemDataSource(problemID);
		PredictiveModel m = a.buildModel(new AlgorithmDAO(dao.getDB(), problemData.getTable(), problemData.getIvColumns(), problemData.getIvFeatureIDs(), problemData.getDvColumn(), problemData.getIdColumn(), null));
		m.toDB(dao.getDB(), problemID, algorithmID);
	}
	
	protected static void evaluateAlgorithm(int problemID, int algorithmID, int modelID) {
		
		Algorithm<PredictiveModel> a = getAlgorithmFromID(algorithmID);	
		ProblemDefinition problemData = dao.getProblemDataSource(problemID);
		CrossValidationEvaluator e = new CrossValidationEvaluator(dao.getDB(), problemData.getTable(), problemData.getIvColumns(), problemData.getIvFeatureIDs(), problemData.getDvColumn(), problemData.getIdColumn(), 5, problemID, algorithmID, modelID);
		double cvr2 = e.evaluate(a);
		// this measure of accuracy is drawn from the generating algorithm, but ultimately linked to the model
		dao.recordModelAccuracy(modelID, cvr2);
		
	}
	
	protected static void applyModel(int modelID, int applyModelRunID) {

		PredictiveModel m = getModelFromID(modelID);
		Vector<Observation> targets = dao.getApplyModelTargets(applyModelRunID);
		for(Observation target : targets) {
			double prediction = m.predict(target.getIndependentVariables());
			target.setPrediction(prediction);
		}
		dao.saveApplyModelResults(targets);
		
	}
	
	protected static Algorithm<PredictiveModel> getAlgorithmFromID(int algorithmID) {
		String algoClass = dao.getAlgorithmClass(algorithmID);
		
		Algorithm<PredictiveModel> a;
		
		try {
			
			Class<? extends Algorithm> c = (Class<? extends Algorithm>) Class.forName("algo." + algoClass);
			a = c.newInstance();
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		return a;
	}
	
	protected static PredictiveModel getModelFromID(int modelID) {
		String modelClass = dao.getModelClass(modelID);
		
		PredictiveModel m;
		
		try {
			Class<? extends PredictiveModel> c = (Class<? extends PredictiveModel>) Class.forName("algo." + modelClass);
			m = c.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		m.fromDB(dao.getDB(), modelID);
		
		return m;
	}

}
