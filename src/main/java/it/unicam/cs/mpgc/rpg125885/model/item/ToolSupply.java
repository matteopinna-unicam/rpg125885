package it.unicam.cs.mpgc.rpg125885.model.item;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectApplier;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Targets;

import java.util.ArrayList;
import java.util.List;

/** Consumabile che riduce di una quantità fissa la gravità di un pericolo. */
public final class ToolSupply extends Supply {

    private final int reduction;

    public ToolSupply(String id, String name, String description, int reduction) {
        super(id, name, description, TargetScope.SINGLE_HAZARD);
        if (reduction <= 0) {
            throw new IllegalArgumentException("La riduzione deve essere positiva: " + reduction);
        }
        this.reduction = reduction;
    }

    @Override
    public List<InterventionEvent> use(Unit user, Targets targets, EffectCalculator calculator) {
        List<InterventionEvent> events = new ArrayList<>();
        for (Hazard hazard : targets.hazards()) {
            if (hazard.isActive()) {
                events.addAll(EffectApplier.reduceGravity(user, hazard, reduction, false));
            }
        }
        return events;
    }
}
