package it.unicam.cs.mpgc.rpg125885.engine.behavior;

import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.SpreadStressAction;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

/** Si propaga: stress al 60% su tutte le unità. */
public final class SpreadingBehavior implements HazardBehavior {

    @Override
    public InterventionAction chooseAction(Hazard hazard, Intervention intervention) {
        return new SpreadStressAction(hazard, intervention.activeUnits());
    }
}
