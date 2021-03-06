package algo.knn;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;
import algo.Observation;
import algo.PredictiveModel;
import algo.util.dao.SQLInsertBuffer;

public class KNNModel extends PredictiveModel {
	
	protected KNNModelDAO dao;
	protected int k;
	protected HashMap<Integer, Double> featureWeights;
	protected String neighborsTable;
	protected String dvsTable;
	protected String featuresTable;

	public KNNModel() {
		super();
	}
	
	public KNNModel(int[] inputFeatures, SQLDatabase db, int k, HashMap<Integer, Double> featureWeights, String neighborsTable, String dvsTable, String featuresTable) {
		super(inputFeatures);
		this.k = k;
		this.featureWeights = featureWeights;
		this.neighborsTable = neighborsTable;
		this.dvsTable = dvsTable;
		this.featuresTable = featuresTable;
		
		this.dao = new KNNModelDAO(db, this.getNeighborsTable(), this.getDvsTable(), this.getFeaturesTable());
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
	
	public HashMap<Integer, Double> getFeatureWeights() {
		return featureWeights;
	}

	public void setFeatureWeights(HashMap<Integer, Double> featureWeights) {
		this.featureWeights = featureWeights;
	}

	public String getNeighborsTable() {
		return neighborsTable;
	}

	public void setNeighborsTable(String neighborsTable) {
		this.neighborsTable = neighborsTable;
	}

	public String getDvsTable() {
		return dvsTable;
	}

	public void setDvsTable(String dvsTable) {
		this.dvsTable = dvsTable;
	}

	public String getFeaturesTable() {
		return featuresTable;
	}

	public void setFeaturesTable(String featuresTable) {
		this.featuresTable = featuresTable;
	}
	
	@Override
	public int getModelTypeID() {
		// TODO make this cleaner
		return 3;
	}
	
	@Override
	public double predict(Map<Integer, Double> ivs) {
		Vector<Map<String,String>> neighbors = dao.getKNearestNeighbors(ivs, this.featureWeights, k);
		return this.predict(neighbors);
	}

	@Override
	public Observation[] predict(Observation[] targets) {
		Map<Integer, Map<Integer, Double>> targetIVs = new HashMap<Integer, Map<Integer,Double>>(targets.length);
		Map<Integer, Observation> targetsMap = new HashMap<Integer, Observation>(targets.length);
		for(Observation o : targets) {
			int targetID = Integer.valueOf(o.getIdentifier());
			targetsMap.put(targetID, o);
			targetIVs.put(targetID, o.getIndependentVariables());
		}
		
		Map<Integer,Vector<Map<String,String>>> targetNeighbors = dao.getKNearestNeighbors(targetIVs, this.featureWeights, this.k);
		
		for(int targetID : targetNeighbors.keySet()) {
			Observation o = targetsMap.get(targetID);
			double prediction = this.predict(targetNeighbors.get(targetID));
			o.setPrediction(prediction);
		}
		
		return targetsMap.values().toArray(new Observation[] {});
	}
	
	protected double predict(Vector<Map<String, String>> neighbors) {
		double prediction = 0.0;
		for(Map<String,String> neighbor : neighbors) {
			prediction += Double.parseDouble(neighbor.get("dv"));
		}
		return prediction / (double)neighbors.size();
	}

	@Override
	protected double predict(double[] indexedInputs) {
		// Not supported - public predict method overridden instead
		throw new UnsupportedOperationException();
	}

	@Override
	public int toDB(SQLDatabase db, int problemID, int algoID) {
		int modelID = super.toDB(db, problemID, algoID);
		
		this.dao = new KNNModelDAO(db, this.getNeighborsTable(), this.getDvsTable(), this.getFeaturesTable());
		
		// 1. permanize neighbors table
		//neighborID, featureID, value
		String permanentNeighborsTableName = this.getNeighborTableName(modelID); 
		dao.renameNeighborsTable(permanentNeighborsTableName);
		this.setNeighborsTable(permanentNeighborsTableName);
		
		// 2. permanize DVs table
		String permanentDVTableName = this.getDVsTableName(modelID); 
		dao.renameDVsTable(permanentDVTableName);
		this.setDvsTable(permanentDVTableName);
		
		// 3. permanize features table
		String permanentFeatureTableName = this.getFeaturesTableName(modelID); 
		dao.renameFeatureTable(permanentFeatureTableName);
		this.setFeaturesTable(permanentFeatureTableName);
		
		// 4. knn_models
		//modelID, k, neighborsTable, dvTable, featuresTable
		String sql = String.format("INSERT INTO knn_models (modelID, k, neighborsTable, dvTable, featuresTable) SELECT %d, %d, '%s', '%s', '%s'"
				, modelID, k, this.getNeighborsTable(), this.getDvsTable(), this.getFeaturesTable());
		db.executeQuery(sql);
		
		// 5. knn_modelFeatures
		SQLInsertBuffer b = new SQLInsertBuffer(db, "knn_modelFeatures", new String[] { "modelID", "inputIndex", "weight" });
		b.startBufferedInsert(this.inputFeatures.length);
		for(int i = 0; i < this.inputFeatures.length; i++) {
			b.insertRow(new String[] {
					String.valueOf(modelID)
					, String.valueOf(i)
					, String.valueOf(this.featureWeights.get(this.inputFeatures[i]))
				});
		}
		b.finishBufferedInsert();
		
		return modelID;
	}

	@Override
	public void fromDB(SQLDatabase db, int modelID) {
		super.fromDB(db, modelID);
		
		String sql = String.format("SELECT k, neighborsTable, dvTable, featuresTable FROM knn_models WHERE modelID = %d", modelID);
		Map<String,String> row = db.getQueryRow(sql);
		
		this.k = Integer.parseInt(row.get("k"));
		this.neighborsTable = row.get("neighborsTable");
		this.dvsTable = row.get("dvTable");
		this.featuresTable = row.get("featuresTable");
		
		this.dao = new KNNModelDAO(db, this.neighborsTable, this.dvsTable, this.featuresTable);
		
		sql = String.format("SELECT inputIndex, [weight] FROM knn_modelFeatures WHERE modelID = %d", modelID);
		Vector<Map<String,String>> rows = db.getQueryRows(sql);
		
		this.featureWeights = new HashMap<Integer, Double>(rows.size());
		for(Map<String,String> feature : rows) {
			int inputIndex = Integer.parseInt(feature.get("inputIndex"));
			this.featureWeights.put(this.getFeatureIDAtInputIndex(inputIndex), Double.parseDouble(feature.get("weight")));
		}
	}
	
	protected String getNeighborTableName(int modelID) {
		return String.format("knn_neighbors_model%d", modelID);
	}
	
	protected String getDVsTableName(int modelID) {
		return String.format("knn_dvs_model%d", modelID);
	}
	
	protected String getFeaturesTableName(int modelID) {
		return String.format("knn_features_model%d", modelID);
	}

	@Override
	public boolean getPrefersBatchPrediction() {
		return true;
	}
}
