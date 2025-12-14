# Test Suite Minimisation using Search-Based Software Engineering

## Overview

This project addresses the **test suite minimisation problem** using **search-based optimisation techniques**.  
Given a test suite and a coverage matrix, the objective is to find **small subsets of tests** that:

- **Maximise line coverage**
- **Minimise test suite size**

The problem is formulated as a **bi-objective optimisation task** and solved using:

- **Random Search**
- **NSGA-II (Non-dominated Sorting Genetic Algorithm II)**

Both algorithms return a **Pareto-optimal front** representing trade-offs between coverage and suite size.

---

## Algorithms

### Random Search

`RandomSearch` explores the solution space by sampling test suites using multiple strategies:

- Full test suite (coverage anchor)
- Greedy coverage prefixes
- Best-K single-test candidates
- Stratified random sampling biased towards smaller suites

Each sampled solution is evaluated **exactly once**, and only **non-dominated solutions** are retained in the Pareto front.

---

### NSGA-II

`NSGA2` is a population-based evolutionary algorithm implementing:

- Fast non-dominated sorting
- Crowding distance assignment
- Binary tournament selection
- Single-point crossover
- Bit-flip mutation

The algorithm evolves a population until the stopping condition is met and returns the **first Pareto front** of the final population.

---

## Fitness Functions

Two **normalised fitness functions** are used: f_size = selected_tests / total_tests

### Line Coverage (Maximising)

f_coverage = covered_lines / total_lines


Both fitness values are guaranteed to be in the range **[0, 1]**.

---

## Stopping Conditions

The search budget is controlled via:

- `MaxFitnessEvaluations`

Each fitness evaluation is explicitly counted, ensuring fair comparison between algorithms and strict adherence to the allocated budget.

---

## Coverage Measurement

Line coverage is measured using **JaCoCo** via the `CoverageTracker` utility:

- Each test case is executed independently
- Coverage is collected per test case
- A **coverage matrix** is constructed where:
  - Rows correspond to test cases
  - Columns correspond to executable source lines

---

## Testing

The project includes extensive **JUnit 5 test suites** designed to achieve:

- High **line coverage**
- High **branch coverage**
- Strong **mutation score**

Testing principles:

- Deterministic randomness where required
- Minimal dummy chromosomes for isolation
- No flaky or non-terminating tests
- CI-safe execution

Tested components include:

- Search algorithms (Random Search, NSGA-II)
- Selection operators
- Mutation operators
- Fitness functions
- Utility classes

---

## Build and Run

### Requirements

- Java 17 or newer
- Maven
- JUnit 5

### Run Tests

```bash
mvn test
