package it.unicam.cs.mpgc.rpg125885.model.maneuver;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.List;

/** Bersagli già risolti di un effetto: chi usa l'effetto legge la lista che gli serve, senza cast. */
public record Targets(List<Unit> units, List<Hazard> hazards) {

    public Targets {
        units = List.copyOf(units);
        hazards = List.copyOf(hazards);
    }

    public static Targets ofUnits(List<Unit> units) {
        return new Targets(units, List.of());
    }

    public static Targets ofHazards(List<Hazard> hazards) {
        return new Targets(List.of(), hazards);
    }
}
