package de.uni_passau.fim.se2.sbse.suite_minimisation.mutation;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Randomness;

/**
 * Bit-flip mutation for test suite chromosomes.
 *
 * Each gene is flipped with probability 1 / n, where n is the number of tests.
 */
public class BitFlipMutation implements Mutation<TestSuiteChromosome> {

    @Override
    public TestSuiteChromosome apply(TestSuiteChromosome parent) {
        TestSuiteChromosome offspring = parent.copy();
        boolean[] genes = offspring.getGenes();

        double p = 1.0 / genes.length;

        for (int i = 0; i < genes.length; i++) {
            if (Randomness.random().nextDouble() < p) {
                genes[i] = !genes[i];
            }
        }

        return new TestSuiteChromosome(
                genes,
                offspring.getMutation(),
                offspring.getCrossover()
        );
    }

    @Override
    public String toString() {
        return "Bit-flip mutation (p = 1/n)";
    }
}
