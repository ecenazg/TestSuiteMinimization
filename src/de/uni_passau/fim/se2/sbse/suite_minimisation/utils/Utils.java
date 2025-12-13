package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.FitnessFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Utils {

    /**
     * Parses a coverage matrix from a string.
     *
     * @param matrixFile the string representation of the coverage matrix
     * @return the parsed coverage matrix
     * @throws IOException if the supplied file could not be read
     */
    public static boolean[][] parseCoverageMatrix(File matrixFile) throws IOException {
        List<String> matrix = Files.readAllLines(matrixFile.toPath());

        // Remove outer brackets
        matrix.removeFirst();
        matrix.removeLast();

        // Initialize 2D boolean array
        boolean[][] parsedMatrix = new boolean[matrix.size()][];

        for (int i = 0; i < matrix.size(); i++) {
            // Remove any remaining brackets and split by comma
            String[] values = matrix.get(i).replace("[", "").replace("]", "").split(", ");
            parsedMatrix[i] = new boolean[values.length];
            for (int j = 0; j < values.length; j++) {
                // Parse "true" or "false" as boolean
                parsedMatrix[i][j] = Boolean.parseBoolean(values[j]);
            }
        }

        return parsedMatrix;
    }

    /**
     * Computes the hyper-volume of the given Pareto {@code front}, using the given fitness
     * functions {@code f1} and {@code f2}, and {@code r1} and {@code r2} as coordinates of the
     * reference point. The fitness functions must produce normalized results between 0 and 1.
     *
     * @param front the front for which to compute the hyper-volume
     * @param f1    the first fitness function
     * @param f2    the second fitness function
     * @param r1    reference coordinate for {@code f1}
     * @param r2    reference coordinate for {@code f2}
     * @return the hyper volume of the given front w.r.t. the reference point
     * @apiNote The function uses ugly raw types because it seems the type system doesn't want to
     * let me express this in any other way :(
     * @implSpec In the implementation of this method you might need to cast or use raw types, too.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static double computeHyperVolume(
            final List front,
            final FitnessFunction f1,
            final FitnessFunction f2,
            final double r1,
            final double r2)
            throws IllegalArgumentException {

        if (front == null || front.isEmpty()) {
            return 0.0;
        }

        // Collect points (f1, f2)
        final int n = front.size();
        double[][] points = new double[n][2];

        for (int i = 0; i < n; i++) {
            Object c = front.get(i);
            points[i][0] = f1.applyAsDouble(c); // coverage
            points[i][1] = f2.applyAsDouble(c); // size
        }

        // Sort by f1 (coverage) ascending
        Arrays.sort(points, Comparator.comparingDouble(p -> p[0]));

        double hyperVolume = 0.0;
        double prevF1 = r1;

        // Sweep left → right
        for (int i = 0; i < n; i++) {
            double currF1 = points[i][0];
            double currF2 = points[i][1];

            double width = currF1 - prevF1;
            double height = r2 - currF2;

            if (width > 0 && height > 0) {
                hyperVolume += width * height;
            }

            prevF1 = currF1;
        }

        return hyperVolume;
    }

}
