package it.unicam.cs.mpgc.rpg125885.engine.rules;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectResult;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

import java.util.Objects;

/** Formule del §2.5 del design. */
public final class StandardEffectCalculator implements EffectCalculator {

    private static final double EXEMPLARY_CHANCE = 0.10;
    private static final double EXEMPLARY_MULTIPLIER = 1.5;
    private static final double VARIATION_MIN = 0.9;
    private static final double VARIATION_RANGE = 0.2;
    private static final double COMPOSURE_DIVISOR = 2.0;
    private static final double COVER_FACTOR = 0.5;
    private static final double NEUTRAL_EFFICIENCY = 1.0;

    private final RandomSource random;
    private final double efficiency;

    /** Calcolatore senza malus. */
    public StandardEffectCalculator(RandomSource random) {
        this(random, NEUTRAL_EFFICIENCY);
    }

    /**
     * @param efficiency moltiplicatore di efficacia delle unità per i requisiti non soddisfatti (1,0 = nessun malus)
     */
    public StandardEffectCalculator(RandomSource random, double efficiency) {
        this.random = Objects.requireNonNull(random, "random");
        if (efficiency <= 0) {
            throw new IllegalArgumentException("L'efficacia deve essere positiva: " + efficiency);
        }
        this.efficiency = efficiency;
    }

    @Override
    public EffectResult gravityReduction(Unit actor, Hazard target, InterventionType type,
                                         double power, boolean ignoresResistance) {
        double modifier = target.modifierFor(type);
        if (ignoresResistance) {
            modifier = Math.max(modifier, 1.0);
        }
        double base = type.relevantStat(actor.effectiveStats()) * power * modifier * efficiency;
        double variation = nextVariation();
        boolean exemplary = random.nextDouble() < EXEMPLARY_CHANCE;
        double value = Math.max(1.0, base) * variation * (exemplary ? EXEMPLARY_MULTIPLIER : 1.0);
        return new EffectResult(atLeastOne(value), exemplary);
    }

    @Override
    public int stressTaken(Unit target, int stressPower) {
        double base = Math.max(1.0, stressPower - target.effectiveStats().composure() / COMPOSURE_DIVISOR);
        double value = base * nextVariation() * (target.isCovered() ? COVER_FACTOR : 1.0);
        return atLeastOne(value);
    }

    @Override
    public int recovery(Unit actor, GaugeKind kind, double power) {
        Stats stats = actor.effectiveStats();
        int stat = switch (kind) {
            case ENDURANCE -> stats.medicine();
            case RESOURCES -> stats.coordination();
        };
        return atLeastOne(stat * power * actor.trait().recoveryMultiplier());
    }

    private double nextVariation() {
        return VARIATION_MIN + VARIATION_RANGE * random.nextDouble();
    }

    private static int atLeastOne(double value) {
        return (int) Math.max(1, Math.round(value));
    }
}
