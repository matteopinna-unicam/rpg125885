package it.unicam.cs.mpgc.rpg125885.content;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Agency;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AerialLadder;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AirAmbulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.MedicalCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.PatrolCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ContentRegistryTest {

    private VehicleTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new VehicleTypeRegistry();
        registry.register(new FireEngine());
        registry.register(new AerialLadder());
        registry.register(new Ambulance());
        registry.register(new MedicalCar());
        registry.register(new PatrolCar());
        registry.register(new AirAmbulance());
    }

    @Test
    void findsRegisteredTypesById() {
        VehicleType ambulance = registry.byId("ambulance");

        assertEquals("Ambulanza", ambulance.displayName());
        assertSame(ambulance, registry.byId("ambulance"));
    }

    @Test
    void rejectsUnknownAndDuplicateIds() {
        assertThrows(IllegalArgumentException.class, () -> registry.byId("tank"));
        assertThrows(IllegalArgumentException.class, () -> registry.register(new FireEngine()));
    }

    @Test
    void purchasableExcludesTheAirAmbulance() {
        assertEquals(5, registry.purchasable().size());
        assertFalse(registry.purchasable().stream().anyMatch(type -> type.id().equals("air_ambulance")));
        assertEquals(6, registry.all().size());
    }

    @Test
    void catalogFindsEveryManeuverOfEveryType() {
        ManeuverCatalog catalog = new ManeuverCatalog(registry);

        assertEquals("Spegnimento", catalog.byId("extinguish").name());
        assertEquals("Recupero aereo", catalog.byId("air_recovery").name());
        assertEquals("Cambio turno", catalog.byId("shift_change").name());
        assertThrows(IllegalArgumentException.class, () -> catalog.byId("unknown"));
    }

    @Test
    void catalogRejectsDuplicateManeuverIds() {
        VehicleType original = new FireEngine();
        registry.register(new VehicleType() {
            @Override public String id() { return "fire_engine_copy"; }
            @Override public String displayName() { return "Copia"; }
            @Override public Agency agency() { return original.agency(); }
            @Override public int cost() { return original.cost(); }
            @Override public Stats baseStats() { return original.baseStats(); }
            @Override public Stats growthPerLevel() { return original.growthPerLevel(); }
            @Override public InterventionType primaryType() { return original.primaryType(); }
            @Override public List<Maneuver> maneuversAt(int level) { return original.maneuversAt(level); }
            @Override public boolean isPurchasable() { return true; }
        });

        assertThrows(IllegalArgumentException.class, () -> new ManeuverCatalog(registry));
    }
}
