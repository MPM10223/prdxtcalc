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
	
	public String getSourceDataQuery(boolean includeIVs, boolean order) {
		return String.format("SELECT %s FROM [%s] WHERE [%s] IS NOT NULL AND %s %s"
				, includeIVs ? this.getColumnList(",", "[", "]", true, true) : String.format("[%s], [%s]", this.getIdColumn(), this.getDvColumn())
				, this.getDataTable()
				, this.getDvColumn()
				, this.getSQLPredicate()
				, order ? "ORDER BY ["  + this.getIdColumn() + "]" : ""
				);
	}
	
	public String getSourceDataDepivotQuery(boolean includeDV) {
		
		StringBuilder featureIDCaseSQL = new StringBuilder();
		featureIDCaseSQL.append("CASE featureName ");
		for(int i = 0; i < this.ivColumns.length; i++) {
			featureIDCaseSQL.append(String.format("WHEN '%s' THEN %d ", this.ivColumns[i], this.ivFeatureIDs[i]));
		}
		featureIDCaseSQL.append("ELSE NULL END");
		
		String ivSourceQuery = this.getSourceDataQuery(true, false);
		String ivColumnList = this.getColumnList(",", "[", "]", false, false);
		
		String sql = String.format("SELECT observationID, %s as featureID, value FROM ( %s ) p UNPIVOT ( value FOR featureName IN ( %s ) ) up", featureIDCaseSQL, ivSourceQuery, ivColumnList );
		
		return sql;
	}
}
