package it.unicam.cs.mpgc.rpg125885.model.shift;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrisisIndexTest {

    @Test
    void startsAtZeroAndIsNotCritical() {
        CrisisIndex index = new CrisisIndex();

        assertEquals(0, index.value());
        assertFalse(index.isCritical());
    }

    @Test
    void raiseAddsAndStopsAtMaximum() {
        CrisisIndex index = new CrisisIndex(90);

        index.raise(5);
        assertEquals(95, index.value());

        index.raise(20);
        assertEquals(CrisisIndex.MAX, index.value());
        assertTrue(index.isCritical());
    }

    @Test
    void lowerSubtractsAndStopsAtZero() {
        CrisisIndex index = new CrisisIndex(3);

        index.lower(5);

        assertEquals(0, index.value());
    }

    @Test
    void rejectsInvalidValuesAndNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new CrisisIndex(-1));
        assertThrows(IllegalArgumentException.class, () -> new CrisisIndex(101));
        assertThrows(IllegalArgumentException.class, () -> new CrisisIndex().raise(-1));
        assertThrows(IllegalArgumentException.class, () -> new CrisisIndex().lower(-1));
    }
}
