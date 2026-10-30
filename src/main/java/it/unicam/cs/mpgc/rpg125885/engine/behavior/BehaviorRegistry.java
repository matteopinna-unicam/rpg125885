package it.unicam.cs.mpgc.rpg125885.engine.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Associa l'id di comportamento scritto nell'XML alla strategia corrispondente. */
public final class BehaviorRegistry {

    private final Map<String, HazardBehavior> behaviors = new HashMap<>();

    /**
     * @throws IllegalArgumentException se l'id è già registrato
     */
    public void register(String id, HazardBehavior behavior) {
        Objects.requireNonNull(behavior, "behavior");
        if (behaviors.putIfAbsent(id, behavior) != null) {
            throw new IllegalArgumentException("Comportamento già registrato: " + id);
        }
    }

    /**
     * @throws IllegalArgumentException se l'id non è registrato
     */
    public HazardBehavior byId(String id) {
        HazardBehavior behavior = behaviors.get(id);
        if (behavior == null) {
            throw new IllegalArgumentException("Comportamento sconosciuto: " + id);
        }
        return behavior;
    }
}
