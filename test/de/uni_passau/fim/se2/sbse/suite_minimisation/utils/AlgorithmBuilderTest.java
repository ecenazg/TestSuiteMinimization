package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.MaxFitnessEvaluations;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmBuilderTest {

    private static boolean[][] smallCoverageMatrix() {
        return new boolean[][]{
                {true, false},
                {false, true}
        };
    }

    private static StoppingCondition stoppingCondition() {
        // allow a few evaluations, then stop
        return MaxFitnessEvaluations.of(5);
    }

    @Test
    void builderCreatesRandomSearchAlgorithm() {
        AlgorithmBuilder builder = new AlgorithmBuilder(
                new Random(1),
                stoppingCondition(),
                smallCoverageMatrix()
        );

        GeneticAlgorithm<? extends Chromosome<?>> algo =
                builder.buildAlgorithm(SearchAlgorithmType.RANDOM_SEARCH);

        assertNotNull(algo);
        assertTrue(algo instanceof RandomSearch);
    }

    @Test
    void builderCreatesNSGA2Algorithm() {
        AlgorithmBuilder builder = new AlgorithmBuilder(
                new Random(1),
                stoppingCondition(),
                smallCoverageMatrix()
        );

        GeneticAlgorithm<? extends Chromosome<?>> algo =
                builder.buildAlgorithm(SearchAlgorithmType.NSGA_II);

        assertNotNull(algo);
        assertTrue(algo instanceof NSGA2);
    }

    @Test
    void sizeFitnessFunctionIsNotNull() {
        AlgorithmBuilder builder = new AlgorithmBuilder(
                new Random(1),
                stoppingCondition(),
                smallCoverageMatrix()
        );

        assertNotNull(builder.getSizeFF());
    }

    @Test
    void coverageFitnessFunctionIsNotNull() {
        AlgorithmBuilder builder = new AlgorithmBuilder(
                new Random(1),
                stoppingCondition(),
                smallCoverageMatrix()
        );

        assertNotNull(builder.getCoverageFF());
    }
}
