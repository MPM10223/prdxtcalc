package dao;

import java.util.HashMap;
import java.util.Map;

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
	
	public Map<String,String> getProblemDataSource(int problemID) {
		//TODO implement for real
		HashMap<String,String> m = new HashMap<String,String>(2);
		m.put("table", "vw_problem_" + String.valueOf(problemID));
		m.put("dvColumn", "DV");
		m.put("idColumn", "observationID");
		return m;
	}
	
	public String getModelClass(int modelID) {
		String sql = String.format("SELECT t.className FROM models m JOIN modelTypes t ON m.modelTypeID = t.modelTypeID WHERE m.modelID = %d", modelID);
		return this.db.getQueryResult(sql);
	}

	public SQLDatabase getDB() {
		return this.db;
	}

	public void recordModelAccuracy(int modelID, double cvr2) {
		//TODO
	}
}
