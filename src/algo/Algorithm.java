package algo;

public abstract class Algorithm<T extends PredictiveModel> {
	
	public abstract T buildModel(AlgorithmDAO dao);

}
