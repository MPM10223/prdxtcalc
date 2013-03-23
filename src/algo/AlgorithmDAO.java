package algo;

import sqlWrappers.SQLDatabase;

public class AlgorithmDAO extends ModelTrainingDataDAO {

	public AlgorithmDAO(SQLDatabase db, String dataTable, String[] ivColumns, String dvColumn, String idColumn, String predicate) {
		super(db, dataTable, ivColumns, dvColumn, idColumn, predicate);
	}

}
