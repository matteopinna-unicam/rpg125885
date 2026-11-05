package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.model.fleet.AerialLadder;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Agency;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProportionalMalusPolicyTest {

    private static final double DELTA = 1e-9;

    private final ProportionalMalusPolicy policy = new ProportionalMalusPolicy(DispatchFixture.vehicleTypes());

    @Test
    void noMalusWhenEveryRequirementIsMet() {
        Requirements requirements = new Requirements(
                Map.of(Agency.FIRE_BRIGADE, 1, Agency.EMS, 1), Set.of());

        DispatchAssessment assessment = policy.assess(requirements,
                List.of(DispatchFixture.engine(), DispatchFixture.ambulance()));

        assertTrue(assessment.isSatisfied());
        assertEquals(1.0, assessment.efficiency(), DELTA);
    }

    @Test
    void eachMissingAgencyCostsTwentyPercent() {
        Requirements requirements = new Requirements(
                Map.of(Agency.FIRE_BRIGADE, 1, Agency.POLICE, 1), Set.of());

        DispatchAssessment assessment = policy.assess(requirements, List.of(DispatchFixture.engine()));

        assertEquals(List.of("1 × " + Agency.POLICE.displayName()), assessment.missing());
        assertEquals(0.8, assessment.efficiency(), DELTA);
    }

    @Test
    void agencyMinimumIsCountedPerUnit() {
        Requirements requirements = new Requirements(Map.of(Agency.EMS, 2), Set.of());

        assertFalse(policy.assess(requirements, List.of(DispatchFixture.ambulance())).isSatisfied());
        assertTrue(policy.assess(requirements,
                List.of(DispatchFixture.ambulance(), DispatchFixture.airAmbulance())).isSatisfied());
    }

    @Test
    void missingVehicleTypeIsNamedWithItsDisplayName() {
        Requirements requirements = new Requirements(Map.of(), Set.of(new AerialLadder().id()));

        DispatchAssessment assessment = policy.assess(requirements, List.of(DispatchFixture.engine()));

        assertEquals(List.of(new AerialLadder().displayName()), assessment.missing());
        assertEquals(0.8, assessment.efficiency(), DELTA);
    }

    @Test
    void efficiencyNeverDropsBelowFortyPercent() {
        Requirements requirements = new Requirements(
                Map.of(Agency.FIRE_BRIGADE, 1, Agency.EMS, 1, Agency.POLICE, 1),
                Set.of(new AerialLadder().id()));

        DispatchAssessment assessment = policy.assess(requirements, List.<Unit>of(DispatchFixture.airAmbulance()));

        assertEquals(3, assessment.missing().size());
        assertEquals(0.4, assessment.efficiency(), DELTA);
    }
}
