package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterveneAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionStatus;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.ReadinessTurnOrder;
import it.unicam.cs.mpgc.rpg125885.engine.progression.LinearLevelCurve;
import it.unicam.cs.mpgc.rpg125885.engine.progression.ProgressionService;
import it.unicam.cs.mpgc.rpg125885.engine.rules.ScriptedRandom;
import it.unicam.cs.mpgc.rpg125885.engine.rules.StandardEffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AerialLadder;
import it.unicam.cs.mpgc.rpg125885.model.fleet.AirAmbulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.MedicalCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.PatrolCar;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.model.item.RecoverySupply;
import it.unicam.cs.mpgc.rpg125885.model.shift.Decision;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.model.shift.Phase;
import it.unicam.cs.mpgc.rpg125885.model.shift.PhaseType;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;
import it.unicam.cs.mpgc.rpg125885.model.shift.Reward;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.NO_BONUS;

/** Costruttori di flotte, chiamate e direttori di prova condivisi dai test del package. */
final class DispatchFixture {

    static final int EASY_GRAVITY = 10;
    static final int IMPOSSIBLE_GRAVITY = 10_000;

    private DispatchFixture() {
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

    static Item supply() {
        return new RecoverySupply("energy_bar", "Barretta", "Recupero", GaugeKind.ENDURANCE, 30);
    }

    static Unit engine() {
        return new Unit("APS 1", "Rossi", new FireEngine(), NO_BONUS);
    }

    static Unit ambulance() {
        return new Unit("Ambulanza 1", "Bianchi", new Ambulance(), NO_BONUS);
    }

    static Unit patrol() {
        return new Unit("Pattuglia 1", "Verdi", new PatrolCar(), NO_BONUS);
    }

    static Unit airAmbulance() {
        return new Unit("ELI 1", "Serra", new AirAmbulance(), NO_BONUS);
    }

    static Scenario scenario(String id, int gravity) {
        return scenario(id, gravity, 3, Requirements.NONE, Optional.empty(), Optional.empty());
    }

    static Scenario scenario(String id, int gravity, int rounds, Requirements requirements,
                             Optional<Decision> decision, Optional<Reward> reward) {
        HazardTemplate template = new HazardTemplate("h_" + id, "Pericolo " + id, gravity, 5,
                Map.of(InterventionType.TECHNICAL, 1.0), false, 1, 5, "steady", 20);
        return new Scenario(id, "Titolo " + id, "Zona", "Testo " + id, List.of(template), rounds, requirements,
                decision, "Successo " + id, "Fallimento " + id, 8, 12, 1, reward);
    }

    static Phase calls(String time, Scenario... scenarios) {
        return new Phase(time, PhaseType.CALLS, List.of(scenarios));
    }

    static Phase finale(String time, Scenario... scenarios) {
        return new Phase(time, PhaseType.FINALE, List.of(scenarios));
    }

    static Phase base(String time) {
        return new Phase(time, PhaseType.BASE, List.of());
    }

    static ScenarioFactory scenarioFactory() {
        return new ScenarioFactory(ScriptedRandom.neutral(), new ReadinessTurnOrder(), InterventionFixture.behaviors(),
                efficiency -> new StandardEffectCalculator(ScriptedRandom.neutral(), efficiency));
    }

    static ShiftDirector director(GameState state) {
        return new ShiftDirector(state, new ProportionalMalusPolicy(vehicleTypes()), scenarioFactory(),
                new ProgressionService(new LinearLevelCurve()), Map.of(supply(), 2));
    }

    /** Gioca l'intervento facendo intervenire ogni unità sul primo pericolo attivo, finché non finisce. */
    static void play(Intervention intervention) {
        intervention.start();
        while (intervention.status() == InterventionStatus.ONGOING) {
            Unit actor = intervention.currentUnit().orElseThrow();
            intervention.submit(new InterveneAction(actor, intervention.activeHazards().get(0)));
        }
    }
}
