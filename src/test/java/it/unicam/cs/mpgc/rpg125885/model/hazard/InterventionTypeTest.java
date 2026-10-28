package it.unicam.cs.mpgc.rpg125885.model.hazard;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterventionTypeTest {

    private static final Stats STATS = new Stats(100, 50, 5, 4, 3, 2, 6);

    @Test
    void eachTypeSelectsItsOwnStat() {
        assertEquals(5, InterventionType.TECHNICAL.relevantStat(STATS));
        assertEquals(4, InterventionType.MEDICAL.relevantStat(STATS));
        assertEquals(3, InterventionType.COORDINATION.relevantStat(STATS));
    }
}
