package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import de.uni_passau.fim.se2.sbse.suite_minimisation.fitness_functions.MaximizingFitnessFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @TempDir
    File tempDir;

    @Test
    void parseCoverageMatrixParsesCorrectly() throws Exception {
        File file = new File(tempDir, "matrix.txt");

        Files.writeString(file.toPath(),
                "[\n" +
                        "[true, false]\n" +
                        "[false, true]\n" +
                        "]");

        boolean[][] matrix = Utils.parseCoverageMatrix(file);

        assertEquals(2, matrix.length);
        assertEquals(2, matrix[0].length);

        assertTrue(matrix[0][0]);
        assertFalse(matrix[0][1]);
        assertFalse(matrix[1][0]);
        assertTrue(matrix[1][1]);
    }

    @Test
    void computeHyperVolumeReturnsZeroForNullFront() {
        double hv = Utils.computeHyperVolume(
                null,
                constantMaxFitness(0.0),
                constantMaxFitness(0.0),
                0.0,
                1.0
        );

        assertEquals(0.0, hv);
    }

    @Test
    void computeHyperVolumeReturnsZeroForEmptyFront() {
        double hv = Utils.computeHyperVolume(
                List.of(),
                constantMaxFitness(0.0),
                constantMaxFitness(0.0),
                0.0,
                1.0
        );

        assertEquals(0.0, hv);
    }

    @Test
    void computeHyperVolumeSimpleCase() {
        Object a = new Object();
        Object b = new Object();

        MaximizingFitnessFunction<Object> f1 =
                o -> (o == a ? 0.2 : 0.6);

        MaximizingFitnessFunction<Object> f2 =
                o -> (o == a ? 0.8 : 0.4);

        double hv = Utils.computeHyperVolume(
                List.of(a, b),
                f1,
                f2,
                0.0,
                1.0
        );

        assertTrue(hv > 0.0);
        assertTrue(hv <= 1.0);
    }

    private static MaximizingFitnessFunction<Object> constantMaxFitness(double value) {
        return o -> value;
    }
}
