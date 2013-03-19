package algo.neuralnetwork;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import algo.util.search.IFitnessFunction;

public class TrainingDataEvaluator implements IFitnessFunction<IModel> {

	protected Observation[] trainingData;
	
	public TrainingDataEvaluator(Observation[] trainingData) {
		this.trainingData = trainingData;
	}
	
	@Override
	public double evaluate(IModel subject) {
		double score = 0;
		for(int i = 0; i < trainingData.length; i++) {
			Observation o = trainingData[i];
			double prediction = subject.predict(o.getIndependentVariables());
			o.setPrediction(prediction);
			
			if(this.isPredictionCorrect(prediction, o.getDependentVariable())) {
				score++;
			}
		}
		return score / (double)trainingData.length;
	}

	@Override
	public Observation[] getPerformanceDetails(IModel subject) {
		Observation[] results = new Observation[trainingData.length];
		for(int i = 0; i < trainingData.length; i++) {
			Observation o = trainingData[i].clone();
			double prediction = subject.predict(o.getIndependentVariables());
			o.setPrediction(prediction);
			results[i] = o;
		}
		return results;
	}
	
	protected boolean isPredictionCorrect(double prediction, double actual) {
		return Math.abs(prediction - actual) < 0.000001;
	}

	public double[] getRangeOfIV(int ivIndex) {
		double[] range = new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
		for(int i = 0; i < this.trainingData.length; i++) {
			if(this.trainingData[i].independentVariables[ivIndex] < range[0]) range[0] = this.trainingData[i].independentVariables[ivIndex];
			if(this.trainingData[i].independentVariables[ivIndex] > range[1]) range[1] = this.trainingData[i].independentVariables[ivIndex];
		}
		return range;
	}

	public double getDVMean() {
		SummaryStatistics ss = new SummaryStatistics();
		for(int i = 0; i < this.trainingData.length; i++) {
			ss.addValue(this.trainingData[i].getDependentVariable());
		}
		return ss.getMean();
	}

	public int getNumberOfObservations() {
		return this.trainingData.length;
	}

	public Double[] getForecasts() {
		Double[] forecasts = new Double[this.trainingData.length];
		for(int i = 0; i < this.trainingData.length; i++) {
			forecasts[i] = this.trainingData[i].getPrediction();
		}
		return forecasts;
	}

	public double[] getActuals() {
		double[] actuals = new double[this.trainingData.length];
		for(int i = 0; i < this.trainingData.length; i++) {
			actuals[i] = this.trainingData[i].getDependentVariable();
		}
		return actuals;
	}

	public String[] getActualDetails() {
		String[] details = new String[this.trainingData.length];
		for(int i = 0; i < this.trainingData.length; i++) {
			details[i] = this.trainingData[i].getDetail();
		}
		return details;
	}

}
