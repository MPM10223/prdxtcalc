package algo.knn;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;
import algo.Algorithm;
import algo.AlgorithmDAO;
import algo.linreg.ForwardStepwiseRegression;
import algo.linreg.RegressionModel;

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
		
		log.logStepSequenceStarted(4);
		
		// 0. stepwise regression to get features and weights
		ForwardStepwiseRegression r = new ForwardStepwiseRegression(0.05);
		r.setLog(log);
		RegressionModel m = r.buildModel(dao);
		
		int[] featureIDs = m.getInputFeatures();
		double[] coefficients = m.getCoefficients(); //TODO: use t-stat or avg impact
		
		log.logStepCompleted();
		
		// 1. create neighbors table
		//neighborID, featureID, value
		String neighborTableName = this.getNeighborTableName();
		
		db.dropTableIfExists(neighborTableName);
		String sql = String.format("CREATE TABLE [%s] (neighborID int not null, featureID int not null, value float, CONSTRAINT PK__%s PRIMARY KEY (neighborID, featureID))", neighborTableName, neighborTableName);
		db.executeQuery(sql);
		
		StringBuilder featureIDList = new StringBuilder();
		for (int i = 0; i < featureIDs.length; i++) {
			if(i > 0) featureIDList.append(",");
			featureIDList.append(featureIDs[i]);
		}
		
		sql = String.format("INSERT INTO [%s] (neighborID, featureID, value) SELECT observationID, featureID, value FROM (%s) x WHERE x.featureID IN (%s)", neighborTableName, dao.getSourceDataDepivotQuery(false), featureIDList.toString());
		db.executeQuery(sql);
		
		log.logStepCompleted();
		
		// 2. create DVs table
		//neighborID, dv
		String dvTableName = this.getDVTableName();

		db.dropTableIfExists(dvTableName);
		sql = String.format("CREATE TABLE [%s] (neighborID int not null, dv float, CONSTRAINT PK__%s PRIMARY KEY (neighborID))", dvTableName, dvTableName);
		db.executeQuery(sql);
		sql = String.format("INSERT INTO [%s] (neighborID, dv) %s", dvTableName, dao.getSourceDataQuery(false, true));
		db.executeQuery(sql);
		
		log.logStepCompleted();
		
		// 3. create features table
		//featureID, mu, sigma
		String featuresTableName = this.getFeaturesTableName();
		
		db.dropTableIfExists(featuresTableName);
		sql = String.format("CREATE TABLE [%s] (featureID int not null, mu float, sigma float, CONSTRAINT PK__%s PRIMARY KEY (featureID))", featuresTableName, featuresTableName);
		db.executeQuery(sql);
		sql = String.format("INSERT INTO [%s] (featureID, mu, sigma) SELECT featureID, AVG(value), STDEV(value) FROM [%s] GROUP BY featureID", featuresTableName, neighborTableName);
		db.executeQuery(sql);
		
		sql = String.format("SELECT featureID, sigma FROM [%s]", featuresTableName);
		Vector<Map<String,String>> rows = db.getQueryRows(sql);
		
		HashMap<Integer,Double> featureSigmas = new HashMap<Integer, Double>(featureIDs.length);
		for(Map<String,String> row : rows) {
			featureSigmas.put(Integer.parseInt(row.get("featureID")), Double.parseDouble(row.get("sigma")));
		}
		
		HashMap<Integer,Double> featureWeights = new HashMap<Integer, Double>(featureIDs.length);
		for(int i = 0; i < featureIDs.length; i++) {
			featureWeights.put(featureIDs[i], Math.abs(coefficients[i]) * 0.675 * featureSigmas.get(featureIDs[i]));
		}
		
		log.logStepCompleted();
		log.logStepSequenceCompleted();
		
		return new KNNModel(featureIDs, db, this.k, featureWeights, neighborTableName, dvTableName, featuresTableName);
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
