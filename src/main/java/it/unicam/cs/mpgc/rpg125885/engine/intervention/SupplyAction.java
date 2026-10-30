package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.item.Inventory;
import it.unicam.cs.mpgc.rpg125885.model.item.Supply;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Usable;

import java.util.Objects;

/** Rifornimento: usa un consumabile dall'inventario in comune. */
public final class SupplyAction extends UsableAction {

    private static final int ONE_UNIT = 1;

    private final Supply supply;
    private final Inventory inventory;

    public SupplyAction(Unit actor, Supply supply, TargetChoice choice, Inventory inventory) {
        super(actor, choice);
        this.supply = Objects.requireNonNull(supply, "supply");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    @Override
    Usable usable() {
        return supply;
    }

    @Override
    void pay() {
        inventory.remove(supply, ONE_UNIT);
    }
}
