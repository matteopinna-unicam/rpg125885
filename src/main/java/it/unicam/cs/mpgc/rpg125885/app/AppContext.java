package it.unicam.cs.mpgc.rpg125885.app;

import it.unicam.cs.mpgc.rpg125885.content.ContentCatalog;
import it.unicam.cs.mpgc.rpg125885.content.ManeuverCatalog;
import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.behavior.BehaviorRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.behavior.EscalatingBehavior;
import it.unicam.cs.mpgc.rpg125885.engine.behavior.SpreadingBehavior;
import it.unicam.cs.mpgc.rpg125885.engine.behavior.SteadyBehavior;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.ProportionalMalusPolicy;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.ScenarioFactory;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.ReadinessTurnOrder;
import it.unicam.cs.mpgc.rpg125885.engine.progression.LinearLevelCurve;
import it.unicam.cs.mpgc.rpg125885.engine.progression.ProgressionService;
import it.unicam.cs.mpgc.rpg125885.engine.rules.JavaRandomSource;
import it.unicam.cs.mpgc.rpg125885.engine.rules.RandomSource;
import it.unicam.cs.mpgc.rpg125885.engine.rules.StandardEffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AerialLadder;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AirAmbulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.MedicalCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.PatrolCar;
import it.unicam.cs.mpgc.rpg125885.persistence.GameRepository;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg125885.persistence.xml.XmlContentCatalog;
import it.unicam.cs.mpgc.rpg125885.persistence.xml.XmlGameRepository;
import it.unicam.cs.mpgc.rpg125885.service.GameService;

/**
 * Composition root: l'unico posto in cui si scelgono le implementazioni concrete.
 * Sostituire una strategia, il formato di salvataggio o la fonte di casualità significa cambiare solo questa classe.
 */
public final class AppContext {

    private final GameService gameService;

    private AppContext(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * @throws PersistenceException     se il contenuto di gioco non si legge o non rispetta lo schema
     * @throws IllegalArgumentException se due manovre dei mezzi registrati hanno lo stesso id
     */
    public static AppContext create() throws PersistenceException {
        VehicleTypeRegistry vehicleTypes = vehicleTypes();
        // Il catalogo rifiuta gli id di manovra duplicati: costruirlo qui blocca l'avvio con un errore chiaro
        new ManeuverCatalog(vehicleTypes);
        ContentCatalog content = new XmlContentCatalog(vehicleTypes);
        GameRepository repository = new XmlGameRepository(XmlGameRepository.defaultDirectory(), content, vehicleTypes);
        RandomSource random = new JavaRandomSource();
        ScenarioFactory scenarioFactory = new ScenarioFactory(random, new ReadinessTurnOrder(), behaviors(),
                efficiency -> new StandardEffectCalculator(random, efficiency));
        ProgressionService progression = new ProgressionService(new LinearLevelCurve());
        return new AppContext(new GameService(content, vehicleTypes, repository,
                new ProportionalMalusPolicy(vehicleTypes), scenarioFactory, progression));
    }

    public GameService gameService() {
        return gameService;
    }

    private static VehicleTypeRegistry vehicleTypes() {
        VehicleTypeRegistry registry = new VehicleTypeRegistry();
        registry.register(new FireEngine());
        registry.register(new AerialLadder());
        registry.register(new Ambulance());
        registry.register(new MedicalCar());
        registry.register(new PatrolCar());
        registry.register(new AirAmbulance());
        return registry;
    }

    private static BehaviorRegistry behaviors() {
        BehaviorRegistry registry = new BehaviorRegistry();
        registry.register("steady", new SteadyBehavior());
        registry.register("escalating", new EscalatingBehavior());
        registry.register("spreading", new SpreadingBehavior());
        return registry;
    }
}
