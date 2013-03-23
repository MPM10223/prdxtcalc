package dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public class JobRunnerDAO {
	
	protected SQLDatabase db;
	protected String jobQtable;
	
	public JobRunnerDAO() {
		this("hwvhpv4cb1.database.windows.net:1433", "prdxt", "username@hwvhpv4cb1", "pwd");
		this.jobQtable = "jobQueue";
	}
	
	public JobRunnerDAO(String server, String database, String username, String password) {
		this.db = new SQLDatabase(server, database, username, password);
	}
	
	public Map<String,String> popJobQueue() {
		//TODO: pop
		return peekJobQueue();
	}
	
	public Map<String,String> peekJobQueue() {
		String sql = String.format("SELECT TOP 1 jobTypeID, args FROM [%s] WHERE jobStatus = 0 ORDER BY requestTime ASC", this.jobQtable);
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
		
		String sql = String.format("INSERT INTO modelAccuracy (modelID, modelStatID, value, detailString) SELECT %d, %d, %f, '%s'", modelID, modelStatID, cvr2, detailString);
		db.executeQuery(sql);
	}
}
