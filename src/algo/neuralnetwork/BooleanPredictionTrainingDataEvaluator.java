package algo.neuralnetwork;

import junit.framework.Assert;

public class BooleanPredictionTrainingDataEvaluator extends TrainingDataEvaluator {

	public BooleanPredictionTrainingDataEvaluator(Observation[] trainingData) {
		super(trainingData);
	}

	@Override
	protected boolean isPredictionCorrect(double prediction, double actual) {
		Assert.assertTrue(Math.abs(actual - 1.0) < 0.000000001 || Math.abs(actual - 0.0) < 0.000000001);
		double booleanPrediction = (prediction > 0.5 ? 1.0 : 0.0);
		return super.isPredictionCorrect(booleanPrediction, actual);
	}
}
