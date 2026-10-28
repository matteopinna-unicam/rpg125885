package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/** Ambulanza: supporto sanitario resistente. */
public final class Ambulance implements VehicleType {

    private static final int COST = 2;
    private static final Stats BASE = new Stats(90, 30, 4, 12, 7, 7, 7);
    private static final Stats GROWTH = new Stats(9, 3, 1, 2, 1, 1, 1);

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
    public boolean isPurchasable() {
        return true;
    }
}
