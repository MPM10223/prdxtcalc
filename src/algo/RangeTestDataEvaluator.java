package algo;

public class RangeTestDataEvaluator extends TestDataEvaluator {

	protected double errorTolerancePct;
	
	public RangeTestDataEvaluator(Observation[] testData, double errorTolerancePct) {
		super(testData);
		this.errorTolerancePct = errorTolerancePct;
	}

	@Override
	protected boolean isPredictionCorrect(double prediction, double actual) {
		return prediction / actual < (1 + errorTolerancePct) && prediction / actual > (1 - errorTolerancePct);
	}
}
