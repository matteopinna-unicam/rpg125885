package it.unicam.cs.mpgc.rpg125885.model.fleet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GaugeTest {

    @Test
    void startsFull() {
        Gauge gauge = new Gauge(50);

        assertEquals(50, gauge.current());
        assertEquals(50, gauge.max());
        assertFalse(gauge.isEmpty());
    }

    @Test
    void decreaseReturnsAppliedAmount() {
        Gauge gauge = new Gauge(50);

        assertEquals(20, gauge.decrease(20));
        assertEquals(30, gauge.current());
    }

    @Test
    void decreaseStopsAtZero() {
        Gauge gauge = new Gauge(50, 10);

        assertEquals(10, gauge.decrease(25));
        assertEquals(0, gauge.current());
        assertTrue(gauge.isEmpty());
    }

    @Test
    void increaseReturnsAppliedAmount() {
        Gauge gauge = new Gauge(50, 10);

        assertEquals(15, gauge.increase(15));
        assertEquals(25, gauge.current());
    }

    @Test
    void increaseStopsAtMax() {
        Gauge gauge = new Gauge(50, 45);

        assertEquals(5, gauge.increase(30));
        assertEquals(50, gauge.current());
    }

    @Test
    void fillRestoresMax() {
        Gauge gauge = new Gauge(50, 0);

        gauge.fill();

        assertEquals(50, gauge.current());
    }

    @Test
    void fillPercentIsProportional() {
        assertEquals(25.0, new Gauge(80, 20).fillPercent());
        assertEquals(100.0, new Gauge(80).fillPercent());
        assertEquals(0.0, new Gauge(80, 0).fillPercent());
    }

    @Test
    void rejectsInvalidConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new Gauge(0));
        assertThrows(IllegalArgumentException.class, () -> new Gauge(10, -1));
        assertThrows(IllegalArgumentException.class, () -> new Gauge(10, 11));
    }

    @Test
    void rejectsNegativeAmounts() {
        Gauge gauge = new Gauge(10);

        assertThrows(IllegalArgumentException.class, () -> gauge.decrease(-1));
        assertThrows(IllegalArgumentException.class, () -> gauge.increase(-1));
    }
}
