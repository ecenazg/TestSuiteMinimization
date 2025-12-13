package de.uni_passau.fim.se2.sbse.suite_minimisation.crossover;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrossoverTest {

    @Test
    void identityCrossoverReturnsCopiesNotSameReferences() {
        TestSuiteChromosome p1 =
                new TestSuiteChromosome(new boolean[]{true, false, true});
        TestSuiteChromosome p2 =
                new TestSuiteChromosome(new boolean[]{false, true, false});

        Crossover<TestSuiteChromosome> identity = Crossover.identity();

        Pair<TestSuiteChromosome> offspring = identity.apply(p1, p2);

        assertEquals(p1, offspring.getFst());
        assertEquals(p2, offspring.getSnd());

        assertNotSame(p1, offspring.getFst());
        assertNotSame(p2, offspring.getSnd());
    }

    @Test
    void identityCrossoverApplyPairWorks() {
        TestSuiteChromosome p1 =
                new TestSuiteChromosome(new boolean[]{true});
        TestSuiteChromosome p2 =
                new TestSuiteChromosome(new boolean[]{false});

        Crossover<TestSuiteChromosome> identity = Crossover.identity();

        Pair<TestSuiteChromosome> parents = Pair.of(p1, p2);
        Pair<TestSuiteChromosome> offspring = identity.apply(parents);

        assertEquals(p1, offspring.getFst());
        assertEquals(p2, offspring.getSnd());
    }

    @Test
    void applyPairThrowsOnNull() {
        Crossover<TestSuiteChromosome> identity = Crossover.identity();

        assertThrows(NullPointerException.class,
                () -> identity.apply((Pair<TestSuiteChromosome>) null));
    }

    @Test
    void identityToStringIsMeaningful() {
        Crossover<TestSuiteChromosome> identity = Crossover.identity();
        assertTrue(identity.toString().toLowerCase().contains("identity"));
    }
}
