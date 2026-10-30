package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;

import java.util.List;

/** Azione eseguibile durante un intervento (Command): ciò che un'unità o un pericolo fa nel proprio turno. */
public interface InterventionAction {

    /** Chi compie l'azione. */
    Combatant actor();

    /** Esegue l'azione e restituisce gli eventi prodotti. */
    List<InterventionEvent> execute(Intervention intervention);
}
