package it.unicam.cs.mpgc.rpg125885.engine.progression;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionServiceTest {

    private static final Trait NO_BONUS = new Trait("none", "Nessuno", Stats.NONE, 1.0, 1.0);
    private static final Trait ROOKIE = new Trait("rookie", "Novellino", Stats.NONE, 1.0, 1.2);

    private final ProgressionService service = new ProgressionService(new LinearLevelCurve());

    private Unit engine;
    private Unit ambulance;

    @BeforeEach
    void setUp() {
        engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);
        ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), NO_BONUS);
    }

    @Test
    void curveRequiresFiftyTimesTheCurrentLevel() {
        LevelCurve curve = new LinearLevelCurve();

        assertEquals(50, curve.experienceToAdvanceFrom(1));
        assertEquals(100, curve.experienceToAdvanceFrom(2));
        assertThrows(IllegalArgumentException.class, () -> new LinearLevelCurve(0));
    }

    @Test
    void experienceIsSplitEvenlyBetweenParticipants() {
        List<LevelUp> levelUps = service.awardExperience(List.of(engine, ambulance), 40);

        assertEquals(20, engine.experience());
        assertEquals(20, ambulance.experience());
        assertTrue(levelUps.isEmpty());
    }

    @Test
    void traitMultiplierIncreasesTheShare() {
        Unit rookie = new Unit("APS 2", "Verdi", new FireEngine(), ROOKIE);

        service.awardExperience(List.of(rookie, ambulance), 40);

        assertEquals(24, rookie.experience());
        assertEquals(20, ambulance.experience());
    }

    @Test
    void unitLevelsUpWhenReachingTheThresholdAndRefillsGauges() {
        engine.endurance().decrease(100);

        List<LevelUp> levelUps = service.awardExperience(List.of(engine), 50);

        assertEquals(List.of(new LevelUp(engine, 2)), levelUps);
        assertEquals(2, engine.level());
        assertEquals(engine.endurance().max(), engine.endurance().current());
    }

    @Test
    void unitCanGainSeveralLevelsAtOnce() {
        List<LevelUp> levelUps = service.awardExperience(List.of(engine), 120);

        // 120 ≥ 50 (livello 2) e ≥ 100 (livello 3), ma < 150
        assertEquals(List.of(new LevelUp(engine, 2), new LevelUp(engine, 3)), levelUps);
        assertEquals(3, engine.level());
    }

    @Test
    void exhaustedUnitsReceiveNothing() {
        ambulance.endurance().decrease(1000);

        service.awardExperience(List.of(engine, ambulance), 100);

        assertEquals(100, engine.experience());
        assertEquals(0, ambulance.experience());
    }

    @Test
    void noActiveParticipantsMeansNoAward() {
        engine.endurance().decrease(1000);

        assertTrue(service.awardExperience(List.of(engine), 100).isEmpty());
        assertEquals(0, engine.experience());
    }
}
