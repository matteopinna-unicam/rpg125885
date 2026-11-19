package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/** Compone la flotta di una nuova partita rispettando il budget e il numero minimo di unità. */
public final class FleetBuilder {

    public static final int DEFAULT_BUDGET = 12;
    public static final int DEFAULT_MINIMUM_UNITS = 4;

    private final int budget;
    private final int minimumUnits;
    private final Map<Item, Integer> startingSupplies;
    private static final int FIRST_NUMBER = 1;

    private final List<Unit> units = new ArrayList<>();
    private int spent;

    /**
     * @param startingSupplies rifornimenti con cui parte la flotta
     */
    public FleetBuilder(int budget, int minimumUnits, Map<Item, Integer> startingSupplies) {
        if (budget <= 0 || minimumUnits <= 0) {
            throw new IllegalArgumentException("Budget e numero minimo di unità devono essere positivi");
        }
        this.budget = budget;
        this.minimumUnits = minimumUnits;
        this.startingSupplies = Map.copyOf(startingSupplies);
    }

    public int budget() {
        return budget;
    }

    public int remainingBudget() {
        return budget - spent;
    }

    public int minimumUnits() {
        return minimumUnits;
    }

    public List<Unit> units() {
        return Collections.unmodifiableList(units);
    }

    /** Se il tipo si può scegliere e il budget residuo basta a pagarlo. */
    public boolean canAfford(VehicleType type) {
        return type.isPurchasable() && type.cost() <= remainingBudget();
    }

    /** Se la flotta ha abbastanza unità per iniziare la partita. */
    public boolean isComplete() {
        return units.size() >= minimumUnits;
    }

    /**
     * Aggiunge un'unità con un nominativo ricavato dal tipo ("Autoscala 2"), col primo numero libero.
     *
     * @return l'unità creata
     * @throws IllegalArgumentException se il tipo non è acquistabile o il budget non basta
     */
    public Unit add(VehicleType type, String crewLeader, Trait trait) {
        Objects.requireNonNull(type, "type");
        if (!type.isPurchasable()) {
            throw new IllegalArgumentException(type.displayName() + " non si può acquistare");
        }
        if (type.cost() > remainingBudget()) {
            throw new IllegalArgumentException("Budget insufficiente: servono " + type.cost()
                    + " punti, ne restano " + remainingBudget());
        }
        Unit unit = new Unit(nextCallsign(type), crewLeader, type, trait);
        units.add(unit);
        spent += type.cost();
        return unit;
    }

    /**
     * Toglie un'unità restituendo i punti spesi. I nominativi già assegnati non cambiano.
     *
     * @throws IllegalArgumentException se l'unità non fa parte della composizione
     */
    public void remove(Unit unit) {
        if (!units.remove(unit)) {
            throw new IllegalArgumentException("Unità non presente: " + unit.name());
        }
        spent -= unit.type().cost();
    }

    /**
     * Crea la flotta con i rifornimenti iniziali.
     *
     * @throws IllegalStateException se le unità sono meno del minimo
     */
    public Fleet build() {
        if (!isComplete()) {
            throw new IllegalStateException("Servono almeno " + minimumUnits + " unità, ne hai scelte " + units.size());
        }
        Fleet fleet = new Fleet(units);
        startingSupplies.forEach((item, quantity) -> fleet.inventory().add(item, quantity));
        return fleet;
    }

    /**
     * Il numero libero più basso per il tipo: i nominativi restano unici e, aggiungendo e togliendo unità,
     * non crescono oltre quanto il budget permette (così non raggiungono quelli dei rinforzi).
     */
    private String nextCallsign(VehicleType type) {
        return IntStream.iterate(FIRST_NUMBER, number -> number + 1)
                .mapToObj(number -> type.displayName() + " " + number)
                .filter(callsign -> units.stream().noneMatch(unit -> unit.name().equals(callsign)))
                .findFirst()
                .orElseThrow();
    }
}
