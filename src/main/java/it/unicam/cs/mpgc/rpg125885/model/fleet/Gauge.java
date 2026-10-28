package it.unicam.cs.mpgc.rpg125885.model.fleet;

/**
 * Contatore con valore compreso tra 0 e un massimo.
 * È l'unico punto in cui questi limiti sono garantiti.
 */
public final class Gauge {

    private static final double PERCENT = 100.0;

    private final int max;
    private int current;

    /** Crea un contatore pieno. */
    public Gauge(int max) {
        this(max, max);
    }

    /** Crea un contatore con un valore iniziale, ad esempio caricato da un salvataggio. */
    public Gauge(int max, int current) {
        if (max <= 0) {
            throw new IllegalArgumentException("Il massimo deve essere positivo: " + max);
        }
        if (current < 0 || current > max) {
            throw new IllegalArgumentException("Valore " + current + " fuori dall'intervallo 0.." + max);
        }
        this.max = max;
        this.current = current;
    }

    public int current() {
        return current;
    }

    public int max() {
        return max;
    }

    public boolean isEmpty() {
        return current == 0;
    }

    /**
     * Sottrae {@code amount}, senza scendere sotto zero.
     *
     * @return la diminuzione effettiva
     */
    public int decrease(int amount) {
        requireNotNegative(amount);
        int applied = Math.min(amount, current);
        current -= applied;
        return applied;
    }

    /**
     * Somma {@code amount}, senza superare il massimo.
     *
     * @return l'aumento effettivo
     */
    public int increase(int amount) {
        requireNotNegative(amount);
        int applied = Math.min(amount, max - current);
        current += applied;
        return applied;
    }

    /** Porta il valore al massimo. */
    public void fill() {
        current = max;
    }

    /** Livello di riempimento da 0 a 100. */
    public double fillPercent() {
        return PERCENT * current / max;
    }

    private static void requireNotNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La quantità non può essere negativa: " + amount);
        }
    }
}
