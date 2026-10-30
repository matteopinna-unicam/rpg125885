package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.engine.behavior.BehaviorRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.behavior.EscalatingBehavior;
import it.unicam.cs.mpgc.rpg125885.engine.behavior.SpreadingBehavior;
import it.unicam.cs.mpgc.rpg125885.engine.behavior.SteadyBehavior;
import it.unicam.cs.mpgc.rpg125885.engine.rules.RandomSource;
import it.unicam.cs.mpgc.rpg125885.engine.rules.ScriptedRandom;
import it.unicam.cs.mpgc.rpg125885.engine.rules.StandardEffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;

import java.util.List;
import java.util.Map;

/** Costruttori di scenari di prova, condivisi dai test del motore. */
public final class InterventionFixture {

    public static final Trait NO_BONUS = new Trait("none", "Nessuno", Stats.NONE, 1.0, 1.0);

    private InterventionFixture() {
    }

    public static BehaviorRegistry behaviors() {
        BehaviorRegistry registry = new BehaviorRegistry();
        registry.register("steady", new SteadyBehavior());
        registry.register("escalating", new EscalatingBehavior());
        registry.register("spreading", new SpreadingBehavior());
        return registry;
    }

    /** Pericolo tecnico (moltiplicatore 1,0) con lo stress e il comportamento indicati. */
    public static Hazard hazard(int gravity, int stress, String behavior) {
        HazardTemplate template = new HazardTemplate("h", "Pericolo", gravity, 5,
                Map.of(InterventionType.TECHNICAL, 1.0), false, stress, 5, behavior, 20);
        return new Hazard(template);
    }

    public static Intervention intervention(List<Unit> units, List<Hazard> hazards, int rounds) {
        return intervention(units, hazards, rounds, new StandardEffectCalculator(ScriptedRandom.neutral()),
                ScriptedRandom.neutral());
    }

    public static Intervention intervention(List<Unit> units, List<Hazard> hazards, int rounds,
                                            EffectCalculator calculator, RandomSource random) {
        return new Intervention(units, hazards, rounds, calculator, random, new ReadinessTurnOrder(), behaviors());
    }
}
