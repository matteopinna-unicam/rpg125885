package it.unicam.cs.mpgc.rpg125885.model.item;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {

    private Inventory inventory;
    private Gear helmet;
    private Gear axe;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        helmet = new Gear("helmet", "Casco", "", GearSlot.HEAD, new Stats(0, 0, 0, 0, 0, 1, 0));
        axe = new Gear("axe", "Accetta", "", GearSlot.TOOL, new Stats(0, 0, 1, 0, 0, 0, 0));
    }

    @Test
    void emptyInventoryHasNothing() {
        assertEquals(0, inventory.quantityOf(helmet));
        assertFalse(inventory.contains(helmet));
    }

    @Test
    void addAccumulatesQuantities() {
        inventory.add(helmet, 2);
        inventory.add(helmet, 3);

        assertEquals(5, inventory.quantityOf(helmet));
        assertTrue(inventory.contains(helmet));
    }

    @Test
    void removeDecreasesQuantity() {
        inventory.add(helmet, 3);

        inventory.remove(helmet, 2);

        assertEquals(1, inventory.quantityOf(helmet));
    }

    @Test
    void removingEverythingDropsTheEntry() {
        inventory.add(helmet, 2);

        inventory.remove(helmet, 2);

        assertFalse(inventory.contains(helmet));
        assertEquals(0, inventory.stream().count());
    }

    @Test
    void removeMoreThanOwnedFailsAndKeepsQuantity() {
        inventory.add(helmet, 1);

        assertThrows(IllegalStateException.class, () -> inventory.remove(helmet, 2));
        assertEquals(1, inventory.quantityOf(helmet));
    }

    @Test
    void removeOfMissingItemFails() {
        assertThrows(IllegalStateException.class, () -> inventory.remove(axe, 1));
    }

    @Test
    void rejectsNonPositiveAmounts() {
        assertThrows(IllegalArgumentException.class, () -> inventory.add(helmet, 0));
        assertThrows(IllegalArgumentException.class, () -> inventory.add(helmet, -1));
        assertThrows(IllegalArgumentException.class, () -> inventory.remove(helmet, 0));
    }

    @Test
    void itemsWithSameIdAreTheSameEntry() {
        Gear sameIdHelmet = new Gear("helmet", "Altro nome", "", GearSlot.HEAD, Stats.NONE);

        inventory.add(helmet, 1);
        inventory.add(sameIdHelmet, 1);

        assertEquals(2, inventory.quantityOf(helmet));
    }

    @Test
    void streamAllowsFilteringAndKeepsInsertionOrder() {
        inventory.add(helmet, 1);
        inventory.add(axe, 2);

        List<Item> tools = inventory.stream()
                .map(entry -> entry.getKey())
                .filter(item -> item instanceof Gear gear && gear.slot() == GearSlot.TOOL)
                .toList();

        assertEquals(List.of(axe), tools);
        assertEquals(List.of(helmet, axe), inventory.stream().map(entry -> entry.getKey()).toList());
    }

    @Test
    void streamIsASnapshot() {
        inventory.add(helmet, 1);

        var snapshot = inventory.stream();
        inventory.add(axe, 1);

        assertEquals(1, snapshot.count());
    }
}
