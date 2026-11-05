package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;

import java.util.List;

/** Regola che trasforma i requisiti non soddisfatti in un effetto sull'intervento. */
public interface RequirementPolicy {

    DispatchAssessment assess(Requirements requirements, List<Unit> units);
}
