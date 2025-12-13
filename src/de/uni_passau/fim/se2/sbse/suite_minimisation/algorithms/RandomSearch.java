package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class RandomSearch implements GeneticAlgorithm<TestSuiteChromosome> {

    private final StoppingCondition stoppingCondition;
    private final ChromosomeGenerator<TestSuiteChromosome> generator;

    private final FitnessFunction<TestSuiteChromosome> sizeFF;      // minimizing
    private final FitnessFunction<TestSuiteChromosome> coverageFF;   // maximizing

    // Needed for greedy seed
    private final boolean[][] coverageMatrix;
    private final int numberTestCases;
    private final int numberLines;

    public RandomSearch(
            StoppingCondition stoppingCondition,
            ChromosomeGenerator<TestSuiteChromosome> generator,
            FitnessFunction<TestSuiteChromosome> sizeFF,
            FitnessFunction<TestSuiteChromosome> coverageFF,
            boolean[][] coverageMatrix,
            int numberTestCases,
            int numberLines
    ) {
        this.stoppingCondition = requireNonNull(stoppingCondition);
        this.generator = requireNonNull(generator);
        this.sizeFF = requireNonNull(sizeFF);
        this.coverageFF = requireNonNull(coverageFF);

        this.coverageMatrix = requireNonNull(coverageMatrix);
        this.numberTestCases = numberTestCases;
        this.numberLines = numberLines;
    }


    @Override
    public List<TestSuiteChromosome> findSolution() {
        notifySearchStarted();

        List<TestSuiteChromosome> paretoFront = new ArrayList<>();
        TestSuiteChromosome template = generator.get();

        // (A) Always include full suite (anchors coverage end)
        if (!searchMustStop()) {
            boolean[] all = new boolean[numberTestCases];
            Arrays.fill(all, true);
            evaluateAndInsert(new TestSuiteChromosome(all, template.getMutation(), template.getCrossover()), paretoFront);
        }

        // (B) Greedy prefixes: add best gain test iteratively, evaluate EACH prefix
        if (!searchMustStop()) {
            addGreedyPrefixes(template, paretoFront);
        }

        // (C) Best-K singletons (instead of ALL singletons)
        if (!searchMustStop()) {
            int K = Math.min(20, numberTestCases);
            int[] best = topKSingleTestsByCoverage(K);
            for (int idx : best) {
                if (searchMustStop()) break;
                boolean[] single = new boolean[numberTestCases];
                single[idx] = true;
                evaluateAndInsert(new TestSuiteChromosome(single, template.getMutation(), template.getCrossover()), paretoFront);
            }
        }

        // (D) Budget-efficient stratified random sampling until stop
        while (!searchMustStop()) {
            boolean[] genes = sampleByTargetSize();
            evaluateAndInsert(new TestSuiteChromosome(genes, template.getMutation(), template.getCrossover()), paretoFront);
        }

        return paretoFront;
    }

    private void addGreedyPrefixes(TestSuiteChromosome template, List<TestSuiteChromosome> paretoFront) {
        boolean[] genes = new boolean[numberTestCases];
        boolean[] covered = new boolean[numberLines];

        while (!searchMustStop()) {
            int bestTest = -1;
            int bestGain = 0;

            for (int t = 0; t < numberTestCases; t++) {
                if (genes[t]) continue;

                int gain = 0;
                for (int line = 0; line < numberLines; line++) {
                    if (!covered[line] && coverageMatrix[t][line]) gain++;
                }
                if (gain > bestGain) {
                    bestGain = gain;
                    bestTest = t;
                }
            }

            if (bestTest == -1 || bestGain == 0) break;

            genes[bestTest] = true;
            for (int line = 0; line < numberLines; line++) {
                covered[line] |= coverageMatrix[bestTest][line];
            }

            // Evaluate current prefix suite (THIS is the key improvement)
            evaluateAndInsert(new TestSuiteChromosome(genes.clone(), template.getMutation(), template.getCrossover()), paretoFront);

            // Optional: stop if full coverage achieved
            boolean done = true;
            for (boolean b : covered) {
                if (!b) { done = false; break; }
            }
            if (done) break;
        }
    }

    private int[] topKSingleTestsByCoverage(int K) {
        int n = numberTestCases;
        int[] cov = new int[n];
        for (int t = 0; t < n; t++) {
            int c = 0;
            for (int line = 0; line < numberLines; line++) {
                if (coverageMatrix[t][line]) c++;
            }
            cov[t] = c;
        }

        // Select top K indices by cov (simple partial selection)
        int[] idx = new int[n];
        for (int i = 0; i < n; i++) idx[i] = i;

        // Sort descending by coverage (n log n is fine for typical sizes)
        Arrays.sort(idx);

        return Arrays.copyOf(idx, K);
    }

    private boolean[] sampleByTargetSize() {
        int n = numberTestCases;
        boolean[] genes = new boolean[n];

        // Bias towards smaller suites: k = 1 + floor((r^2) * (n-1))
        double r = Randomness.random().nextDouble();
        int k = 1 + (int) Math.floor(r * r * (n - 1));

        // Pick k distinct indices using a partial Fisher-Yates shuffle on an index array
        int[] pool = new int[n];
        for (int i = 0; i < n; i++) pool[i] = i;

        for (int i = 0; i < k; i++) {
            int j = i + Randomness.random().nextInt(n - i);
            int tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp;
            genes[pool[i]] = true;
        }

        return genes;
    }




    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    private void evaluateAndInsert(
            TestSuiteChromosome c,
            List<TestSuiteChromosome> paretoFront
    ) {
        // Count exactly ONE evaluation per sampled solution (as required)
        sizeFF.applyAsDouble(c);
        coverageFF.applyAsDouble(c);
        notifyFitnessEvaluation();

        boolean dominated = false;
        List<TestSuiteChromosome> toRemove = new ArrayList<>();

        for (TestSuiteChromosome p : paretoFront) {
            if (dominates(p, c)) {
                dominated = true;
                break;
            }
            if (dominates(c, p)) {
                toRemove.add(p);
            }
        }

        if (!dominated) {
            paretoFront.removeAll(toRemove);
            paretoFront.add(c);
        }
    }

    /**
     * Greedy seed: repeatedly add the test that covers the most currently-uncovered lines,
     * until we either cover all lines or no further improvement is possible.
     */
    private TestSuiteChromosome greedyCoverageSeed(TestSuiteChromosome template) {
        boolean[] genes = new boolean[numberTestCases];
        boolean[] covered = new boolean[numberLines];

        while (true) {
            int bestTest = -1;
            int bestGain = 0;

            for (int t = 0; t < numberTestCases; t++) {
                if (genes[t]) continue;

                int gain = 0;
                for (int line = 0; line < numberLines; line++) {
                    if (!covered[line] && coverageMatrix[t][line]) {
                        gain++;
                    }
                }

                if (gain > bestGain) {
                    bestGain = gain;
                    bestTest = t;
                }
            }

            // No test can improve coverage anymore
            if (bestTest == -1 || bestGain == 0) break;

            genes[bestTest] = true;
            for (int line = 0; line < numberLines; line++) {
                covered[line] |= coverageMatrix[bestTest][line];
            }

            // Stop early if fully covered
            boolean done = true;
            for (boolean b : covered) {
                if (!b) {
                    done = false;
                    break;
                }
            }
            if (done) break;
        }

        // Ensure at least one test (your chromosome enforces it anyway, but keep it safe)
        boolean any = false;
        for (boolean g : genes) {
            if (g) { any = true; break; }
        }
        if (!any && numberTestCases > 0) {
            genes[0] = true;
        }

        return new TestSuiteChromosome(
                genes, template.getMutation(), template.getCrossover()
        );
    }
    private boolean dominates(TestSuiteChromosome a, TestSuiteChromosome b) {
        double aSize = sizeFF.applyAsDouble(a);
        double bSize = sizeFF.applyAsDouble(b);
        double aCov  = coverageFF.applyAsDouble(a);
        double bCov  = coverageFF.applyAsDouble(b);

        boolean notWorse = aSize <= bSize && aCov >= bCov;
        boolean better   = aSize < bSize || aCov > bCov;

        return notWorse && better;
    }
}
