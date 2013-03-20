package algo;

public class R2TestDataEvaluator extends TestDataEvaluator {

	
	public R2TestDataEvaluator(Observation[] testData) {
		super(testData);
	}

	@Override
	public double evaluate(PredictiveModel subject) {
		double naiveVariance = 0.0;
		double modelVariance = 0.0;
		
		for(int i = 0; i < testData.length; i++) {
			Observation o = testData[i];
			double prediction = subject.predict(o.getIndependentVariables());
			o.setPrediction(prediction);
			
			double naivePrediction = this.getDVMean(); // TODO: make this configurable
			
			modelVariance += Math.pow(o.getDependentVariable() - prediction, 2);
			naiveVariance += Math.pow(o.getDependentVariable() - naivePrediction, 2);
		}
		
		return 1.0 - (modelVariance / naiveVariance);
	}

	@Override
	protected boolean isPredictionCorrect(double prediction, double actual) {
		throw new RuntimeException("Correct predction is undefined wrt R2");
	}
}
