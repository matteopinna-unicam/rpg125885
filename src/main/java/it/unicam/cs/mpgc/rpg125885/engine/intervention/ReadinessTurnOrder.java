package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Iniziativa decrescente; a parità agiscono prima le unità. */
public final class ReadinessTurnOrder implements TurnOrderPolicy {

    @Override
    public List<Combatant> order(List<Unit> units, List<Hazard> hazards) {
        // L'ordinamento di uno stream ordinato è stabile: a parità restano prima le unità.
        return Stream.<Combatant>concat(units.stream(), hazards.stream())
                .sorted(Comparator.comparingInt(Combatant::initiative).reversed())
                .toList();
    }
}
