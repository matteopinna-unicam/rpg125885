package it.unicam.cs.mpgc.rpg125885.engine.rules;

/** Sorgente casuale per i test: restituisce i valori dati in ordine, poi ripete l'ultimo. */
public final class ScriptedRandom implements RandomSource {

    private static final double MIDDLE = 0.5;

    private final double[] values;
    private int next;

    public ScriptedRandom(double... values) {
        this.values = values.length == 0 ? new double[]{MIDDLE} : values.clone();
    }

    /** Sorgente che restituisce sempre 0,5: variazione neutra e nessun intervento esemplare. */
    public static ScriptedRandom neutral() {
        return new ScriptedRandom(MIDDLE);
    }

    @Override
    public double nextDouble() {
        double value = values[Math.min(next, values.length - 1)];
        next++;
        return value;
    }

    @Override
    public int nextInt(int bound) {
        return (int) (nextDouble() * bound);
    }
}
