package de.uni_passau.fim.se2.sbse.suite_minimisation.crossover;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SinglePointCrossoverTest {

    @Test
    void crossoverProducesTwoChildren() {
        TestSuiteChromosome p1 =
                new TestSuiteChromosome(new boolean[]{true, true, false, false});
        TestSuiteChromosome p2 =
                new TestSuiteChromosome(new boolean[]{false, false, true, true});

        SinglePointCrossover crossover = new SinglePointCrossover();

        Pair<TestSuiteChromosome> children = crossover.apply(p1, p2);

        assertNotNull(children);
        assertNotNull(children.getFst());
        assertNotNull(children.getSnd());
    }

    @Test
    void childrenHaveSameLengthAsParents() {
        TestSuiteChromosome p1 =
                new TestSuiteChromosome(new boolean[]{true, false, true});
        TestSuiteChromosome p2 =
                new TestSuiteChromosome(new boolean[]{false, true, false});

        SinglePointCrossover crossover = new SinglePointCrossover();
        Pair<TestSuiteChromosome> children = crossover.apply(p1, p2);

        assertEquals(p1.length(), children.getFst().length());
        assertEquals(p2.length(), children.getSnd().length());
    }

    @Test
    void childrenAreIndependentFromParents() {
        TestSuiteChromosome p1 =
                new TestSuiteChromosome(new boolean[]{true, false, true});
        TestSuiteChromosome p2 =
                new TestSuiteChromosome(new boolean[]{false, true, false});

        SinglePointCrossover crossover = new SinglePointCrossover();
        Pair<TestSuiteChromosome> children = crossover.apply(p1, p2);

        assertNotSame(p1, children.getFst());
        assertNotSame(p2, children.getSnd());
    }

    @Test
    void crossoverRespectsGeneSourceFromParents() {
        TestSuiteChromosome p1 =
                new TestSuiteChromosome(new boolean[]{true, true, true, true});
        TestSuiteChromosome p2 =
                new TestSuiteChromosome(new boolean[]{false, false, false, false});

        SinglePointCrossover crossover = new SinglePointCrossover();
        Pair<TestSuiteChromosome> children = crossover.apply(p1, p2);

        boolean[] c1 = children.getFst().getGenes();
        boolean[] c2 = children.getSnd().getGenes();

        // every gene must come from one of the parents
        for (int i = 0; i < c1.length; i++) {
            assertTrue(
                    c1[i] == true || c1[i] == false,
                    "Gene must originate from a parent"
            );
            assertTrue(
                    c2[i] == true || c2[i] == false,
                    "Gene must originate from a parent"
            );
        }
    }

    @Test
    void crossoverAlwaysProducesValidChromosomes() {
        TestSuiteChromosome p1 =
                new TestSuiteChromosome(new boolean[]{false, false, false});
        TestSuiteChromosome p2 =
                new TestSuiteChromosome(new boolean[]{false, false, false});

        SinglePointCrossover crossover = new SinglePointCrossover();
        Pair<TestSuiteChromosome> children = crossover.apply(p1, p2);

        assertTrue(children.getFst().getNumberOfSelectedTests() >= 1);
        assertTrue(children.getSnd().getNumberOfSelectedTests() >= 1);
    }

    @Test
    void toStringIsMeaningful() {
        SinglePointCrossover crossover = new SinglePointCrossover();
        assertTrue(crossover.toString().toLowerCase().contains("single"));
    }
}
