package it.unicam.cs.mpgc.rpg125885.engine.behavior;

import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionAction;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

/** Come un pericolo sceglie la propria azione nel suo turno (Strategy). */
@FunctionalInterface
public interface HazardBehavior {

    InterventionAction chooseAction(Hazard hazard, Intervention intervention);
}
