package it.unicam.cs.mpgc.rpg125885.model.maneuver;

import it.unicam.cs.mpgc.rpg125885.engine.rules.ScriptedRandom;
import it.unicam.cs.mpgc.rpg125885.engine.rules.StandardEffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.GravityReduced;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.HazardResolved;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.Recovered;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.TookCover;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AerialLadder;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.PatrolCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManeuverTest {

    private static final Trait NO_BONUS = new Trait("none", "Nessuno", Stats.NONE, 1.0, 1.0);

    private final EffectCalculator calculator = new StandardEffectCalculator(ScriptedRandom.neutral());

    private Unit engine;
    private Unit ladder;
    private Unit ambulance;
    private Unit patrol;

    @BeforeEach
    void setUp() {
        engine = new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);
        ladder = new Unit("AUT 1", "Neri", new AerialLadder(), NO_BONUS);
        ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), NO_BONUS);
        patrol = new Unit("PAT 1", "Verdi", new PatrolCar(), NO_BONUS);
    }

    private static Hazard hazard(boolean elevated) {
        return hazard(elevated, 80);
    }

    private static Hazard hazard(boolean elevated, int gravity) {
        HazardTemplate template = new HazardTemplate("h", "Pericolo", 80, 5,
                Map.of(InterventionType.TECHNICAL, 1.0), elevated, 8, 5, "steady", 10);
        return new Hazard(template, gravity);
    }

    private static Maneuver maneuver(VehicleType type, String id) {
        return type.maneuversAt(VehicleType.ADVANCED_MANEUVER_LEVEL).stream()
                .filter(candidate -> candidate.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void highRescueHitsElevatedHazardsHarderThanOthers() {
        Hazard high = hazard(true);
        Hazard ground = hazard(false);
        Maneuver highRescue = maneuver(new AerialLadder(), "high_rescue");

        highRescue.use(ladder, Targets.ofHazards(List.of(high)), calculator);
        highRescue.use(ladder, Targets.ofHazards(List.of(ground)), calculator);

        // Tecnica 12: ×2,0 in quota = 24, ×0,8 altrove = 9,6
        assertEquals(80 - 24, high.gravity().current());
        assertEquals(80 - 10, ground.gravity().current());
    }

    @Test
    void allHazardsScopeManeuverReducesEveryTarget() {
        Hazard first = hazard(false);
        Hazard second = hazard(false);

        List<InterventionEvent> events = maneuver(new FireEngine(), "cooling")
                .use(engine, Targets.ofHazards(List.of(first, second)), calculator);

        // Tecnica 14 × 0,9 = 12,6
        assertEquals(2, events.size());
        assertEquals(80 - 13, first.gravity().current());
        assertEquals(80 - 13, second.gravity().current());
    }

    @Test
    void reportsResolutionAndOnlyTheAppliedReduction() {
        Hazard weak = hazard(false, 10);

        List<InterventionEvent> events = maneuver(new FireEngine(), "extinguish")
                .use(engine, Targets.ofHazards(List.of(weak)), calculator);

        assertEquals(new GravityReduced(engine, weak, 10, false), events.get(0));
        assertEquals(new HazardResolved(weak), events.get(1));
        assertFalse(weak.isActive());
    }

    @Test
    void skipsHazardsAlreadyResolved() {
        Hazard resolved = hazard(false, 10);
        resolved.gravity().decrease(10);

        List<InterventionEvent> events = maneuver(new FireEngine(), "extinguish")
                .use(engine, Targets.ofHazards(List.of(resolved)), calculator);

        assertTrue(events.isEmpty());
    }

    @Test
    void securingIgnoresTheHazardResistance() {
        HazardTemplate resistant = new HazardTemplate("r", "Resistente", 80, 5,
                Map.of(InterventionType.TECHNICAL, 0.5), false, 8, 5, "steady", 10);
        Hazard normalHit = new Hazard(resistant);
        Hazard securingHit = new Hazard(resistant);

        maneuver(new FireEngine(), "extinguish").use(engine, Targets.ofHazards(List.of(normalHit)), calculator);
        maneuver(new FireEngine(), "securing").use(engine, Targets.ofHazards(List.of(securingHit)), calculator);

        // Spegnimento: 14 × 1,6 × 0,5 = 11,2. Messa in sicurezza: 14 × 1,2 × 1,0 = 16,8
        assertEquals(80 - 11, normalHit.gravity().current());
        assertEquals(80 - 17, securingHit.gravity().current());
    }

    @Test
    void supportRestoresEnduranceOfTheTargetUnit() {
        engine.endurance().decrease(50);

        List<InterventionEvent> events = maneuver(new Ambulance(), "ambulance_support")
                .use(ambulance, Targets.ofUnits(List.of(engine)), calculator);

        // Medicina 12 × 1,2 = 14,4
        assertEquals(List.of(new Recovered(ambulance, engine,
                it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind.ENDURANCE, 14)), events);
        assertEquals(120 - 50 + 14, engine.endurance().current());
    }

    @Test
    void recoveryReportsOnlyWhatWasActuallyRestored() {
        engine.endurance().decrease(5);

        List<InterventionEvent> events = maneuver(new Ambulance(), "ambulance_support")
                .use(ambulance, Targets.ofUnits(List.of(engine)), calculator);

        assertEquals(5, ((Recovered) events.get(0)).amount());
        assertEquals(120, engine.endurance().current());
    }

    @Test
    void shiftChangeRestoresResourcesOfAllUnits() {
        engine.resources().decrease(20);
        ambulance.resources().decrease(20);

        maneuver(new PatrolCar(), "shift_change")
                .use(patrol, Targets.ofUnits(List.of(engine, ambulance)), calculator);

        // Coordinamento 15 × 1,0, ma il massimo limita il recupero
        assertEquals(engine.resources().max() - 5, engine.resources().current());
        assertEquals(ambulance.resources().max() - 5, ambulance.resources().current());
    }

    @Test
    void safetyCordonPutsUnitsUnderCover() {
        List<InterventionEvent> events = maneuver(new PatrolCar(), "safety_cordon")
                .use(patrol, Targets.ofUnits(List.of(engine, ambulance)), calculator);

        assertTrue(engine.isCovered());
        assertTrue(ambulance.isCovered());
        assertEquals(List.of(new TookCover(engine), new TookCover(ambulance)), events);
    }

    @Test
    void coverMakesSupportedUnitsSufferLessStress() {
        int exposed = calculator.stressTaken(engine, 11);

        maneuver(new PatrolCar(), "safety_cordon").use(patrol, Targets.ofUnits(List.of(engine)), calculator);

        assertTrue(calculator.stressTaken(engine, 11) < exposed);
    }

    @Test
    void advancedManeuverUnlocksAtLevelThree() {
        VehicleType type = new AerialLadder();

        assertEquals(2, type.maneuversAt(1).size());
        assertEquals(2, type.maneuversAt(2).size());
        assertEquals(3, type.maneuversAt(3).size());
    }

    @Test
    void costsAreNotNegative() {
        assertThrows(IllegalArgumentException.class, () -> new CoverManeuver("x", "X", -1, TargetScope.SELF));
    }

    @Test
    void targetsAreImmutableCopies() {
        List<Unit> source = new java.util.ArrayList<>(List.of(engine));
        Targets targets = Targets.ofUnits(source);

        source.clear();

        assertEquals(List.of(engine), targets.units());
        assertThrows(UnsupportedOperationException.class, () -> targets.units().clear());
    }
}
