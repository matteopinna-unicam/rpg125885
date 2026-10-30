package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.engine.behavior.BehaviorRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.rules.RandomSource;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.InterventionEnded;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.RoundStarted;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.TurnStarted;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionListener;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Targets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Conduce un intervento a turni. Dopo {@link #start()} il motore gioca da solo i turni dei pericoli
 * e si ferma quando tocca a un'unità: il chiamante sceglie l'azione e la passa a {@link #submit}.
 */
public final class Intervention {

    private final List<Unit> units;
    private final List<Hazard> hazards;
    private final EffectCalculator calculator;
    private final RandomSource random;
    private final TurnOrderPolicy turnOrder;
    private final BehaviorRegistry behaviors;
    private final List<InterventionListener> listeners = new ArrayList<>();
    private final List<InterventionEvent> log = new ArrayList<>();
    private final Deque<Combatant> turnQueue = new ArrayDeque<>();

    private int round;
    private int roundsRemaining;
    private InterventionStatus status = InterventionStatus.ONGOING;
    private Unit awaitingUnit;
    private boolean started;

    /**
     * @param rounds     round disponibili per risolvere i pericoli
     * @param calculator formule degli effetti, già con l'eventuale malus sui requisiti
     */
    public Intervention(List<Unit> units, List<Hazard> hazards, int rounds, EffectCalculator calculator,
                        RandomSource random, TurnOrderPolicy turnOrder, BehaviorRegistry behaviors) {
        this.units = List.copyOf(units);
        this.hazards = List.copyOf(hazards);
        if (this.units.isEmpty() || this.hazards.isEmpty()) {
            throw new IllegalArgumentException("Servono almeno un'unità e un pericolo");
        }
        if (rounds <= 0) {
            throw new IllegalArgumentException("I round devono essere positivi: " + rounds);
        }
        this.roundsRemaining = rounds;
        this.calculator = Objects.requireNonNull(calculator, "calculator");
        this.random = Objects.requireNonNull(random, "random");
        this.turnOrder = Objects.requireNonNull(turnOrder, "turnOrder");
        this.behaviors = Objects.requireNonNull(behaviors, "behaviors");
    }

    public void addListener(InterventionListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Avvia il primo round e gioca i turni fino alla prima unità. Restituisce gli eventi prodotti. */
    public List<InterventionEvent> start() {
        if (started) {
            throw new IllegalStateException("L'intervento è già iniziato");
        }
        started = true;
        List<InterventionEvent> produced = new ArrayList<>();
        beginRound(produced);
        advance(produced);
        return produced;
    }

    /**
     * Esegue l'azione dell'unità di turno, poi gioca i turni successivi fino alla prossima unità
     * o alla fine dell'intervento. Restituisce gli eventi prodotti.
     *
     * @throws IllegalStateException    se l'intervento non è in attesa di un'azione
     * @throws IllegalArgumentException se l'azione non è dell'unità di turno
     */
    public List<InterventionEvent> submit(InterventionAction action) {
        if (awaitingUnit == null) {
            throw new IllegalStateException("L'intervento non attende azioni");
        }
        if (action.actor() != awaitingUnit) {
            throw new IllegalArgumentException("Non è il turno di " + action.actor().name());
        }
        List<InterventionEvent> produced = new ArrayList<>();
        record(produced, action.execute(this));
        awaitingUnit = null;
        if (!checkEnd(produced)) {
            advance(produced);
        }
        return produced;
    }

    public InterventionStatus status() {
        return status;
    }

    /** Numero del round corrente, da 1. */
    public int round() {
        return round;
    }

    /** Round ancora disponibili, compreso quello in corso. */
    public int roundsRemaining() {
        return roundsRemaining;
    }

    /** L'unità che deve scegliere un'azione, se l'intervento è in corso. */
    public Optional<Unit> currentUnit() {
        return Optional.ofNullable(awaitingUnit);
    }

    public List<Unit> activeUnits() {
        return units.stream().filter(Unit::isActive).toList();
    }

    public List<Hazard> activeHazards() {
        return hazards.stream().filter(Hazard::isActive).toList();
    }

    public EffectCalculator calculator() {
        return calculator;
    }

    public RandomSource random() {
        return random;
    }

    /** Tutti gli eventi dall'inizio dell'intervento. */
    public List<InterventionEvent> events() {
        return Collections.unmodifiableList(log);
    }

    /**
     * Traduce l'ambito di un effetto nei bersagli concreti.
     *
     * @throws IllegalArgumentException se la scelta manca o indica un bersaglio non valido
     */
    public Targets resolveTargets(TargetScope scope, Unit actor, TargetChoice choice) {
        return switch (scope) {
            case SINGLE_HAZARD -> Targets.ofHazards(List.of(chosenHazard(choice)));
            case ALL_HAZARDS -> Targets.ofHazards(activeHazards());
            case SINGLE_UNIT -> Targets.ofUnits(List.of(chosenUnit(choice)));
            case ALL_UNITS -> Targets.ofUnits(activeUnits());
            case SELF -> Targets.ofUnits(List.of(actor));
        };
    }

    private Hazard chosenHazard(TargetChoice choice) {
        Hazard hazard = choice.hazard()
                .orElseThrow(() -> new IllegalArgumentException("Serve un pericolo bersaglio"));
        if (!activeHazards().contains(hazard)) {
            throw new IllegalArgumentException("Pericolo non valido o già risolto: " + hazard.name());
        }
        return hazard;
    }

    private Unit chosenUnit(TargetChoice choice) {
        Unit unit = choice.unit()
                .orElseThrow(() -> new IllegalArgumentException("Serve un'unità bersaglio"));
        if (!activeUnits().contains(unit)) {
            throw new IllegalArgumentException("Unità non valida o esausta: " + unit.name());
        }
        return unit;
    }

    private void beginRound(List<InterventionEvent> produced) {
        round++;
        record(produced, new RoundStarted(round));
        turnQueue.clear();
        turnQueue.addAll(turnOrder.order(activeUnits(), activeHazards()));
    }

    /** Gioca i turni fino a quando tocca a un'unità o l'intervento finisce. */
    private void advance(List<InterventionEvent> produced) {
        while (status == InterventionStatus.ONGOING) {
            Combatant next = turnQueue.poll();
            if (next == null) {
                endRound(produced);
            } else if (next.isActive()) {
                record(produced, new TurnStarted(next));
                Optional<Unit> unit = unitOf(next);
                if (unit.isPresent()) {
                    unit.get().endCover();
                    awaitingUnit = unit.get();
                    return;
                }
                playHazardTurn(hazardOf(next), produced);
            }
        }
    }

    private void playHazardTurn(Hazard hazard, List<InterventionEvent> produced) {
        InterventionAction action = behaviors.byId(hazard.template().behaviorId()).chooseAction(hazard, this);
        record(produced, action.execute(this));
        checkEnd(produced);
    }

    private void endRound(List<InterventionEvent> produced) {
        roundsRemaining--;
        if (roundsRemaining == 0) {
            finish(false, produced);
        } else {
            beginRound(produced);
        }
    }

    /** Termina l'intervento se i pericoli sono risolti o le unità sono tutte esauste. */
    private boolean checkEnd(List<InterventionEvent> produced) {
        if (activeHazards().isEmpty()) {
            finish(true, produced);
        } else if (activeUnits().isEmpty()) {
            finish(false, produced);
        }
        return status != InterventionStatus.ONGOING;
    }

    private void finish(boolean successful, List<InterventionEvent> produced) {
        status = successful ? InterventionStatus.RESOLVED : InterventionStatus.FAILED;
        units.forEach(Unit::endCover);
        record(produced, new InterventionEnded(successful));
    }

    private Optional<Unit> unitOf(Combatant combatant) {
        return units.stream().filter(unit -> unit == combatant).findFirst();
    }

    private Hazard hazardOf(Combatant combatant) {
        return hazards.stream().filter(hazard -> hazard == combatant).findFirst().orElseThrow();
    }

    private void record(List<InterventionEvent> produced, List<InterventionEvent> events) {
        events.forEach(event -> record(produced, event));
    }

    private void record(List<InterventionEvent> produced, InterventionEvent event) {
        produced.add(event);
        log.add(event);
        listeners.forEach(listener -> listener.onEvent(event));
    }
}
