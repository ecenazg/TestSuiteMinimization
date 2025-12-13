package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.SinglePointCrossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.BitFlipMutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.MaxFitnessEvaluations;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomSearchTest {

    private boolean[][] coverageMatrix() {
        return new boolean[][]{
                {true, false},
                {false, true},
                {true, true}
        };
    }

    private ChromosomeGenerator<TestSuiteChromosome> generator() {
        return () -> {
            boolean[] genes = new boolean[]{true, false, false};
            return new TestSuiteChromosome(
                    genes,
                    new BitFlipMutation(),
                    new SinglePointCrossover()
            );
        };
    }

    private FitnessFunction<TestSuiteChromosome> sizeFF() {
        return (MinimizingFitnessFunction<TestSuiteChromosome>)
                TestSuiteChromosome::getNumberOfSelectedTests;
    }

    private FitnessFunction<TestSuiteChromosome> coverageFF() {
        return (MaximizingFitnessFunction<TestSuiteChromosome>)
                c -> {
                    boolean[] covered = new boolean[2];
                    for (int t : c.getSelectedTestIndices()) {
                        for (int l = 0; l < 2; l++) {
                            covered[l] |= coverageMatrix()[t][l];
                        }
                    }
                    int count = 0;
                    for (boolean b : covered) if (b) count++;
                    return (double) count;
                };
    }

    @Test
    void randomSearchReturnsNonEmptyParetoFront() {
        RandomSearch rs = new RandomSearch(
                new MaxFitnessEvaluations(15),
                generator(),
                sizeFF(),
                coverageFF(),
                coverageMatrix(),
                3,
                2
        );

        List<TestSuiteChromosome> result = rs.findSolution();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void stoppingConditionIsExposed() {
        MaxFitnessEvaluations sc = new MaxFitnessEvaluations(5);

        RandomSearch rs = new RandomSearch(
                sc,
                generator(),
                sizeFF(),
                coverageFF(),
                coverageMatrix(),
                3,
                2
        );

        assertSame(sc, rs.getStoppingCondition());
    }

    @Test
    void solutionsAreValidChromosomes() {
        RandomSearch rs = new RandomSearch(
                new MaxFitnessEvaluations(10),
                generator(),
                sizeFF(),
                coverageFF(),
                coverageMatrix(),
                3,
                2
        );

        for (TestSuiteChromosome c : rs.findSolution()) {
            assertNotNull(c.getGenes());
            assertTrue(c.getNumberOfSelectedTests() >= 1);
        }
    }
}
