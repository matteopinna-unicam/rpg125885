package it.unicam.cs.mpgc.rpg125885.engine.progression;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;

/** Un'unità è salita di livello. */
public record LevelUp(Unit unit, int newLevel) {
}
