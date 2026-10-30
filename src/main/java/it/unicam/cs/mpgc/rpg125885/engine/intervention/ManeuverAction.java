package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Usable;

import java.util.Objects;

/** Manovra del mezzo: costa Risorse. */
public final class ManeuverAction extends UsableAction {

    private final Maneuver maneuver;

    public ManeuverAction(Unit actor, Maneuver maneuver, TargetChoice choice) {
        super(actor, choice);
        this.maneuver = Objects.requireNonNull(maneuver, "maneuver");
    }

    @Override
    Usable usable() {
        return maneuver;
    }

    @Override
    void pay() {
        if (actor().resources().current() < maneuver.resourceCost()) {
            throw new IllegalStateException("Risorse insufficienti per " + maneuver.name());
        }
        actor().resources().decrease(maneuver.resourceCost());
    }
}
