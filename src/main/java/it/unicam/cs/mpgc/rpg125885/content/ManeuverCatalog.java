package it.unicam.cs.mpgc.rpg125885.content;

import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;

import java.util.HashMap;
import java.util.Map;

/** Associa gli id alle manovre di tutti i tipi di mezzo registrati. */
public final class ManeuverCatalog {

    private final Map<String, Maneuver> maneuvers = new HashMap<>();

    /**
     * @throws IllegalArgumentException se due manovre hanno lo stesso id
     */
    public ManeuverCatalog(VehicleTypeRegistry registry) {
        for (VehicleType type : registry.all()) {
            for (Maneuver maneuver : type.maneuversAt(VehicleType.ADVANCED_MANEUVER_LEVEL)) {
                if (maneuvers.putIfAbsent(maneuver.id(), maneuver) != null) {
                    throw new IllegalArgumentException("Manovra duplicata: " + maneuver.id());
                }
            }
        }
    }

    /**
     * @throws IllegalArgumentException se l'id non esiste
     */
    public Maneuver byId(String id) {
        Maneuver maneuver = maneuvers.get(id);
        if (maneuver == null) {
            throw new IllegalArgumentException("Manovra sconosciuta: " + id);
        }
        return maneuver;
    }
}
