package de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions;

import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.MaxFitnessEvaluations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaxFitnessEvaluationsTest {

    @Test
    void searchStopsAfterMaxEvaluations() {
        MaxFitnessEvaluations sc = MaxFitnessEvaluations.of(3);

        sc.notifySearchStarted();

        assertFalse(sc.searchMustStop());

        sc.notifyFitnessEvaluation();
        sc.notifyFitnessEvaluation();
        sc.notifyFitnessEvaluation();

        assertTrue(sc.searchMustStop());
    }

    @Test
    void progressIsZeroAtStart() {
        MaxFitnessEvaluations sc = MaxFitnessEvaluations.of(5);
        sc.notifySearchStarted();

        assertEquals(0.0, sc.getProgress());
    }

    @Test
    void progressIncreasesCorrectly() {
        MaxFitnessEvaluations sc = MaxFitnessEvaluations.of(4);
        sc.notifySearchStarted();

        sc.notifyFitnessEvaluation();
        sc.notifyFitnessEvaluation();

        assertEquals(0.5, sc.getProgress());
    }

    @Test
    void notifyFitnessEvaluationsRejectsNegative() {
        MaxFitnessEvaluations sc = MaxFitnessEvaluations.of(5);

        assertThrows(IllegalArgumentException.class,
                () -> sc.notifyFitnessEvaluations(-1));
    }

    @Test
    void constructorRejectsNonPositiveBudget() {
        assertThrows(IllegalArgumentException.class,
                () -> MaxFitnessEvaluations.of(0));

        assertThrows(IllegalArgumentException.class,
                () -> MaxFitnessEvaluations.of(-3));
    }
}
