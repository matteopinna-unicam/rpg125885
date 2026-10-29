package it.unicam.cs.mpgc.rpg125885.model.shift;

import java.util.List;
import java.util.Objects;

/**
 * Voce del registro delle chiamate.
 *
 * @param unitNames nominativi delle unità impiegate; vuoto se la chiamata è stata ignorata
 */
public record CallRecord(String scenarioId, CallOutcome outcome, List<String> unitNames) {

    public CallRecord {
        Objects.requireNonNull(scenarioId, "scenarioId");
        Objects.requireNonNull(outcome, "outcome");
        unitNames = List.copyOf(unitNames);
    }
}
