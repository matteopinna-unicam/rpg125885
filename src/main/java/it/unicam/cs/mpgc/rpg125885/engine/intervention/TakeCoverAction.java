package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.TookCover;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;

import java.util.List;
import java.util.Objects;

/** Mettiti al riparo: lo stress subito è dimezzato fino al proprio turno successivo. */
public final class TakeCoverAction implements InterventionAction {

    private final Unit actor;

    public TakeCoverAction(Unit actor) {
        this.actor = Objects.requireNonNull(actor, "actor");
    }

    @Override
    public Unit actor() {
        return actor;
    }

    @Override
    public List<InterventionEvent> execute(Intervention intervention) {
        actor.takeCover();
        return List.of(new TookCover(actor));
    }
}
