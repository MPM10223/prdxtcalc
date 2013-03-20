package algo;

import sqlWrappers.SQLDatabase;

public abstract class PredictiveModel {

	public abstract void toDB(SQLDatabase db);
	public abstract double predict(double[] ivs);
	public abstract void fromDB(SQLDatabase sqlDatabase, int modelID);

}
