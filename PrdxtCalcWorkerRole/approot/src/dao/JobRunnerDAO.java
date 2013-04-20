package dao;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import algo.Observation;
import algo.util.dao.SQLInsertBuffer;

import sqlWrappers.SQLDatabase;

public class JobRunnerDAO {
	
	protected SQLDatabase db;
	protected String jobQtable;
	protected Integer calcServerID;
	
	protected float progress = 0;
	
	public JobRunnerDAO(String server, String port, String database, String jobQTable, String username, String password) {
		this.db = new SQLDatabase(server, port, database, username, password);
		this.jobQtable = jobQTable;
		this.calcServerID = null;
	}
	
	public int getCalcServerID() {
		if(calcServerID == null) {
			String serverName;
			try {
				serverName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
			String sql = String.format("SELECT calcServerID FROM calcServers WHERE calcServerName = '%s'", serverName);
			this.calcServerID = Integer.parseInt(db.getQueryResult(sql));
		}
		return this.calcServerID;
	}
	
	public Map<String,String> popJobQueue() {
		
		// see if we own any orphaned jobs
		String sql = String.format("SELECT jobID, jobTypeID, args FROM [%s] WHERE jobStatus = 0 AND calcServerId = %d", this.jobQtable, this.getCalcServerID());
		Vector<Map<String,String>> result = db.getQueryRows(sql);
		
		Map<String,String> job = null;
		if(result.size() == 0) {
			// try to claim a new job
			String jobIDQuery = String.format("SELECT TOP 1 jobID FROM [%s] WHERE jobStatus = 0 AND calcServerID IS NULL ORDER BY requestTime ASC", this.jobQtable);
			String claimQuery = String.format("UPDATE [%s] SET calcServerID = %d WHERE jobID IN (%s)", this.jobQtable, calcServerID, jobIDQuery);
			db.executeQuery(claimQuery);
			
			Vector<Map<String,String>> results = db.getQueryRows(sql);
			if(results.size() == 1) {
				job = results.firstElement();
			} else if(results.size() > 1) {
				throw new RuntimeException("Claim query claimed multiple jobs - this should never happen");
			}
		} else if(result.size() > 1) {
			throw new RuntimeException("This server has claimed multiple jobs already");
		} else {
			// adopt this orphaned job
			job = result.firstElement();
		}
		
		return job;
	}
	
	public Map<String,String> peekJobQueue() {
		String sql = String.format("SELECT TOP 1 jobID, jobTypeID, args FROM [%s] WHERE jobStatus = 0 AND calcServerID IS NULL ORDER BY requestTime ASC", this.jobQtable);
		Map<String,String> pop = db.getQueryRow(sql);
		return pop;
	}
	
	public String getAlgorithmClass(int algorithmID) {
		String sql = String.format("SELECT className FROM algos WHERE algoID = %d", algorithmID);
		return db.getQueryResult(sql);
	}
	
	public ProblemDefinition getProblemDataSource(int problemID) {
		String sql = String.format("SELECT sourceTable, idColumn FROM problems WHERE problemID = %d", problemID);
		Map<String,String> row = db.getQueryRow(sql);
		
		sql = String.format("SELECT featureID, featureName, featureType, isDV, isIV FROM problemFeatures WHERE problemID = %d", problemID);
		Vector<Map<String,String>> rows = db.getQueryRows(sql);
		
		String dvColumn = null;
		Vector<String> ivColumnList = new Vector<String>(rows.size());
		Vector<Integer> ivFeatureIDList = new Vector<Integer>(rows.size());
		for(Map<String,String> column : rows) {
			if(column.get("isDV").equals("1")) {
				if(dvColumn != null) throw new RuntimeException("Multiple DVs not supported");
				dvColumn = column.get("featureName");
			}
			if(column.get("isIV").equals("1")) {
				ivColumnList.add(column.get("featureName"));
				ivFeatureIDList.add(Integer.parseInt(column.get("featureID")));
			}
		}
		
		String[] ivColumns = new String[ivColumnList.size()];
		ivColumnList.copyInto(ivColumns);
		
		int[] ivFeatureIDs = new int[ivFeatureIDList.size()];
		for(int i = 0; i < ivFeatureIDs.length; i++) {
			ivFeatureIDs[i] = ivFeatureIDList.get(i);
		}
		
		return new ProblemDefinition(row.get("sourceTable"), dvColumn, row.get("idColumn"), ivColumns, ivFeatureIDs);
	}
	
	public String getModelClass(int modelID) {
		String sql = String.format("SELECT t.className FROM models m JOIN modelTypes t ON m.modelTypeID = t.modelTypeID WHERE m.modelID = %d", modelID);
		return this.db.getQueryResult(sql);
	}

	public SQLDatabase getDB() {
		return this.db;
	}

	public void recordModelAccuracy(int modelID, double cvr2) {
		int modelStatID = 1; //TODO: make this cleaner with an enum
		String detailString = String.format(""); //TODO: provide more detail
		
		// delete any existing entries
		String sql = String.format("DELETE modelAccuracy WHERE modelID = %d AND modelStatID = %d", modelID, modelStatID);
		db.executeQuery(sql);
		
		sql = String.format("INSERT INTO modelAccuracy (modelID, modelStatID, value, detailString) SELECT %d, %d, %f, '%s'", modelID, modelStatID, cvr2, detailString);
		db.executeQuery(sql);
	}

	public Observation[] getApplyModelTargets(int applyModelRunID) {
		
		Vector<Observation> targets = new Vector<Observation>();
		
		String sql = String.format("SELECT rt.applyModelTargetID, mf.featureID, i.value FROM applyModelRuns r JOIN applyModelTargets rt ON r.applyModelRunID = rt.applyModelRunID JOIN applyModelInputs i ON i.applyModelTargetID = rt.applyModelTargetID JOIN modelFeatures mf ON mf.modelID = i.modelID AND mf.inputIndex = i.inputIndex WHERE r.applyModelRunID = %d ORDER BY rt.applyModelTargetID, i.inputIndex", applyModelRunID);
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		if(results.size() > 0) {
		
			Integer targetID = null;
			Map<Integer, Double> inputs = null;
			
			for(Map<String,String> row : results) {
				int thisTargetID = Integer.parseInt(row.get("applyModelTargetID"));
				if(targetID == null || targetID != thisTargetID) {
					// save and store the old observation
					if(inputs != null) {
						targets.add(new Observation(String.valueOf(targetID), inputs, null));
					}
					
					// start a new observation
					inputs = new HashMap<Integer, Double>();
					targetID = thisTargetID;
				}
				
				Integer featureID = Integer.parseInt(row.get("featureID"));
				Double value = Double.parseDouble(row.get("value"));
				inputs.put(featureID, value);
			}
			
			targets.add(new Observation(String.valueOf(targetID), inputs, null));
		}
		
		return targets.toArray(new Observation[] {});
	}

	public void saveApplyModelResults(Observation[] targets) {
		// create temp table
		db.dropTableIfExists(this.getSaveApplyModelTempTableName());
		
		String sql = String.format("CREATE TABLE [%s] ( applyModelTargetID int not null, prediction float not null, PRIMARY KEY (applyModelTargetID) )", this.getSaveApplyModelTempTableName());
		db.executeQuery(sql);
		
		// insert into temp table
		SQLInsertBuffer b = new SQLInsertBuffer(this.db, this.getSaveApplyModelTempTableName(), new String[] {"applyModelTargetID", "prediction"} );
		b.startBufferedInsert(targets.length);
		for(Observation o : targets) {
			b.insertRow(new String[] {o.getIdentifier(), String.valueOf(o.getPrediction())} );
		}
		b.finishBufferedInsert();
		
		// update permanent table with join
		sql = String.format("UPDATE t SET t.prediction = p.prediction, t.predictionTime = getDate() FROM applyModelTargets t JOIN [%s] p ON t.applyModelTargetID = p.applyModelTargetID WHERE t.prediction IS NULL AND t.predictionTime IS NULL", this.getSaveApplyModelTempTableName());
		db.executeQuery(sql);
		
		// drop temp table
		db.dropTableIfExists(this.getSaveApplyModelTempTableName());
	}
	
	public void requestModelEvaluation(int problemID, int algorithmID, int modelID) {
		String sql = String.format("INSERT INTO [%s] (jobTypeID, args) SELECT 2, '%d %d %d'", this.jobQtable, problemID, algorithmID, modelID);
		db.executeQuery(sql);
	}
	
	public void recordJobReturnValue(int jobID, String returnValue) {
		String sql = String.format("UPDATE [%s] SET returnVal = '%s' WHERE jobID = %d", this.jobQtable, returnValue, jobID);
		db.executeQuery(sql);
	}
	
	protected String getSaveApplyModelTempTableName() {
		//TODO: deal with concurrency issues
		return "temp_applyModel";
	}
}
