package de.uni_passau.fim.se2.sbse.suite_minimisation.utils;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PairTest {

    @Test
    void pairStoresElementsCorrectly() {
        Pair<String> pair = Pair.of("A", "B");

        assertEquals("A", pair.getFst());
        assertEquals("B", pair.getSnd());
        assertEquals(2, pair.size());
    }

    @Test
    void pairEqualsAndHashCode() {
        Pair<Integer> p1 = Pair.of(1, 2);
        Pair<Integer> p2 = Pair.of(1, 2);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void pairMapSingleFunction() {
        Pair<Integer> pair = Pair.of(1, 2);

        Pair<String> mapped = pair.map(Object::toString);

        assertEquals("1", mapped.getFst());
        assertEquals("2", mapped.getSnd());
    }

    @Test
    void pairMapTwoFunctions() {
        Pair<Integer> pair = Pair.of(2, 3);

        Pair<Integer> mapped = pair.map(x -> x * 2, x -> x * 3);

        assertEquals(4, mapped.getFst());
        assertEquals(9, mapped.getSnd());
    }

    @Test
    void pairReduceWithBiFunction() {
        Pair<Integer> pair = Pair.of(2, 3);

        int sum = pair.reduce(Integer::sum);

        assertEquals(5, sum);
    }

    @Test
    void pairIteratorWorksAndThrows() {
        Pair<String> pair = Pair.of("A", "B");

        Iterator<String> it = pair.iterator();

        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());

        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void pairGenerateUsesSupplierTwice() {
        AtomicInteger counter = new AtomicInteger();

        Pair<Integer> pair = Pair.generate(counter::incrementAndGet);

        assertEquals(1, pair.getFst());
        assertEquals(2, pair.getSnd());
    }

    @Test
    void pairToStringContainsValues() {
        Pair<String> pair = Pair.of("X", "Y");

        String s = pair.toString();

        assertTrue(s.contains("X"));
        assertTrue(s.contains("Y"));
    }
}
