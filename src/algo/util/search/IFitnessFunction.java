package algo.util.search;

import algo.Observation;

public interface IFitnessFunction<T> {

	public double evaluate(T subject);
	public Observation[] getPerformanceDetails(T subject); 

}
