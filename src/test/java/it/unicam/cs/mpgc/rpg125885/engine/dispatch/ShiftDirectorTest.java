package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Agency;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.CrisisIndex;
import it.unicam.cs.mpgc.rpg125885.model.shift.Decision;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;
import it.unicam.cs.mpgc.rpg125885.model.shift.Reward;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.model.shift.Shift;
import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftOutcome;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static it.unicam.cs.mpgc.rpg125885.engine.dispatch.DispatchFixture.EASY_GRAVITY;
import static it.unicam.cs.mpgc.rpg125885.engine.dispatch.DispatchFixture.IMPOSSIBLE_GRAVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShiftDirectorTest {

    private static final int START_CRISIS = 20;

    private final Unit engine = DispatchFixture.engine();
    private final Unit ambulance = DispatchFixture.ambulance();
    private final Unit patrol = DispatchFixture.patrol();
    private final Unit airAmbulance = DispatchFixture.airAmbulance();

    private ShiftDirector start(Scenario... firstPhase) {
        return start(List.of(DispatchFixture.calls("22:00", firstPhase), DispatchFixture.calls("23:00")));
    }

    private ShiftDirector start(List<it.unicam.cs.mpgc.rpg125885.model.shift.Phase> phases) {
        Fleet fleet = new Fleet(new ArrayList<>(List.of(engine, ambulance, patrol, airAmbulance)));
        GameState state = new GameState(fleet, new Shift("test", phases), 0, new CrisisIndex(START_CRISIS),
                List.of(), false);
        return DispatchFixture.director(state);
    }

    private InterventionReport playNext(ShiftDirector director) {
        DispatchFixture.play(director.begin(Optional.empty()));
        return director.finish();
    }

    // --- assegnazioni ---

    @Test
    void assignmentIsLimitedToThreeUnitsOutsideTheFinale() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY);
        ShiftDirector director = start(call);

        assertThrows(IllegalArgumentException.class,
                () -> director.assign(call, List.of(engine, ambulance, patrol, airAmbulance)));
        director.assign(call, List.of(engine, ambulance, patrol));

        assertEquals(1, director.assignments().size());
    }

    @Test
    void finaleAllowsFourUnits() {
        Scenario call = DispatchFixture.scenario("final", EASY_GRAVITY);
        ShiftDirector director = start(List.of(DispatchFixture.finale("06:00", call)));

        director.assign(call, List.of(engine, ambulance, patrol, airAmbulance));

        assertEquals(4, director.maxUnitsPerCall());
    }

    @Test
    void aUnitCannotServeTwoCallsInTheSamePhase() {
        Scenario first = DispatchFixture.scenario("a", EASY_GRAVITY);
        Scenario second = DispatchFixture.scenario("b", EASY_GRAVITY);
        ShiftDirector director = start(first, second);
        director.assign(first, List.of(engine));

        assertFalse(director.availableUnits().contains(engine));
        assertThrows(IllegalArgumentException.class, () -> director.assign(second, List.of(engine, ambulance)));
    }

    @Test
    void assigningAgainReplacesThePreviousAssignment() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY);
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));

        director.assign(call, List.of(engine, ambulance));

        assertEquals(List.of(engine, ambulance), director.assignments().get(0).units());
    }

    @Test
    void rejectsCallsOfOtherPhasesAndRepeatedUnits() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY);
        Scenario elsewhere = DispatchFixture.scenario("elsewhere", EASY_GRAVITY);
        ShiftDirector director = start(call);

        assertThrows(IllegalArgumentException.class, () -> director.assign(elsewhere, List.of(engine)));
        assertThrows(IllegalArgumentException.class, () -> director.assign(call, List.of(engine, engine)));
        assertThrows(IllegalArgumentException.class, () -> director.assign(call, List.of()));
    }

    @Test
    void previewReportsMissingRequirementsWithoutAssigning() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY, 3,
                new Requirements(Map.of(Agency.POLICE, 1), Set.of()), Optional.empty(), Optional.empty());
        ShiftDirector director = start(call);

        DispatchAssessment assessment = director.preview(call, List.of(engine));

        assertFalse(assessment.isSatisfied());
        assertEquals(0.8, assessment.efficiency(), 1e-9);
        assertTrue(director.assignments().isEmpty());
    }

    // --- interventi ---

    @Test
    void successLowersCrisisAwardsExperienceRewardsAndRecords() {
        Reward reward = Reward.ofItem(DispatchFixture.supply(), 2);
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY, 3, Requirements.NONE,
                Optional.empty(), Optional.of(reward));
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));

        InterventionReport report = playNext(director);

        assertEquals(CallOutcome.RESOLVED, report.outcome());
        assertEquals("Successo a", report.text());
        assertEquals(START_CRISIS - 5, director.state().crisisIndex().value());
        assertEquals(20, engine.experience());
        assertEquals(2, director.state().fleet().inventory().quantityOf(DispatchFixture.supply()));
        assertEquals(Optional.of(reward.description()), report.rewardText());
        assertEquals(CallOutcome.RESOLVED, director.state().callLog().get(0).outcome());
        assertEquals(List.of(engine.name()), director.state().callLog().get(0).unitNames());
    }

    @Test
    void failureRaisesCrisisByTheFailPenaltyAndGivesNoReward() {
        Scenario call = DispatchFixture.scenario("a", IMPOSSIBLE_GRAVITY, 2, Requirements.NONE,
                Optional.empty(), Optional.of(Reward.ofItem(DispatchFixture.supply(), 1)));
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));

        InterventionReport report = playNext(director);

        assertEquals(CallOutcome.FAILED, report.outcome());
        assertEquals("Fallimento a", report.text());
        assertEquals(START_CRISIS + 12, director.state().crisisIndex().value());
        assertEquals(0, director.state().fleet().inventory().quantityOf(DispatchFixture.supply()));
        assertEquals(Optional.empty(), report.rewardText());
        assertEquals(0, engine.experience());
    }

    @Test
    void interventionsArePlayedOneAfterTheOtherInAssignmentOrder() {
        Scenario first = DispatchFixture.scenario("a", EASY_GRAVITY);
        Scenario second = DispatchFixture.scenario("b", EASY_GRAVITY);
        ShiftDirector director = start(first, second);
        director.assign(second, List.of(ambulance));
        director.assign(first, List.of(engine));

        assertEquals("b", playNext(director).scenario().id());
        assertEquals("a", playNext(director).scenario().id());
        assertTrue(director.nextAssignment().isEmpty());
    }

    @Test
    void usedUnitsStayBusyForTheScenarioBusyPhases() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY); // occupazione: 1 fase
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));

        playNext(director);

        assertFalse(engine.isAvailableAt(1));
        assertTrue(engine.isAvailableAt(2));
        assertTrue(ambulance.isAvailableAt(1));
    }

    @Test
    void anExhaustedUnitComesBackAtThirtyPercentAndStaysBusyOnePhaseLonger() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY);
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));
        DispatchFixture.play(director.begin(Optional.empty()));
        engine.endurance().decrease(engine.endurance().max());

        director.finish();

        assertEquals(Math.round(engine.endurance().max() * 0.30), engine.endurance().current());
        assertFalse(engine.isAvailableAt(2));
        assertTrue(engine.isAvailableAt(3));
    }

    @Test
    void finishFailsWhileTheInterventionIsStillOngoing() {
        Scenario call = DispatchFixture.scenario("a", IMPOSSIBLE_GRAVITY);
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));
        director.begin(Optional.empty()).start();

        assertThrows(IllegalStateException.class, director::finish);
        assertThrows(IllegalStateException.class, () -> director.begin(Optional.empty()));
    }

    // --- scelta narrativa ---

    @Test
    void aCallWithADecisionRequiresAValidChoice() {
        DecisionOption first = new DecisionOption("Subito", 0, 0, 0);
        DecisionOption second = new DecisionOption("Attendere", -20, -1, 0);
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY, 3, Requirements.NONE,
                Optional.of(new Decision("Come procedete?", first, second)), Optional.empty());
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));

        assertThrows(IllegalArgumentException.class, () -> director.begin(Optional.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> director.begin(Optional.of(new DecisionOption("Altro", 0, 0, 0))));
        assertEquals(2, director.begin(Optional.of(second)).roundsRemaining());
    }

    // --- chiusura della fase ---

    @Test
    void ignoredCallsRaiseCrisisAndAreRecordedWhenThePhaseCloses() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY);
        ShiftDirector director = start(call);

        director.closePhase();

        assertEquals(START_CRISIS + 8, director.state().crisisIndex().value());
        assertEquals(CallOutcome.IGNORED, director.state().callLog().get(0).outcome());
        assertEquals(1, director.state().phaseIndex());
        assertTrue(director.isBetweenPhases());
    }

    @Test
    void closingThePhaseWithInterventionsPendingIsRefused() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY);
        ShiftDirector director = start(call);
        director.assign(call, List.of(engine));

        assertThrows(IllegalStateException.class, director::closePhase);
        assertFalse(director.isBetweenPhases());
    }

    @Test
    void assignmentsAreRefusedOnceInterventionsHaveStarted() {
        Scenario first = DispatchFixture.scenario("a", EASY_GRAVITY);
        Scenario second = DispatchFixture.scenario("b", EASY_GRAVITY);
        ShiftDirector director = start(first, second);
        director.assign(first, List.of(engine));
        playNext(director);

        assertThrows(IllegalStateException.class, () -> director.assign(second, List.of(ambulance)));
    }

    // --- Elisoccorso ---

    @Test
    void theAirAmbulanceServesOnlyOneInterventionPerNight() {
        Scenario first = DispatchFixture.scenario("a", EASY_GRAVITY);
        Scenario second = DispatchFixture.scenario("b", EASY_GRAVITY);
        ShiftDirector director = start(List.of(DispatchFixture.calls("22:00", first),
                DispatchFixture.calls("23:00"), DispatchFixture.calls("00:00", second),
                DispatchFixture.calls("01:00")));
        director.assign(first, List.of(airAmbulance));
        playNext(director);
        director.closePhase();
        director.closePhase();
        director.closePhase();

        assertTrue(airAmbulance.isAvailableAt(director.state().phaseIndex()));
        assertTrue(director.state().isAirAmbulanceUsed());
        assertFalse(director.availableUnits().contains(airAmbulance));
    }

    // --- base ---

    @Test
    void restRefillsEveryUnitAndMakesThemAvailable() {
        ShiftDirector director = start(List.of(DispatchFixture.base("02:00"), DispatchFixture.calls("03:00")));
        engine.endurance().decrease(50);
        engine.resources().decrease(10);
        engine.occupyUntil(5);

        director.applyBase(BaseOption.REST);

        assertEquals(engine.endurance().max(), engine.endurance().current());
        assertEquals(engine.resources().max(), engine.resources().current());
        assertTrue(engine.isAvailableAt(director.state().phaseIndex()));
        assertEquals(1, director.state().phaseIndex());
    }

    @Test
    void resupplyAddsThePackageAndRecoversAQuarterOfEndurance() {
        ShiftDirector director = start(List.of(DispatchFixture.base("02:00"), DispatchFixture.calls("03:00")));
        engine.endurance().decrease(engine.endurance().max());

        director.applyBase(BaseOption.RESUPPLY);

        assertEquals(Math.round(engine.endurance().max() * 0.25), engine.endurance().current());
        assertEquals(2, director.state().fleet().inventory().quantityOf(DispatchFixture.supply()));
    }

    @Test
    void baseOptionsAndPhaseClosingAreMutuallyExclusive() {
        ShiftDirector baseDirector = start(List.of(DispatchFixture.base("02:00"), DispatchFixture.calls("03:00")));
        ShiftDirector callsDirector = start(DispatchFixture.scenario("a", EASY_GRAVITY));

        assertThrows(IllegalStateException.class, baseDirector::closePhase);
        assertThrows(IllegalStateException.class, () -> callsDirector.applyBase(BaseOption.REST));
    }

    // --- fine della notte ---

    @Test
    void resolvingTheFinaleWinsTheNight() {
        Scenario call = DispatchFixture.scenario("final", EASY_GRAVITY);
        ShiftDirector director = start(List.of(DispatchFixture.finale("06:00", call)));
        director.assign(call, List.of(engine));
        playNext(director);

        assertEquals(ShiftOutcome.IN_PROGRESS, director.outcome());
        director.closePhase();

        assertEquals(ShiftOutcome.VICTORY, director.outcome());
    }

    @Test
    void failingOrIgnoringTheFinaleLosesTheNight() {
        Scenario call = DispatchFixture.scenario("final", IMPOSSIBLE_GRAVITY, 2, Requirements.NONE,
                Optional.empty(), Optional.empty());
        ShiftDirector failed = start(List.of(DispatchFixture.finale("06:00", call)));
        failed.assign(call, List.of(engine));
        playNext(failed);
        failed.closePhase();
        ShiftDirector ignored = start(List.of(DispatchFixture.finale("06:00", call)));
        ignored.closePhase();

        assertEquals(ShiftOutcome.DEFEAT, failed.outcome());
        assertEquals(ShiftOutcome.DEFEAT, ignored.outcome());
    }

    @Test
    void aCriticalCrisisIndexLosesTheNightAtOnceAndBlocksFurtherActions() {
        Scenario call = DispatchFixture.scenario("a", EASY_GRAVITY);
        ShiftDirector director = start(call);
        director.state().crisisIndex().raise(CrisisIndex.MAX);

        assertEquals(ShiftOutcome.DEFEAT, director.outcome());
        assertThrows(IllegalStateException.class, () -> director.assign(call, List.of(engine)));
        assertThrows(IllegalStateException.class, director::closePhase);
    }
}
