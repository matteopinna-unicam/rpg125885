package it.unicam.cs.mpgc.rpg125885.model.item;

import it.unicam.cs.mpgc.rpg125885.engine.rules.ScriptedRandom;
import it.unicam.cs.mpgc.rpg125885.engine.rules.StandardEffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.GravityReduced;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.Recovered;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Targets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SupplyTest {

    private final EffectCalculator calculator = new StandardEffectCalculator(ScriptedRandom.neutral());

    private Unit unit;

    @BeforeEach
    void setUp() {
        unit = new Unit("AMB 1", "Bianchi", new Ambulance(),
                new Trait("none", "Nessuno", Stats.NONE, 1.0, 1.0));
    }

    @Test
    void toolSupplyReducesGravityByFixedAmount() {
        Hazard hazard = new Hazard(new HazardTemplate("h", "Pericolo", 80, 5, Map.of(),
                false, 8, 5, "steady", 10));
        ToolSupply extinguisher = new ToolSupply("extinguisher", "Estintore", "", 15);

        List<InterventionEvent> events = extinguisher.use(unit, Targets.ofHazards(List.of(hazard)), calculator);

        assertEquals(List.of(new GravityReduced(unit, hazard, 15, false)), events);
        assertEquals(65, hazard.gravity().current());
        assertEquals(TargetScope.SINGLE_HAZARD, extinguisher.scope());
    }

    @Test
    void recoverySupplyRestoresFixedAmount() {
        unit.resources().decrease(20);
        RecoverySupply oxygen = new RecoverySupply("oxygen", "Bombola", "", GaugeKind.RESOURCES, 10);

        List<InterventionEvent> events = oxygen.use(unit, Targets.ofUnits(List.of(unit)), calculator);

        assertEquals(List.of(new Recovered(unit, unit, GaugeKind.RESOURCES, 10)), events);
        assertEquals(30 - 20 + 10, unit.resources().current());
        assertEquals(TargetScope.SINGLE_UNIT, oxygen.scope());
    }

    @Test
    void rejectsNonPositiveAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new ToolSupply("x", "X", "", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new RecoverySupply("x", "X", "", GaugeKind.ENDURANCE, -5));
    }
}
