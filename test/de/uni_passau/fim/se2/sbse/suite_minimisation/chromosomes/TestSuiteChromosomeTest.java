package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TestSuiteChromosomeTest {

    @Test
    void chromosomeAlwaysHasAtLeastOneTest() {
        boolean[] genes = new boolean[10]; // all false
        TestSuiteChromosome c = new TestSuiteChromosome(
                genes,
                null,
                null
        );

        assertTrue(c.getNumberOfSelectedTests() >= 1,
                "Chromosome must contain at least one selected test");
    }

    @Test
    void copyProducesEqualButIndependentChromosome() {
        boolean[] genes = {true, false, true};
        TestSuiteChromosome c1 = new TestSuiteChromosome(genes, null, null);
        TestSuiteChromosome c2 = c1.copy();

        assertEquals(c1, c2);
        assertNotSame(c1, c2);
    }

    @Test
    void lengthMatchesGeneArray() {
        boolean[] genes = {true, false, true, false};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        assertEquals(4, c.length());
    }

    @Test
    void getGenesReturnsCopyNotReference() {
        boolean[] genes = {true, false, true};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        boolean[] returned = c.getGenes();
        returned[0] = false;

        // original chromosome must not change
        assertTrue(c.getGenes()[0]);
    }

    @Test
    void getSelectedTestIndicesCorrect() {
        boolean[] genes = {true, false, true, false};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        var indices = c.getSelectedTestIndices();

        assertEquals(2, indices.size());
        assertTrue(indices.contains(0));
        assertTrue(indices.contains(2));
    }

    @Test
    void equalsDependsOnlyOnGenes() {
        boolean[] g1 = {true, false, true};
        boolean[] g2 = {true, false, true};

        TestSuiteChromosome c1 = new TestSuiteChromosome(g1);
        TestSuiteChromosome c2 = new TestSuiteChromosome(g2);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void toStringContainsBinaryRepresentation() {
        boolean[] genes = {true, false, true};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        String s = c.toString();

        assertTrue(s.contains("1"));
        assertTrue(s.contains("0"));
    }

    @Test
    void lengthEqualsGeneArrayLength() {
        boolean[] genes = {true, false, true, false};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        assertEquals(4, c.length());
    }

    @Test
    void chromosomeNeverEmptyEvenIfAllGenesFalse() {
        boolean[] genes = new boolean[10]; // all false
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        assertTrue(c.getNumberOfSelectedTests() >= 1);
    }
    @Test
    void getGenesReturnsDefensiveCopy() {
        boolean[] genes = {true, false, true};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        boolean[] copy = c.getGenes();
        copy[0] = false;

        assertTrue(c.getGenes()[0], "Internal genes must not be modified externally");
    }

    @Test
    void selectedTestIndicesCorrect() {
        boolean[] genes = {true, false, true, false, true};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        var indices = c.getSelectedTestIndices();

        assertEquals(3, indices.size());
        assertTrue(indices.contains(0));
        assertTrue(indices.contains(2));
        assertTrue(indices.contains(4));
    }

    @Test
    void numberOfSelectedTestsMatchesIndices() {
        boolean[] genes = {true, false, true};
        TestSuiteChromosome c = new TestSuiteChromosome(genes);

        assertEquals(c.getSelectedTestIndices().size(),
                c.getNumberOfSelectedTests());
    }

    @Test
    void chromosomesWithSameGenesAreEqual() {
        boolean[] g1 = {true, false, true};
        boolean[] g2 = {true, false, true};

        TestSuiteChromosome c1 = new TestSuiteChromosome(g1);
        TestSuiteChromosome c2 = new TestSuiteChromosome(g2);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void chromosomesWithDifferentGenesNotEqual() {
        TestSuiteChromosome c1 = new TestSuiteChromosome(new boolean[]{true, false});
        TestSuiteChromosome c2 = new TestSuiteChromosome(new boolean[]{false, true});

        assertNotEquals(c1, c2);
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        TestSuiteChromosome c = new TestSuiteChromosome(new boolean[]{true});

        assertNotEquals(c, "not a chromosome");
    }


    @Test
    void copyIsEqualButNotSameObject() {
        boolean[] genes = {true, false, true};
        TestSuiteChromosome original = new TestSuiteChromosome(genes);

        TestSuiteChromosome copy = original.copy();

        assertEquals(original, copy);
        assertNotSame(original, copy);
    }


    @Test
    void nullMutationAndCrossoverFallBackToIdentity() {
        TestSuiteChromosome c = new TestSuiteChromosome(
                new boolean[]{true, false},
                null,
                null
        );

        assertNotNull(c.getMutation());
        assertNotNull(c.getCrossover());
    }

    @Test
    void mutateReturnsValidChromosome() {
        TestSuiteChromosome c = new TestSuiteChromosome(new boolean[]{true, false});

        TestSuiteChromosome mutated = c.mutate();

        assertNotNull(mutated);
        assertEquals(c.length(), mutated.length());
        assertTrue(mutated.getNumberOfSelectedTests() >= 1);
    }

    @Test
    void crossoverReturnsTwoValidChildren() {
        TestSuiteChromosome c1 = new TestSuiteChromosome(new boolean[]{true, false, true});
        TestSuiteChromosome c2 = new TestSuiteChromosome(new boolean[]{false, true, false});

        var children = c1.crossover(c2);

        assertNotNull(children);
        assertNotNull(children.getFst());
        assertNotNull(children.getSnd());
        assertEquals(c1.length(), children.getFst().length());
        assertEquals(c1.length(), children.getSnd().length());
    }

    @Test
    void selfReturnsThis() {
        TestSuiteChromosome c = new TestSuiteChromosome(new boolean[]{true});

        assertSame(c, c.self());
    }



}
