package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.PatrolCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.NO_BONUS;
import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.hazard;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadinessTurnOrderTest {

    private final ReadinessTurnOrder order = new ReadinessTurnOrder();

    @Test
    void ordersByDescendingInitiative() {
        Unit engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);   // Prontezza 5
        Unit patrol = new Unit("PAT 1", "Neri", new PatrolCar(), NO_BONUS);     // Prontezza 10
        Unit ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), NO_BONUS); // Prontezza 7

        List<Combatant> ordered = order.order(List.of(engine, patrol, ambulance), List.of());

        assertEquals(List.of(patrol, ambulance, engine), ordered);
    }

    @Test
    void unitsPrecedeHazardsOnTies() {
        Unit engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS); // Prontezza 5
        Hazard hazard = hazard(50, 5, "steady");                              // pace 5

        List<Combatant> ordered = order.order(List.of(engine), List.of(hazard));

        assertEquals(List.of(engine, hazard), ordered);
    }

    @Test
    void fasterHazardActsBeforeSlowerUnits() {
        Unit engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);
        Hazard slowUnitsHazard = hazard(50, 5, "steady");

        Unit slow = new Unit("AUT 1", "Neri", new it.unicam.cs.mpgc.rpg125885.model.fleet.AerialLadder(), NO_BONUS); // 3

        assertEquals(List.of(engine, slowUnitsHazard, slow), order.order(List.of(engine, slow), List.of(slowUnitsHazard)));
    }
}
