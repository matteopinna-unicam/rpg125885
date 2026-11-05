package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Agency;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.item.RecoverySupply;
import it.unicam.cs.mpgc.rpg125885.model.item.ToolSupply;
import it.unicam.cs.mpgc.rpg125885.model.shift.Decision;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.model.shift.Phase;
import it.unicam.cs.mpgc.rpg125885.model.shift.PhaseType;
import it.unicam.cs.mpgc.rpg125885.model.shift.Reward;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.model.shift.Shift;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica il contenuto incluso nel JAR; si appoggia su id che i contenuti completi devono mantenere. */
class XmlContentCatalogTest {

    private static XmlContentCatalog catalog;

    @BeforeAll
    static void loadContent() throws PersistenceException {
        catalog = new XmlContentCatalog(PersistenceFixture.vehicleTypes());
    }

    @Test
    void hazardKeepsItsAttributesAndModifiers() {
        HazardTemplate fire = catalog.hazard("apartment_fire");

        assertEquals(80, fire.gravity());
        assertEquals(5, fire.pace());
        assertEquals(11, fire.stress());
        assertEquals(9, fire.escalation());
        assertEquals("escalating", fire.behaviorId());
        assertEquals(26, fire.xp());
        assertTrue(fire.elevated());
        assertEquals(1.4, fire.modifierFor(InterventionType.TECHNICAL));
        assertEquals(0.5, fire.modifierFor(InterventionType.MEDICAL));
        assertFalse(catalog.hazard("cardiac_arrest").elevated());
    }

    @Test
    void traitUsesNeutralValuesForMissingAttributes() {
        Trait veteran = catalog.trait("veteran");
        Trait empathic = catalog.trait("empathic");
        Trait rookie = catalog.trait("rookie");

        assertEquals(new Stats(0, 0, 0, 0, 0, 2, 0), veteran.bonus());
        assertEquals(1.0, veteran.recoveryMultiplier());
        assertEquals(Stats.NONE, empathic.bonus());
        assertEquals(1.2, empathic.recoveryMultiplier());
        assertEquals(1.2, rookie.experienceMultiplier());
    }

    @Test
    void itemsAreCreatedAccordingToTheirKind() {
        Gear backpack = assertInstanceOf(Gear.class, catalog.item("medical_backpack"));
        assertEquals(GearSlot.TOOL, backpack.slot());
        assertEquals(new Stats(0, 5, 0, 3, 0, 0, 0), backpack.bonus());

        assertInstanceOf(ToolSupply.class, catalog.item("foam_canister"));
        assertInstanceOf(RecoverySupply.class, catalog.item("energy_bar"));
        assertInstanceOf(RecoverySupply.class, catalog.item("spare_kit"));
    }

    @Test
    void shiftLinksPhasesToTheScenariosTheyOffer() {
        Shift shift = catalog.shift("red_shift");

        assertEquals(2, shift.phaseCount());
        Phase first = shift.phaseAt(0);
        assertEquals("22:00", first.time());
        assertEquals(PhaseType.CALLS, first.type());
        assertEquals(List.of("via_tigli_gas", "via_roma_crash"), first.scenarios().stream().map(Scenario::id).toList());
        assertEquals(List.of("flooded_underpass"),
                shift.phaseAt(1).scenarios().stream().map(Scenario::id).toList());
    }

    @Test
    void scenarioCarriesRequirementsDecisionAndOutcome() {
        Scenario gas = scenario("via_tigli_gas");

        assertEquals("Borgo Nuovo", gas.district());
        assertEquals(6, gas.rounds());
        assertEquals(1, gas.busyPhases());
        assertEquals(8, gas.ignorePenalty());
        assertEquals(12, gas.failPenalty());
        assertEquals(List.of(catalog.hazard("gas_leak")), gas.hazards());
        assertEquals(Map.of(Agency.FIRE_BRIGADE, 1), gas.requirements().minimumByAgency());
        assertTrue(gas.callText().startsWith("Pronto, 112?"));
        assertFalse(gas.callText().contains("\n"));
        assertEquals("Fuga chiusa, palazzo arieggiato: i residenti rientrano all'alba.", gas.successText());

        Decision decision = gas.decision().orElseThrow();
        assertEquals(new DecisionOption("Entrare subito", 0, 0, 10), decision.first());
        assertEquals(new DecisionOption("Attendere la chiusura del contatore", -20, -1, 0), decision.second());
    }

    @Test
    void scenarioWithoutDecisionHasSeveralHazardsAndRequirements() {
        Scenario crash = scenario("via_roma_crash");

        assertEquals(2, crash.hazards().size());
        assertEquals(Map.of(Agency.EMS, 1, Agency.POLICE, 1), crash.requirements().minimumByAgency());
        assertTrue(crash.decision().isEmpty());
    }

    @Test
    void itemRewardAddsTheQuantityToTheInventory() {
        Fleet fleet = new Fleet(List.of());

        scenario("via_roma_crash").reward().orElseThrow().applyTo(fleet);

        assertEquals(2, fleet.inventory().quantityOf(catalog.item("energy_bar")));
    }

    @Test
    void reinforcementRewardCreatesAFreshUnitEveryTime() {
        Reward reward = scenario("flooded_underpass").reward().orElseThrow();
        Fleet first = new Fleet(List.of());
        Fleet second = new Fleet(List.of());

        reward.applyTo(first);
        reward.applyTo(second);

        Unit unit = first.units().getFirst();
        assertEquals("ELI 1", unit.name());
        assertEquals("air_ambulance", unit.type().id());
        assertEquals("veteran", unit.trait().id());
        assertNotSame(unit, second.units().getFirst());
    }

    @Test
    void unknownIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> catalog.hazard("inesistente"));
        assertThrows(IllegalArgumentException.class, () -> catalog.item("inesistente"));
        assertThrows(IllegalArgumentException.class, () -> catalog.trait("inesistente"));
        assertThrows(IllegalArgumentException.class, () -> catalog.shift("inesistente"));
    }

    @Test
    void missingContentIsReported() {
        VehicleTypeRegistry types = PersistenceFixture.vehicleTypes();

        PersistenceException error = assertThrows(PersistenceException.class,
                () -> new XmlContentCatalog(types, "/assente/"));

        assertTrue(error.getMessage().contains("mancante"));
    }

    private static Scenario scenario(String id) {
        return catalog.shift("red_shift").phases().stream()
                .flatMap(phase -> phase.scenarios().stream())
                .filter(scenario -> scenario.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
