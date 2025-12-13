package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.GeneticAlgorithm;
import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.NSGA2;
import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.RandomSearch;
import de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms.SearchAlgorithmType;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.Chromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.crossover.SinglePointCrossover;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.*;
import de.uni_passau.fim.se2.sbse.suite_minimisation.mutation.BitFlipMutation;
import de.uni_passau.fim.se2.sbse.suite_minimisation.selection.BinaryTournamentSelection;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;

import java.util.Random;

public class AlgorithmBuilder {

    /**
     * The default source randomness.
     */
    private final Random random;

    /**
     * The stopping condition to use.
     */
    private final StoppingCondition stoppingCondition;

    /**
     * The coverage matrix to use. Every row in the matrix represents a test case and every column
     * represents a line in the CUT. An entry {@code matrix[i][j] == true} indicates that test case
     * {@code i} covers line {@code j}. The matrix is rectangular.
     */
    private final boolean[][] coverageMatrix;

    /**
     * The number of test cases in the test suite (corresponds to the number of rows in the {@code
     * coverageMatrix} and the length of the {@code testCaseNames} array).
     */
    private final int numberTestCases;

    /**
     * The number of lines in the system under test (corresponds to the number of columns in the
     * {@code coverageMatrix}).
     */
    private final int numberLines;

    /**
     * A normalizing fitness function measuring the size of test suite chromosomes.
     */
    private final MinimizingFitnessFunction<? extends Chromosome<?>> sizeFF;

    /**
     * A normalizing fitness function measuring the coverage of test suite chromosomes.
     */
    private final MaximizingFitnessFunction<? extends Chromosome<?>> coverageFF;

    public AlgorithmBuilder(final Random random,
                            final StoppingCondition stoppingCondition,
                            final boolean[][] coverageMatrix) {
        this.random = random;
        this.stoppingCondition = stoppingCondition;
        this.coverageMatrix = coverageMatrix;
        this.numberLines = coverageMatrix[0].length;
        this.numberTestCases = coverageMatrix.length;
        this.sizeFF = makeTestSuiteSizeFitnessFunction();
        this.coverageFF = makeTestSuiteCoverageFitnessFunction();
    }

    /**
     * Creates a new normalizing fitness function that measures the size of a given test suite chromosome.
     *
     * @return the test suite size fitness function
     * @apiNote The return type uses a wildcard type "{@code ?}". This is because your custom
     * subclass of {@code Chromosome} has not existed yet at the time of writing this code, so I
     * couldn't specify it.
     * @implSpec When implementing this method, you can instantiate your custom subclasses such as
     * {@code FitnessFunction} as usual. No need to use the wildcard type "{@code ?}" yourself. The
     * fitness function shall be normalizing. The overall number of test cases can be retrieved from
     * the `numberTestCases` field.
     */
    private MinimizingFitnessFunction<? extends Chromosome<?>> makeTestSuiteSizeFitnessFunction() {

        return (MinimizingFitnessFunction<Chromosome<?>>) chromosome -> {
            // We know this is our concrete chromosome
            TestSuiteChromosome c = (TestSuiteChromosome) chromosome;

            int selectedTests = c.getNumberOfSelectedTests();

            // Normalized to [0, 1]
            return (double) selectedTests / numberTestCases;
        };
    }


    /**
     * Creates a new normalizing fitness function that measures the coverage of a given test suite chromosome.
     *
     * @return the test suite coverage fitness function
     * @apiNote The return type uses a wildcard type "{@code ?}". This is because your custom
     * subclass of {@code Chromosome} has not existed yet at the time of writing this code, so I
     * couldn't specify it.
     * @implSpec When implementing this method, you can instantiate your custom subclasses such as
     * {@code FitnessFunction} as usual. No need to use the wildcard type "{@code ?}" yourself. The
     * fitness function shall be normalizing. The overall number of lines in the SUT
     * (system-under-test) can be retrieved from the `numberLines` field.
     */
    private MaximizingFitnessFunction<? extends Chromosome<?>> makeTestSuiteCoverageFitnessFunction() {

        return (MaximizingFitnessFunction<Chromosome<?>>) chromosome -> {
            TestSuiteChromosome c = (TestSuiteChromosome) chromosome;

            boolean[] covered = new boolean[numberLines];

            for (int testIndex : c.getSelectedTestIndices()) {
                for (int line = 0; line < numberLines; line++) {
                    covered[line] |= coverageMatrix[testIndex][line];
                }
            }

            int coveredLines = 0;
            for (boolean b : covered) {
                if (b) coveredLines++;
            }

            // Normalized to [0, 1]
            return (double) coveredLines / numberLines;
        };
    }


    public MinimizingFitnessFunction<? extends Chromosome<?>> getSizeFF() {
        return sizeFF;
    }

    public MaximizingFitnessFunction<? extends Chromosome<?>> getCoverageFF() {
        return coverageFF;
    }

    /**
     * Builds the specified search algorithm using the fields of this class.
     *
     * @param algorithm the algorithm to build
     * @return the algorithm
     * @apiNote The return type uses a wildcard type "{@code ?}". This is because your custom
     * subclass of {@code Chromosome} has not existed yet at the time of writing this code, so I
     * couldn't specify it.
     */
    public GeneticAlgorithm<? extends Chromosome<?>> buildAlgorithm(final SearchAlgorithmType algorithm) {
        return switch (algorithm) {
            case RANDOM_SEARCH -> buildRandomSearch();
            case NSGA_II -> buildNSGA2();
        };
    }

    /**
     * Returns an instance of the NSGA-II search algorithm to find a solution for the test suite
     * minimization problem. The algorithm is constructed using the fields of this class.
     *
     * @return the search algorithm
     * @apiNote The return type uses a wildcard type "{@code ?}". This is because your custom
     * subclass of {@code Chromosome} has not existed yet at the time of writing this code, so I
     * couldn't specify it.
     * @implSpec When implementing this method, you can instantiate your custom subclasses such as
     * {@code Mutation}, {@code Chromosome}, and {@code SearchAlgorithm} as usual. No need to use
     * the wildcard type "{@code ?}" yourself. However, depending on the circumstances it might be
     * necessary to add unchecked casts, e.g., when using the fitness function fields of this
     * class.
     */
    @SuppressWarnings("unchecked")
    private GeneticAlgorithm<? extends Chromosome<?>> buildNSGA2() {

        var mutation = new BitFlipMutation();
        var crossover = new SinglePointCrossover();

        ChromosomeGenerator<TestSuiteChromosome> generator =
                new TestSuiteChromosomeGenerator(numberTestCases, mutation, crossover);

        NSGA2 nsga2 = new NSGA2(
                stoppingCondition,
                random,
                50,
                generator,
                (FitnessFunction<TestSuiteChromosome>) sizeFF,
                (FitnessFunction<TestSuiteChromosome>) coverageFF
        );

        var selection = new BinaryTournamentSelection<TestSuiteChromosome>(
                nsga2.nsga2Comparator(),
                random
        );

        return new NSGA2(
                stoppingCondition,
                random,
                50,
                generator,
                (FitnessFunction<TestSuiteChromosome>) sizeFF,
                (FitnessFunction<TestSuiteChromosome>) coverageFF
        );
    }


    /**
     * Returns an instance of the Random Search algorithm to find a solution for the test suite
     * minimization problem. The algorithm is constructed using the fields of this class.
     * <p>
     * Instead of sampling a number of test suites at random and simply returning the best one, we
     * consider all sampled test suites and determine all non-dominated ones, according to the
     * objectives of line coverage and test suite size. This non-dominated set of solutions
     * represents the first Pareto-front.
     * <p>
     * Note: The more samples to take, the longer it takes for Random Search to run. At some
     * point, random search will run considerably longer than NSGA-II because the run time is
     * dominated by fast non-dominated sort, which runs in O(samples * samples).
     *
     * @return the search algorithm
     * @apiNote The return type uses a wildcard type "{@code ?}". This is because your custom
     * subclass of {@code Chromosome} has not existed yet at the time of writing this code, so I
     * couldn't specify it.
     * @implSpec When implementing this method, you can instantiate your custom subclasses such as
     * {@code Mutation}, {@code Chromosome}, and {@code SearchAlgorithm} as usual. No need to use
     * the wildcard type "{@code ?}" yourself. However, depending on the circumstances it might be
     * necessary to add unchecked casts, e.g., when using the fitness function fields of this
     * class.
     */
    @SuppressWarnings("unchecked")
    private GeneticAlgorithm<? extends Chromosome<?>> buildRandomSearch() {

        var mutation = new BitFlipMutation();
        var crossover = new SinglePointCrossover();

        ChromosomeGenerator<TestSuiteChromosome> generator =
                new TestSuiteChromosomeGenerator(numberTestCases, mutation, crossover);

        return new RandomSearch(
                stoppingCondition,
                generator,
                (FitnessFunction<TestSuiteChromosome>) sizeFF,
                (FitnessFunction<TestSuiteChromosome>) coverageFF,
                coverageMatrix,
                numberTestCases,
                numberLines
        );

    }

}
