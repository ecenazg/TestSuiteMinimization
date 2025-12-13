package de.uni_passau.fim.se2.sbse.suite_minimisation.selection;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BinaryTournamentSelectionTest {

    /**
     * Minimal concrete chromosome implementation for testing.
     * Correctly EXTENDS Chromosome and respects its contract.
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
    // Constructor tests
    // --------------------------------------------------

    @Test
    void constructorRejectsNullComparator() {
        assertThrows(
                NullPointerException.class,
                () -> new BinaryTournamentSelection<>(null, new Random())
        );
    }

    @Test
    void constructorRejectsNullRandom() {
        assertThrows(
                NullPointerException.class,
                () -> new BinaryTournamentSelection<>(
                        Comparator.comparingInt(DummyChromosome::value),
                        null
                )
        );
    }

    // --------------------------------------------------
    // apply() input validation
    // --------------------------------------------------

    @Test
    void applyRejectsNullPopulation() {
        BinaryTournamentSelection<DummyChromosome> sel =
                new BinaryTournamentSelection<>(
                        Comparator.comparingInt(DummyChromosome::value),
                        new Random()
                );

        assertThrows(NullPointerException.class, () -> sel.apply(null));
    }

    @Test
    void applyRejectsEmptyPopulation() {
        BinaryTournamentSelection<DummyChromosome> sel =
                new BinaryTournamentSelection<>(
                        Comparator.comparingInt(DummyChromosome::value),
                        new Random()
                );

        assertThrows(NoSuchElementException.class, () -> sel.apply(List.of()));
    }

    // --------------------------------------------------
    // Edge cases
    // --------------------------------------------------

    @Test
    void applyReturnsOnlyElementIfPopulationSizeOne() {
        DummyChromosome c = new DummyChromosome(42);

        BinaryTournamentSelection<DummyChromosome> sel =
                new BinaryTournamentSelection<>(
                        Comparator.comparingInt(DummyChromosome::value),
                        new Random()
                );

        DummyChromosome result = sel.apply(List.of(c));

        assertSame(c, result);
    }

    // --------------------------------------------------
    // Comparator behavior
    // --------------------------------------------------

    @Test
    void betterChromosomeIsSelectedAccordingToComparator() {
        DummyChromosome worse = new DummyChromosome(1);
        DummyChromosome better = new DummyChromosome(10);

        // Deterministic random: pick index 0 then 1
        Random deterministic = new Random() {
            private int calls = 0;
            @Override
            public int nextInt(int bound) {
                return calls++ == 0 ? 0 : 1;
            }
        };

        BinaryTournamentSelection<DummyChromosome> sel =
                new BinaryTournamentSelection<>(
                        Comparator.comparingInt(DummyChromosome::value),
                        deterministic
                );

        DummyChromosome result = sel.apply(List.of(worse, better));

        assertSame(better, result);
    }

    @Test
    void comparatorTieReturnsFirstCandidate() {
        DummyChromosome a = new DummyChromosome(5);
        DummyChromosome b = new DummyChromosome(5);

        Random deterministic = new Random() {
            private int i = 0;
            @Override
            public int nextInt(int bound) {
                return i++;
            }
        };

        BinaryTournamentSelection<DummyChromosome> sel =
                new BinaryTournamentSelection<>(
                        Comparator.comparingInt(DummyChromosome::value),
                        deterministic
                );

        DummyChromosome result = sel.apply(List.of(a, b));

        assertSame(a, result);
    }

    // --------------------------------------------------
    // Loop safety (i2 != i1)
    // --------------------------------------------------

    @Test
    void secondIndexIsAlwaysDifferentFromFirst() {
        DummyChromosome a = new DummyChromosome(1);
        DummyChromosome b = new DummyChromosome(2);
        DummyChromosome c = new DummyChromosome(3);

        AtomicInteger calls = new AtomicInteger();

        Random controlledRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                int call = calls.incrementAndGet();
                if (call == 1) return 0; // i1
                if (call == 2) return 0; // i2 == i1 → retry
                return 1;               // valid second index
            }
        };

        BinaryTournamentSelection<DummyChromosome> sel =
                new BinaryTournamentSelection<>(
                        Comparator.comparingInt(DummyChromosome::value),
                        controlledRandom
                );

        DummyChromosome result = sel.apply(List.of(a, b, c));

        assertNotNull(result);
    }
}
