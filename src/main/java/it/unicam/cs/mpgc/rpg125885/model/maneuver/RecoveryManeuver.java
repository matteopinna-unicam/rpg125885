package it.unicam.cs.mpgc.rpg125885.model.maneuver;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectApplier;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Manovra che fa recuperare Resistenza o Risorse alle unità bersaglio. */
public final class RecoveryManeuver extends AbstractManeuver {

    private final GaugeKind kind;
    private final double power;

    public RecoveryManeuver(String id, String name, int resourceCost, TargetScope scope,
                            GaugeKind kind, double power) {
        super(id, name, resourceCost, scope);
        this.kind = Objects.requireNonNull(kind, "kind");
        this.power = power;
    }

    @Override
    public List<InterventionEvent> use(Unit user, Targets targets, EffectCalculator calculator) {
        int amount = calculator.recovery(user, kind, power);
        List<InterventionEvent> events = new ArrayList<>();
        for (Unit target : targets.units()) {
            events.add(EffectApplier.recover(user, target, kind, amount));
        }
        return events;
    }
}
