package it.unicam.cs.mpgc.rpg125885.model.maneuver;

/** Abilità di un tipo di mezzo: un effetto che costa Risorse. */
public interface Maneuver extends Usable {

    /** Identificatore stabile, unico tra tutte le manovre. */
    String id();

    int resourceCost();
}
