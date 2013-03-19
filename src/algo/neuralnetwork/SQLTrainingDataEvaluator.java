package algo.neuralnetwork;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import algo.util.search.IFitnessFunction;
import sqlWrappers.SQLDatabase;

public class SQLTrainingDataEvaluator<T extends IModel> implements IFitnessFunction<IModel> {
	
	protected TrainingDataEvaluator e;
	
	protected SQLDatabase db;
	protected String tableName;
	protected String[] ivColumns;
	protected String dvColumn;
	protected String predicate;
	protected String detailColumn;

	public SQLTrainingDataEvaluator(SQLDatabase db, String tableName, String[] ivColumns, String dvColumn, String predicate, String detailColumn, boolean isBooleanPredictor) {
		super();
		this.db = db;
		this.tableName = tableName;
		this.ivColumns = ivColumns;
		this.dvColumn = dvColumn;
		this.predicate = predicate;
		this.detailColumn = detailColumn;
		
		String sql = this.getTrainingDataQuery();
		Vector<Map<String,String>> results = db.getQueryRows(sql);
		
		Observation[] data = new Observation[results.size()];
		
		for(int i = 0; i < results.size(); i++) {
			
			Map<String,String> row = results.get(i);
			
			double[] ivs = new double[ivColumns.length];
			for(int j = 0; j < ivColumns.length; j++) {
				String ivColumn = ivColumns[j].replace("[", "").replace("]", "");
				String ivValueString = row.get(ivColumn);
				double ivValue = Double.parseDouble(ivValueString);
				ivs[j] = ivValue;
			}
			
			double dv = Double.parseDouble(row.get(this.dvColumn));
			
			String detail = null;
			if(this.detailColumn != null) {
				detail = row.get(this.detailColumn);
			}
			
			data[i] = new Observation(ivs, dv, detail);
		}
		
		if(isBooleanPredictor) {
			this.e = new BooleanPredictionTrainingDataEvaluator(data);
		} else {
			this.e = new TrainingDataEvaluator(data);
		}
	}
	
	protected String getTrainingDataQuery() {
		String ivSelectList = Arrays.toString(this.ivColumns);
		ivSelectList = ivSelectList.substring(1, ivSelectList.length() - 1);
		return String.format("SELECT %s, %s %s FROM %s WHERE 1=1 %s", ivSelectList, this.dvColumn, (this.detailColumn == null ? "" : ", " + this.detailColumn), this.tableName, (this.predicate == null ? "" : " AND " + this.predicate));
	}

	@Override
	public double evaluate(IModel subject) {
		return e.evaluate(subject);
	}

	@Override
	public Observation[] getPerformanceDetails(IModel subject) {
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
