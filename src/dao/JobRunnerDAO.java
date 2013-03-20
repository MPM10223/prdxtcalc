package dao;

import java.util.HashMap;
import java.util.Map;

import algo.PredictiveModel;

import sqlWrappers.SQLDatabase;

public class JobRunnerDAO {
	
	protected SQLDatabase db;
	protected String jobQtable;
	
	public JobRunnerDAO() {
		this("tcp:hwvhpv4cb1.database.windows.net,1433", "prdxt", "prdxt_calc@hwvhpv4cb1", "s1mul4t3");
		this.jobQtable = "jobQueue";
	}
	
	public JobRunnerDAO(String server, String database, String username, String password) {
		this.db = new SQLDatabase(server, database, username, password);
	}
	
	public Map<String,String> popJobQueue() {
		String sql = String.format("SELECT TOP 1 jobTypeID, args FROM [%s] ORDER BY date ASC", this.jobQtable);
		Map<String,String> pop = db.getQueryRow(sql);
		return pop;
	}
	
	public String getAlgorithmClass(int algorithmID) {
		String sql = String.format("SELECT class FROM algorithms WHERE algorithmID = %d", algorithmID);
		return db.getQueryResult(sql);
	}
	
	public Map<String,String> getProblemDataSource(int problemID) {
		//TODO
		HashMap<String,String> m = new HashMap<String,String>(2);
		m.put("table", "vw_problem_" + String.valueOf(problemID));
		m.put("dvColumn", "dv");
		return m;
	}
	
	public String getModelClass(int modelID) {
		String sql = String.format("SELECT t.class FROM models m JOIN modelTypes t ON m.modelTypeID = t.modelTypeID WHERE m.modelID = %d", modelID);
		return this.db.getQueryResult(sql);
	}

	public SQLDatabase getDB() {
		return this.db;
	}

	public void recordModelAccuracy(int modelID, double cvr2) {
		//TODO
	}
}
