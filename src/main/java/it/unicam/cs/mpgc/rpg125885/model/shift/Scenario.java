package it.unicam.cs.mpgc.rpg125885.model.shift;

import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Una chiamata, letta da {@code shift.xml}.
 *
 * @param hazards       modelli dei pericoli presenti
 * @param rounds        round disponibili per risolverla
 * @param ignorePenalty aumento dell'Indice di Crisi se viene ignorata
 * @param failPenalty   aumento dell'Indice di Crisi se l'intervento fallisce
 * @param busyPhases    fasi per cui restano occupate le unità impiegate
 */
public record Scenario(
        String id,
        String title,
        String district,
        String callText,
        List<HazardTemplate> hazards,
        int rounds,
        Requirements requirements,
        Optional<Decision> decision,
        String successText,
        String failureText,
        int ignorePenalty,
        int failPenalty,
        int busyPhases,
        Optional<Reward> reward) {

    public Scenario {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(district, "district");
        Objects.requireNonNull(callText, "callText");
        Objects.requireNonNull(requirements, "requirements");
        Objects.requireNonNull(decision, "decision");
        Objects.requireNonNull(successText, "successText");
        Objects.requireNonNull(failureText, "failureText");
        Objects.requireNonNull(reward, "reward");
        hazards = List.copyOf(hazards);
        if (hazards.isEmpty()) {
            throw new IllegalArgumentException("Una chiamata deve avere almeno un pericolo: " + id);
        }
        if (rounds <= 0) {
            throw new IllegalArgumentException("I round devono essere positivi: " + id);
        }
        if (ignorePenalty < 0 || failPenalty < 0 || busyPhases < 0) {
            throw new IllegalArgumentException("Penalità e fasi di occupazione non possono essere negative: " + id);
        }
    }
}
