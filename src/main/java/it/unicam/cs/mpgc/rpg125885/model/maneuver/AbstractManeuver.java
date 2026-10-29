package it.unicam.cs.mpgc.rpg125885.model.maneuver;

import java.util.Objects;

/** Dati comuni a tutte le manovre. */
abstract class AbstractManeuver implements Maneuver {

    private final String id;
    private final String name;
    private final int resourceCost;
    private final TargetScope scope;

    AbstractManeuver(String id, String name, int resourceCost, TargetScope scope) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        if (resourceCost < 0) {
            throw new IllegalArgumentException("Il costo non può essere negativo: " + resourceCost);
        }
        this.resourceCost = resourceCost;
        this.scope = Objects.requireNonNull(scope, "scope");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int resourceCost() {
        return resourceCost;
    }

    @Override
    public TargetScope scope() {
        return scope;
    }
}
