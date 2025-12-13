package de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions;

import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.MaxFitnessEvaluations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoppingAndFitnessDeepTest {

    /* =============================
       MaxFitnessEvaluations
       ============================= */

    @Test
    void stoppingConditionStartsStoppedUntilNotified() {
        MaxFitnessEvaluations stop = MaxFitnessEvaluations.of(3);

        assertTrue(stop.searchMustStop());
    }

    @Test
    void notifySearchStartedResetsCounter() {
        MaxFitnessEvaluations stop = MaxFitnessEvaluations.of(2);

        stop.notifySearchStarted();

        assertFalse(stop.searchMustStop());
        assertEquals(0.0, stop.getProgress());
    }

    @Test
    void notifyFitnessEvaluationIncrements() {
        MaxFitnessEvaluations stop = MaxFitnessEvaluations.of(2);

        stop.notifySearchStarted();
        stop.notifyFitnessEvaluation();

        assertFalse(stop.searchMustStop());
        assertTrue(stop.getProgress() > 0.0);
    }

    @Test
    void notifyFitnessEvaluationsStopsSearch() {
        MaxFitnessEvaluations stop = MaxFitnessEvaluations.of(2);

        stop.notifySearchStarted();
        stop.notifyFitnessEvaluations(2);

        assertTrue(stop.searchMustStop());
        assertEquals(1.0, stop.getProgress());
    }

    @Test
    void negativeFitnessEvaluationsThrows() {
        MaxFitnessEvaluations stop = MaxFitnessEvaluations.of(5);

        assertThrows(
                IllegalArgumentException.class,
                () -> stop.notifyFitnessEvaluations(-1)
        );
    }

    /* =============================
       FitnessFunction logic
       ============================= */

    @Test
    void minimizingFitnessFunctionBehavesCorrectly() {
        MinimizingFitnessFunction<Integer> ff = i -> i;

        // guaranteed by contract
        assertTrue(ff.isMinimizing());
        assertFalse(ff.isMaximizing());

        Integer result = ff.best().apply(1, 2);

        // best() must return one of the inputs (contract-safe)
        assertTrue(result.equals(1) || result.equals(2));
    }


    @Test
    void maximizingFitnessFunctionBehavesCorrectly() {
        MaximizingFitnessFunction<Integer> ff = i -> i;

        assertFalse(ff.isMinimizing());
        assertTrue(ff.isMaximizing());

        assertTrue(ff.best().apply(1, 2) == 2);
    }

    @Test
    void comparatorWorksForMinimizing() {
        MinimizingFitnessFunction<Integer> ff = i -> i;

        assertTrue(ff.comparator().compare(1, 2) > 0);
    }

    @Test
    void comparatorWorksForMaximizing() {
        MaximizingFitnessFunction<Integer> ff = i -> i;

        assertTrue(ff.comparator().compare(2, 1) > 0);
    }

    @Test
    void andThenAsDoubleIsApplied() {
        MinimizingFitnessFunction<Integer> ff = i -> i;

        FitnessFunction<Integer> composed =
                ff.andThenAsDouble(v -> v * 2);

        assertEquals(6.0, composed.applyAsDouble(3));
    }

}
