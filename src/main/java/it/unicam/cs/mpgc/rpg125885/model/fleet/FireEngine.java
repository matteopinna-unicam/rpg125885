package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.InterventionManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;

import java.util.List;

/** Autopompa serbatoio (APS): resistente, danno tecnico. */
public final class FireEngine implements VehicleType {

    private static final int COST = 3;
    private static final Stats BASE = new Stats(120, 20, 14, 3, 5, 11, 5);
    private static final Stats GROWTH = new Stats(11, 2, 3, 0, 1, 2, 1);

    private static final Maneuver EXTINGUISH = new InterventionManeuver("extinguish", "Spegnimento", 4,
            TargetScope.SINGLE_HAZARD, InterventionType.TECHNICAL, 1.6, false);
    private static final Maneuver COOLING = new InterventionManeuver("cooling", "Raffreddamento", 7,
            TargetScope.ALL_HAZARDS, InterventionType.TECHNICAL, 0.9, false);
    private static final Maneuver SECURING = new InterventionManeuver("securing", "Messa in sicurezza", 6,
            TargetScope.SINGLE_HAZARD, InterventionType.TECHNICAL, 1.2, true);
    private static final List<Maneuver> STARTING_MANEUVERS = List.of(EXTINGUISH, COOLING);
    private static final List<Maneuver> ALL_MANEUVERS = List.of(EXTINGUISH, COOLING, SECURING);

    @Override
    public String id() {
        return "fire_engine";
    }

    @Override
    public String displayName() {
        return "Autopompa serbatoio";
    }

    @Override
    public Agency agency() {
        return Agency.FIRE_BRIGADE;
    }

    @Override
    public int cost() {
        return COST;
    }

    @Override
    public Stats baseStats() {
        return BASE;
    }

    @Override
    public Stats growthPerLevel() {
        return GROWTH;
    }

    @Override
    public InterventionType primaryType() {
        return InterventionType.TECHNICAL;
    }

    @Override
    public List<Maneuver> maneuversAt(int level) {
        return level >= ADVANCED_MANEUVER_LEVEL ? ALL_MANEUVERS : STARTING_MANEUVERS;
    }

    @Override
    public boolean isPurchasable() {
        return true;
    }
}
