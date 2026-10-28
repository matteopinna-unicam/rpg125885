package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/**
 * Tipo di mezzo: definisce ente, costo e statistiche delle unità che lo usano.
 * Le manovre per livello (maneuversAt) si aggiungono in S3, con il package {@code maneuver}.
 */
public interface VehicleType {

    /** Identificatore stabile, usato nei salvataggi. */
    String id();

    String displayName();

    Agency agency();

    /** Costo in punti budget per schierare il mezzo. */
    int cost();

    Stats baseStats();

    /** Statistiche guadagnate a ogni livello oltre il primo. */
    Stats growthPerLevel();

    InterventionType primaryType();

    /** Se può essere scelto nella composizione della flotta. */
    boolean isPurchasable();
}
