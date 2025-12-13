package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import static org.junit.jupiter.api.Assertions.*;

import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import org.junit.jupiter.api.Test;

class TestSuiteChromosomeGeneratorTest {

    @Test
    void generatorProducesValidChromosome() {
        TestSuiteChromosomeGenerator gen =
                new TestSuiteChromosomeGenerator(20, null, null);

        TestSuiteChromosome c = gen.get();

        assertNotNull(c);
        assertTrue(c.getNumberOfSelectedTests() >= 1);
        assertEquals(20, c.length());
    }

    @Test
    void generatorNeverProducesEmptySuite() {
        TestSuiteChromosomeGenerator gen =
                new TestSuiteChromosomeGenerator(15, null, null);

        for (int i = 0; i < 100; i++) {
            TestSuiteChromosome c = gen.get();
            assertTrue(c.getNumberOfSelectedTests() >= 1);
        }
    }

    @Test
    void generatorProducesCorrectLength() {
        int n = 25;
        TestSuiteChromosomeGenerator gen =
                new TestSuiteChromosomeGenerator(n, null, null);

        TestSuiteChromosome c = gen.get();

        assertEquals(n, c.length());
    }

    @Test
    void generatorUsesProvidedOperators() {
        Mutation<TestSuiteChromosome> m = Mutation.identity();
        Crossover<TestSuiteChromosome> x = Crossover.identity();

        TestSuiteChromosomeGenerator gen =
                new TestSuiteChromosomeGenerator(10, m, x);

        TestSuiteChromosome c = gen.get();

        assertSame(m, c.getMutation());
        assertSame(x, c.getCrossover());
    }

    @Test
    void generatorProducesDifferentChromosomes() {
        TestSuiteChromosomeGenerator gen =
                new TestSuiteChromosomeGenerator(20, null, null);

        TestSuiteChromosome c1 = gen.get();
        TestSuiteChromosome c2 = gen.get();

        // probabilistic, but extremely unlikely to fail
        assertNotEquals(c1, c2);
    }

}
