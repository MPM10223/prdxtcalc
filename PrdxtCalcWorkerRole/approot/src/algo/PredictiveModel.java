package algo;

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
		SQLInsertBuffer b = new SQLInsertBuffer(db, "modelFeatures", new String[] {"modelID","inputIndex","problemID","featureID"});
		
		b.startBufferedInsert(inputFeatures.length);
		for(int i = 0; i < inputFeatures.length; i++) {
			b.insertRow(new String[] { 
					String.valueOf(this.modelID)
					, String.valueOf(i)
					, String.valueOf(problemID)
					, String.valueOf(inputFeatures[i])
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