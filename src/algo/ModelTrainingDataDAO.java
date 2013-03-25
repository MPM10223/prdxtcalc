package algo;

import java.util.Map;
import java.util.Vector;

import sqlWrappers.SQLDatabase;

public class ModelTrainingDataDAO {

	protected SQLDatabase db;
	
	protected String dataTable;
	protected String[] ivColumns;
	protected int[] ivFeatureIDs;
	protected String dvColumn;
	protected String idColumn;
	protected String predicate;
	
	protected double[][] featureRanges;

	public ModelTrainingDataDAO(String server, String database, String port, String userName, String password, String dataTable, String[] ivColumns, int[] ivFeatureIDs, String dvColumn, String idColumn, String predicate) {
		this(new SQLDatabase(server, database, port, userName, password), dataTable, ivColumns, ivFeatureIDs, dvColumn, idColumn, predicate);
	}
	
	public ModelTrainingDataDAO(SQLDatabase db, String dataTable, String[] ivColumns, int[] ivFeatureIDs, String dvColumn, String idColumn, String predicate) {
		this.db = db;
		this.dataTable = dataTable;
		this.ivColumns = ivColumns;
		this.ivFeatureIDs = ivFeatureIDs;
		this.dvColumn = dvColumn;
		this.idColumn = idColumn;
		this.predicate = predicate;
	}

	public int getDatasetObservationCount() {
		String sql = String.format("SELECT COUNT(*) as numRows FROM [%s] WHERE %s", dataTable, this.getSQLPredicate());
		return Integer.parseInt(this.db.getQueryResult(sql));
	}

	public SQLDatabase getDb() {
		return db;
	}

	public String getDataTable() {
		return dataTable;
	}

	public void setDataTable(String dataTable) {
		this.dataTable = dataTable;
	}

	public String getDvColumn() {
		return dvColumn;
	}

	public void setDvColumn(String dvColumn) {
		this.dvColumn = dvColumn;
	}

	public String getIdColumn() {
		return idColumn;
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
	
	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public String[] getIvColumns() {
		return ivColumns;
	}
	
	public int[] getIvFeatureIDs() {
		return ivFeatureIDs;
	}
	
	public Vector<Map<String,String>> getColumnInfo() {
		String sql = String.format("SELECT c.name as [column], t.name as [type], c.is_nullable, CASE c.name WHEN '%s' THEN 1 ELSE 0 END as isIDColumn, CASE c.name WHEN '%s' THEN 1 ELSE 0 END as isDVColumn FROM sys.columns c JOIN sys.types t ON c.system_type_id = t.system_type_id WHERE c.object_id=object_id('%s') ORDER BY c.column_id", this.idColumn, this.dvColumn, this.dataTable);
		return this.db.getQueryRows(sql);
	}

	public boolean getDVIsBinary() {
		String sql = String.format("SELECT [%s], COUNT(*) as numRows FROM [%s] WHERE [%s] IS NOT NULL AND %s GROUP BY [%s] ORDER BY [%s] ASC", this.dvColumn, this.dataTable, this.dvColumn, this.getSQLPredicate(), this.dvColumn, this.dvColumn);
		Vector<Map<String,String>> r = this.db.getQueryRows(sql);
		if(r.size() == 2) {
			if(r.firstElement().get(this.dvColumn).equals("0.0") && r.lastElement().get(this.dvColumn).equals("1.0"))
				return true;
		}
		return false;
	}
	
	protected String getSQLPredicate() {
		return "1=1" + (predicate == null ? "" : " AND " + predicate);
	}
	
	public int getFeatureID(String columnName) {
		for(int i = 0; i < this.ivColumns.length; i++) {
			if(this.ivColumns[i].equalsIgnoreCase(columnName)) return this.ivFeatureIDs[i];
		}
		return -1;
	}
	
	public int getFeatureIndex(int featureID) {
		for(int i = 0; i < this.ivFeatureIDs.length; i++) {
			if(this.ivFeatureIDs[i] == featureID) return i;
		}
		throw new RuntimeException("featureID not found");
	}
	
	public double[] getFeatureRange(int featureID) {
		int featureIndex = this.getFeatureIndex(featureID);
		
		if(this.featureRanges == null) {
			this.featureRanges = new double[this.ivColumns.length][];
		}
		
		if(this.featureRanges[featureIndex] == null) {
			String sql = String.format("SELECT MIN([%s]) as minValue, MAX([%s]) as maxValue FROM [%s] WHERE [%s] IS NOT NULL AND %s "
					, ivColumns[featureIndex], ivColumns[featureIndex], this.dataTable, this.dvColumn, this.getSQLPredicate()
					);
			Map<String,String> result = db.getQueryRow(sql);
			
			this.featureRanges[featureIndex] = new double[2];
			this.featureRanges[featureIndex][0] = Double.parseDouble(result.get("minValue"));
			this.featureRanges[featureIndex][1] = Double.parseDouble(result.get("maxValue"));
		}
		
		return this.featureRanges[featureIndex];
	}
}
