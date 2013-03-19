package algo.util.search;

import java.util.Random;

public class RepeatedRandom<TOrganism> extends Search<TOrganism> {
	
	protected int iterationLimit;
	
	public RepeatedRandom(IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int iterationLimit) {
		super(generator, fitness);
		this.iterationLimit = iterationLimit;
	}
	
	public RepeatedRandom(IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int iterationLimit, int seed) {
		super(generator, fitness, seed);
		this.iterationLimit = iterationLimit;
	}
	
	public RepeatedRandom(IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int iterationLimit, Random r) {
		super(generator, fitness, r);
		this.iterationLimit = iterationLimit;
	}
	
	@Override
	public TOrganism search() {
		return search(this.iterationLimit);
	}
	
	public TOrganism search(int iterationLimit) {
		double best = Double.NEGATIVE_INFINITY;
		TOrganism winner = null;
		
		for(int i = 0; i < iterationLimit; i++) {
			
			byte[] dna = new byte[this.generator.getDNASize()];
			r.nextBytes(dna);
			
			TOrganism organism = this.generator.generate(dna);
			double fitness = this.fitness.evaluate(organism);
			
			//System.out.println(String.format("Iteration %d - Fitness: %f, Champion: %f", i+1, fitness, best));
			
			if(fitness > best) {
				System.out.println(String.format("Iteration %d - New Champion @ Fitness: %f", i+1, fitness));
				winner = organism;
				best = fitness;
			}
		}
		
		return winner;
	}
}
