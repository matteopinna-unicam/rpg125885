package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;

import java.util.List;

/** Tipo di mezzo: definisce ente, costo, statistiche e manovre delle unità che lo usano. */
public interface VehicleType {

    /** Livello a cui un mezzo ottiene la sua terza manovra. */
    int ADVANCED_MANEUVER_LEVEL = 3;

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

    /** Manovre disponibili a un'unità del livello indicato. */
    List<Maneuver> maneuversAt(int level);

    /** Se può essere scelto nella composizione della flotta. */
    boolean isPurchasable();
}
