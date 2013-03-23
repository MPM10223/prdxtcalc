package algo;

import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public abstract class PredictiveModel {
	
	public abstract int getModelTypeID();

	public abstract double predict(double[] ivs);
	
	public int toDB(SQLDatabase db, int problemID, int algoID) {
		// models table
		String sql = String.format("INSERT INTO models (modelTypeID, problemID, algoID) SELECT %d, %d, %d GO SELECT scope_identity() as modelID", this.getModelTypeID(), problemID, algoID);
		Vector<Vector<Map<String,String>>> results = db.getQueryBatchResults(sql);
		String modelID = results.lastElement().firstElement().get("modelID");
		return Integer.parseInt(modelID);
	}
	
	public abstract void fromDB(SQLDatabase sqlDatabase, int modelID);

}
