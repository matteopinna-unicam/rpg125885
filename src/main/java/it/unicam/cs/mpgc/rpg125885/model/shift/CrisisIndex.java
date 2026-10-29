package it.unicam.cs.mpgc.rpg125885.model.shift;

/** Gravità complessiva dell'emergenza in città, da 0 a 100; al massimo la partita è persa. */
public final class CrisisIndex {

    public static final int MAX = 100;

    private int value;

    public CrisisIndex() {
        this(0);
    }

    public CrisisIndex(int value) {
        if (value < 0 || value > MAX) {
            throw new IllegalArgumentException("Valore fuori dall'intervallo 0.." + MAX + ": " + value);
        }
        this.value = value;
    }

    public int value() {
        return value;
    }

    public void raise(int amount) {
        requireNotNegative(amount);
        value = Math.min(MAX, value + amount);
    }

    public void lower(int amount) {
        requireNotNegative(amount);
        value = Math.max(0, value - amount);
    }

    public boolean isCritical() {
        return value == MAX;
    }

    private static void requireNotNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La quantità non può essere negativa: " + amount);
        }
    }
}
