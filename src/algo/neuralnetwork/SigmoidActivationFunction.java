package algo.neuralnetwork;

public class SigmoidActivationFunction extends ActivationFunction {

	@Override
	public double process(double[] input) {
		double sum = this.getLinearSum(input);
		return 1 / (1 + Math.exp(-sum));
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
