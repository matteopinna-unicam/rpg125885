package it.unicam.cs.mpgc.rpg125885.model.shift;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Tutto ciò che si salva di una partita: flotta, turno, fase corrente, Indice di Crisi e registro. */
public final class GameState {

    private final Fleet fleet;
    private final Shift shift;
    private final CrisisIndex crisisIndex;
    private final List<CallRecord> callLog;
    private int phaseIndex;
    private boolean airAmbulanceUsed;

    /** Partita nuova: prima fase, Indice di Crisi a zero, registro vuoto. */
    public GameState(Fleet fleet, Shift shift) {
        this(fleet, shift, 0, new CrisisIndex(), List.of(), false);
    }

    /** Partita in corso, ad esempio caricata da un salvataggio. */
    public GameState(Fleet fleet, Shift shift, int phaseIndex, CrisisIndex crisisIndex,
                     List<CallRecord> callLog, boolean airAmbulanceUsed) {
        this.fleet = Objects.requireNonNull(fleet, "fleet");
        this.shift = Objects.requireNonNull(shift, "shift");
        this.crisisIndex = Objects.requireNonNull(crisisIndex, "crisisIndex");
        if (phaseIndex < 0 || phaseIndex >= shift.phaseCount()) {
            throw new IllegalArgumentException("Fase inesistente: " + phaseIndex);
        }
        this.phaseIndex = phaseIndex;
        this.callLog = new ArrayList<>(callLog);
        this.airAmbulanceUsed = airAmbulanceUsed;
    }

    public Fleet fleet() {
        return fleet;
    }

    public Shift shift() {
        return shift;
    }

    public CrisisIndex crisisIndex() {
        return crisisIndex;
    }

    public int phaseIndex() {
        return phaseIndex;
    }

    public Phase currentPhase() {
        return shift.phaseAt(phaseIndex);
    }

    public boolean hasNextPhase() {
        return phaseIndex < shift.phaseCount() - 1;
    }

    /** Passa alla fase successiva. */
    public void advancePhase() {
        if (!hasNextPhase()) {
            throw new IllegalStateException("La notte è già all'ultima fase");
        }
        phaseIndex++;
    }

    public List<CallRecord> callLog() {
        return Collections.unmodifiableList(callLog);
    }

    public void record(CallRecord entry) {
        callLog.add(Objects.requireNonNull(entry, "entry"));
    }

    /** L'Elisoccorso si può usare in un solo intervento per notte. */
    public boolean isAirAmbulanceUsed() {
        return airAmbulanceUsed;
    }

    public void markAirAmbulanceUsed() {
        airAmbulanceUsed = true;
    }
}
