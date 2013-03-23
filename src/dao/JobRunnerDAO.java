package dao;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public class JobRunnerDAO {
	
	protected SQLDatabase db;
	protected String jobQtable;
	protected Integer calcServerID;
	
	public JobRunnerDAO(String server, String database, String jobQTable, String username, String password) {
		this.db = new SQLDatabase(server, database, username, password);
		this.jobQtable = jobQTable;
		this.calcServerID = null;
	}
	
	protected int getCalcServerID() {
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
		
		sql = String.format("SELECT featureName, featureType, isDV, isIV FROM problemFeatures WHERE problemID = %d", problemID);
		Vector<Map<String,String>> rows = db.getQueryRows(sql);
		
		String dvColumn = null;
		Vector<String> ivColumnList = new Vector<String>(rows.size());
		for(Map<String,String> column : rows) {
			if(column.get("isDV").equals("1")) {
				if(dvColumn != null) throw new RuntimeException("Multiple DVs not supported");
				dvColumn = column.get("featureName");
			}
			if(column.get("isIV").equals("1")) {
				ivColumnList.add(column.get("featureName"));
			}
		}
		
		String[] ivColumns = new String[ivColumnList.size()];
		ivColumnList.copyInto(ivColumns);
		
		return new ProblemDefinition(row.get("sourceTable"), dvColumn, row.get("idColumn"), ivColumns);
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
		
		//TODO: handle existing entry
		String sql = String.format("INSERT INTO modelAccuracy (modelID, modelStatID, value, detailString) SELECT %d, %d, %f, '%s'", modelID, modelStatID, cvr2, detailString);
		db.executeQuery(sql);
	}

	public void logJobStarted(int jobID) {
		logJobStatusChange(jobID, 0, 1);
	}

	public void logJobCompleted(int jobID) {
		logJobStatusChange(jobID, 1, 2);
	}
	
	public void logJobFailed(int jobID) {
		logJobStatusChange(jobID, 1, 3);
	}
	
	protected void logJobStatusChange(int jobID, int oldStatus, int newStatus) {
		String sql = String.format("SELECT jobID FROM [%s] WHERE jobID = %d AND calcServerID = %d AND jobStatus = %d", this.jobQtable, jobID, this.getCalcServerID(), oldStatus);
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		if(results.size() == 0) throw new RuntimeException("Invalid jobID / status combination - no matching job/state found");
		
		sql = String.format("UPDATE [%s] SET jobStatus = %d WHERE jobID = %d AND calcServerID = %d AND jobStatus = %d", this.jobQtable, newStatus, jobID, this.getCalcServerID(), oldStatus);
		db.executeQuery(sql);
	}
}
