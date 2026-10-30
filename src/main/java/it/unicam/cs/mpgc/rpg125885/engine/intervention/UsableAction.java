package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Targets;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Usable;

import java.util.List;
import java.util.Objects;

/**
 * Azione di un'unità che usa qualcosa di {@link Usable}: risolve i bersagli, paga il costo, applica l'effetto.
 * Le sottoclassi dicono cosa usano e come si paga.
 */
abstract class UsableAction implements InterventionAction {

    private final Unit actor;
    private final TargetChoice choice;

    UsableAction(Unit actor, TargetChoice choice) {
        this.actor = Objects.requireNonNull(actor, "actor");
        this.choice = Objects.requireNonNull(choice, "choice");
    }

    @Override
    public Unit actor() {
        return actor;
    }

    abstract Usable usable();

    /** Spende il costo dell'azione; deve fallire senza effetti se il costo non è sostenibile. */
    abstract void pay();

    @Override
    public List<InterventionEvent> execute(Intervention intervention) {
        Targets targets = intervention.resolveTargets(usable().scope(), actor, choice);
        pay();
        List<InterventionEvent> events = usable().use(actor, targets, intervention.calculator());
        targets.hazards().forEach(hazard -> hazard.markFacedIn(intervention.round()));
        return events;
    }
}
