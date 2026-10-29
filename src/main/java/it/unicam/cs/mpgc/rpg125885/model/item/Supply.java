package it.unicam.cs.mpgc.rpg125885.model.item;

import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Usable;

import java.util.Objects;

/** Consumabile: si usa come una manovra, ma non costa Risorse e si toglie dall'inventario. */
public abstract class Supply extends Item implements Usable {

    private final TargetScope scope;

    protected Supply(String id, String name, String description, TargetScope scope) {
        super(id, name, description);
        this.scope = Objects.requireNonNull(scope, "scope");
    }

    @Override
    public TargetScope scope() {
        return scope;
    }
}
