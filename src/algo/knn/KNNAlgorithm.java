package algo.knn;

import sqlWrappers.SQLDatabase;
import algo.Algorithm;
import algo.AlgorithmDAO;

public class KNNAlgorithm extends Algorithm<KNNModel> {
	
	protected int k;
	
	public KNNAlgorithm() {
		this(20);
	}
	
	public KNNAlgorithm(int k) {
		this.k = k;
	}


	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
	
	@Override
	public KNNModel buildModel(AlgorithmDAO dao) {
		SQLDatabase db = dao.getDb();
		
		// 1. create neighbors table
		//neighborID, featureID, value
		String neighborTableName = this.getNeighborTableName();
		
		db.dropTableIfExists(neighborTableName);
		String sql = String.format("CREATE TABLE [%s] (neighborID int not null, featureID int not null, value float, CONSTRAINT PK__%s PRIMARY KEY (neighborID, featureID))", neighborTableName, neighborTableName);
		db.executeQuery(sql);
		sql = String.format("INSERT INTO [%s] (neighborID, featureID, value) %s", neighborTableName, dao.getSourceDataDepivotQuery(false));
		db.executeQuery(sql);
		
		// 2. create DVs table
		//neighborID, dv
		String dvTableName = this.getDVTableName();

		db.dropTableIfExists(dvTableName);
		sql = String.format("CREATE TABLE [%s] (neighborID int not null, dv float, CONSTRAINT PK__%s PRIMARY KEY (neighborID))", dvTableName, dvTableName);
		db.executeQuery(sql);
		sql = String.format("INSERT INTO [%s] (neighborID, dv) %s", dvTableName, dao.getSourceDataQuery(false, true));
		db.executeQuery(sql);
		
		// 3. create features table
		//featureID, mu, sigma
		String featuresTableName = this.getFeaturesTableName();
		
		db.dropTableIfExists(featuresTableName);
		sql = String.format("CREATE TABLE [%s] (featureID int not null, mu float, sigma float, CONSTRAINT PK__%s PRIMARY KEY (featureID))", featuresTableName, featuresTableName);
		db.executeQuery(sql);
		sql = String.format("INSERT INTO [%s] (featureID, mu, sigma) SELECT featureID, AVG(value), STDEV(value) FROM [%s] GROUP BY featureID", featuresTableName, neighborTableName);
		db.executeQuery(sql);
		
		return new KNNModel(dao.getIvFeatureIDs(), db, this.k, neighborTableName, dvTableName, featuresTableName);
	}
	
	protected String getNeighborTableName() {
		//TODO: deal with concurrency
		return String.format("knn_algorithmNeighbors");
	}
	
	protected String getDVTableName() {
		//TODO: deal with concurrency
		return String.format("knn_algorithmDVs");
	}
	
	protected String getFeaturesTableName() {
		//TODO: deal with concurrency
		return String.format("knn_algorithmFeatures");
	}
}
