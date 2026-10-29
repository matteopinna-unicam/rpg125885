package it.unicam.cs.mpgc.rpg125885.model.fleet;

import java.util.Objects;

/**
 * Partecipante a un intervento: un'unità o un pericolo.
 * Ha un nome e un contatore di condizione; è attivo finché non si esaurisce.
 */
public abstract class Combatant {

    private final String name;
    private final Gauge condition;

    protected Combatant(String name, Gauge condition) {
        this.name = requireNonBlank(name);
        this.condition = Objects.requireNonNull(condition, "condition");
    }

    public String name() {
        return name;
    }

    /** Valore che determina l'ordine di turno: più alto agisce prima. */
    public abstract int initiative();

    public boolean isActive() {
        return !condition.isEmpty();
    }

    /** Il contatore di condizione, che le sottoclassi espongono col nome del proprio dominio. */
    protected Gauge condition() {
        return condition;
    }

    private static String requireNonBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto");
        }
        return name;
    }
}
