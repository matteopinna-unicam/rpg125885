package it.unicam.cs.mpgc.rpg125885.model.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/** Quantità posseduta di ciascun oggetto; non è mai negativa. */
public final class Inventory {

    private final Map<Item, Integer> quantities = new LinkedHashMap<>();

    public int quantityOf(Item item) {
        return quantities.getOrDefault(item, 0);
    }

    public boolean contains(Item item) {
        return quantityOf(item) > 0;
    }

    public void add(Item item, int amount) {
        Objects.requireNonNull(item, "item");
        requirePositive(amount);
        quantities.merge(item, amount, Integer::sum);
    }

    /**
     * Toglie {@code amount} esemplari.
     *
     * @throws IllegalStateException se la quantità posseduta non basta
     */
    public void remove(Item item, int amount) {
        requirePositive(amount);
        int owned = quantityOf(item);
        if (owned < amount) {
            throw new IllegalStateException(
                    "Quantità insufficiente di " + item + ": richieste " + amount + ", disponibili " + owned);
        }
        if (owned == amount) {
            quantities.remove(item);
        } else {
            quantities.put(item, owned - amount);
        }
    }

    /** Copia degli oggetti posseduti con le quantità, per filtri e salvataggio. */
    public Stream<Map.Entry<Item, Integer>> stream() {
        return new LinkedHashMap<>(quantities).entrySet().stream();
    }

    private static void requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("La quantità deve essere positiva: " + amount);
        }
    }
}
