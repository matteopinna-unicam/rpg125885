package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/** Automedica: supporto sanitario fragile e potente. */
public final class MedicalCar implements VehicleType {

    private static final int COST = 3;
    private static final Stats BASE = new Stats(70, 35, 3, 17, 6, 8, 9);
    private static final Stats GROWTH = new Stats(6, 3, 0, 3, 1, 1, 1);

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
    public boolean isPurchasable() {
        return true;
    }
}
