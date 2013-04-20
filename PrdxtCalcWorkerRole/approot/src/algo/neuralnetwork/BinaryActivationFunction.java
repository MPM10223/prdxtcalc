package algo.neuralnetwork;

public class BinaryActivationFunction extends ActivationFunction {

	protected double threshhold;
	
	public BinaryActivationFunction(double threshhold) {
		this.threshhold = threshhold;
	}
	
	@Override
	public double process(double[] input) {
		double sum = this.getLinearSum(input);
		return sum > threshhold ? 1.0 : 0.0;
	}

	@Override
	public int hashCode() {
		return (int)Math.rint(threshhold * 1000);
	}
}
