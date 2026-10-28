package it.unicam.cs.mpgc.rpg125885.model.fleet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class StatsTest {

    private static final Stats BASE = new Stats(100, 50, 5, 4, 3, 2, 6);
    private static final Stats BONUS = new Stats(10, 5, 1, 1, 1, 1, 1);

    @Test
    void plusSumsEveryStat() {
        assertEquals(new Stats(110, 55, 6, 5, 4, 3, 7), BASE.plus(BONUS));
    }

    @Test
    void plusLeavesOperandsUnchanged() {
        Stats sum = BASE.plus(BONUS);

        assertNotSame(BASE, sum);
        assertEquals(new Stats(100, 50, 5, 4, 3, 2, 6), BASE);
    }

    @Test
    void noneIsNeutralElement() {
        assertEquals(BASE, BASE.plus(Stats.NONE));
        assertEquals(BASE, Stats.NONE.plus(BASE));
    }

    @Test
    void plusIsCommutative() {
        assertEquals(BASE.plus(BONUS), BONUS.plus(BASE));
    }
}