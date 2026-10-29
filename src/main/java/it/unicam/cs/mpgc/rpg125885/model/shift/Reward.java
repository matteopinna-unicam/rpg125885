package it.unicam.cs.mpgc.rpg125885.model.shift;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Premio di una chiamata risolta: un oggetto, una dotazione o un'unità di rinforzo.
 * Sa da sé come assegnarsi alla flotta, così chi lo assegna non distingue i casi.
 *
 * @param description testo mostrato al giocatore
 * @param grant       assegnazione alla flotta
 */
public record Reward(String description, Consumer<Fleet> grant) {

    public Reward {
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(grant, "grant");
    }

    /** Premio che aggiunge {@code quantity} esemplari di un oggetto o di una dotazione all'inventario. */
    public static Reward ofItem(Item item, int quantity) {
        return new Reward(quantity + " × " + item.name(), fleet -> fleet.inventory().add(item, quantity));
    }

    /**
     * Premio che aggiunge una nuova unità. La fabbrica crea un'unità nuova a ogni assegnazione,
     * perché lo scenario è contenuto condiviso tra le partite.
     */
    public static Reward ofReinforcement(String description, Supplier<Unit> unitFactory) {
        return new Reward(description, fleet -> fleet.addReinforcement(unitFactory.get()));
    }

    public void applyTo(Fleet fleet) {
        grant.accept(fleet);
    }
}
