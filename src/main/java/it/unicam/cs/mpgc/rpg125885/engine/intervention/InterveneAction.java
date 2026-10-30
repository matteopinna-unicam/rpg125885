package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.InterventionManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Usable;

/** Intervieni: l'azione base, senza costo, con il tipo principale del mezzo su un pericolo. */
public final class InterveneAction extends UsableAction {

    private static final String ID = "intervene";
    private static final String NAME = "Intervieni";
    private static final int NO_COST = 0;
    private static final double BASE_POWER = 1.0;

    private final Usable maneuver;

    public InterveneAction(Unit actor, Hazard target) {
        super(actor, TargetChoice.ofHazard(target));
        this.maneuver = new InterventionManeuver(ID, NAME, NO_COST, TargetScope.SINGLE_HAZARD,
                actor.type().primaryType(), BASE_POWER, false);
    }

    @Override
    Usable usable() {
        return maneuver;
    }

    @Override
    void pay() {
        // Intervieni non costa nulla.
    }
}
