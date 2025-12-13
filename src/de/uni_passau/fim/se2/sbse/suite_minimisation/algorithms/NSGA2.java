package de.uni_passau.fim.se2.sbse.suite_minimisation.algorithms;

import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.ChromosomeGenerator;
import de.uni_passau.fim.se2.sbse.suite_minimisation.chromosomes.TestSuiteChromosome;
import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.sbse.suite_minimisation.selection.BinaryTournamentSelection;
import de.uni_passau.fim.se2.sbse.suite_minimisation.selection.Selection;
import de.uni_passau.fim.se2.sbse.suite_minimisation.stopping_conditions.StoppingCondition;
import de.uni_passau.fim.se2.sbse.suite_minimisation.utils.Pair;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class NSGA2 implements GeneticAlgorithm<TestSuiteChromosome> {

    private final StoppingCondition stoppingCondition;
    private final Random random;

    private final int populationSize;

    private final ChromosomeGenerator<TestSuiteChromosome> generator;
    private Selection<TestSuiteChromosome> selection;

    private final FitnessFunction<TestSuiteChromosome> sizeFF;      // minimizing
    private final FitnessFunction<TestSuiteChromosome> coverageFF;   // maximizing

    // Metadata used by tournament comparator
    private final Map<TestSuiteChromosome, Integer> rank = new IdentityHashMap<>();
    private final Map<TestSuiteChromosome, Double> crowding = new IdentityHashMap<>();

    public NSGA2(
            StoppingCondition stoppingCondition,
            Random random,
            int populationSize,
            ChromosomeGenerator<TestSuiteChromosome> generator,
            FitnessFunction<TestSuiteChromosome> sizeFF,
            FitnessFunction<TestSuiteChromosome> coverageFF
    ) {
        this.stoppingCondition = requireNonNull(stoppingCondition);
        this.random = requireNonNull(random);
        this.populationSize = populationSize;
        this.generator = requireNonNull(generator);
        this.sizeFF = requireNonNull(sizeFF);
        this.coverageFF = requireNonNull(coverageFF);
    }

    @Override
    public List<TestSuiteChromosome> findSolution() {
        notifySearchStarted();

        // 1) init population
        List<TestSuiteChromosome> population = new ArrayList<>(populationSize);
        while (population.size() < populationSize && !searchMustStop()) {
            TestSuiteChromosome c = generator.get();
            evaluate(c);
            population.add(c);
        }

        // 2) generations
        while (!searchMustStop()) {

            // update rank + crowding for parent selection
            List<List<TestSuiteChromosome>> fronts = fastNonDominatedSort(population);
            assignRankAndCrowding(fronts);

            this.selection = new BinaryTournamentSelection<>(
                    (a, b) -> {
                        int ra = rank.get(a);
                        int rb = rank.get(b);
                        if (ra != rb) return Integer.compare(rb, ra);

                        double ca = crowding.get(a);
                        double cb = crowding.get(b);
                        return Double.compare(ca, cb);
                    },
                    random
            );


            // create offspring of size N
            List<TestSuiteChromosome> offspring = new ArrayList<>(populationSize);
            while (offspring.size() < populationSize && !searchMustStop()) {
                TestSuiteChromosome p1 = selection.apply(population);
                TestSuiteChromosome p2 = selection.apply(population);

                Pair<TestSuiteChromosome> children = p1.crossover(p2);
                TestSuiteChromosome c1 = children.getFst().mutate();
                TestSuiteChromosome c2 = children.getSnd().mutate();

                evaluate(c1);
                offspring.add(c1);
                if (offspring.size() < populationSize) {
                    evaluate(c2);
                    offspring.add(c2);
                }
            }

            // combine and select next population
            List<TestSuiteChromosome> combined = new ArrayList<>(population.size() + offspring.size());
            combined.addAll(population);
            combined.addAll(offspring);

            List<List<TestSuiteChromosome>> combinedFronts = fastNonDominatedSort(combined);
            assignRankAndCrowding(combinedFronts);

            population = selectNextPopulation(combinedFronts);
        }

        // return first Pareto front of final population
        List<List<TestSuiteChromosome>> finalFronts = fastNonDominatedSort(population);
        return finalFronts.isEmpty() ? List.of() : finalFronts.get(0);
    }

    @Override
    public StoppingCondition getStoppingCondition() {
        return stoppingCondition;
    }

    // ---------- evaluation ----------
    private void evaluate(TestSuiteChromosome c) {
        // one evaluation budget per chromosome (even though we compute two objectives)
        sizeFF.applyAsDouble(c);
        coverageFF.applyAsDouble(c);
        notifyFitnessEvaluation();
    }

    // ---------- dominance ----------
    private boolean dominates(TestSuiteChromosome a, TestSuiteChromosome b) {
        double aSize = sizeFF.applyAsDouble(a);
        double bSize = sizeFF.applyAsDouble(b);

        double aCov = coverageFF.applyAsDouble(a);
        double bCov = coverageFF.applyAsDouble(b);

        boolean notWorseAll = (aSize <= bSize) && (aCov >= bCov);
        boolean betterOne = (aSize < bSize) || (aCov > bCov);
        return notWorseAll && betterOne;
    }

    // ---------- NSGA-II: fast non-dominated sort ----------
    private List<List<TestSuiteChromosome>> fastNonDominatedSort(List<TestSuiteChromosome> pop) {
        Map<TestSuiteChromosome, List<TestSuiteChromosome>> S = new IdentityHashMap<>();
        Map<TestSuiteChromosome, Integer> n = new IdentityHashMap<>();

        List<List<TestSuiteChromosome>> fronts = new ArrayList<>();
        List<TestSuiteChromosome> first = new ArrayList<>();

        for (TestSuiteChromosome p : pop) {
            S.put(p, new ArrayList<>());
            n.put(p, 0);

            for (TestSuiteChromosome q : pop) {
                if (p == q) continue;
                if (dominates(p, q)) {
                    S.get(p).add(q);
                } else if (dominates(q, p)) {
                    n.put(p, n.get(p) + 1);
                }
            }

            if (n.get(p) == 0) first.add(p);
        }

        fronts.add(first);

        int i = 0;
        while (i < fronts.size() && !fronts.get(i).isEmpty()) {
            List<TestSuiteChromosome> next = new ArrayList<>();
            for (TestSuiteChromosome p : fronts.get(i)) {
                for (TestSuiteChromosome q : S.get(p)) {
                    n.put(q, n.get(q) - 1);
                    if (n.get(q) == 0) next.add(q);
                }
            }
            i++;
            if (!next.isEmpty()) fronts.add(next);
        }

        return fronts;
    }

    // ---------- NSGA-II: crowding distance ----------
    private void assignRankAndCrowding(List<List<TestSuiteChromosome>> fronts) {
        rank.clear();
        crowding.clear();

        for (int i = 0; i < fronts.size(); i++) {
            List<TestSuiteChromosome> front = fronts.get(i);
            for (TestSuiteChromosome c : front) rank.put(c, i);
            computeCrowdingDistance(front);
        }
    }

    private void computeCrowdingDistance(List<TestSuiteChromosome> front) {
        int n = front.size();
        if (n == 0) return;

        for (TestSuiteChromosome c : front) crowding.put(c, 0.0);
        if (n <= 2) {
            for (TestSuiteChromosome c : front) crowding.put(c, Double.POSITIVE_INFINITY);
            return;
        }

        // Objective 1: size (min)
        front.sort(Comparator.comparingDouble(c -> sizeFF.applyAsDouble(c)));
        crowding.put(front.get(0), Double.POSITIVE_INFINITY);
        crowding.put(front.get(n - 1), Double.POSITIVE_INFINITY);

        double min1 = sizeFF.applyAsDouble(front.get(0));
        double max1 = sizeFF.applyAsDouble(front.get(n - 1));
        double range1 = max1 - min1;

        if (range1 > 0) {
            for (int i = 1; i < n - 1; i++) {
                double prev = sizeFF.applyAsDouble(front.get(i - 1));
                double next = sizeFF.applyAsDouble(front.get(i + 1));
                crowding.put(front.get(i), crowding.get(front.get(i)) + (next - prev) / range1);
            }
        }

        // Objective 2: coverage (max) -> sort ascending still fine for distance, we use raw values
        front.sort(Comparator.comparingDouble(c -> coverageFF.applyAsDouble(c)));
        crowding.put(front.get(0), Double.POSITIVE_INFINITY);
        crowding.put(front.get(n - 1), Double.POSITIVE_INFINITY);

        double min2 = coverageFF.applyAsDouble(front.get(0));
        double max2 = coverageFF.applyAsDouble(front.get(n - 1));
        double range2 = max2 - min2;

        if (range2 > 0) {
            for (int i = 1; i < n - 1; i++) {
                double prev = coverageFF.applyAsDouble(front.get(i - 1));
                double next = coverageFF.applyAsDouble(front.get(i + 1));
                crowding.put(front.get(i), crowding.get(front.get(i)) + (next - prev) / range2);
            }
        }
    }

    // ---------- survivor selection ----------
    private List<TestSuiteChromosome> selectNextPopulation(List<List<TestSuiteChromosome>> fronts) {
        List<TestSuiteChromosome> next = new ArrayList<>(populationSize);

        for (List<TestSuiteChromosome> front : fronts) {
            if (next.size() + front.size() <= populationSize) {
                next.addAll(front);
            } else {
                // fill remaining by descending crowding distance
                front.sort((a, b) -> Double.compare(
                        crowding.getOrDefault(b, 0.0),
                        crowding.getOrDefault(a, 0.0)
                ));
                int remaining = populationSize - next.size();
                next.addAll(front.subList(0, remaining));
                break;
            }
        }

        return next;
    }

    // Expose comparator logic for tournament selection if you want to build it from here
    public Comparator<TestSuiteChromosome> nsga2Comparator() {
        return (a, b) -> {
            int ra = rank.getOrDefault(a, Integer.MAX_VALUE);
            int rb = rank.getOrDefault(b, Integer.MAX_VALUE);
            if (ra != rb) return Integer.compare(rb, ra); // higher result => a better (smaller rank)
            double ca = crowding.getOrDefault(a, 0.0);
            double cb = crowding.getOrDefault(b, 0.0);
            return Double.compare(ca, cb); // higher crowding => better
        };
    }
}
