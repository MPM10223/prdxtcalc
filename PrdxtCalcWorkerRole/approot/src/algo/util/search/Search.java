package algo.util.search;

import java.util.Random;

import algo.util.dao.ILog;

public abstract class Search<TOrganism> {

	protected ILog log;
	protected IOrganismGenerator<TOrganism> generator;
	protected IFitnessFunction<TOrganism> fitness;
	protected Random r;
	
	public Search(ILog log, IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness) {
		this(log, generator, fitness, new Random());
	}
	
	public Search(ILog log, IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int seed) {
		this(log, generator, fitness, new Random(seed));
	}
	
	public Search(ILog log, IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, Random r) {
		this.log = log;
		this.generator = generator;
		this.fitness = fitness;
		this.r = r;
	}

	public ILog getLog() {
		return log;
	}

	public void setLog(ILog log) {
		this.log = log;
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
