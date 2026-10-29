package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.CoverManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.InterventionManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.RecoveryManeuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;

import java.util.List;

/** Pattuglia: supporto e controllo della scena. */
public final class PatrolCar implements VehicleType {

    private static final int COST = 2;
    private static final Stats BASE = new Stats(85, 25, 6, 4, 15, 9, 10);
    private static final Stats GROWTH = new Stats(8, 2, 1, 0, 3, 1, 2);

    private static final Maneuver CROWD_CONTROL = new InterventionManeuver("crowd_control", "Gestione folla", 4,
            TargetScope.SINGLE_HAZARD, InterventionType.COORDINATION, 1.5, false);
    private static final Maneuver SAFETY_CORDON = new CoverManeuver("safety_cordon", "Cordone di sicurezza", 5,
            TargetScope.ALL_UNITS);
    private static final Maneuver SHIFT_CHANGE = new RecoveryManeuver("shift_change", "Cambio turno", 8,
            TargetScope.ALL_UNITS, GaugeKind.RESOURCES, 1.0);
    private static final List<Maneuver> STARTING_MANEUVERS = List.of(CROWD_CONTROL, SAFETY_CORDON);
    private static final List<Maneuver> ALL_MANEUVERS = List.of(CROWD_CONTROL, SAFETY_CORDON, SHIFT_CHANGE);

    @Override
    public String id() {
        return "patrol_car";
    }

    @Override
    public String displayName() {
        return "Pattuglia";
    }

    @Override
    public Agency agency() {
        return Agency.POLICE;
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
        return InterventionType.COORDINATION;
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
