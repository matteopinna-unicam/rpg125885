package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.item.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** La flotta del giocatore: le unità e i rifornimenti in comune. */
public final class Fleet {

    private final List<Unit> units = new ArrayList<>();
    private final Inventory inventory = new Inventory();

    public Fleet(List<Unit> initialUnits) {
        Objects.requireNonNull(initialUnits, "initialUnits");
        initialUnits.forEach(this::addReinforcement);
    }

    public List<Unit> units() {
        return Collections.unmodifiableList(units);
    }

    public Inventory inventory() {
        return inventory;
    }

    /** Unità non occupate alla fase indicata, nell'ordine della flotta. */
    public List<Unit> availableUnits(int phase) {
        return units.stream()
                .filter(unit -> unit.isAvailableAt(phase))
                .toList();
    }

    /**
     * Aggiunge un'unità alla flotta.
     *
     * @throws IllegalArgumentException se esiste già un'unità con lo stesso nominativo
     */
    public void addReinforcement(Unit unit) {
        Objects.requireNonNull(unit, "unit");
        boolean duplicate = units.stream().anyMatch(existing -> existing.name().equals(unit.name()));
        if (duplicate) {
            throw new IllegalArgumentException("Esiste già un'unità con nominativo " + unit.name());
        }
        units.add(unit);
    }
}
