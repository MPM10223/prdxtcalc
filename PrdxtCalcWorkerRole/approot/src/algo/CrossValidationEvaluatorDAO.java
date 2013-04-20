package algo;

import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public class CrossValidationEvaluatorDAO extends ModelTrainingDataDAO {
	
	protected int problemID;
	protected int algorithmID;
	protected int modelID;

	public CrossValidationEvaluatorDAO(SQLDatabase db, String dataTable, String[] ivColumns, int[] ivFeatureIDs, String dvColumn, String idColumn, int problemID, int algorithmID, int modelID) {
		super(db, dataTable, ivColumns, ivFeatureIDs, dvColumn, idColumn, null); // predicates not supported for now
		this.problemID = problemID;
		this.algorithmID = algorithmID;
		this.modelID = modelID;
	}
	
	public void assignFolds(int numFolds) {
		String sql = String.format("IF(object_id('%s') IS NOT NULL) DROP TABLE [%s]", this.getFoldTable(), this.getFoldTable());
		db.executeQuery(sql);
		
		Vector<Map<String,String>> columnInfo = this.getColumnInfo();
		StringBuilder columnDef = new StringBuilder();
		StringBuilder columnList = new StringBuilder();
		boolean first = true;
		
		for(Map<String,String> column : columnInfo) {
			if(first) first = false;
			else {
				columnDef.append(", ");
				columnList.append(", ");
			}
			
			columnDef.append(String.format("[%s] %s %s", column.get("column"), column.get("type"), (column.get("is_nullable").equals("0") ? "NOT NULL" : "NULL")));
			columnList.append(String.format("[%s]", column.get("column")));
		}
		
		sql = String.format("CREATE TABLE [%s] ( %s, foldID int not null, CONSTRAINT PK__%s PRIMARY KEY ([%s]) )", this.getFoldTable(), columnDef.toString(), this.getFoldTable(), this.getIdColumn());
		db.executeQuery(sql);
		
		sql = String.format("INSERT INTO [%s] SELECT %s, (Row_Number() OVER (ORDER BY newID()) - 1) %% %d as foldID FROM [%s]", this.getFoldTable(), columnList.toString(), numFolds, this.dataTable);
		this.db.executeQuery(sql);
	}

	public int getFoldSize(int foldID) {
		String sql = String.format("SELECT COUNT(*) as numRows FROM [%s] WHERE foldID = %d", this.getFoldTable(), foldID);
		return Integer.parseInt(this.db.getQueryResult(sql));
	}
	
	public String getFoldTable() {
		return String.format("cv_p%d_a%d_m%d", problemID, algorithmID, modelID);
	}
}
