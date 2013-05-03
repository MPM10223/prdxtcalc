package algo;

public class R2TestDataEvaluator extends TestDataEvaluator {

	
	public R2TestDataEvaluator(Observation[] testData) {
		super(testData);
	}

	@Override
	public double evaluateAfterPrediction(PredictiveModel subject) {
		double naiveVariance = 0.0;
		double modelVariance = 0.0;
		
		double naivePrediction = this.getDVMean(); // TODO: make this configurable
		
		for(int i = 0; i < testData.length; i++) {
			Observation o = testData[i];
			
			modelVariance += Math.pow(o.getDependentVariable() - o.getPrediction(), 2);
			//double diff = (o.getDependentVariable() - o.getPrediction());
			//modelVariance += (diff * diff);
			naiveVariance += Math.pow(o.getDependentVariable() - naivePrediction, 2);
			//diff = (o.getDependentVariable() - naivePrediction);
			//naiveVariance += (diff * diff);
		}
		
		return 1.0 - (modelVariance / naiveVariance);
	}

	@Override
	protected boolean isPredictionCorrect(double prediction, double actual) {
		throw new RuntimeException("Correct predction is undefined wrt R2");
	}
}
