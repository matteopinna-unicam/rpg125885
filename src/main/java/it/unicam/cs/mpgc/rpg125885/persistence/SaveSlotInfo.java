package it.unicam.cs.mpgc.rpg125885.persistence;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Riepilogo di uno slot, letto senza ricostruire la partita.
 *
 * @param slot       nome dello slot
 * @param fleetName  nome della flotta salvata
 * @param savedAt    data e ora del salvataggio
 * @param phaseIndex fase raggiunta (da zero)
 * @param crisis     Indice di Crisi
 * @param unitCount  numero di unità
 */
public record SaveSlotInfo(
        String slot,
        String fleetName,
        LocalDateTime savedAt,
        int phaseIndex,
        int crisis,
        int unitCount) {

    public SaveSlotInfo {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(fleetName, "fleetName");
        Objects.requireNonNull(savedAt, "savedAt");
    }
}
