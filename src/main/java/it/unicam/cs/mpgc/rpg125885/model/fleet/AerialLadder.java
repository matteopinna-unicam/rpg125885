package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.CoverManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.InterventionManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;

import java.util.List;

/** Autoscala: specialista lento, efficace sui pericoli in quota. */
public final class AerialLadder implements VehicleType {

    private static final int COST = 3;
    private static final Stats BASE = new Stats(100, 25, 12, 2, 4, 9, 3);
    private static final Stats GROWTH = new Stats(9, 2, 3, 0, 1, 1, 1);

    private static final Maneuver HIGH_RESCUE = new InterventionManeuver("high_rescue", "Soccorso in quota", 5,
            TargetScope.SINGLE_HAZARD, InterventionType.TECHNICAL, 0.8, 2.0, false);
    private static final Maneuver SCENE_LIGHTING = new CoverManeuver("scene_lighting", "Illuminazione scena", 5,
            TargetScope.ALL_UNITS);
    private static final Maneuver AERIAL_EVACUATION = new InterventionManeuver("aerial_evacuation",
            "Evacuazione dall'alto", 9, TargetScope.ALL_HAZARDS, InterventionType.TECHNICAL, 1.0, 1.5, false);
    private static final List<Maneuver> STARTING_MANEUVERS = List.of(HIGH_RESCUE, SCENE_LIGHTING);
    private static final List<Maneuver> ALL_MANEUVERS = List.of(HIGH_RESCUE, SCENE_LIGHTING, AERIAL_EVACUATION);

    @Override
    public String id() {
        return "aerial_ladder";
    }

    @Override
    public String displayName() {
        return "Autoscala";
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
