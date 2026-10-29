package it.unicam.cs.mpgc.rpg125885.model.item;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;

import java.util.Objects;

/** Dotazione equipaggiabile: occupa uno slot e aggiunge un bonus alle statistiche. */
public final class Gear extends Item {

    private final GearSlot slot;
    private final Stats bonus;

    public Gear(String id, String name, String description, GearSlot slot, Stats bonus) {
        super(id, name, description);
        this.slot = Objects.requireNonNull(slot, "slot");
        this.bonus = Objects.requireNonNull(bonus, "bonus");
    }

    public GearSlot slot() {
        return slot;
    }

    public Stats bonus() {
        return bonus;
    }
}
