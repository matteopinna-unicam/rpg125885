package it.unicam.cs.mpgc.rpg125885.model.hazard;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;

import java.util.function.ToIntFunction;

/** Natura di un intervento; ogni tipo sa quale statistica lo governa. */
public enum InterventionType {
    TECHNICAL(Stats::technique),
    MEDICAL(Stats::medicine),
    COORDINATION(Stats::coordination);

    private final ToIntFunction<Stats> statSelector;

    InterventionType(ToIntFunction<Stats> statSelector) {
        this.statSelector = statSelector;
    }

    /** La statistica di {@code stats} rilevante per questo tipo di intervento. */
    public int relevantStat(Stats stats) {
        return statSelector.applyAsInt(stats);
    }
}
