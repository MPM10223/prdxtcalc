package algo;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import algo.util.search.IFitnessFunction;

public class TestDataEvaluator implements IFitnessFunction<PredictiveModel> {

	protected Observation[] testData;
	
	public TestDataEvaluator(Observation[] testData) {
		this.testData = testData;
	}
	
	@Override
	public double evaluate(PredictiveModel subject) {
		return this.evaluate(subject, false);
	}
	
	public double evaluate(PredictiveModel subject, boolean inBatch) {

		if(inBatch) {
			this.predictInBatch(subject);
		} else {
			this.predictIndividually(subject);
		}
		
		return this.evaluateAfterPrediction(subject);
	}
	
	protected double evaluateAfterPrediction(PredictiveModel subject) {
		double score = 0;
		for(int i = 0; i < testData.length; i++) {
			Observation o = testData[i];
			if(this.isPredictionCorrect(o.getPrediction(), o.getDependentVariable())) {
				score++;
			}
		}
		return score / (double)testData.length;
	}
	
	private void predictIndividually(PredictiveModel subject) {
		for(int i = 0; i < testData.length; i++) {
			Observation o = testData[i];
			double prediction = subject.predict(o.getIndependentVariables());
			o.setPrediction(prediction);
		}
	}
	
	private void predictInBatch(PredictiveModel subject) {
		this.testData = subject.predict(this.testData);
	}

	@Override
	public Observation[] getPerformanceDetails(PredictiveModel subject) {
		Observation[] results = new Observation[testData.length];
		for(int i = 0; i < testData.length; i++) {
			Observation o = testData[i].clone();
			double prediction = subject.predict(o.getIndependentVariables());
			o.setPrediction(prediction);
			results[i] = o;
		}
		return results;
	}
	
	protected boolean isPredictionCorrect(double prediction, double actual) {
		return Math.abs(prediction - actual) < 0.000001;
	}

	public double[] getRangeOfIV(int featureID) {
		double[] range = new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
		for(int i = 0; i < this.testData.length; i++) {
			double ivValue = this.testData[i].getIndependentVariables().get(featureID);
			if(ivValue < range[0]) range[0] = ivValue;
			if(ivValue > range[1]) range[1] = ivValue;
		}
		return range;
	}

	public double getDVMean() {
		SummaryStatistics ss = new SummaryStatistics();
		for(int i = 0; i < this.testData.length; i++) {
			ss.addValue(this.testData[i].getDependentVariable());
		}
		return ss.getMean();
	}

	public int getNumberOfObservations() {
		return this.testData.length;
	}

	public Double[] getForecasts() {
		Double[] forecasts = new Double[this.testData.length];
		for(int i = 0; i < this.testData.length; i++) {
			forecasts[i] = this.testData[i].getPrediction();
		}
		return forecasts;
	}

	public double[] getActuals() {
		double[] actuals = new double[this.testData.length];
		for(int i = 0; i < this.testData.length; i++) {
			actuals[i] = this.testData[i].getDependentVariable();
		}
		return actuals;
	}

	public String[] getActualDetails() {
		String[] details = new String[this.testData.length];
		for(int i = 0; i < this.testData.length; i++) {
			details[i] = this.testData[i].getDetail();
		}
		return details;
	}
}
