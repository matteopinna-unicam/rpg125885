package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/** Pattuglia: supporto e controllo della scena. */
public final class PatrolCar implements VehicleType {

    private static final int COST = 2;
    private static final Stats BASE = new Stats(85, 25, 6, 4, 15, 9, 10);
    private static final Stats GROWTH = new Stats(8, 2, 1, 0, 3, 1, 2);

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
    public boolean isPurchasable() {
        return true;
    }
}
