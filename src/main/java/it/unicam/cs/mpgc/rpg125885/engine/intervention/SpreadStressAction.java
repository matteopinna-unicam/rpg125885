package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Il pericolo si propaga: stress ridotto al 60% a tutte le unità. */
public final class SpreadStressAction implements InterventionAction {

    private static final double SPREAD_FACTOR = 0.6;

    private final Hazard source;
    private final List<Unit> targets;

    public SpreadStressAction(Hazard source, List<Unit> targets) {
        this.source = Objects.requireNonNull(source, "source");
        this.targets = List.copyOf(targets);
    }

    @Override
    public Hazard actor() {
        return source;
    }

    @Override
    public List<InterventionEvent> execute(Intervention intervention) {
        int stressPower = (int) Math.round(source.template().stress() * SPREAD_FACTOR);
        List<InterventionEvent> events = new ArrayList<>();
        for (Unit target : targets) {
            events.addAll(StressAction.inflict(source, target, stressPower, intervention.calculator()));
        }
        return events;
    }
}
