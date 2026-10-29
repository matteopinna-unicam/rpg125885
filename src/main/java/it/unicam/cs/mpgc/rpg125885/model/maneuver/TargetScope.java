package it.unicam.cs.mpgc.rpg125885.model.maneuver;

/** Chi può essere colpito da un effetto; il motore lo traduce in {@link Targets}. */
public enum TargetScope {
    SINGLE_HAZARD,
    ALL_HAZARDS,
    SINGLE_UNIT,
    ALL_UNITS,
    SELF
}
