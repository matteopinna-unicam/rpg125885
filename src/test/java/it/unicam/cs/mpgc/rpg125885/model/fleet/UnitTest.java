package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitTest {

    private static final Trait VETERAN = new Trait("veteran", "Veterano",
            new Stats(0, 0, 0, 0, 0, 2, 0), 1.0, 1.0);

    private final VehicleType fireEngine = new FireEngine();
    private Unit unit;

    @BeforeEach
    void setUp() {
        unit = new Unit("APS 1", "Rossi", fireEngine, VETERAN);
    }

    @Test
    void startsAtLevelOneWithFullGauges() {
        assertEquals(1, unit.level());
        assertEquals(0, unit.experience());
        assertEquals(120, unit.endurance().current());
        assertEquals(20, unit.resources().current());
        assertTrue(unit.isActive());
    }

    @Test
    void effectiveStatsAddTraitToBaseAtLevelOne() {
        // base APS 120/20/14/3/5/11/5 + veterano +2 Sangue Freddo
        assertEquals(new Stats(120, 20, 14, 3, 5, 13, 5), unit.effectiveStats());
    }

    @Test
    void effectiveStatsAddGrowthForLevelsAboveFirst() {
        unit.levelUp();
        unit.levelUp();

        // crescita APS 11/2/3/0/1/2/1 per 2 livelli
        assertEquals(new Stats(142, 24, 20, 3, 7, 17, 7), unit.effectiveStats());
    }

    @Test
    void effectiveStatsIncludeGear() {
        Gear helmet = new Gear("helmet", "Casco", "", GearSlot.HEAD, new Stats(10, 0, 0, 0, 0, 1, 0));
        Gear axe = new Gear("axe", "Accetta", "", GearSlot.TOOL, new Stats(0, 0, 2, 0, 0, 0, 0));

        unit.equip(helmet);
        unit.equip(axe);

        assertEquals(new Stats(130, 20, 16, 3, 5, 14, 5), unit.effectiveStats());
    }

    @Test
    void equipReplacesGearInSameSlot() {
        Gear oldHelmet = new Gear("old", "Casco vecchio", "", GearSlot.HEAD, Stats.NONE);
        Gear newHelmet = new Gear("new", "Casco nuovo", "", GearSlot.HEAD, Stats.NONE);
        unit.equip(oldHelmet);

        Optional<Gear> replaced = unit.equip(newHelmet);

        assertEquals(Optional.of(oldHelmet), replaced);
        assertEquals(Optional.of(newHelmet), unit.gearIn(GearSlot.HEAD));
    }

    @Test
    void unequipRemovesBonusAndLowersMaxButKeepsCurrentWithinLimit() {
        Gear vest = new Gear("vest", "Giubbotto", "", GearSlot.BODY, new Stats(30, 0, 0, 0, 0, 0, 0));
        unit.equip(vest);
        unit.endurance().fill();
        assertEquals(150, unit.endurance().current());

        assertEquals(Optional.of(vest), unit.unequip(GearSlot.BODY));

        assertEquals(120, unit.endurance().max());
        assertEquals(120, unit.endurance().current());
        assertTrue(unit.gearIn(GearSlot.BODY).isEmpty());
    }

    @Test
    void levelUpRaisesMaxAndRefillsGauges() {
        unit.endurance().decrease(100);
        unit.resources().decrease(15);

        unit.levelUp();

        assertEquals(2, unit.level());
        assertEquals(131, unit.endurance().max());
        assertEquals(131, unit.endurance().current());
        assertEquals(22, unit.resources().current());
    }

    @Test
    void initiativeIsReadiness() {
        assertEquals(5, unit.initiative());
    }

    @Test
    void isInactiveWhenEnduranceIsEmpty() {
        unit.endurance().decrease(1000);

        assertFalse(unit.isActive());
    }

    @Test
    void availabilityDependsOnBusyPhase() {
        assertTrue(unit.isAvailableAt(0));

        unit.occupyUntil(4);

        assertFalse(unit.isAvailableAt(3));
        assertTrue(unit.isAvailableAt(4));
        assertTrue(unit.isAvailableAt(5));
    }

    @Test
    void addExperienceAccumulatesAndRejectsNegatives() {
        unit.addExperience(20);
        unit.addExperience(15);

        assertEquals(35, unit.experience());
        assertThrows(IllegalArgumentException.class, () -> unit.addExperience(-1));
    }

    @Test
    void gaugeSelectsTheRequestedCounter() {
        assertSame(unit.endurance(), unit.gauge(GaugeKind.ENDURANCE));
        assertSame(unit.resources(), unit.gauge(GaugeKind.RESOURCES));
    }

    @Test
    void coverLastsUntilEnded() {
        assertFalse(unit.isCovered());

        unit.takeCover();
        assertTrue(unit.isCovered());

        unit.endCover();
        assertFalse(unit.isCovered());
    }

    @Test
    void rejectsInvalidConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new Unit(" ", "Rossi", fireEngine, VETERAN));
        assertThrows(IllegalArgumentException.class, () -> new Unit("APS 2", "", fireEngine, VETERAN));
        assertThrows(NullPointerException.class, () -> new Unit("APS 2", "Rossi", null, VETERAN));
        assertThrows(NullPointerException.class, () -> new Unit("APS 2", "Rossi", fireEngine, null));
    }
}
