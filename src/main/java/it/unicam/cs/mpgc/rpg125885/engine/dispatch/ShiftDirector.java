package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionStatus;
import it.unicam.cs.mpgc.rpg125885.engine.progression.LevelUp;
import it.unicam.cs.mpgc.rpg125885.engine.progression.ProgressionService;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Gauge;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallRecord;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.model.shift.Phase;
import it.unicam.cs.mpgc.rpg125885.model.shift.PhaseType;
import it.unicam.cs.mpgc.rpg125885.model.shift.Reward;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftOutcome;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Conduce la notte fase per fase: chiamate e assegnazioni, interventi in sequenza, conseguenze
 * (Indice di Crisi, esperienza, premi, occupazione delle unità), sosta in base e fine del turno.
 * Non conosce la GUI né il salvataggio: lavora sullo {@link GameState} che riceve.
 */
public final class ShiftDirector {

    public static final int MAX_UNITS_PER_CALL = 3;
    public static final int MAX_UNITS_IN_FINALE = 4;
    public static final String AIR_AMBULANCE_ID = "air_ambulance";

    private static final int SUCCESS_RELIEF = 5;
    private static final double EXHAUSTED_RECOVERY = 0.30;
    private static final double RESUPPLY_RECOVERY = 0.25;

    private final GameState state;
    private final RequirementPolicy requirementPolicy;
    private final ScenarioFactory scenarioFactory;
    private final ProgressionService progression;
    private final Map<Item, Integer> resupplyPackage;

    private final Map<String, Assignment> assignments = new LinkedHashMap<>();
    private final Map<String, CallOutcome> phaseResults = new HashMap<>();
    private ActiveCall active;
    private boolean playing;
    private ShiftOutcome finalOutcome = ShiftOutcome.IN_PROGRESS;

    /**
     * @param resupplyPackage consumabili ricevuti scegliendo il rifornimento in base
     */
    public ShiftDirector(GameState state, RequirementPolicy requirementPolicy, ScenarioFactory scenarioFactory,
                         ProgressionService progression, Map<Item, Integer> resupplyPackage) {
        this.state = Objects.requireNonNull(state, "state");
        this.requirementPolicy = Objects.requireNonNull(requirementPolicy, "requirementPolicy");
        this.scenarioFactory = Objects.requireNonNull(scenarioFactory, "scenarioFactory");
        this.progression = Objects.requireNonNull(progression, "progression");
        this.resupplyPackage = Map.copyOf(resupplyPackage);
    }

    public GameState state() {
        return state;
    }

    public Phase currentPhase() {
        return state.currentPhase();
    }

    /** Le chiamate della fase corrente. */
    public List<Scenario> calls() {
        return currentPhase().scenarios();
    }

    /** Vittoria o sconfitta; con l'Indice di Crisi al massimo la notte è persa in qualunque momento. */
    public ShiftOutcome outcome() {
        return state.crisisIndex().isCritical() ? ShiftOutcome.DEFEAT : finalOutcome;
    }

    /** Fra due fasi, senza assegnazioni né interventi in corso: l'unico momento in cui si può salvare. */
    public boolean isBetweenPhases() {
        return !playing && assignments.isEmpty() && active == null;
    }

    public int maxUnitsPerCall() {
        return currentPhase().type() == PhaseType.FINALE ? MAX_UNITS_IN_FINALE : MAX_UNITS_PER_CALL;
    }

    /** Unità che si possono ancora assegnare: libere, non già impegnate in un'altra chiamata. */
    public List<Unit> availableUnits() {
        return availableFor(Optional.empty());
    }

    /** Requisiti mancanti e malus che avrebbe questa assegnazione. */
    public DispatchAssessment preview(Scenario scenario, List<Unit> units) {
        return requirementPolicy.assess(scenario.requirements(), units);
    }

    /**
     * Assegna le unità a una chiamata della fase corrente, sostituendo un'assegnazione precedente.
     *
     * @throws IllegalArgumentException se la chiamata non è della fase, le unità sono troppe, ripetute o non disponibili
     * @throws IllegalStateException    se gli interventi sono già iniziati o la notte è finita
     */
    public void assign(Scenario scenario, List<Unit> units) {
        requireOngoing();
        if (playing) {
            throw new IllegalStateException("Gli interventi sono già iniziati");
        }
        requireCallOfCurrentPhase(scenario);
        Assignment assignment = new Assignment(scenario, units);
        if (assignment.units().size() > maxUnitsPerCall()) {
            throw new IllegalArgumentException("Al massimo " + maxUnitsPerCall() + " unità per chiamata");
        }
        if (assignment.units().stream().distinct().count() != assignment.units().size()) {
            throw new IllegalArgumentException("Un'unità è indicata più volte");
        }
        List<Unit> allowed = availableFor(Optional.of(scenario));
        for (Unit unit : assignment.units()) {
            if (!allowed.contains(unit)) {
                throw new IllegalArgumentException("Unità non disponibile: " + unit.name());
            }
        }
        assignments.put(scenario.id(), assignment);
    }

    /** Toglie l'assegnazione di una chiamata; non fa nulla se non c'era. */
    public void unassign(Scenario scenario) {
        if (playing) {
            throw new IllegalStateException("Gli interventi sono già iniziati");
        }
        assignments.remove(scenario.id());
    }

    public List<Assignment> assignments() {
        return List.copyOf(assignments.values());
    }

    /** L'intervento da giocare ora, se ce n'è uno in attesa di iniziare. */
    public Optional<Assignment> nextAssignment() {
        return assignments.values().stream().findFirst();
    }

    /**
     * Prepara il prossimo intervento nell'ordine delle assegnazioni. L'intervento non è ancora avviato:
     * chi lo gioca aggiunge i propri listener e poi chiama {@link Intervention#start()}.
     *
     * @param choice scelta narrativa, obbligatoria se la chiamata ne prevede una
     */
    public Intervention begin(Optional<DecisionOption> choice) {
        requireOngoing();
        if (active != null) {
            throw new IllegalStateException("C'è già un intervento in corso");
        }
        Assignment assignment = nextAssignment()
                .orElseThrow(() -> new IllegalStateException("Nessun intervento da giocare"));
        Scenario scenario = assignment.scenario();
        requireValidChoice(scenario, choice);
        DispatchAssessment assessment = requirementPolicy.assess(scenario.requirements(), assignment.units());
        Intervention intervention = scenarioFactory.create(scenario, choice, assignment.units(),
                assessment.efficiency());
        assignments.remove(scenario.id());
        playing = true;
        if (assignment.units().stream().anyMatch(ShiftDirector::isAirAmbulance)) {
            state.markAirAmbulanceUsed();
        }
        active = new ActiveCall(assignment, intervention);
        return intervention;
    }

    /**
     * Applica le conseguenze dell'intervento concluso: Indice di Crisi, esperienza, premio, registro
     * e occupazione delle unità.
     *
     * @throws IllegalStateException se non c'è un intervento o non è ancora finito
     */
    public InterventionReport finish() {
        if (active == null) {
            throw new IllegalStateException("Nessun intervento in corso");
        }
        Intervention intervention = active.intervention();
        if (intervention.status() == InterventionStatus.ONGOING) {
            throw new IllegalStateException("L'intervento non è ancora concluso");
        }
        Scenario scenario = active.assignment().scenario();
        List<Unit> units = active.assignment().units();
        boolean success = intervention.status() == InterventionStatus.RESOLVED;
        List<LevelUp> levelUps = List.of();
        Optional<String> rewardText = Optional.empty();
        if (success) {
            state.crisisIndex().lower(SUCCESS_RELIEF);
            levelUps = progression.awardExperience(units, totalExperience(scenario));
            rewardText = scenario.reward().map(this::grant);
        } else {
            state.crisisIndex().raise(scenario.failPenalty());
        }
        releaseUnits(units, scenario.busyPhases());
        CallOutcome outcome = success ? CallOutcome.RESOLVED : CallOutcome.FAILED;
        recordCall(scenario, outcome, units.stream().map(Unit::name).toList());
        active = null;
        return new InterventionReport(scenario, outcome, success ? scenario.successText() : scenario.failureText(),
                levelUps, rewardText);
    }

    /**
     * Chiude la fase: le chiamate senza intervento contano come ignorate e la notte passa alla fase
     * successiva. Dopo l'ultima fase stabilisce vittoria o sconfitta.
     *
     * @throws IllegalStateException se restano interventi da giocare o la fase è una sosta in base
     */
    public void closePhase() {
        requireOngoing();
        Phase phase = currentPhase();
        if (phase.type() == PhaseType.BASE) {
            throw new IllegalStateException("In base si sceglie un'opzione con applyBase");
        }
        if (active != null || !assignments.isEmpty()) {
            throw new IllegalStateException("Restano interventi da giocare");
        }
        for (Scenario scenario : phase.scenarios()) {
            if (!phaseResults.containsKey(scenario.id())) {
                state.crisisIndex().raise(scenario.ignorePenalty());
                recordCall(scenario, CallOutcome.IGNORED, List.of());
            }
        }
        if (state.hasNextPhase()) {
            state.advancePhase();
        } else {
            finalOutcome = allResolved(phase) ? ShiftOutcome.VICTORY : ShiftOutcome.DEFEAT;
        }
        phaseResults.clear();
        playing = false;
    }

    /**
     * Sosta in base per tutta la flotta, poi passa alla fase successiva.
     *
     * @throws IllegalStateException se la fase corrente non è una sosta in base
     */
    public void applyBase(BaseOption option) {
        requireOngoing();
        if (currentPhase().type() != PhaseType.BASE) {
            throw new IllegalStateException("La fase corrente non è una sosta in base");
        }
        List<Unit> units = state.fleet().units();
        switch (option) {
            case REST -> units.forEach(this::rest);
            case RESUPPLY -> {
                resupplyPackage.forEach((item, quantity) -> state.fleet().inventory().add(item, quantity));
                units.forEach(unit -> restore(unit.endurance(), RESUPPLY_RECOVERY));
            }
        }
        state.advancePhase();
    }

    private void rest(Unit unit) {
        unit.endurance().fill();
        unit.resources().fill();
        unit.occupyUntil(state.phaseIndex());
    }

    private void releaseUnits(List<Unit> units, int busyPhases) {
        int freeAt = state.phaseIndex() + 1 + busyPhases;
        for (Unit unit : units) {
            boolean exhausted = unit.endurance().isEmpty();
            if (exhausted) {
                restore(unit.endurance(), EXHAUSTED_RECOVERY);
            }
            unit.occupyUntil(exhausted ? freeAt + 1 : freeAt);
        }
    }

    private static void restore(Gauge gauge, double fraction) {
        gauge.increase(Math.max(1, (int) Math.round(gauge.max() * fraction)));
    }

    private String grant(Reward reward) {
        reward.applyTo(state.fleet());
        return reward.description();
    }

    private static int totalExperience(Scenario scenario) {
        return scenario.hazards().stream().mapToInt(HazardTemplate::xp).sum();
    }

    private void recordCall(Scenario scenario, CallOutcome outcome, List<String> unitNames) {
        state.record(new CallRecord(scenario.id(), outcome, unitNames));
        phaseResults.put(scenario.id(), outcome);
    }

    private boolean allResolved(Phase phase) {
        boolean everyCallResolved = phase.scenarios().stream()
                .allMatch(scenario -> phaseResults.get(scenario.id()) == CallOutcome.RESOLVED);
        return everyCallResolved && !state.crisisIndex().isCritical();
    }

    private List<Unit> availableFor(Optional<Scenario> own) {
        List<Unit> busyWithOthers = assignments.values().stream()
                .filter(assignment -> own.isEmpty() || !assignment.scenario().id().equals(own.get().id()))
                .flatMap(assignment -> assignment.units().stream())
                .toList();
        return state.fleet().availableUnits(state.phaseIndex()).stream()
                .filter(unit -> !busyWithOthers.contains(unit))
                .filter(unit -> !(isAirAmbulance(unit) && state.isAirAmbulanceUsed()))
                .toList();
    }

    private static boolean isAirAmbulance(Unit unit) {
        return unit.type().id().equals(AIR_AMBULANCE_ID);
    }

    private void requireOngoing() {
        if (outcome() != ShiftOutcome.IN_PROGRESS) {
            throw new IllegalStateException("La notte è finita: " + outcome());
        }
    }

    private void requireCallOfCurrentPhase(Scenario scenario) {
        boolean inPhase = calls().stream().anyMatch(call -> call.id().equals(scenario.id()));
        if (!inPhase) {
            throw new IllegalArgumentException("La chiamata non è della fase corrente: " + scenario.id());
        }
    }

    private static void requireValidChoice(Scenario scenario, Optional<DecisionOption> choice) {
        if (scenario.decision().isPresent() && choice.isEmpty()) {
            throw new IllegalArgumentException("La chiamata richiede una scelta: " + scenario.id());
        }
        boolean valid = choice.isEmpty()
                || scenario.decision().map(decision -> decision.options().contains(choice.get())).orElse(false);
        if (!valid) {
            throw new IllegalArgumentException("Scelta non prevista dalla chiamata: " + scenario.id());
        }
    }

    private record ActiveCall(Assignment assignment, Intervention intervention) {
    }
}
