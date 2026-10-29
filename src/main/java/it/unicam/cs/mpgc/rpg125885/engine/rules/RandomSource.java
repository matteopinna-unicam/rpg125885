package it.unicam.cs.mpgc.rpg125885.engine.rules;

/** Sorgente di casualità iniettabile: nei test se ne usa una a valori fissi. */
public interface RandomSource {

    /** Valore in [0, 1). */
    double nextDouble();

    /** Intero in [0, bound). */
    int nextInt(int bound);
}
