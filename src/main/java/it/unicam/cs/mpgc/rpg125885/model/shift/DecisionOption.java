package it.unicam.cs.mpgc.rpg125885.model.shift;

import java.util.Objects;

/**
 * Una delle due risposte a una scelta narrativa, con i modificatori che applica all'intervento.
 *
 * @param gravityChangePercent variazione percentuale della gravità iniziale dei pericoli (-20 = 20% in meno)
 * @param roundsChange         variazione dei round disponibili
 * @param initialStress        stress subito da ogni unità all'inizio dell'intervento
 */
public record DecisionOption(String text, int gravityChangePercent, int roundsChange, int initialStress) {

    public DecisionOption {
        Objects.requireNonNull(text, "text");
        if (initialStress < 0) {
            throw new IllegalArgumentException("Lo stress iniziale non può essere negativo: " + initialStress);
        }
    }
}
