package it.unicam.cs.mpgc.rpg125885.model.hazard;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Descrizione di un tipo di pericolo, letta da {@code hazards.xml}.
 *
 * @param gravity    gravità iniziale
 * @param pace       iniziativa nell'ordine di turno
 * @param modifiers  moltiplicatore dell'efficacia per tipo d'intervento
 * @param elevated   se il pericolo è in quota
 * @param stress     stress inflitto a ogni azione
 * @param escalation aumento di gravità quando nessuno lo affronta
 * @param behaviorId id del comportamento (risolto dal motore, il modello non conosce le strategie)
 * @param xp         esperienza assegnata alla risoluzione
 */
public record HazardTemplate(
        String id,
        String name,
        int gravity,
        int pace,
        Map<InterventionType, Double> modifiers,
        boolean elevated,
        int stress,
        int escalation,
        String behaviorId,
        int xp) {

    private static final double NEUTRAL_MODIFIER = 1.0;

    public HazardTemplate {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(behaviorId, "behaviorId");
        if (gravity <= 0) {
            throw new IllegalArgumentException("La gravità deve essere positiva: " + gravity);
        }
        if (stress < 0 || escalation < 0 || xp < 0) {
            throw new IllegalArgumentException("Stress, escalation e XP non possono essere negativi");
        }
        modifiers = copyOf(modifiers);
    }

    /** Moltiplicatore per il tipo indicato; 1,0 se il pericolo non lo specifica. */
    public double modifierFor(InterventionType type) {
        return modifiers.getOrDefault(type, NEUTRAL_MODIFIER);
    }

    private static Map<InterventionType, Double> copyOf(Map<InterventionType, Double> source) {
        Objects.requireNonNull(source, "modifiers");
        Map<InterventionType, Double> copy = new EnumMap<>(InterventionType.class);
        copy.putAll(source);
        return Map.copyOf(copy);
    }
}
