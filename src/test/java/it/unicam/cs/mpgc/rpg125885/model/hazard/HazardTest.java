package it.unicam.cs.mpgc.rpg125885.model.hazard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HazardTest {

    private HazardTemplate template;

    @BeforeEach
    void setUp() {
        template = new HazardTemplate("apartment_fire", "Incendio in appartamento", 80, 5,
                Map.of(InterventionType.TECHNICAL, 1.4, InterventionType.MEDICAL, 0.5),
                true, 11, 9, "escalating", 26);
    }

    @Test
    void startsWithTemplateGravity() {
        Hazard hazard = new Hazard(template);

        assertEquals(80, hazard.gravity().current());
        assertEquals("Incendio in appartamento", hazard.name());
        assertTrue(hazard.isActive());
    }

    @Test
    void canStartWithModifiedGravity() {
        Hazard hazard = new Hazard(template, 95);

        assertEquals(95, hazard.gravity().current());
        assertEquals(95, hazard.gravity().max());
    }

    @Test
    void initiativeIsPace() {
        assertEquals(5, new Hazard(template).initiative());
    }

    @Test
    void isResolvedWhenGravityReachesZero() {
        Hazard hazard = new Hazard(template);

        hazard.gravity().decrease(80);

        assertFalse(hazard.isActive());
    }

    @Test
    void modifierDefaultsToNeutralWhenNotSpecified() {
        Hazard hazard = new Hazard(template);

        assertEquals(1.4, hazard.modifierFor(InterventionType.TECHNICAL));
        assertEquals(0.5, hazard.modifierFor(InterventionType.MEDICAL));
        assertEquals(1.0, hazard.modifierFor(InterventionType.COORDINATION));
    }

    @Test
    void remembersLastFacedRound() {
        Hazard hazard = new Hazard(template);
        assertFalse(hazard.wasFacedIn(0));

        hazard.markFacedIn(2);

        assertTrue(hazard.wasFacedIn(2));
        assertFalse(hazard.wasFacedIn(3));
    }

    @Test
    void elevatedFlagComesFromTemplate() {
        assertTrue(new Hazard(template).isElevated());
    }

    @Test
    void templateRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new HazardTemplate(
                "x", "X", 0, 5, Map.of(), false, 1, 1, "steady", 1));
        assertThrows(IllegalArgumentException.class, () -> new HazardTemplate(
                "x", "X", 10, 5, Map.of(), false, -1, 1, "steady", 1));
    }

    @Test
    void templateModifiersAreImmutableCopy() {
        assertThrows(UnsupportedOperationException.class,
                () -> template.modifiers().put(InterventionType.COORDINATION, 2.0));
    }
}
