package it.unicam.cs.mpgc.rpg125885.model.maneuver;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectApplier;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectResult;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Manovra che riduce la gravità dei pericoli bersaglio. */
public final class InterventionManeuver extends AbstractManeuver {

    private final InterventionType type;
    private final double power;
    private final double elevatedPower;
    private final boolean ignoresResistance;

    /**
     * @param power             potenza contro i pericoli normali
     * @param elevatedPower     potenza contro i pericoli in quota
     * @param ignoresResistance se ignora i moltiplicatori inferiori a 1 del pericolo
     */
    public InterventionManeuver(String id, String name, int resourceCost, TargetScope scope,
                                InterventionType type, double power, double elevatedPower,
                                boolean ignoresResistance) {
        super(id, name, resourceCost, scope);
        this.type = Objects.requireNonNull(type, "type");
        this.power = power;
        this.elevatedPower = elevatedPower;
        this.ignoresResistance = ignoresResistance;
    }

    /** Manovra con la stessa potenza dentro e fuori quota. */
    public InterventionManeuver(String id, String name, int resourceCost, TargetScope scope,
                                InterventionType type, double power, boolean ignoresResistance) {
        this(id, name, resourceCost, scope, type, power, power, ignoresResistance);
    }

    @Override
    public List<InterventionEvent> use(Unit user, Targets targets, EffectCalculator calculator) {
        List<InterventionEvent> events = new ArrayList<>();
        for (Hazard hazard : targets.hazards()) {
            if (hazard.isActive()) {
                EffectResult result = calculator.gravityReduction(
                        user, hazard, type, powerAgainst(hazard), ignoresResistance);
                events.addAll(EffectApplier.reduceGravity(user, hazard, result.amount(), result.exemplary()));
            }
        }
        return events;
    }

    private double powerAgainst(Hazard hazard) {
        return hazard.isElevated() ? elevatedPower : power;
    }
}
