package it.unicam.cs.mpgc.rpg125885.model.maneuver;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.TookCover;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;

import java.util.ArrayList;
import java.util.List;

/** Manovra di supporto che mette al riparo le unità bersaglio. */
public final class CoverManeuver extends AbstractManeuver {

    public CoverManeuver(String id, String name, int resourceCost, TargetScope scope) {
        super(id, name, resourceCost, scope);
    }

    @Override
    public List<InterventionEvent> use(Unit user, Targets targets, EffectCalculator calculator) {
        List<InterventionEvent> events = new ArrayList<>();
        for (Unit target : targets.units()) {
            target.takeCover();
            events.add(new TookCover(target));
        }
        return events;
    }
}
