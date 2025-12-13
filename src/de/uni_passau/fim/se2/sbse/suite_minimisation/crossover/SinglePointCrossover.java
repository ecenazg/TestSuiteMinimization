package de.uni_passau.fim.se2.sbse.suite_minimisation.crossover;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Randomness;

/**
 * Single-point crossover for test suite chromosomes.
 */
public class SinglePointCrossover implements Crossover<TestSuiteChromosome> {

    @Override
    public Pair<TestSuiteChromosome> apply(
            TestSuiteChromosome parent1,
            TestSuiteChromosome parent2) {

        boolean[] g1 = parent1.getGenes();
        boolean[] g2 = parent2.getGenes();

        int length = g1.length;
        int cut = Randomness.random().nextInt(length);

        boolean[] child1 = new boolean[length];
        boolean[] child2 = new boolean[length];

        for (int i = 0; i < length; i++) {
            if (i < cut) {
                child1[i] = g1[i];
                child2[i] = g2[i];
            } else {
                child1[i] = g2[i];
                child2[i] = g1[i];
            }
        }

        return Pair.of(
                new TestSuiteChromosome(child1, parent1.getMutation(), parent1.getCrossover()),
                new TestSuiteChromosome(child2, parent2.getMutation(), parent2.getCrossover())
        );
    }

    @Override
    public String toString() {
        return "Single-point crossover";
    }
}
