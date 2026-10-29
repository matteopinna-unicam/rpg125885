package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/** Autopompa serbatoio (APS): resistente, danno tecnico. */
public final class FireEngine implements VehicleType {

    private static final int COST = 3;
    private static final Stats BASE = new Stats(120, 20, 14, 3, 5, 11, 5);
    private static final Stats GROWTH = new Stats(11, 2, 3, 0, 1, 2, 1);

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
    public boolean isPurchasable() {
        return true;
    }
}
