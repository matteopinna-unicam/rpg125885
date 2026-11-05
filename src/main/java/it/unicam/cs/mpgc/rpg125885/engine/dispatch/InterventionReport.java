package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.engine.progression.LevelUp;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;

import java.util.List;
import java.util.Optional;

/**
 * Conseguenze di un intervento concluso.
 *
 * @param text      testo di esito della chiamata
 * @param levelUps  livelli guadagnati dalle unità
 * @param rewardText descrizione del premio ottenuto, se c'è
 */
public record InterventionReport(Scenario scenario, CallOutcome outcome, String text,
                                 List<LevelUp> levelUps, Optional<String> rewardText) {

    public InterventionReport {
        levelUps = List.copyOf(levelUps);
    }
}
