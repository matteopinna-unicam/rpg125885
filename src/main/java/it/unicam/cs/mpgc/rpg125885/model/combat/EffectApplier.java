package it.unicam.cs.mpgc.rpg125885.model.combat;

import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.GravityReduced;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.HazardResolved;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.Recovered;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.ArrayList;
import java.util.List;

/**
 * Applica un effetto già calcolato ai contatori e ne produce gli eventi.
 * È condiviso da manovre e rifornimenti, che differiscono solo nel modo di calcolare la quantità.
 */
public final class EffectApplier {

    private EffectApplier() {
    }

    /** Riduce la gravità; la quantità riportata è quella effettivamente tolta. */
    public static List<InterventionEvent> reduceGravity(Unit source, Hazard target, int amount, boolean exemplary) {
        int applied = target.gravity().decrease(amount);
        List<InterventionEvent> events = new ArrayList<>();
        events.add(new GravityReduced(source, target, applied, exemplary));
        if (!target.isActive()) {
            events.add(new HazardResolved(target));
        }
        return events;
    }

    /** Fa recuperare il contatore {@code kind}; la quantità riportata è quella effettiva. */
    public static InterventionEvent recover(Unit source, Unit target, GaugeKind kind, int amount) {
        int applied = target.gauge(kind).increase(amount);
        return new Recovered(source, target, kind, applied);
    }
}
