package it.unicam.cs.mpgc.rpg125885.model.shift;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Agency;

import java.util.Map;
import java.util.Set;

/**
 * Cosa richiede una chiamata.
 *
 * @param minimumByAgency      numero minimo di unità per ente
 * @param requiredVehicleTypes id dei tipi di mezzo obbligatori
 */
public record Requirements(Map<Agency, Integer> minimumByAgency, Set<String> requiredVehicleTypes) {

    /** Nessun requisito. */
    public static final Requirements NONE = new Requirements(Map.of(), Set.of());

    public Requirements {
        if (minimumByAgency.values().stream().anyMatch(minimum -> minimum <= 0)) {
            throw new IllegalArgumentException("I minimi per ente devono essere positivi");
        }
        minimumByAgency = Map.copyOf(minimumByAgency);
        requiredVehicleTypes = Set.copyOf(requiredVehicleTypes);
    }
}
