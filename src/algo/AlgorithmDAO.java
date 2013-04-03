package algo;

import sqlWrappers.SQLDatabase;

public class AlgorithmDAO extends ModelTrainingDataDAO {

	public AlgorithmDAO(SQLDatabase db, String dataTable, String[] ivColumns, int[] ivFeatureIDs, String dvColumn, String idColumn, String predicate) {
		super(db, dataTable, ivColumns, ivFeatureIDs, dvColumn, idColumn, predicate);
	}
	
	public String getColumnList(String delimiter, String openQualifier, String closeQualifier, boolean includeIdColumn, boolean includeDvColumn) {
		StringBuilder b = new StringBuilder();
		
		if(includeIdColumn) {
			b.append(String.format("%s%s%s", openQualifier, this.getIdColumn(), closeQualifier));
		}
		
		for(int i = 0; i < this.ivColumns.length; i++) {
			if(b.length() > 0) b.append(delimiter);
			b.append(String.format("%s%s%s", openQualifier, this.ivColumns[i], closeQualifier));
		}
		
		if(includeDvColumn) {
			b.append(String.format("%s%s%s%s", delimiter, openQualifier, this.getDvColumn(), closeQualifier));
		}
		
		return b.toString();
	}
	
	public String getSourceDataQuery(boolean includeIVs) {
		return String.format("SELECT %s FROM [%s] WHERE [%s] IS NOT NULL AND %s ORDER BY [%s]"
				, includeIVs ? this.getColumnList(",", "[", "]", true, true) : String.format("[%s], [%s]", this.getIdColumn(), this.getDvColumn())
				, this.getDataTable()
				, this.getDvColumn()
				, this.getSQLPredicate()
				, this.getIdColumn()
				);
	}
	
	public String getSourceDataDepivotQuery(boolean includeDV) {
		//TODO: implement
		return "";
	}
}
