package it.unicam.cs.mpgc.rpg125885.model.fleet;

/**
 * Le sette statistiche di un'unità, immutabili.
 * Lo stesso tipo rappresenta valori base, crescite per livello e bonus (di tratti e dotazioni).
 */
public record Stats(
        int maxEndurance,
        int maxResources,
        int technique,
        int medicine,
        int coordination,
        int composure,
        int readiness) {

    /** Tutte le statistiche a zero: elemento neutro di {@link #plus(Stats)}. */
    public static final Stats NONE = new Stats(0, 0, 0, 0, 0, 0, 0);

    /** Somma statistica per statistica; restituisce un nuovo oggetto. */
    public Stats plus(Stats other) {
        return new Stats(
                maxEndurance + other.maxEndurance,
                maxResources + other.maxResources,
                technique + other.technique,
                medicine + other.medicine,
                coordination + other.coordination,
                composure + other.composure,
                readiness + other.readiness);
    }

    /** Moltiplica ogni statistica per {@code factor}; serve ad accumulare le crescite per livello. */
    public Stats times(int factor) {
        return new Stats(
                maxEndurance * factor,
                maxResources * factor,
                technique * factor,
                medicine * factor,
                coordination * factor,
                composure * factor,
                readiness * factor);
    }
}