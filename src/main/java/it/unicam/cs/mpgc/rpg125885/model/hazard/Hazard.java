package it.unicam.cs.mpgc.rpg125885.model.hazard;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Gauge;

import java.util.Objects;

/**
 * Pericolo in un intervento. La gravità è il contatore di condizione:
 * a zero il pericolo è risolto.
 */
public final class Hazard extends Combatant {

    private static final int NEVER_FACED = Integer.MIN_VALUE;

    private final HazardTemplate template;
    private int lastFacedRound = NEVER_FACED;

    /** Crea il pericolo con la gravità del modello. */
    public Hazard(HazardTemplate template) {
        this(template, Objects.requireNonNull(template, "template").gravity());
    }

    /** Crea il pericolo con una gravità iniziale diversa, ad esempio modificata da una scelta. */
    public Hazard(HazardTemplate template, int startingGravity) {
        super(Objects.requireNonNull(template, "template").name(), new Gauge(startingGravity));
        this.template = template;
    }

    public HazardTemplate template() {
        return template;
    }

    public Gauge gravity() {
        return condition();
    }

    @Override
    public int initiative() {
        return template.pace();
    }

    public double modifierFor(InterventionType type) {
        return template.modifierFor(type);
    }

    public boolean isElevated() {
        return template.elevated();
    }

    /** Registra che il pericolo è stato affrontato nel round indicato. */
    public void markFacedIn(int round) {
        lastFacedRound = round;
    }

    public boolean wasFacedIn(int round) {
        return lastFacedRound == round;
    }
}
