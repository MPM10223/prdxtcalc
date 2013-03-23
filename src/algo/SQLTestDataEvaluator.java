package algo;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import algo.util.search.IFitnessFunction;
import sqlWrappers.SQLDatabase;

public class SQLTestDataEvaluator<T extends PredictiveModel> implements IFitnessFunction<T> {
	
	protected TestDataEvaluator e;
	
	protected SQLDatabase db;
	protected String tableName;
	protected String[] ivColumns;
	protected String dvColumn;
	protected String predicate;
	protected String detailColumn;
	
	public SQLTestDataEvaluator(SQLDatabase db, String tableName, String[] ivColumns, String dvColumn, String predicate, EvaluationType t) {
		this(db, tableName, ivColumns, dvColumn, predicate, null, t);
	}

	public SQLTestDataEvaluator(SQLDatabase db, String tableName, String[] ivColumns, String dvColumn, String predicate, String detailColumn, EvaluationType t) {
		super();
		this.db = db;
		this.tableName = tableName;
		this.ivColumns = ivColumns;
		this.dvColumn = dvColumn;
		this.predicate = predicate;
		this.detailColumn = detailColumn;
		
		String sql = this.getTestDataQuery();
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		Observation[] data = new Observation[results.size()];
		
		for(int i = 0; i < results.size(); i++) {
			
			Map<String,String> row = results.get(i);
			
			double[] ivs = new double[ivColumns.length];
			for(int j = 0; j < ivColumns.length; j++) {
				String ivColumn = ivColumns[j].replace("[", "").replace("]", "");
				String ivValueString = row.get(ivColumn);
				if(ivValueString != null) {
					double ivValue = Double.parseDouble(ivValueString);
					ivs[j] = ivValue;
				} else {
					//TODO: deal with missing IV data
				}
			}
			
			double dv;
			if(row.get(this.dvColumn) == null) {
				//TODO: deal with missing DV data
				throw new RuntimeException();
			} else {
				dv = Double.parseDouble(row.get(this.dvColumn));
			}
			
			String detail = null;
			if(this.detailColumn != null) {
				detail = row.get(this.detailColumn);
			}
			
			data[i] = new Observation(ivs, dv, detail);
		}
		
		switch(t) {
		case BOOLEAN:
			this.e = new BooleanPredictionTestDataEvaluator(data);
			break;
		case DISCRETE:
			this.e = new TestDataEvaluator(data);
			break;
		case CONTINUOUS_R2:
			this.e = new R2TestDataEvaluator(data);
			break;
		case CONTINUOUS_RANGE:
			this.e = new RangeTestDataEvaluator(data, 0.2); // TODO: expose % tolerance as an arg
			break;
		default:
			throw new RuntimeException("Unsupported evaluation type");
		}
	}
	
	protected String getTestDataQuery() {
		StringBuilder b = new StringBuilder();
		for(String column : this.ivColumns) {
			if(b.length() > 0) b.append(", ");
			b.append(String.format("[%s]", column));
		}
		//TODO: support missing DV data in query
		return String.format("SELECT %s, [%s] %s FROM [%s] WHERE 1=1 AND [%s] IS NOT NULL %s", b.toString(), this.dvColumn, (this.detailColumn == null ? "" : ", " + this.detailColumn), this.tableName, this.dvColumn, (this.predicate == null ? "" : " AND " + this.predicate));
	}

	@Override
	public double evaluate(PredictiveModel subject) {
		return e.evaluate(subject);
	}

	@Override
	public Observation[] getPerformanceDetails(PredictiveModel subject) {
		return e.getPerformanceDetails(subject);
	}

	public String[] getIVs() {
		return this.ivColumns;
	}

	public double[] getRangeOfIV(int ivIndex) {
		return e.getRangeOfIV(ivIndex);
	}

	public double getDVMean() {
		return e.getDVMean();
	}
	
	public int getNumberOfObservations() {
		return this.e.getNumberOfObservations();
	}

	public Double[] getForecasts() {
		return this.e.getForecasts();
	}

	public double[] getActuals() {
		return this.e.getActuals();
	}

	public String[] getActualDetails() {
		return this.e.getActualDetails();
	}

}
