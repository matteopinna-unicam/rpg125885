package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;

import java.util.List;
import java.util.Objects;

/** Le unità che il giocatore manda a una chiamata. */
public record Assignment(Scenario scenario, List<Unit> units) {

    public Assignment {
        Objects.requireNonNull(scenario, "scenario");
        units = List.copyOf(units);
        if (units.isEmpty()) {
            throw new IllegalArgumentException("Serve almeno un'unità per la chiamata " + scenario.id());
        }
    }
}
