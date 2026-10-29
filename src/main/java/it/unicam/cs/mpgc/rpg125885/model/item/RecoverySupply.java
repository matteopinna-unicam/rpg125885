package it.unicam.cs.mpgc.rpg125885.model.item;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectApplier;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Targets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Consumabile che fa recuperare a un'unità una quantità fissa di Resistenza o Risorse. */
public final class RecoverySupply extends Supply {

    private final GaugeKind kind;
    private final int amount;

    public RecoverySupply(String id, String name, String description, GaugeKind kind, int amount) {
        super(id, name, description, TargetScope.SINGLE_UNIT);
        this.kind = Objects.requireNonNull(kind, "kind");
        if (amount <= 0) {
            throw new IllegalArgumentException("Il recupero deve essere positivo: " + amount);
        }
        this.amount = amount;
    }

    @Override
    public List<InterventionEvent> use(Unit user, Targets targets, EffectCalculator calculator) {
        List<InterventionEvent> events = new ArrayList<>();
        for (Unit target : targets.units()) {
            events.add(EffectApplier.recover(user, target, kind, amount));
        }
        return events;
    }
}
