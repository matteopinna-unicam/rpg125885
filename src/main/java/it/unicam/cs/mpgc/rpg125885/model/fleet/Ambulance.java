package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.InterventionManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.RecoveryManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;

import java.util.List;

/** Ambulanza: supporto sanitario resistente. */
public final class Ambulance implements VehicleType {

    private static final int COST = 2;
    private static final Stats BASE = new Stats(90, 30, 4, 12, 7, 7, 7);
    private static final Stats GROWTH = new Stats(9, 3, 1, 2, 1, 1, 1);

    private static final Maneuver STABILIZATION = new InterventionManeuver("stabilization", "Stabilizzazione", 5,
            TargetScope.SINGLE_HAZARD, InterventionType.MEDICAL, 1.5, false);
    private static final Maneuver SUPPORT = new RecoveryManeuver("ambulance_support", "Supporto", 5,
            TargetScope.SINGLE_UNIT, GaugeKind.ENDURANCE, 1.2);
    private static final Maneuver DEFUSING = new RecoveryManeuver("defusing", "Defusing", 10,
            TargetScope.ALL_UNITS, GaugeKind.ENDURANCE, 0.7);
    private static final List<Maneuver> STARTING_MANEUVERS = List.of(STABILIZATION, SUPPORT);
    private static final List<Maneuver> ALL_MANEUVERS = List.of(STABILIZATION, SUPPORT, DEFUSING);

    @Override
    public String id() {
        return "ambulance";
    }

    @Override
    public String displayName() {
        return "Ambulanza";
    }

    @Override
    public Agency agency() {
        return Agency.EMS;
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
        return InterventionType.MEDICAL;
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
