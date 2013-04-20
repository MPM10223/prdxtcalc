import java.util.Map;
import java.util.Vector;

import algo.Algorithm;
import algo.AlgorithmDAO;
import algo.CrossValidationEvaluator;
import algo.Observation;
import algo.PredictiveModel;
import algo.util.dao.ILog;

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
			JobLog l = new JobLog(dao.getDB(), jobQtable, dao.getCalcServerID(), jobID);
			
			try {
				
				String jobTypeID = pop.get("jobTypeID");
				JobType jobType = JobType.fromInt(Integer.parseInt(jobTypeID)); //JobType.valueOf(jobTypeID);
				
				String[] jobArgs = pop.get("args").split(" ");
				
				l.logJobStarted();
				l.initJobProgress();
				
				switch(jobType) {
				case BUILD_MODEL:
					
					// args usage: problemID algorithmID 
					if(jobArgs.length != 2) throw new RuntimeException();
					int problemID = Integer.parseInt(jobArgs[0]);
					int algorithmID = Integer.parseInt(jobArgs[1]);
					
					int modelID = buildModel(problemID, algorithmID, l);
					
					dao.recordJobReturnValue(jobID, String.valueOf(modelID));
					
					break;
									
				case EVALUATE_MODEL:
					
					// args usage: problemID algorithmID modelID
					if(jobArgs.length != 3) throw new RuntimeException();
					problemID = Integer.parseInt(jobArgs[0]);
					algorithmID = Integer.parseInt(jobArgs[1]);
					modelID = Integer.parseInt(jobArgs[2]);
					
					double accuracy = evaluateAlgorithm(problemID, algorithmID, modelID, l);
					
					dao.recordJobReturnValue(jobID, String.valueOf(accuracy));
					
					break;
					
				case APPLY_MODEL:
					
					// args usage: modelID modelInputSetID
					if(jobArgs.length != 2) throw new RuntimeException();
					modelID = Integer.parseInt(jobArgs[0]);
					int applyModelRunID = Integer.parseInt(jobArgs[1]);
					
					Double prediction = applyModel(modelID, applyModelRunID, l);
					
					if(prediction != null) {
						dao.recordJobReturnValue(jobID, String.valueOf(prediction));
					}
					
					break;
					
				default:
					throw new RuntimeException(String.format("Unsupported jobTypeID: %d", jobTypeID));
				}
				
				l.logJobCompleted();
		
			} catch(Exception e) {
				//l.logJobFailed(e);
				throw new RuntimeException(e);
			}
		}
		
		// TODO: put this in a loop
	}

	protected static int buildModel(int problemID, int algorithmID, ILog log) {
		
		Algorithm<? extends PredictiveModel> a = getAlgorithmFromID(algorithmID);
		a.setLog(log);
		ProblemDefinition problemData = dao.getProblemDataSource(problemID);
		PredictiveModel m = a.buildModel(new AlgorithmDAO(dao.getDB(), problemData.getTable(), problemData.getIvColumns(), problemData.getIvFeatureIDs(), problemData.getDvColumn(), problemData.getIdColumn(), null));
		int modelID = m.toDB(dao.getDB(), problemID, algorithmID);
		dao.requestModelEvaluation(problemID, algorithmID, modelID);
		
		return modelID;
		
	}
	
	protected static double evaluateAlgorithm(int problemID, int algorithmID, int modelID, ILog log) {
		
		Algorithm<PredictiveModel> a = getAlgorithmFromID(algorithmID);
		a.setLog(log);
		ProblemDefinition problemData = dao.getProblemDataSource(problemID);
		CrossValidationEvaluator e = new CrossValidationEvaluator(log, dao.getDB(), problemData.getTable(), problemData.getIvColumns(), problemData.getIvFeatureIDs(), problemData.getDvColumn(), problemData.getIdColumn(), 5, problemID, algorithmID, modelID);
		double cvr2 = e.evaluate(a);
		// this measure of accuracy is drawn from the generating algorithm, but ultimately linked to the model
		dao.recordModelAccuracy(modelID, cvr2);
		
		return cvr2;
		
	}
	
	protected static Double applyModel(int modelID, int applyModelRunID, ILog log) {

		PredictiveModel m = getModelFromID(modelID);
		m.setLog(log);
		Observation[] targets = dao.getApplyModelTargets(applyModelRunID);
		m.predict(targets);
		dao.saveApplyModelResults(targets);
		
		if(targets.length == 1) return targets[0].getPrediction();
		return null;
		
	}
	
	protected static Algorithm<PredictiveModel> getAlgorithmFromID(int algorithmID) {
		String algoClass = dao.getAlgorithmClass(algorithmID);
		
		Algorithm<PredictiveModel> a;
		
		try {
			
			Class<? extends Algorithm> c = (Class<? extends Algorithm>) Class.forName("algo." + algoClass);
			a = c.newInstance(); // all Algorithms must support zero argument constructors
			
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
			m = c.newInstance(); // all PredictiveModels must support zero argument constructors...
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		m.fromDB(dao.getDB(), modelID); // ...followed by a fromDB call
		
		return m;
	}

}
