package it.unicam.cs.mpgc.rpg125885.model.shift;

import java.util.List;
import java.util.Objects;

/** Scelta narrativa a inizio intervento: una domanda e due opzioni. */
public record Decision(String question, DecisionOption first, DecisionOption second) {

    public Decision {
        Objects.requireNonNull(question, "question");
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
    }

    public List<DecisionOption> options() {
        return List.of(first, second);
    }
}
