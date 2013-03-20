package algo;

public class CrossValidationEvaluatorDAO extends ModelTrainingDataDAO {
	
	protected int problemID;
	protected int algorithmID;
	protected int modelID;

	public CrossValidationEvaluatorDAO(String dataTable, String dvColumn, int problemID, int algorithmID, int modelID) {
		super(dataTable, dvColumn, null); // predicates not supported for now
		this.problemID = problemID;
		this.algorithmID = algorithmID;
		this.modelID = modelID;
	}
	
	public void assignFolds(int numFolds) {
		//TODO: make this actually adhere to numFolds strictly
		String sql = String.format("SELECT *, cast(cast(newID() as varbinary) as int) % %d as foldID INTO [%s] FROM [%s]", numFolds, this.getFoldTable(), this.dataTable);
		this.db.executeQuery(sql);
	}

	public int getFoldSize(int foldID) {
		String sql = String.format("SELECT COUNT(*) as numRows FROM [%s] WHERE foldID = %d", this.dataTable, foldID);
		return Integer.parseInt(this.db.getQueryResult(sql));
	}
	
	public String getFoldTable() {
		return String.format("cv_p%d_a%d_m%d", problemID, algorithmID, modelID);
	}
}
