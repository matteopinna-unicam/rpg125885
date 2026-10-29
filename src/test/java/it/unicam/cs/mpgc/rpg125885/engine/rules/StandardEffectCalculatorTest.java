package it.unicam.cs.mpgc.rpg125885.engine.rules;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectResult;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardEffectCalculatorTest {

    private static final Trait NO_BONUS = new Trait("none", "Nessuno", Stats.NONE, 1.0, 1.0);
    private static final Trait EMPATHIC = new Trait("empathic", "Empatico", Stats.NONE, 1.2, 1.0);

    private Unit engine;
    private Unit ambulance;
    private Hazard fire;

    @BeforeEach
    void setUp() {
        // APS: Tecnica 14, Medicina 3, Sangue Freddo 11. Ambulanza: Medicina 12, Coordinamento 7.
        engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);
        ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), NO_BONUS);
        fire = new Hazard(new HazardTemplate("fire", "Incendio", 80, 5,
                Map.of(InterventionType.TECHNICAL, 1.4, InterventionType.MEDICAL, 0.5),
                false, 11, 9, "steady", 20));
    }

    private StandardEffectCalculator neutral() {
        return new StandardEffectCalculator(ScriptedRandom.neutral());
    }

    @Test
    void reductionUsesRelevantStatPowerAndHazardModifier() {
        // 14 × 1,0 × 1,4 = 19,6
        EffectResult result = neutral().gravityReduction(engine, fire, InterventionType.TECHNICAL, 1.0, false);

        assertEquals(20, result.amount());
        assertFalse(result.exemplary());
    }

    @Test
    void weaknessAndResistanceChangeTheOutcome() {
        // Medicina 3 contro un pericolo che resiste (×0,5): 1,5
        int resisted = neutral().gravityReduction(engine, fire, InterventionType.MEDICAL, 1.0, false).amount();
        // Coordinamento 5 contro un moltiplicatore non specificato (1,0): 5
        int neutralHit = neutral().gravityReduction(engine, fire, InterventionType.COORDINATION, 1.0, false).amount();

        assertEquals(2, resisted);
        assertEquals(5, neutralHit);
    }

    @Test
    void reductionIsAtLeastOne() {
        EffectResult result = neutral().gravityReduction(engine, fire, InterventionType.MEDICAL, 0.1, false);

        assertEquals(1, result.amount());
    }

    @Test
    void ignoringResistanceRemovesTheReduction() {
        int resisted = neutral().gravityReduction(ambulance, fire, InterventionType.MEDICAL, 1.0, false).amount();
        int ignoring = neutral().gravityReduction(ambulance, fire, InterventionType.MEDICAL, 1.0, true).amount();

        assertEquals(6, resisted);
        assertEquals(12, ignoring);
    }

    @Test
    void ignoringResistanceKeepsWeaknessBonus() {
        int ignoring = neutral().gravityReduction(engine, fire, InterventionType.TECHNICAL, 1.0, true).amount();

        assertEquals(20, ignoring);
    }

    @Test
    void exemplaryInterventionMultipliesByOneAndAHalf() {
        // 1ª estrazione: variazione neutra; 2ª: sotto il 10% → esemplare. 19,6 × 1,5 = 29,4
        StandardEffectCalculator calculator = new StandardEffectCalculator(new ScriptedRandom(0.5, 0.05));

        EffectResult result = calculator.gravityReduction(engine, fire, InterventionType.TECHNICAL, 1.0, false);

        assertEquals(29, result.amount());
        assertTrue(result.exemplary());
    }

    @Test
    void variationScalesBetweenNinetyAndOneHundredTenPercent() {
        StandardEffectCalculator low = new StandardEffectCalculator(new ScriptedRandom(0.0, 0.5));
        StandardEffectCalculator high = new StandardEffectCalculator(new ScriptedRandom(1.0, 0.5));

        assertEquals(18, low.gravityReduction(engine, fire, InterventionType.TECHNICAL, 1.0, false).amount());
        assertEquals(22, high.gravityReduction(engine, fire, InterventionType.TECHNICAL, 1.0, false).amount());
    }

    @Test
    void requirementMalusScalesReduction() {
        // 14 × 1,0 × 1,4 × 0,6 = 11,76
        StandardEffectCalculator calculator = new StandardEffectCalculator(ScriptedRandom.neutral(), 0.6);

        int amount = calculator.gravityReduction(engine, fire, InterventionType.TECHNICAL, 1.0, false).amount();

        assertEquals(12, amount);
    }

    @Test
    void stressIsReducedByHalfOfComposure() {
        // 11 − 11/2 = 5,5
        assertEquals(6, neutral().stressTaken(engine, 11));
    }

    @Test
    void stressIsAtLeastOne() {
        assertEquals(1, neutral().stressTaken(engine, 2));
    }

    @Test
    void coverHalvesStress() {
        engine.takeCover();

        // 5,5 × 0,5 = 2,75
        assertEquals(3, neutral().stressTaken(engine, 11));
    }

    @Test
    void recoveryUsesMedicineForEnduranceAndCoordinationForResources() {
        assertEquals(14, neutral().recovery(ambulance, GaugeKind.ENDURANCE, 1.2));
        assertEquals(7, neutral().recovery(ambulance, GaugeKind.RESOURCES, 1.0));
    }

    @Test
    void recoveryAppliesTraitMultiplier() {
        Unit empathicAmbulance = new Unit("AMB 2", "Verdi", new Ambulance(), EMPATHIC);

        // 12 × 1,2 × 1,2 = 17,28
        assertEquals(17, neutral().recovery(empathicAmbulance, GaugeKind.ENDURANCE, 1.2));
    }

    @Test
    void rejectsNonPositiveEfficiency() {
        assertThrows(IllegalArgumentException.class,
                () -> new StandardEffectCalculator(ScriptedRandom.neutral(), 0));
    }
}
