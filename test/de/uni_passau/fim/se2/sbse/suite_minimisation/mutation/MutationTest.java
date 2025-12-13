package de.uni_passau.fim.se2.sbse.suite_minimisation.mutation;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Randomness;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MutationTest {

    /**
     * Minimal chromosome for testing Mutation.identity().
     */
    static final class DummyChromosome extends Chromosome<DummyChromosome> {

        private final int value;

        DummyChromosome(int value) {
            super(); // identity mutation & crossover
            this.value = value;
        }

        int value() {
            return value;
        }

        @Override
        public DummyChromosome copy() {
            return new DummyChromosome(value);
        }

        @Override
        public DummyChromosome self() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DummyChromosome d)) return false;
            return value == d.value;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(value);
        }
    }

    // --------------------------------------------------
    // Identity mutation tests
    // --------------------------------------------------

    @Test
    void identityMutationReturnsEqualButIndependentCopy() {
        DummyChromosome original = new DummyChromosome(42);
        Mutation<DummyChromosome> identity = Mutation.identity();

        DummyChromosome mutated = identity.apply(original);

        assertEquals(original, mutated);
        assertNotSame(original, mutated);
    }

    @Test
    void identityMutationPreservesSelfType() {
        DummyChromosome original = new DummyChromosome(7);
        Mutation<DummyChromosome> identity = Mutation.identity();

        DummyChromosome mutated = identity.apply(original);

        assertInstanceOf(DummyChromosome.class, mutated);
    }

    // --------------------------------------------------
    // BitFlipMutation tests
    // --------------------------------------------------

    @Test
    void bitFlipMutationReturnsNewChromosome() {
        boolean[] genes = {true, false, true, false};
        TestSuiteChromosome parent = new TestSuiteChromosome(genes);

        BitFlipMutation mutation = new BitFlipMutation();
        TestSuiteChromosome offspring = mutation.apply(parent);

        assertNotSame(parent, offspring);
    }

    @Test
    void bitFlipMutationPreservesLength() {
        boolean[] genes = {true, false, true, false, true};
        TestSuiteChromosome parent = new TestSuiteChromosome(genes);

        BitFlipMutation mutation = new BitFlipMutation();
        TestSuiteChromosome offspring = mutation.apply(parent);

        assertEquals(parent.length(), offspring.length());
    }

    @Test
    void bitFlipMutationKeepsAtLeastOneTestSelected() {
        boolean[] genes = {true, false, false, false};
        TestSuiteChromosome parent = new TestSuiteChromosome(genes);

        BitFlipMutation mutation = new BitFlipMutation();
        TestSuiteChromosome offspring = mutation.apply(parent);

        assertTrue(offspring.getNumberOfSelectedTests() >= 1);
    }

    @Test
    void bitFlipMutationMayChangeGenesButDoesNotCrash() {
        boolean[] genes = {true, true, true, true};
        TestSuiteChromosome parent = new TestSuiteChromosome(genes);

        BitFlipMutation mutation = new BitFlipMutation();
        TestSuiteChromosome offspring = mutation.apply(parent);

        // Either same or different is fine — mutation is probabilistic
        assertNotNull(offspring.getGenes());
    }

    @Test
    void bitFlipMutationDoesNotModifyParentGenes() {
        boolean[] genes = {true, false, true, false};
        TestSuiteChromosome parent = new TestSuiteChromosome(genes);

        boolean[] before = parent.getGenes();

        BitFlipMutation mutation = new BitFlipMutation();
        mutation.apply(parent);

        assertArrayEquals(before, parent.getGenes());
    }

    @Test
    void bitFlipMutationToStringIsNonEmpty() {
        BitFlipMutation mutation = new BitFlipMutation();

        assertFalse(mutation.toString().isBlank());
    }
}
