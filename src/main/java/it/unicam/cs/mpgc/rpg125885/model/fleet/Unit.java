package it.unicam.cs.mpgc.rpg125885.model.fleet;

import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Unità della flotta: un mezzo con il suo equipaggio.
 * La Resistenza è il contatore di condizione ereditato da {@link Combatant}.
 */
public final class Unit extends Combatant {

    private static final int FIRST_LEVEL = 1;

    private final String crewLeader;
    private final VehicleType type;
    private final Trait trait;
    private final Gauge resources;
    private final Map<GearSlot, Gear> gear = new EnumMap<>(GearSlot.class);
    private int level = FIRST_LEVEL;
    private int experience;
    private int busyUntilPhase;
    private boolean covered;

    /** Crea un'unità di primo livello, con Resistenza e Risorse al massimo. */
    public Unit(String callsign, String crewLeader, VehicleType type, Trait trait) {
        super(callsign, new Gauge(startingStats(type, trait).maxEndurance()));
        this.crewLeader = requireNonBlank(crewLeader);
        this.type = type;
        this.trait = trait;
        this.resources = new Gauge(startingStats(type, trait).maxResources());
    }

    public String crewLeader() {
        return crewLeader;
    }

    public VehicleType type() {
        return type;
    }

    public Trait trait() {
        return trait;
    }

    public int level() {
        return level;
    }

    public int experience() {
        return experience;
    }

    public Gauge endurance() {
        return condition();
    }

    public Gauge resources() {
        return resources;
    }

    /** Il contatore indicato da {@code kind}. */
    public Gauge gauge(GaugeKind kind) {
        return switch (kind) {
            case ENDURANCE -> endurance();
            case RESOURCES -> resources;
        };
    }

    /** Al riparo, lo stress subito è dimezzato fino a {@link #endCover()}. */
    public boolean isCovered() {
        return covered;
    }

    public void takeCover() {
        covered = true;
    }

    public void endCover() {
        covered = false;
    }

    /** Statistiche base del mezzo + crescita per i livelli oltre il primo + tratto + dotazioni. */
    public Stats effectiveStats() {
        Stats stats = type.baseStats()
                .plus(type.growthPerLevel().times(level - FIRST_LEVEL))
                .plus(trait.bonus());
        for (Gear equipped : gear.values()) {
            stats = stats.plus(equipped.bonus());
        }
        return stats;
    }

    @Override
    public int initiative() {
        return effectiveStats().readiness();
    }

    public Optional<Gear> gearIn(GearSlot slot) {
        return Optional.ofNullable(gear.get(slot));
    }

    /**
     * Equipaggia una dotazione nel suo slot.
     *
     * @return la dotazione che occupava lo slot, se c'era
     */
    public Optional<Gear> equip(Gear newGear) {
        Objects.requireNonNull(newGear, "gear");
        Gear previous = gear.put(newGear.slot(), newGear);
        refreshLimits();
        return Optional.ofNullable(previous);
    }

    /** Toglie la dotazione dallo slot e la restituisce, se c'era. */
    public Optional<Gear> unequip(GearSlot slot) {
        Gear removed = gear.remove(slot);
        refreshLimits();
        return Optional.ofNullable(removed);
    }

    public void addExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("L'esperienza non può essere negativa: " + amount);
        }
        experience += amount;
    }

    /** Sale di un livello: i massimi crescono e Resistenza e Risorse tornano al massimo. */
    public void levelUp() {
        level++;
        refreshLimits();
        endurance().fill();
        resources.fill();
    }

    /** Occupa l'unità fino alla fase indicata (esclusa). */
    public void occupyUntil(int phase) {
        busyUntilPhase = phase;
    }

    public boolean isAvailableAt(int phase) {
        return phase >= busyUntilPhase;
    }

    private void refreshLimits() {
        Stats stats = effectiveStats();
        endurance().changeMax(stats.maxEndurance());
        resources.changeMax(stats.maxResources());
    }

    private static Stats startingStats(VehicleType type, Trait trait) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(trait, "trait");
        return type.baseStats().plus(trait.bonus());
    }

    private static String requireNonBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Il capo-equipaggio non può avere nome vuoto");
        }
        return value;
    }
}
