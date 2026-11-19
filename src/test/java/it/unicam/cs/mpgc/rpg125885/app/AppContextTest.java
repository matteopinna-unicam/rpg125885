package it.unicam.cs.mpgc.rpg125885.app;

import it.unicam.cs.mpgc.rpg125885.engine.dispatch.FleetBuilder;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionStatus;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterveneAction;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prova di integrazione: la composition root collega davvero contenuto, motore e servizio. */
class AppContextTest {

    private static final int MAX_TURNS = 500;

    private GameService game;

    @BeforeEach
    void createContext() throws PersistenceException {
        game = AppContext.create().gameService();
    }

    @Test
    void contentIsLoadedAndFleetCanBeComposed() {
        assertFalse(game.purchasableVehicleTypes().isEmpty());
        assertFalse(game.traits().isEmpty());
        assertFalse(game.hasGame());
    }

    @Test
    void aNewNightStartsAtTheFirstPhaseWithStartingSupplies() {
        startNewGame();

        assertTrue(game.hasGame());
        assertEquals("22:00", game.currentPhase().time());
        assertFalse(game.supplies().isEmpty());
        assertEquals(GameService.INITIAL_CRISIS, game.crisisIndex().value());
    }

    @Test
    void aWholeCallCanBePlayedAndThePhaseCanBeClosed() {
        startNewGame();
        Scenario call = game.calls().getFirst();
        game.assign(call, game.availableUnits().subList(0, 3));

        Intervention intervention = game.beginIntervention(call.decision().map(decision -> decision.first()));
        intervention.start();
        int turns = 0;
        while (intervention.status() == InterventionStatus.ONGOING && turns++ < MAX_TURNS) {
            intervention.submit(new InterveneAction(intervention.currentUnit().orElseThrow(),
                    intervention.activeHazards().getFirst()));
        }
        game.finishIntervention();
        game.closePhase();

        assertNotEquals(InterventionStatus.ONGOING, intervention.status());
        assertEquals("23:00", game.currentPhase().time());
    }

    private void startNewGame() {
        FleetBuilder builder = game.newFleetBuilder();
        Trait trait = game.traits().getFirst();
        List<VehicleType> types = game.purchasableVehicleTypes();
        for (VehicleType type : types) {
            if (builder.canAfford(type) && builder.units().size() < builder.minimumUnits()) {
                builder.add(type, "Capo", trait);
            }
        }
        while (!builder.isComplete()) {
            VehicleType cheapest = types.stream().filter(builder::canAfford)
                    .min((a, b) -> Integer.compare(a.cost(), b.cost())).orElseThrow();
            builder.add(cheapest, "Capo", trait);
        }
        game.startNewGame(builder);
    }
}
