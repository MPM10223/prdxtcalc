package algo.linreg;

import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;
import algo.PredictiveModel;
import algo.util.dao.SQLInsertBuffer;

public class RegressionModel extends PredictiveModel {

	protected double[] coefficients;
	protected double intercept;
	protected double[][] ivRanges;
	
	public RegressionModel(int[] inputFeatures, double[] coefficients, double intercept, double[][] ivRanges) {
		super(inputFeatures);
		this.coefficients = coefficients;
		this.intercept = intercept;
		this.ivRanges = ivRanges;
	}

	@Override
	public int getModelTypeID() {
		return 2; //TODO: make this cleaner, e.g. use an enum
	}

	@Override
	protected double predict(double[] indexedInputs) {
		if(indexedInputs.length != this.coefficients.length) throw new RuntimeException();
		
		double prediction = this.intercept;
		for(int i = 0; i < this.coefficients.length; i++) {
			double iv = indexedInputs[i];
			if(iv < ivRanges[i][0]) iv = ivRanges[i][0];
			if(iv > ivRanges[i][1]) iv = ivRanges[i][1];
			prediction += this.coefficients[i] * iv;
		}
		
		return prediction;
	}

	@Override
	public int toDB(SQLDatabase db, int problemID, int algoID) {
		int modelID = super.toDB(db, problemID, algoID);
		
		SQLInsertBuffer b = new SQLInsertBuffer(db, "lr_modelFeatures", new String[] {"modelID","featureID","coefficient","rangeMin","rangeMax"});
		
		b.startBufferedInsert(this.coefficients.length + 1);
		
		b.insertRow(new String[] { String.valueOf(modelID), "0", String.valueOf(this.intercept), "NULL", "NULL" });
		for(int i = 0; i < this.coefficients.length; i++) {
			b.insertRow(new String[] {
					String.valueOf(modelID)
					, String.valueOf(this.getFeatureIDAtInputIndex(i))
					, String.valueOf(this.coefficients[i])
					, String.valueOf(this.ivRanges[i][0])
					, String.valueOf(this.ivRanges[i][1])
			});
		}
		
		b.finishBufferedInsert();
		
		return modelID;
	}

	@Override
	public void fromDB(SQLDatabase db, int modelID) {
		super.fromDB(db, modelID);
		
		String sql = String.format("SELECT featureID, coefficient, rangeMin, rangeMax FROM lr_modelFeatures WHERE modelID = %d", modelID);
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		this.coefficients = new double[results.size() - 1]; // -1 for intercept which is stored in the table
		this.ivRanges = new double[results.size() - 1][2];
		
		for(Map<String,String> row : results) {
			int featureID = Integer.parseInt(row.get("featureID"));
			if(featureID == 0) {
				this.intercept = Double.parseDouble(row.get("coefficient"));
			} else {
				int inputIndex = this.getInputIndexFromFeatureID(featureID);
				this.coefficients[inputIndex] = Double.parseDouble(row.get("coefficient"));
				this.ivRanges[inputIndex][0] = Double.parseDouble(row.get("rangeMin"));
				this.ivRanges[inputIndex][1] = Double.parseDouble(row.get("rangeMax"));
			}
		}
	}
}
