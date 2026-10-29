package it.unicam.cs.mpgc.rpg125885.model.fleet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FleetTest {

    private static final Trait NO_BONUS = new Trait("none", "Nessuno", Stats.NONE, 1.0, 1.0);

    private Unit engine;
    private Unit ambulance;
    private Fleet fleet;

    @BeforeEach
    void setUp() {
        engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);
        ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), NO_BONUS);
        fleet = new Fleet(List.of(engine, ambulance));
    }

    @Test
    void keepsUnitsInOrder() {
        assertEquals(List.of(engine, ambulance), fleet.units());
    }

    @Test
    void availableUnitsExcludesBusyOnes() {
        engine.occupyUntil(3);

        assertEquals(List.of(ambulance), fleet.availableUnits(2));
        assertEquals(List.of(engine, ambulance), fleet.availableUnits(3));
    }

    @Test
    void reinforcementJoinsTheFleet() {
        Unit helicopter = new Unit("ELI 1", "Verdi", new AirAmbulance(), NO_BONUS);

        fleet.addReinforcement(helicopter);

        assertEquals(List.of(engine, ambulance, helicopter), fleet.units());
    }

    @Test
    void rejectsDuplicateCallsign() {
        Unit duplicate = new Unit("APS 1", "Neri", new FireEngine(), NO_BONUS);

        assertThrows(IllegalArgumentException.class, () -> fleet.addReinforcement(duplicate));
        assertEquals(2, fleet.units().size());
    }

    @Test
    void unitsViewIsReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> fleet.units().clear());
    }

    @Test
    void sharedInventoryStartsEmpty() {
        assertEquals(0, fleet.inventory().stream().count());
    }
}
