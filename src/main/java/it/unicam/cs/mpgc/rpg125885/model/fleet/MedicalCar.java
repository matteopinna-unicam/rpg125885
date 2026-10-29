package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.InterventionManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.RecoveryManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;

import java.util.List;

/** Automedica: supporto sanitario fragile e potente. */
public final class MedicalCar implements VehicleType {

    private static final int COST = 3;
    private static final Stats BASE = new Stats(70, 35, 3, 17, 6, 8, 9);
    private static final Stats GROWTH = new Stats(6, 3, 0, 3, 1, 1, 1);

    private static final Maneuver ADVANCED_MANEUVERS = new InterventionManeuver("advanced_maneuvers",
            "Manovre avanzate", 7, TargetScope.SINGLE_HAZARD, InterventionType.MEDICAL, 2.0, false);
    private static final Maneuver ADVANCED_SUPPORT = new RecoveryManeuver("advanced_support", "Supporto avanzato", 7,
            TargetScope.SINGLE_UNIT, GaugeKind.ENDURANCE, 1.6);
    private static final Maneuver MEDICAL_COORDINATION = new InterventionManeuver("medical_coordination",
            "Coordinamento sanitario", 10, TargetScope.ALL_HAZARDS, InterventionType.MEDICAL, 1.0, false);
    private static final List<Maneuver> STARTING_MANEUVERS = List.of(ADVANCED_MANEUVERS, ADVANCED_SUPPORT);
    private static final List<Maneuver> ALL_MANEUVERS =
            List.of(ADVANCED_MANEUVERS, ADVANCED_SUPPORT, MEDICAL_COORDINATION);

    @Override
    public String id() {
        return "medical_car";
    }

    @Override
    public String displayName() {
        return "Automedica";
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
