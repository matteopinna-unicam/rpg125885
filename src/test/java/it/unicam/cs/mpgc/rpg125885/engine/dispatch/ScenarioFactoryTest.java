package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioFactoryTest {

    private final ScenarioFactory factory = DispatchFixture.scenarioFactory();
    private final Scenario scenario = DispatchFixture.scenario("call", 80, 6, Requirements.NONE,
            Optional.empty(), Optional.empty());

    @Test
    void withoutAChoiceHazardsAndRoundsAreUnchanged() {
        List<Hazard> hazards = factory.createHazards(scenario, Optional.empty());

        assertEquals(80, hazards.get(0).gravity().max());
        assertEquals(6, factory.roundsFor(scenario, Optional.empty()));
    }

    @Test
    void choiceChangesGravityByPercentAndRoundsByAmount() {
        Optional<DecisionOption> careful = Optional.of(new DecisionOption("Attendere", -20, -1, 0));

        assertEquals(64, factory.createHazards(scenario, careful).get(0).gravity().max());
        assertEquals(5, factory.roundsFor(scenario, careful));
    }

    @Test
    void gravityAndRoundsNeverDropBelowOne() {
        Optional<DecisionOption> extreme = Optional.of(new DecisionOption("Estremo", -100, -50, 0));

        assertEquals(1, factory.createHazards(scenario, extreme).get(0).gravity().max());
        assertEquals(1, factory.roundsFor(scenario, extreme));
    }

    @Test
    void initialStressHurtsEveryUnitBeforeTheInterventionStarts() {
        Unit engine = DispatchFixture.engine();
        Unit ambulance = DispatchFixture.ambulance();
        Optional<DecisionOption> rush = Optional.of(new DecisionOption("Entrare subito", 0, 0, 20));

        factory.create(scenario, rush, List.of(engine, ambulance), 1.0);

        assertTrue(engine.endurance().current() < engine.endurance().max());
        assertTrue(ambulance.endurance().current() < ambulance.endurance().max());
    }

    @Test
    void createdInterventionUsesTheModifiedRounds() {
        Optional<DecisionOption> careful = Optional.of(new DecisionOption("Attendere", 0, -2, 0));

        Intervention intervention = factory.create(scenario, careful, List.of(DispatchFixture.engine()), 1.0);
        intervention.start();

        assertEquals(4, intervention.roundsRemaining());
    }
}
