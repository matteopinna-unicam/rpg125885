package it.unicam.cs.mpgc.rpg125885.engine.behavior;

import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.rules.ScriptedRandom;
import it.unicam.cs.mpgc.rpg125885.engine.rules.StandardEffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.GravityEscalated;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.StressInflicted;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BehaviorTest {

    private Unit engine;    // Resistenza 120, Sangue Freddo 11
    private Unit ambulance; // Resistenza 90, Sangue Freddo 7
    private Hazard hazard;

    @BeforeEach
    void setUp() {
        engine = new Unit("APS 1", "Rossi", new FireEngine(), InterventionFixture.NO_BONUS);
        ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), InterventionFixture.NO_BONUS);
        hazard = InterventionFixture.hazard(40, 10, "steady");
    }

    private Intervention started(ScriptedRandom random) {
        Intervention intervention = InterventionFixture.intervention(
                List.of(engine, ambulance), List.of(hazard), 5,
                new StandardEffectCalculator(ScriptedRandom.neutral()), random);
        intervention.start();
        return intervention;
    }

    @Test
    void steadyStressesTheUnitWithLowestEndurance() {
        Intervention intervention = started(ScriptedRandom.neutral());

        List<InterventionEvent> events = new SteadyBehavior().chooseAction(hazard, intervention).execute(intervention);

        StressInflicted stress = (StressInflicted) events.get(0);
        assertEquals(ambulance, stress.target());
    }

    @Test
    void spreadingStressesEveryUnitAtSixtyPercent() {
        Intervention intervention = started(ScriptedRandom.neutral());

        List<InterventionEvent> events = new SpreadingBehavior().chooseAction(hazard, intervention).execute(intervention);

        // Stress 10 × 0,6 = 6. APS: 6 − 11/2 → minimo 1. Ambulanza: 6 − 7/2 = 2,5 → 3
        assertEquals(List.of(new StressInflicted(hazard, engine, 1), new StressInflicted(hazard, ambulance, 3)), events);
    }

    @Test
    void escalatingWorsensTheHazardWhenNobodyFacedItLastRound() {
        Intervention intervention = started(ScriptedRandom.neutral());

        List<InterventionEvent> events = new EscalatingBehavior().chooseAction(hazard, intervention).execute(intervention);

        // Escalation del modello: 5, anche oltre la gravità iniziale
        assertEquals(List.of(new GravityEscalated(hazard, 5)), events);
        assertEquals(45, hazard.gravity().current());
    }

    @Test
    void escalatingStressesARandomUnitWhenItWasFacedLastRound() {
        Intervention intervention = started(new ScriptedRandom(0.99));
        hazard.markFacedIn(intervention.round() - 1);

        List<InterventionEvent> events = new EscalatingBehavior().chooseAction(hazard, intervention).execute(intervention);

        // nextInt(2) con 0,99 → seconda unità
        assertEquals(ambulance, ((StressInflicted) events.get(0)).target());
        assertEquals(40, hazard.gravity().current());
    }

    @Test
    void registryFindsBehaviorsAndRejectsUnknownOrDuplicateIds() {
        BehaviorRegistry registry = new BehaviorRegistry();
        HazardBehavior steady = new SteadyBehavior();
        registry.register("steady", steady);

        assertEquals(steady, registry.byId("steady"));
        assertThrows(IllegalArgumentException.class, () -> registry.byId("unknown"));
        assertThrows(IllegalArgumentException.class, () -> registry.register("steady", steady));
    }
}
