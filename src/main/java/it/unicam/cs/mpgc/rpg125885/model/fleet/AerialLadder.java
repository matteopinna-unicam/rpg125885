package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/** Autoscala: specialista lento, efficace sui pericoli in quota. */
public final class AerialLadder implements VehicleType {

    private static final int COST = 3;
    private static final Stats BASE = new Stats(100, 25, 12, 2, 4, 9, 3);
    private static final Stats GROWTH = new Stats(9, 2, 3, 0, 1, 1, 1);

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
    public boolean isPurchasable() {
        return true;
    }
}
