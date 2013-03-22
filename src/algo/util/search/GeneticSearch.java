package algo.util.search;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Random;

import algo.util.statistics.Combinatorics;

public class GeneticSearch<TOrganism extends IGeneticOrganism> extends Search<TOrganism> {
	
	protected int numOrganisms;
	protected int maxTrials;

	protected double minMutationRate;
	protected double maxMutationRate;
	protected double minCrossoverRate;
	protected double maxCrossoverRate;
	
	protected Class<TOrganism> ct;

	public GeneticSearch(IOrganismGenerator<TOrganism> generator, IFitnessFunction<TOrganism> fitness, int numOrganisms,
			int maxTrials, double minMutationRate, double maxMutationRate, double minCrossoverRate, double maxCrossoverRate, Class<TOrganism> ct) {
		super(generator, fitness);
		this.numOrganisms = numOrganisms;
		this.maxTrials = maxTrials;
		this.minMutationRate = minMutationRate;
		this.maxMutationRate = maxMutationRate;
		this.minCrossoverRate = minCrossoverRate;
		this.maxCrossoverRate = maxCrossoverRate;
		this.ct = ct;
	}
	
	@Override
	public TOrganism search() {
		/* (http://www.obitko.com/tutorials/genetic-algorithms/ga-basic-description.php)
		 * Outline of the Basic Genetic Algorithm

		1. [Start] Generate random population of n chromosomes (suitable solutions for the problem)
		2. [Fitness] Evaluate the fitness f(x) of each chromosome x in the population
		3. [New population] Create a new population by repeating following steps until the new population is complete
			A. [Selection] Select two parent chromosomes from a population according to their fitness (the better fitness, the bigger chance to be selected)
			B. [Crossover] With a crossover probability cross over the parents to form a new offspring (children). If no crossover was performed, offspring is an exact copy of parents.
			C. [Mutation] With a mutation probability mutate new offspring at each locus (position in chromosome).
			D. [Accepting] Place new offspring in a new population
		4. [Replace] Use new generated population for a further run of algorithm
		5. [Test] If the end condition is satisfied, stop, and return the best solution in current population
		6. [Loop] Go to step 2

		 */
		
		TOrganism winner = null;
		double best = Double.NEGATIVE_INFINITY;
		
		// Start
		TOrganism[] population = (TOrganism [])Array.newInstance(ct, this.numOrganisms);
		for(int i = 0; i < this.numOrganisms; i++) {
			byte[] dna = new byte[generator.getDNASize()];
			r.nextBytes(dna);
			population[i] = generator.generate(dna);
		}
		
		int trial = 0;
		while(trial < this.maxTrials) {
			
			if(trial % 1 == 0) System.out.println("Trial " + trial);
			
			// Fitness
			HashMap<TOrganism, Double> fitness = new HashMap<TOrganism, Double>(this.numOrganisms);
			HashMap<TOrganism, Double> selectionLikelihood = new HashMap<TOrganism, Double>(this.numOrganisms);
			
			for(int i = 0; i < this.numOrganisms; i++) {
				TOrganism o = population[i];
				Double f = this.fitness.evaluate(o);
				fitness.put(o, f);
				selectionLikelihood.put(o, Math.pow(f, 2));
				
				if(f > best) {
					System.out.println(String.format("Trial %d - New Champion @ Fitness: %f", trial+1, f));
					winner = o;
					best = f;
				}
			}
			
			TOrganism[] nextGeneration = (TOrganism [])Array.newInstance(ct, this.numOrganisms);
			
			// New Population
			for(int i = 0; i < this.numOrganisms; i++) {
				// Parent Selection
				TOrganism p1 = Combinatorics.randomElement(population, selectionLikelihood, r);
				TOrganism p2 = Combinatorics.randomElement(population, selectionLikelihood, r);
				
				// Crossover
				byte[] offspringDNA;
				
				double crossOverThreshhold = Combinatorics.randomDouble(this.minCrossoverRate, this.maxCrossoverRate, r);
				double crossOver = r.nextDouble();
				
				if(crossOver < crossOverThreshhold) {
					// cross over
					offspringDNA = crossover(p1.getDNA(), p2.getDNA(), r);
				} else {
					// clone
					double f1 = fitness.get(p1);
					double f2 = fitness.get(p2);
					offspringDNA = (f1 > f2 ? p1.getDNA() : p2.getDNA());
				}
				
				// Mutation
				double mutationRate = Combinatorics.randomDouble(this.minMutationRate, this.maxMutationRate, r);
				offspringDNA = mutate(offspringDNA, mutationRate, r);
				
				// Acceptance
				TOrganism offspring = generator.generate(offspringDNA);
				nextGeneration[i] = offspring;
			}
			
			population = nextGeneration;
			
			trial++;
		}
		
		return winner;
	}

	protected static byte[] crossover(byte[] dna1, byte[] dna2, Random r) {
		
		if(dna1.length != dna2.length) throw new IllegalArgumentException();
		
		int crossoverIndex = r.nextInt(dna1.length);
		
		byte[] offspring = new byte[dna1.length];
		for(int i = 0; i < dna1.length; i++) {
			offspring[i] = (i < crossoverIndex ? dna1[i] : dna2[i]);
		}
		
		return offspring;
	}

	protected static byte[] mutate(byte[] dna, double mutationRate, Random r) {
		for(int i = 0; i < dna.length; i++) {
			if(r.nextDouble() < mutationRate) {
				byte[] newGene = new byte[1];
				r.nextBytes(newGene);
				dna[i] = newGene[0];
			}
		}
		return dna;
	}

}
