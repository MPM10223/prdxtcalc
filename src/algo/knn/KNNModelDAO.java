package algo.knn;

import java.util.Map;
import java.util.Vector;

import algo.util.dao.SQLInsertBuffer;
import sqlWrappers.SQLDatabase;

public class KNNModelDAO {
	
	protected SQLDatabase db;
	
	protected String neighborsTable;
	protected String dvTable;
	protected String featuresTable;
	
	public KNNModelDAO(SQLDatabase db, String neighborsTable, String dvTable, String featuresTable) {
		this.db = db;
		this.neighborsTable = neighborsTable;
		this.dvTable = dvTable;
		this.featuresTable = featuresTable;
	}
	
	public String getNeighborsTable() {
		return neighborsTable;
	}

	public void setNeighborsTable(String neighborsTable) {
		this.neighborsTable = neighborsTable;
	}

	public String getDvTable() {
		return dvTable;
	}

	public void setDvTable(String dvTable) {
		this.dvTable = dvTable;
	}

	public String getFeaturesTable() {
		return featuresTable;
	}

	public void setFeaturesTable(String featuresTable) {
		this.featuresTable = featuresTable;
	}
	
	private String findNearestNeighbors(int modelID, Map<Integer, Double> targetIVs) {
		// 1. create target table for join
		String targetTable = this.getTargetTempTableName();
		db.dropTableIfExists(targetTable);
		String sql = String.format("CREATE TABLE [%s] (targetID int not null, featureID int not null, value float, PRIMARY KEY (targetID, featureID))", targetTable);
		db.executeQuery(sql);
		
		// 2. fill target table
		SQLInsertBuffer b = new SQLInsertBuffer(db, targetTable, new String[] {"targetID","featureID","value"});
		
		b.startBufferedInsert(targetIVs.size());
		for(Integer featureID : targetIVs.keySet()) {
			Double rawValue = targetIVs.get(featureID);
			b.insertRow(new String[] { "1", String.valueOf(featureID), String.valueOf(rawValue) });
		}
		
		b.finishBufferedInsert();
		
		// 3. create neighborRank table
		String targetNeighborTable = this.getTargetNeighborTableName(modelID);
		String distanceExpression = String.format("sqrt(SUM( power(((n.value - f.mu) / f.sigma) - ((t.value - f.mu) / f.sigma), 2) ))");
		//TODO: support more nuanced handling of ties
		db.dropTableIfExists(targetNeighborTable);
		sql = String.format("CREATE TABLE [%s] (targetID int not null, neighborID int not null, distance float not null, neighborRank int not null, PRIMARY KEY (targetID, neighborID))", targetNeighborTable);
		db.executeQuery(sql);
		
		// 4. fill neighborRank table
		sql = String.format("INSERT INTO [%s] (targetID, neighborID, distance, neighborRank) SELECT t.targetID, n.neighborID, %s as distance, rank() OVER (PARTITION BY t.targetID ORDER BY %s ASC) as neighborRank FROM [%s] n JOIN [%s] f ON n.featureID = f.featureID JOIN [%s] t ON n.featureID = t.featureID GROUP BY t.targetID, n.neighborID", targetNeighborTable, distanceExpression, distanceExpression, neighborsTable, featuresTable, targetTable);
		db.executeQuery(sql);
		
		return targetNeighborTable;
	}
	
	public Vector<Map<String,String>> getKNearestNeighbors(int modelID, Map<Integer, Double> targetIVs, int k) {
		//TODO: memoize this
		String targetNeighborTable = this.findNearestNeighbors(modelID, targetIVs);
		
		String sql = String.format("SELECT n.targetID, n.neighborID, d.dv FROM [%s] n JOIN [%s] d ON n.neighborID = d.neighborID WHERE n.neighborRank <= %d ORDER BY n.targetID, n.neighborID, d.dv", targetNeighborTable, dvTable, k);
		return db.getQueryRows(sql);
	}
		
	protected String getTargetTempTableName() {
		//TODO: deal with concurrency
		return String.format("knn_targetFeatures");
	}
	
	protected String getTargetNeighborTableName(int modelID) {
		//TODO: deal with concurrency
		return String.format("knn_targetNeighbors_model%d", modelID);
	}

	public void renameNeighborsTable(String newTableName) {
		renameTableAndPrimaryKey(this.getNeighborsTable(), newTableName);
		this.setNeighborsTable(newTableName);
	}

	public void renameDVsTable(String newTableName) {
		renameTableAndPrimaryKey(this.getDvTable(), newTableName);
		this.setDvTable(newTableName);
	}

	public void renameFeatureTable(String newTableName) {
		renameTableAndPrimaryKey(this.getFeaturesTable(), newTableName);
		this.setFeaturesTable(newTableName);
	}
	
	private void renameTableAndPrimaryKey(String oldTableName, String newTableName) {
		db.renameObject(oldTableName, newTableName);
		db.renameObject("PK__" + oldTableName, "PK__" + newTableName);		
	}
	
}
