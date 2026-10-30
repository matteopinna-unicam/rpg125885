package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.List;

/** Decide l'ordine in cui agiscono i combattenti attivi in un round. */
public interface TurnOrderPolicy {

    List<Combatant> order(List<Unit> units, List<Hazard> hazards);
}
