package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.engine.behavior.BehaviorRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.TurnOrderPolicy;
import it.unicam.cs.mpgc.rpg125885.engine.rules.RandomSource;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleFunction;

/**
 * Trasforma una chiamata in un intervento pronto da giocare: pericoli dai modelli, round e stress
 * iniziale modificati dalla scelta narrativa, efficacia ridotta dal malus sui requisiti.
 */
public final class ScenarioFactory {

    private static final int PERCENT = 100;
    private static final int MINIMUM_GRAVITY = 1;
    private static final int MINIMUM_ROUNDS = 1;

    private final RandomSource random;
    private final TurnOrderPolicy turnOrder;
    private final BehaviorRegistry behaviors;
    private final DoubleFunction<EffectCalculator> calculators;

    /**
     * @param calculators crea il calcolatore degli effetti per un dato moltiplicatore di efficacia
     */
    public ScenarioFactory(RandomSource random, TurnOrderPolicy turnOrder, BehaviorRegistry behaviors,
                           DoubleFunction<EffectCalculator> calculators) {
        this.random = Objects.requireNonNull(random, "random");
        this.turnOrder = Objects.requireNonNull(turnOrder, "turnOrder");
        this.behaviors = Objects.requireNonNull(behaviors, "behaviors");
        this.calculators = Objects.requireNonNull(calculators, "calculators");
    }

    /** I pericoli della chiamata, con la gravità iniziale variata dalla scelta (minimo 1). */
    public List<Hazard> createHazards(Scenario scenario, Optional<DecisionOption> choice) {
        int changePercent = choice.map(DecisionOption::gravityChangePercent).orElse(0);
        return scenario.hazards().stream()
                .map(template -> new Hazard(template, scaled(template.gravity(), changePercent)))
                .toList();
    }

    /** I round disponibili, variati dalla scelta (minimo 1). */
    public int roundsFor(Scenario scenario, Optional<DecisionOption> choice) {
        int change = choice.map(DecisionOption::roundsChange).orElse(0);
        return Math.max(MINIMUM_ROUNDS, scenario.rounds() + change);
    }

    /**
     * Crea l'intervento e applica lo stress iniziale della scelta a tutte le unità.
     *
     * @param efficiency moltiplicatore di efficacia calcolato dalla {@link RequirementPolicy}
     */
    public Intervention create(Scenario scenario, Optional<DecisionOption> choice, List<Unit> units,
                               double efficiency) {
        EffectCalculator calculator = calculators.apply(efficiency);
        choice.map(DecisionOption::initialStress)
                .filter(stress -> stress > 0)
                .ifPresent(stress -> units.forEach(unit ->
                        unit.endurance().decrease(calculator.stressTaken(unit, stress))));
        return new Intervention(units, createHazards(scenario, choice), roundsFor(scenario, choice),
                calculator, random, turnOrder, behaviors);
    }

    private static int scaled(int gravity, int changePercent) {
        long value = Math.round(gravity * (PERCENT + changePercent) / (double) PERCENT);
        return (int) Math.max(MINIMUM_GRAVITY, value);
    }
}
