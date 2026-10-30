package it.unicam.cs.mpgc.rpg125885.engine.progression;

/** Quanta esperienza serve per salire di livello. */
public interface LevelCurve {

    /** Esperienza totale che un'unità deve avere per lasciare il livello indicato. */
    int experienceToAdvanceFrom(int level);
}
