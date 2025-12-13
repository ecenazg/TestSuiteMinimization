package de.uni_passau.fim.se2.sbse.suite_minimisation.selection;


import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Implements a binary tournament selection operator that chooses individuals without replacement.
 *
 * @param <C> the type of chromosomes
 */
public class BinaryTournamentSelection<C extends Chromosome<C>> implements Selection<C> {

    private static final int TOURNAMENT_SIZE = 2;

    private final Random random;
    private final Comparator<C> comparator;

    /**
     * Creates a new binary tournament selection operator without replacement,
     * comparing individuals according to the given comparator.
     *
     * @param comparator for comparing chromosomes
     * @param random     the source of randomness
     * @throws NullPointerException if the comparator is null
     */
    public BinaryTournamentSelection(
            final Comparator<C> comparator,
            final Random random)
            throws NullPointerException, IllegalArgumentException {
        this.random = requireNonNull(random);
        this.comparator = requireNonNull(comparator);
    }

    /**
     * Applies binary tournament selection without replacement to the given population.
     *
     * @param population of chromosomes from which to select
     * @return the best individual in the tournament
     * @throws NullPointerException   if the population is {@code null}
     * @throws NoSuchElementException if the population is empty
     */
    @Override
    public C apply(final List<C> population)
            throws NullPointerException, NoSuchElementException {

        requireNonNull(population);

        int size = population.size();
        if (size == 0) {
            throw new NoSuchElementException("Population is empty");
        }

        // If only one individual exists, return it
        if (size == 1) {
            return population.get(0);
        }

        // Select first candidate
        int i1 = random.nextInt(size);

        // Select second candidate (without replacement)
        int i2;
        do {
            i2 = random.nextInt(size);
        } while (i2 == i1);

        C c1 = population.get(i1);
        C c2 = population.get(i2);

        // Comparator decides which one is better
        return comparator.compare(c1, c2) >= 0 ? c1 : c2;

    }

}
