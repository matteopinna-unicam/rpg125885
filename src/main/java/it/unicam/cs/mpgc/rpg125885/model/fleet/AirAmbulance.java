package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/** Elisoccorso: rinforzo raro, non acquistabile, ottenuto come premio di una chiamata. */
public final class AirAmbulance implements VehicleType {

    private static final int NOT_FOR_SALE = 0;
    private static final Stats BASE = new Stats(80, 40, 5, 16, 8, 10, 12);

    @Override
    public String id() {
        return "air_ambulance";
    }

    @Override
    public String displayName() {
        return "Elisoccorso";
    }

    @Override
    public Agency agency() {
        return Agency.EMS;
    }

    @Override
    public int cost() {
        return NOT_FOR_SALE;
    }

    @Override
    public Stats baseStats() {
        return BASE;
    }

    @Override
    public Stats growthPerLevel() {
        return Stats.NONE;
    }

    @Override
    public InterventionType primaryType() {
        return InterventionType.MEDICAL;
    }

    @Override
    public boolean isPurchasable() {
        return false;
    }
}
