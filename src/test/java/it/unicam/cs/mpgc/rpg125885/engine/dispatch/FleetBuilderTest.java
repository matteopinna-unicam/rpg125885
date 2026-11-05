package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.model.fleet.AirAmbulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.PatrolCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.NO_BONUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetBuilderTest {

    private FleetBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new FleetBuilder(12, 4, Map.of(DispatchFixture.supply(), 3));
    }

    @Test
    void addingUnitsSpendsBudgetAndNumbersCallsignsPerType() {
        Unit first = builder.add(new FireEngine(), "Rossi", NO_BONUS);
        Unit second = builder.add(new FireEngine(), "Neri", NO_BONUS);

        assertEquals(new FireEngine().displayName() + " 1", first.name());
        assertEquals(new FireEngine().displayName() + " 2", second.name());
        assertEquals(12 - 2 * new FireEngine().cost(), builder.remainingBudget());
    }

    @Test
    void rejectsAUnitThatExceedsTheBudget() {
        for (int i = 0; i < 4; i++) {
            builder.add(new FireEngine(), "Capo " + i, NO_BONUS);
        }

        assertEquals(0, builder.remainingBudget());
        assertFalse(builder.canAfford(new Ambulance()));
        assertThrows(IllegalArgumentException.class, () -> builder.add(new Ambulance(), "Bianchi", NO_BONUS));
    }

    @Test
    void airAmbulanceCannotBeBought() {
        assertFalse(builder.canAfford(new AirAmbulance()));
        assertThrows(IllegalArgumentException.class, () -> builder.add(new AirAmbulance(), "Serra", NO_BONUS));
    }

    @Test
    void removingAUnitRefundsItsCost() {
        Unit unit = builder.add(new FireEngine(), "Rossi", NO_BONUS);

        builder.remove(unit);

        assertEquals(12, builder.remainingBudget());
        assertTrue(builder.units().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> builder.remove(unit));
    }

    @Test
    void callsignsStayUniqueAfterARemoval() {
        Unit first = builder.add(new PatrolCar(), "Verdi", NO_BONUS);
        builder.add(new PatrolCar(), "Gialli", NO_BONUS);
        builder.remove(first);

        Unit third = builder.add(new PatrolCar(), "Blu", NO_BONUS);

        assertEquals(new PatrolCar().displayName() + " 3", third.name());
    }

    @Test
    void buildNeedsTheMinimumNumberOfUnits() {
        builder.add(new Ambulance(), "A", NO_BONUS);
        builder.add(new Ambulance(), "B", NO_BONUS);
        builder.add(new PatrolCar(), "C", NO_BONUS);
        assertFalse(builder.isComplete());
        assertThrows(IllegalStateException.class, builder::build);

        builder.add(new PatrolCar(), "D", NO_BONUS);

        assertTrue(builder.isComplete());
        Fleet fleet = builder.build();
        assertEquals(4, fleet.units().size());
        assertEquals(3, fleet.inventory().quantityOf(DispatchFixture.supply()));
    }
}
