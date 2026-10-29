package it.unicam.cs.mpgc.rpg125885.model.combat;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

/**
 * Fatto accaduto durante un intervento. Gli eventi sono la sola informazione che il motore
 * comunica all'esterno (registro, interfaccia), così chi li legge non dipende dal motore.
 */
public sealed interface InterventionEvent {

    record RoundStarted(int round) implements InterventionEvent {
    }

    record TurnStarted(Combatant actor) implements InterventionEvent {
    }

    record GravityReduced(Unit source, Hazard target, int amount, boolean exemplary)
            implements InterventionEvent {
    }

    record GravityEscalated(Hazard hazard, int amount) implements InterventionEvent {
    }

    record StressInflicted(Combatant source, Unit target, int amount) implements InterventionEvent {
    }

    record Recovered(Unit source, Unit target, GaugeKind kind, int amount) implements InterventionEvent {
    }

    record TookCover(Unit unit) implements InterventionEvent {
    }

    record UnitExhausted(Unit unit) implements InterventionEvent {
    }

    record HazardResolved(Hazard hazard) implements InterventionEvent {
    }

    record InterventionEnded(boolean successful) implements InterventionEvent {
    }
}
