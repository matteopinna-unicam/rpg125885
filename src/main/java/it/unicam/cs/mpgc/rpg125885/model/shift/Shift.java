package it.unicam.cs.mpgc.rpg125885.model.shift;

import java.util.List;
import java.util.Objects;

/** La notte: la sequenza ordinata delle sue fasi. */
public final class Shift {

    private final String id;
    private final List<Phase> phases;

    public Shift(String id, List<Phase> phases) {
        this.id = Objects.requireNonNull(id, "id");
        this.phases = List.copyOf(phases);
        if (this.phases.isEmpty()) {
            throw new IllegalArgumentException("Il turno deve avere almeno una fase");
        }
    }

    public String id() {
        return id;
    }

    public List<Phase> phases() {
        return phases;
    }

    public int phaseCount() {
        return phases.size();
    }

    public Phase phaseAt(int index) {
        return phases.get(index);
    }
}
