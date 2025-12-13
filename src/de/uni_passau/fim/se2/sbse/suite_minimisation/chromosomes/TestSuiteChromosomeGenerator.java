package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Randomness;

public class TestSuiteChromosomeGenerator
        implements ChromosomeGenerator<TestSuiteChromosome> {

    private final int numberOfTests;
    private final Mutation<TestSuiteChromosome> mutation;
    private final Crossover<TestSuiteChromosome> crossover;

    /**
     * @param numberOfTests full size of the available test suite
     */
    public TestSuiteChromosomeGenerator(int numberOfTests,
                                        Mutation<TestSuiteChromosome> mutation,
                                        Crossover<TestSuiteChromosome> crossover) {
        this.numberOfTests = numberOfTests;
        this.mutation = mutation;
        this.crossover = crossover;
    }

    @Override
    public TestSuiteChromosome get() {
        boolean[] genes = new boolean[numberOfTests];

        // Pick target size k with bias towards smaller suites but still varied.
        // k in [1, numberOfTests]
        double r = Randomness.random().nextDouble();
        int k = 1 + (int) Math.floor(r * r * (numberOfTests - 1)); // quadratic bias

        // Sample k distinct indices (partial Fisher–Yates shuffle)
        int[] idx = new int[numberOfTests];
        for (int i = 0; i < numberOfTests; i++) idx[i] = i;

        for (int i = 0; i < k; i++) {
            int j = i + Randomness.random().nextInt(numberOfTests - i);
            int tmp = idx[i];
            idx[i] = idx[j];
            idx[j] = tmp;
            genes[idx[i]] = true;
        }

        return new TestSuiteChromosome(genes, mutation, crossover);
    }

}
