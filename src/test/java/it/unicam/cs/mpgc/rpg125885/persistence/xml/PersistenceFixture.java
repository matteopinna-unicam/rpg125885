package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AerialLadder;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AirAmbulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.MedicalCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.PatrolCar;

/** Registro dei sei tipi di mezzo, come lo comporrà l'applicazione. */
final class PersistenceFixture {

    private PersistenceFixture() {
    }

    static VehicleTypeRegistry vehicleTypes() {
        VehicleTypeRegistry registry = new VehicleTypeRegistry();
        registry.register(new FireEngine());
        registry.register(new AerialLadder());
        registry.register(new Ambulance());
        registry.register(new MedicalCar());
        registry.register(new PatrolCar());
        registry.register(new AirAmbulance());
        return registry;
    }
}
