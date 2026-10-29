package it.unicam.cs.mpgc.rpg125885.model.shift;

import java.util.List;
import java.util.Objects;

/**
 * Un'ora della notte.
 *
 * @param time      orario mostrato al giocatore, ad esempio "22:00"
 * @param scenarios chiamate della fase; vuote nella fase di base
 */
public record Phase(String time, PhaseType type, List<Scenario> scenarios) {

    public Phase {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(type, "type");
        scenarios = List.copyOf(scenarios);
    }
}
