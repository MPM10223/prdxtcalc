package algo.util.search;

import java.util.Random;

public abstract class Search<TOrganism> {

	protected IOrganismGenerator<TOrganism> generator;
	protected IFitnessFunction<TOrganism> fitness;
	protected Random r;
	
	public Search(IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness) {
		this(generator, fitness, new Random());
	}
	
	public Search(IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int seed) {
		this(generator, fitness, new Random(seed));
	}
	
	public Search(IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, Random r) {
		this.generator = generator;
		this.fitness = fitness;
		this.r = r;
	}

	public IOrganismGenerator<TOrganism> getGenerator() {
		return generator;
	}

	public void setGenerator(IOrganismGenerator<TOrganism> generator) {
		this.generator = generator;
	}

	public IFitnessFunction<TOrganism> getFitness() {
		return fitness;
	}

	public void setFitness(IFitnessFunction<TOrganism> fitness) {
		this.fitness = fitness;
	}

	public Random getR() {
		return r;
	}

	public void setR(Random r) {
		this.r = r;
	}
	
	public abstract TOrganism search();
}
