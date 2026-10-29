package it.unicam.cs.mpgc.rpg125885.model.shift;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    private static final Trait NO_BONUS = new Trait("none", "Nessuno", Stats.NONE, 1.0, 1.0);

    private Shift shift;
    private GameState state;

    @BeforeEach
    void setUp() {
        shift = new Shift("night", List.of(
                new Phase("22:00", PhaseType.CALLS, List.of()),
                new Phase("02:00", PhaseType.BASE, List.of()),
                new Phase("06:00", PhaseType.FINALE, List.of())));
        state = new GameState(new Fleet(List.of()), shift);
    }

    @Test
    void newGameStartsAtFirstPhaseWithCleanState() {
        assertEquals(0, state.phaseIndex());
        assertEquals("22:00", state.currentPhase().time());
        assertEquals(0, state.crisisIndex().value());
        assertTrue(state.callLog().isEmpty());
        assertFalse(state.isAirAmbulanceUsed());
    }

    @Test
    void advancePhaseMovesThroughTheNightAndStopsAtTheEnd() {
        state.advancePhase();
        assertEquals(PhaseType.BASE, state.currentPhase().type());

        state.advancePhase();
        assertEquals(PhaseType.FINALE, state.currentPhase().type());
        assertFalse(state.hasNextPhase());
        assertThrows(IllegalStateException.class, state::advancePhase);
    }

    @Test
    void recordsCallsInOrder() {
        CallRecord first = new CallRecord("gas", CallOutcome.RESOLVED, List.of("APS 1"));
        CallRecord second = new CallRecord("fire", CallOutcome.IGNORED, List.of());

        state.record(first);
        state.record(second);

        assertEquals(List.of(first, second), state.callLog());
        assertThrows(UnsupportedOperationException.class, () -> state.callLog().clear());
    }

    @Test
    void airAmbulanceCanBeMarkedUsed() {
        state.markAirAmbulanceUsed();

        assertTrue(state.isAirAmbulanceUsed());
    }

    @Test
    void restoredGameKeepsItsProgress() {
        CrisisIndex crisis = new CrisisIndex(40);

        GameState restored = new GameState(new Fleet(List.of()), shift, 2, crisis, List.of(), true);

        assertEquals(PhaseType.FINALE, restored.currentPhase().type());
        assertEquals(40, restored.crisisIndex().value());
        assertTrue(restored.isAirAmbulanceUsed());
        assertThrows(IllegalArgumentException.class,
                () -> new GameState(new Fleet(List.of()), shift, 3, crisis, List.of(), false));
    }

    @Test
    void itemRewardAddsToSharedInventory() {
        Gear helmet = new Gear("helmet", "Casco", "", GearSlot.HEAD, Stats.NONE);
        Fleet fleet = new Fleet(List.of());

        Reward.ofItem(helmet, 2).applyTo(fleet);

        assertEquals(2, fleet.inventory().quantityOf(helmet));
    }

    @Test
    void reinforcementRewardCreatesAFreshUnitEachTime() {
        AtomicInteger counter = new AtomicInteger();
        Reward reward = Reward.ofReinforcement("Elisoccorso",
                () -> new Unit("ELI " + counter.incrementAndGet(), "Verdi", new Ambulance(), NO_BONUS));
        Fleet first = new Fleet(List.of());
        Fleet second = new Fleet(List.of());

        reward.applyTo(first);
        reward.applyTo(second);

        assertEquals("ELI 1", first.units().get(0).name());
        assertEquals("ELI 2", second.units().get(0).name());
    }

    @Test
    void scenarioRejectsInvalidData() {
        HazardTemplate hazard = new HazardTemplate("h", "Pericolo", 50, 5, Map.of(),
                false, 5, 5, "steady", 10);

        assertThrows(IllegalArgumentException.class, () -> scenario(List.of(), 5));
        assertThrows(IllegalArgumentException.class, () -> scenario(List.of(hazard), 0));
    }

    private static Scenario scenario(List<HazardTemplate> hazards, int rounds) {
        return new Scenario("s", "Titolo", "Zona", "Testo", hazards, rounds, Requirements.NONE,
                Optional.empty(), "ok", "ko", 5, 10, 1, Optional.empty());
    }
}
