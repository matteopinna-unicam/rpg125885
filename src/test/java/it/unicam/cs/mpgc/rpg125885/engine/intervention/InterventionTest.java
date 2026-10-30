package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.engine.rules.ScriptedRandom;
import it.unicam.cs.mpgc.rpg125885.engine.rules.StandardEffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.HazardResolved;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.InterventionEnded;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.RoundStarted;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.StressInflicted;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.TurnStarted;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.UnitExhausted;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.item.Inventory;
import it.unicam.cs.mpgc.rpg125885.model.item.ToolSupply;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.NO_BONUS;
import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.hazard;
import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.intervention;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterventionTest {

    private Unit engine;    // APS: Tecnica 14, Prontezza 5, Resistenza 120
    private Unit ambulance; // Ambulanza: Medicina 12, Prontezza 7, Resistenza 90

    @BeforeEach
    void setUp() {
        engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);
        ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), NO_BONUS);
    }

    /** Fa giocare a ogni unità l'azione base finché l'intervento non finisce. */
    private static void playUntilEnd(Intervention intervention, Hazard target) {
        while (intervention.status() == InterventionStatus.ONGOING) {
            Unit unit = intervention.currentUnit().orElseThrow();
            intervention.submit(new InterveneAction(unit, target));
        }
    }

    @Test
    void firstTurnGoesToTheMostReadyUnit() {
        Intervention intervention = intervention(List.of(engine, ambulance), List.of(hazard(500, 5, "steady")), 3);

        List<InterventionEvent> events = intervention.start();

        assertEquals(List.of(new RoundStarted(1), new TurnStarted(ambulance)), events);
        assertEquals(Optional.of(ambulance), intervention.currentUnit());
    }

    @Test
    void unitsActBeforeHazardsOnEqualReadinessThenTheHazardActs() {
        Hazard hazard = hazard(500, 5, "steady");
        Intervention intervention = intervention(List.of(engine, ambulance), List.of(hazard), 3);
        intervention.start();

        intervention.submit(new TakeCoverAction(ambulance));
        List<InterventionEvent> afterEngine = intervention.submit(new TakeCoverAction(engine));

        // Prontezza: Ambulanza 7, APS 5 e pericolo 5 → a parità l'unità precede il pericolo
        List<InterventionEvent> turns = intervention.events().stream()
                .filter(event -> event instanceof TurnStarted).limit(3).toList();
        assertEquals(List.of(new TurnStarted(ambulance), new TurnStarted(engine), new TurnStarted(hazard)), turns);
        assertTrue(afterEngine.contains(new TurnStarted(hazard)));
    }

    @Test
    void succeedsWhenAllHazardsAreResolved() {
        Hazard hazard = hazard(26, 5, "steady");
        Intervention intervention = intervention(List.of(engine, ambulance), List.of(hazard), 3);
        intervention.start();

        intervention.submit(new InterveneAction(ambulance, hazard)); // Medicina 12
        List<InterventionEvent> events = intervention.submit(new InterveneAction(engine, hazard)); // Tecnica 14

        assertEquals(InterventionStatus.RESOLVED, intervention.status());
        assertTrue(events.contains(new HazardResolved(hazard)));
        assertEquals(new InterventionEnded(true), events.get(events.size() - 1));
        assertTrue(intervention.currentUnit().isEmpty());
    }

    @Test
    void failsWhenTimeRunsOut() {
        Hazard hazard = hazard(500, 5, "steady");
        Intervention intervention = intervention(List.of(engine), List.of(hazard), 2);
        intervention.start();

        playUntilEnd(intervention, hazard);

        assertEquals(InterventionStatus.FAILED, intervention.status());
        assertEquals(0, intervention.roundsRemaining());
        assertEquals(2, intervention.events().stream().filter(event -> event instanceof RoundStarted).count());
        assertEquals(new InterventionEnded(false), intervention.events().get(intervention.events().size() - 1));
    }

    @Test
    void failsWhenAllUnitsAreExhausted() {
        engine.endurance().decrease(119);
        ambulance.endurance().decrease(89);
        Hazard hazard = hazard(500, 40, "steady");
        Intervention intervention = intervention(List.of(engine, ambulance), List.of(hazard), 5);
        intervention.start();

        playUntilEnd(intervention, hazard);

        assertEquals(InterventionStatus.FAILED, intervention.status());
        assertTrue(intervention.events().contains(new UnitExhausted(engine)));
        assertTrue(intervention.events().contains(new UnitExhausted(ambulance)));
        assertTrue(intervention.activeUnits().isEmpty());
    }

    @Test
    void exhaustedUnitsSkipTheirTurns() {
        engine.endurance().decrease(119);
        Hazard hazard = hazard(500, 40, "steady");
        Intervention intervention = intervention(List.of(engine, ambulance), List.of(hazard), 5);
        intervention.start();
        intervention.submit(new InterveneAction(ambulance, hazard));
        // il pericolo stressa l'APS (Resistenza più bassa) che si esaurisce nello stesso round
        intervention.submit(new InterveneAction(engine, hazard));
        assertFalse(engine.isActive());

        // round 2: tocca all'Ambulanza e poi direttamente al pericolo
        assertEquals(Optional.of(ambulance), intervention.currentUnit());
        intervention.submit(new InterveneAction(ambulance, hazard));

        long engineTurnsInRound2 = intervention.events().stream()
                .dropWhile(event -> !event.equals(new RoundStarted(2)))
                .filter(event -> event.equals(new TurnStarted(engine))).count();
        assertEquals(0, engineTurnsInRound2);
    }

    @Test
    void coverHalvesTheStressAndEndsOnTheUnitsNextTurn() {
        Hazard hazard = hazard(500, 10, "steady");
        Intervention intervention = intervention(List.of(engine, ambulance), List.of(hazard), 3);
        intervention.start();

        intervention.submit(new TakeCoverAction(ambulance));
        assertTrue(ambulance.isCovered());
        intervention.submit(new InterveneAction(engine, hazard));

        // Stress 10, Sangue Freddo 7: 6,5 dimezzato = 3 (il pericolo sceglie l'Ambulanza, Resistenza più bassa)
        StressInflicted stress = intervention.events().stream()
                .filter(event -> event instanceof StressInflicted)
                .map(event -> (StressInflicted) event).findFirst().orElseThrow();
        assertEquals(ambulance, stress.target());
        assertEquals(3, stress.amount());
        // Nel round 2 tocca di nuovo all'Ambulanza e il riparo è finito
        assertEquals(Optional.of(ambulance), intervention.currentUnit());
        assertFalse(ambulance.isCovered());
    }

    @Test
    void requirementMalusWeakensTheUnits() {
        Hazard full = hazard(500, 5, "steady");
        Hazard penalized = hazard(500, 5, "steady");
        Intervention normal = intervention(List.of(engine), List.of(full), 3);
        Unit otherEngine = new Unit("APS 2", "Verdi", new FireEngine(), NO_BONUS);
        Intervention withMalus = InterventionFixture.intervention(List.of(otherEngine), List.of(penalized), 3,
                new StandardEffectCalculator(ScriptedRandom.neutral(), 0.5), ScriptedRandom.neutral());
        normal.start();
        withMalus.start();

        normal.submit(new InterveneAction(engine, full));
        withMalus.submit(new InterveneAction(otherEngine, penalized));

        assertEquals(500 - 14, full.gravity().current());
        assertEquals(500 - 7, penalized.gravity().current());
    }

    @Test
    void maneuverSpendsResourcesAndHitsAllHazardsOfItsScope() {
        Hazard first = hazard(500, 5, "steady");
        Hazard second = hazard(500, 5, "steady");
        Intervention intervention = intervention(List.of(engine), List.of(first, second), 3);
        intervention.start();
        Maneuver cooling = new FireEngine().maneuversAt(1).get(1); // Raffreddamento: tutti i pericoli, costo 7

        intervention.submit(new ManeuverAction(engine, cooling, TargetChoice.none()));

        assertEquals(20 - 7, engine.resources().current());
        assertEquals(500 - 13, first.gravity().current());
        assertEquals(500 - 13, second.gravity().current());
        assertTrue(first.wasFacedIn(1));
        assertTrue(second.wasFacedIn(1));
    }

    @Test
    void maneuverWithoutEnoughResourcesFailsWithoutSideEffects() {
        Hazard hazard = hazard(500, 5, "steady");
        Intervention intervention = intervention(List.of(engine), List.of(hazard), 3);
        intervention.start();
        engine.resources().decrease(18);
        Maneuver extinguish = new FireEngine().maneuversAt(1).get(0); // costo 4
        int eventsBefore = intervention.events().size();

        assertThrows(IllegalStateException.class,
                () -> intervention.submit(new ManeuverAction(engine, extinguish, TargetChoice.ofHazard(hazard))));

        assertEquals(2, engine.resources().current());
        assertEquals(500, hazard.gravity().current());
        assertEquals(eventsBefore, intervention.events().size());
        assertEquals(Optional.of(engine), intervention.currentUnit());
    }

    @Test
    void supplyIsTakenFromTheInventory() {
        Hazard hazard = hazard(500, 5, "steady");
        Intervention intervention = intervention(List.of(engine), List.of(hazard), 3);
        intervention.start();
        Inventory inventory = new Inventory();
        ToolSupply extinguisher = new ToolSupply("extinguisher", "Estintore", "", 15);
        inventory.add(extinguisher, 1);

        intervention.submit(new SupplyAction(engine, extinguisher, TargetChoice.ofHazard(hazard), inventory));

        assertEquals(485, hazard.gravity().current());
        assertEquals(0, inventory.quantityOf(extinguisher));
        assertThrows(IllegalStateException.class,
                () -> intervention.submit(new SupplyAction(engine, extinguisher, TargetChoice.ofHazard(hazard), inventory)));
    }

    @Test
    void rejectsActionsOfTheWrongUnitOrOnInvalidTargets() {
        Hazard hazard = hazard(500, 5, "steady");
        Hazard foreign = hazard(500, 5, "steady");
        Intervention intervention = intervention(List.of(engine, ambulance), List.of(hazard), 3);
        intervention.start();

        assertThrows(IllegalArgumentException.class, () -> intervention.submit(new InterveneAction(engine, hazard)));
        assertThrows(IllegalArgumentException.class, () -> intervention.submit(new InterveneAction(ambulance, foreign)));
    }

    @Test
    void rejectsActionsOnceFinishedAndRestart() {
        Hazard hazard = hazard(5, 5, "steady");
        Intervention intervention = intervention(List.of(ambulance), List.of(hazard), 3);
        intervention.start();
        intervention.submit(new InterveneAction(ambulance, hazard));

        assertEquals(InterventionStatus.RESOLVED, intervention.status());
        assertThrows(IllegalStateException.class, () -> intervention.submit(new TakeCoverAction(ambulance)));
        assertThrows(IllegalStateException.class, intervention::start);
    }

    @Test
    void listenersReceiveEveryEventInOrder() {
        Hazard hazard = hazard(5, 5, "steady");
        Intervention intervention = intervention(List.of(ambulance), List.of(hazard), 3);
        List<InterventionEvent> received = new ArrayList<>();
        intervention.addListener(received::add);

        intervention.start();
        intervention.submit(new InterveneAction(ambulance, hazard));

        assertEquals(intervention.events(), received);
        assertFalse(received.isEmpty());
    }

    @Test
    void rejectsInvalidSetup() {
        Hazard hazard = hazard(10, 5, "steady");

        assertThrows(IllegalArgumentException.class, () -> intervention(List.of(), List.of(hazard), 3));
        assertThrows(IllegalArgumentException.class, () -> intervention(List.of(engine), List.of(), 3));
        assertThrows(IllegalArgumentException.class, () -> intervention(List.of(engine), List.of(hazard), 0));
    }
}
