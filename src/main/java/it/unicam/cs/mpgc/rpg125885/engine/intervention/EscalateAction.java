package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.GravityEscalated;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.List;
import java.util.Objects;

/** Il pericolo peggiora: la gravità sale del valore di escalation, anche oltre quella iniziale. */
public final class EscalateAction implements InterventionAction {

    private final Hazard hazard;

    public EscalateAction(Hazard hazard) {
        this.hazard = Objects.requireNonNull(hazard, "hazard");
    }

    @Override
    public Hazard actor() {
        return hazard;
    }

    @Override
    public List<InterventionEvent> execute(Intervention intervention) {
        int amount = hazard.template().escalation();
        hazard.gravity().changeMax(hazard.gravity().max() + amount);
        hazard.gravity().increase(amount);
        return List.of(new GravityEscalated(hazard, amount));
    }
}
