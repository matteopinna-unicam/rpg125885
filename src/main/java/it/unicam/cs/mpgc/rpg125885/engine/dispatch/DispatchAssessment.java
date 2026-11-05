package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import java.util.List;

/**
 * Esito del confronto tra i requisiti di una chiamata e le unità assegnate.
 *
 * @param missing    descrizione di ogni requisito non soddisfatto, pronta da mostrare
 * @param efficiency moltiplicatore dell'efficacia delle unità (1,0 = nessun malus)
 */
public record DispatchAssessment(List<String> missing, double efficiency) {

    public DispatchAssessment {
        missing = List.copyOf(missing);
        if (efficiency <= 0 || efficiency > 1) {
            throw new IllegalArgumentException("L'efficacia deve essere in (0, 1]: " + efficiency);
        }
    }

    public boolean isSatisfied() {
        return missing.isEmpty();
    }
}
