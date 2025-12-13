package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.SinglePointCrossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.BitFlipMutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.MaxFitnessEvaluations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class NSGA2Test {

    private ChromosomeGenerator<TestSuiteChromosome> generator() {
        return () -> {
            boolean[] genes = new boolean[]{true, false};
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
                c -> c.getNumberOfSelectedTests() == 2 ? 1.0 : 0.5;
    }

    @Test
    void nsga2ProducesParetoFront() {
        NSGA2 nsga2 = new NSGA2(
                new MaxFitnessEvaluations(20),
                new Random(1),
                4,
                generator(),
                sizeFF(),
                coverageFF()
        );

        List<TestSuiteChromosome> result = nsga2.findSolution();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void nsga2RespectsStoppingCondition() {
        MaxFitnessEvaluations sc = new MaxFitnessEvaluations(1);

        NSGA2 nsga2 = new NSGA2(
                sc,
                new Random(1),
                10,
                generator(),
                sizeFF(),
                coverageFF()
        );

        nsga2.findSolution();

        assertTrue(sc.searchMustStop());
    }

    @Test
    void nsga2ComparatorHandlesMissingMetadataSafely() {
        NSGA2 nsga2 = new NSGA2(
                new MaxFitnessEvaluations(5),
                new Random(),
                2,
                generator(),
                sizeFF(),
                coverageFF()
        );

        TestSuiteChromosome a = generator().get();
        TestSuiteChromosome b = generator().get();

        // no exception even without rank/crowding initialized
        assertDoesNotThrow(() -> nsga2.nsga2Comparator().compare(a, b));
    }

}
