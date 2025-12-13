package de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes;

import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.Crossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.Mutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Randomness;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Chromosome representing a reduced test suite using a boolean gene array.
 *
 * gene[i] = true  → test i included
 * gene[i] = false → test i excluded
 *
 * The chromosome always represents a valid test suite: at least one test case must be included.
 */
public class TestSuiteChromosome extends Chromosome<TestSuiteChromosome> {

    private final boolean[] genes;

    /**
     * Primary constructor used during evolutionary search.
     */

    // 1) Main constructor (used by algorithms)
    public TestSuiteChromosome(
            boolean[] genes,
            Mutation<TestSuiteChromosome> mutation,
            Crossover<TestSuiteChromosome> crossover
    ) {
        super(
                mutation != null ? mutation : Mutation.identity(),
                crossover != null ? crossover : Crossover.identity()
        );
        this.genes = Objects.requireNonNull(genes).clone();
        ensureAtLeastOneTest();
    }


    // 2) Copy constructor
    public TestSuiteChromosome(TestSuiteChromosome other) {
        super(other);
        this.genes = other.genes.clone();
    }

    // 3) TEST constructor
    public TestSuiteChromosome(boolean[] genes) {
        super(); // identity mutation & crossover
        this.genes = Objects.requireNonNull(genes).clone();
        ensureAtLeastOneTest();
    }

    /**
     * Returns the number of genes = number of tests.
     */
    public int length() {
        return genes.length;
    }

    /**
     * Returns the underlying gene array (copy).
     */
    public boolean[] getGenes() {
        return genes.clone();
    }

    /**
     * Returns indices of selected tests.
     */
    public List<Integer> getSelectedTestIndices() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < genes.length; i++) {
            if (genes[i]) {
                list.add(i);
            }
        }
        return list;
    }

    /**
     * Number of included tests (not normalised).
     */
    public int getNumberOfSelectedTests() {
        int count = 0;
        for (boolean g : genes) if (g) count++;
        return count;
    }

    /**
     * Ensures the chromosome contains at least one selected test.
     * If all genes are false, randomly activates one.
     */
    private void ensureAtLeastOneTest() {
        boolean any = false;
        for (boolean b : genes) {
            if (b) {
                any = true;
                break;
            }
        }
        //A chromosome must represent a test suite with at least one test case
        if (!any && genes.length > 0) {
            int idx = Randomness.random().nextInt(genes.length);
            genes[idx] = true;
        }
    }

    @Override
    public TestSuiteChromosome copy() {
        return new TestSuiteChromosome(this);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TestSuiteChromosome o)) return false;
        return Arrays.equals(this.genes, o.genes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(genes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Chromosome(");
        for (boolean g : genes) sb.append(g ? "1" : "0");
        sb.append(")");
        return sb.toString();
    }

    /**
     * <p>
     * Returns the runtime type of the implementor (a.k.a. "self-type"). This method must only be
     * implemented in concrete, non-abstract subclasses by returning a reference to {@code this},
     * and nothing else. Returning a reference to any other runtime type other than {@code this}
     * breaks the contract.
     * <p>
     * In other words, every concrete subclass {@code Foo} that implements the interface {@code
     * SelfTyped} must implement this method as follows:
     * <pre>{@code
     * public final class Foo implements SelfTyped<Foo> {
     *     @Override
     *     public Foo self() {
     *         return this;
     *     }
     * }
     * }</pre>
     *
     * @return a reference to the self-type
     */
    @Override
    public TestSuiteChromosome self() {
        return this;
    }
}
