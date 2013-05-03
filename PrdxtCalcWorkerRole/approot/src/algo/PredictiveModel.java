package algo;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import algo.util.dao.ILog;
import algo.util.dao.SQLInsertBuffer;

import sqlWrappers.SQLDatabase;

public abstract class PredictiveModel {
	
	// PROTECTED MEMBERS
	protected ILog log;
	protected Integer modelID;
	protected int[] inputFeatures;
	
	// CONSTRUCTORS
	public PredictiveModel() {
		super();
	}
	
	protected PredictiveModel(int[] inputFeatures) {
		this();
		this.inputFeatures = inputFeatures;
	}
	
	// GETTERS / SETTERS
	public ILog getLog() {
		return log;
	}

	public void setLog(ILog log) {
		this.log = log;
	}
	
	public Integer getModelID() {
		return modelID;
	}

	public void setModelID(Integer modelID) {
		this.modelID = modelID;
	}
	
	public int[] getInputFeatures() {
		return inputFeatures;
	}

	public void setInputFeatures(int[] inputFeatures) {
		this.inputFeatures = inputFeatures;
	}

	// ABSTRACT METHODS
	public abstract int getModelTypeID();
	public abstract boolean getPrefersBatchPrediction();
	protected abstract double predict(double[] indexedInputs);
	
	// PUBLIC METHODS
	public double predict(Map<Integer, Double> ivs) {
		double[] indexedInputs = this.getIndexedInputsFromFeatures(ivs);
		return this.predict(indexedInputs);
	}
	
	public Observation[] predict(Observation[] targets) {
		for(Observation t : targets) {
			t.setPrediction(this.predict(t.getIndependentVariables()));
		}
		return targets;
	}
	
	public int toDB(SQLDatabase db, int problemID, int algoID) {
		// models table
		String sql = String.format("INSERT INTO models (modelTypeID, problemID, algoID) SELECT %d, %d, %d%nSELECT scope_identity() as modelID", this.getModelTypeID(), problemID, algoID);
		Vector<Vector<Map<String,String>>> results = db.getQueryBatchResults(sql);
		String modelID = results.lastElement().firstElement().get("modelID");
		this.modelID = Integer.parseInt(modelID);
		
		// model input features table
		String featureIDList = Arrays.toString(this.inputFeatures);
		featureIDList = featureIDList.substring(1, featureIDList.length() - 1);
		// TODO: also calculate average difference from mean
		sql = String.format("SELECT featureID, AVG(value) mean, MIN(value) rangeMin, MAX(value) rangeMax, STDEV(value) sigma, COUNT(*) num FROM problemData p WHERE problemID = %d AND featureID IN (%s) AND EXISTS (SELECT * FROM problemData x WHERE x.observationID = p.observationID AND x.featureID = (SELECT featureID FROM problemFeatures WHERE problemID = %d AND isDV = 1)) GROUP BY featureID", problemID, featureIDList, problemID);
		Vector<Map<String,String>> featureStats = db.getQueryRows(sql);
		if(featureStats.size() != this.inputFeatures.length) throw new RuntimeException("feature statistics query returned too few rows");
		
		SQLInsertBuffer b = new SQLInsertBuffer(db, "modelFeatures", new String[] {"modelID","inputIndex","problemID","featureID","mean","sigma","rangeMin","rangeMax","num"});
		
		b.startBufferedInsert(inputFeatures.length);
		for(Map<String,String> featureStat : featureStats) {
			int featureID = Integer.parseInt(featureStat.get("featureID"));
			int index = this.getInputIndexFromFeatureID(featureID);
			b.insertRow(new String[] { 
					String.valueOf(this.modelID)
					, String.valueOf(index)
					, String.valueOf(problemID)
					, String.valueOf(featureID)
					, featureStat.get("mean")
					, featureStat.get("sigma")
					, featureStat.get("rangeMin")
					, featureStat.get("rangeMax")
					, featureStat.get("num")
				});
		}
		b.finishBufferedInsert();
		
		return this.modelID;
	}
	
	public void fromDB(SQLDatabase db, int modelID) {
		String sql = String.format("SELECT modelTypeID FROM models WHERE modelID = %d", modelID);
		int modelTypeID = Integer.parseInt(db.getQueryResult(sql));
		if(this.getModelTypeID() != modelTypeID) throw new RuntimeException("This modelID is invalid for this model type");
		this.modelID = modelID;
		
		sql = String.format("SELECT inputIndex, featureID FROM modelFeatures WHERE modelID = %d", modelID);
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		this.inputFeatures = new int[results.size()];
		for(Map<String,String> row : results) {
			int i = Integer.parseInt(row.get("inputIndex"));
			this.inputFeatures[i] = Integer.parseInt(row.get("featureID"));
		}
	}
	
	// PROTECTED METHODS
	protected double[] getIndexedInputsFromFeatures(Map<Integer, Double> featureValues) {
		double[] indexedInputs = new double[inputFeatures.length];
		for(int inputIndex = 0; inputIndex < indexedInputs.length; inputIndex++) {
			//TODO: handle missing data
			indexedInputs[inputIndex] = featureValues.get(inputFeatures[inputIndex]);
		}
		return indexedInputs;
	}
	
	protected int getFeatureIDAtInputIndex(int inputIndex) {
		return inputFeatures[inputIndex];
	}
	
	protected int getInputIndexFromFeatureID(int featureID) {
		for(int inputIndex = 0; inputIndex < this.inputFeatures.length; inputIndex++) {
			if(this.getFeatureIDAtInputIndex(inputIndex) == featureID) return inputIndex;
		}
		throw new RuntimeException("featureID not found");
	}
}
