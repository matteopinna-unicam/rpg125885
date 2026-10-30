package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.StressInflicted;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent.UnitExhausted;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Il pericolo infligge il proprio stress a un'unità. */
public final class StressAction implements InterventionAction {

    private final Hazard source;
    private final Unit target;

    public StressAction(Hazard source, Unit target) {
        this.source = Objects.requireNonNull(source, "source");
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public Hazard actor() {
        return source;
    }

    @Override
    public List<InterventionEvent> execute(Intervention intervention) {
        return inflict(source, target, source.template().stress(), intervention.calculator());
    }

    /** Infligge uno stress di potenza data e segnala l'eventuale esaurimento dell'unità. */
    static List<InterventionEvent> inflict(Hazard source, Unit target, int stressPower, EffectCalculator calculator) {
        int applied = target.endurance().decrease(calculator.stressTaken(target, stressPower));
        List<InterventionEvent> events = new ArrayList<>();
        events.add(new StressInflicted(source, target, applied));
        if (!target.isActive()) {
            events.add(new UnitExhausted(target));
        }
        return events;
    }
}
