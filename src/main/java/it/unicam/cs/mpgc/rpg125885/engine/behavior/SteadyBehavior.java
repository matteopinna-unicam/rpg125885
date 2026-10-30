package it.unicam.cs.mpgc.rpg125885.engine.behavior;

import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.StressAction;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.Comparator;

/** Costante: stress all'unità con meno Resistenza. */
public final class SteadyBehavior implements HazardBehavior {

    @Override
    public InterventionAction chooseAction(Hazard hazard, Intervention intervention) {
        Unit weakest = intervention.activeUnits().stream()
                .min(Comparator.comparingInt(unit -> unit.endurance().current()))
                .orElseThrow();
        return new StressAction(hazard, weakest);
    }
}
