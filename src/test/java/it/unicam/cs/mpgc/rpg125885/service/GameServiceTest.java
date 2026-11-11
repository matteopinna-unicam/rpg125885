package it.unicam.cs.mpgc.rpg125885.service;

import it.unicam.cs.mpgc.rpg125885.content.ContentCatalog;
import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.FleetBuilder;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.InterventionReport;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.ProportionalMalusPolicy;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.ScenarioFactory;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterveneAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture;
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
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.model.item.RecoverySupply;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.CrisisIndex;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.model.shift.Phase;
import it.unicam.cs.mpgc.rpg125885.model.shift.PhaseType;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.model.shift.Shift;
import it.unicam.cs.mpgc.rpg125885.persistence.GameRepository;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg125885.persistence.SaveSlotInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionFixture.NO_BONUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceTest {

    private static final Gear HELMET = new Gear("helmet", "Casco", "", GearSlot.HEAD, new Stats(0, 0, 0, 0, 0, 3, 0));
    private static final Gear CAP = new Gear("cap", "Berretto", "", GearSlot.HEAD, new Stats(0, 0, 0, 0, 0, 1, 0));

    private StubContent content;
    private MemoryRepository repository;
    private GameService service;

    @BeforeEach
    void setUp() {
        content = new StubContent();
        repository = new MemoryRepository();
        VehicleTypeRegistry types = new VehicleTypeRegistry();
        List.of(new FireEngine(), new AerialLadder(), new Ambulance(), new MedicalCar(), new PatrolCar(),
                new AirAmbulance()).forEach(types::register);
        ScenarioFactory factory = new ScenarioFactory(ScriptedRandom.neutral(), new ReadinessTurnOrder(),
                InterventionFixture.behaviors(),
                efficiency -> new StandardEffectCalculator(ScriptedRandom.neutral(), efficiency));
        service = new GameService(content, types, repository, new ProportionalMalusPolicy(types), factory,
                new ProgressionService(new LinearLevelCurve()));
    }

    private void startWithFourUnits() {
        service.startNewGame(fourUnitBuilder());
    }

    private FleetBuilder fourUnitBuilder() {
        FleetBuilder builder = service.newFleetBuilder();
        builder.add(new FireEngine(), "A", NO_BONUS);
        builder.add(new Ambulance(), "B", NO_BONUS);
        builder.add(new PatrolCar(), "C", NO_BONUS);
        builder.add(new PatrolCar(), "D", NO_BONUS);
        return builder;
    }

    @Test
    void newFleetBuilderHasTheBudgetAndTheStartingSupplies() {
        FleetBuilder builder = fourUnitBuilder();

        assertEquals(FleetBuilder.DEFAULT_BUDGET - 3 - 2 - 2 - 2, builder.remainingBudget());
        assertEquals(3, builder.build().inventory().quantityOf(content.item("energy_bar")));
    }

    @Test
    void offersOnlyPurchasableTypesAndTheCatalogTraits() {
        assertEquals(5, service.purchasableVehicleTypes().size());
        assertEquals(List.of(NO_BONUS), service.traits());
    }

    @Test
    void aNewGameStartsAtTwentyCrisisInTheFirstPhase() {
        startWithFourUnits();

        assertEquals(GameService.INITIAL_CRISIS, service.crisisIndex().value());
        assertEquals(0, service.phaseIndex());
        assertEquals("22:00", service.currentPhase().time());
        assertEquals(4, service.fleet().units().size());
    }

    @Test
    void everyOperationNeedsAGame() {
        assertFalse(service.hasGame());
        assertThrows(IllegalStateException.class, service::crisisIndex);
        assertThrows(IllegalStateException.class, service::calls);
    }

    @Test
    void anIncompleteFleetCannotStart() {
        FleetBuilder builder = service.newFleetBuilder();
        builder.add(new FireEngine(), "A", NO_BONUS);

        assertThrows(IllegalStateException.class, () -> service.startNewGame(builder));
    }

    @Test
    void aWholeCallGoesThroughTheService() {
        startWithFourUnits();
        Scenario call = service.calls().get(0);
        Unit engine = service.availableUnits().get(0);

        service.assign(call, List.of(engine));
        Intervention intervention = service.beginIntervention(Optional.empty());
        intervention.start();
        while (intervention.status() == InterventionStatus.ONGOING) {
            intervention.submit(new InterveneAction(intervention.currentUnit().orElseThrow(),
                    intervention.activeHazards().get(0)));
        }
        InterventionReport report = service.finishIntervention();
        service.closePhase();

        assertEquals(CallOutcome.RESOLVED, report.outcome());
        assertEquals(GameService.INITIAL_CRISIS - 5, service.crisisIndex().value());
        assertEquals(1, service.phaseIndex());
        assertEquals(1, service.callLog().size());
    }

    @Test
    void savingIsRefusedWhileAssignmentsArePending() {
        startWithFourUnits();
        service.assign(service.calls().get(0), List.of(service.availableUnits().get(0)));

        assertThrows(IllegalStateException.class, () -> service.saveGame("slot"));
        assertTrue(repository.saved.isEmpty());
    }

    @Test
    void savingBetweenPhasesAndLoadingRestoreTheSavedState() throws PersistenceException {
        startWithFourUnits();
        service.saveGame("slot");
        service.closePhase();
        assertEquals(1, service.phaseIndex());

        service.loadGame("slot");

        assertEquals(0, service.phaseIndex());
        assertEquals(GameService.INITIAL_CRISIS, service.crisisIndex().value());
    }

    @Test
    void equippingMovesTheGearFromTheInventoryAndReturnsThePreviousOne() {
        startWithFourUnits();
        Unit unit = service.fleet().units().get(0);
        service.fleet().inventory().add(HELMET, 1);
        service.fleet().inventory().add(CAP, 1);

        service.equip(unit, CAP);
        service.equip(unit, HELMET);

        assertSame(HELMET, unit.gearIn(GearSlot.HEAD).orElseThrow());
        assertEquals(0, service.fleet().inventory().quantityOf(HELMET));
        assertEquals(1, service.fleet().inventory().quantityOf(CAP));

        service.unequip(unit, GearSlot.HEAD);

        assertTrue(unit.gearIn(GearSlot.HEAD).isEmpty());
        assertEquals(1, service.fleet().inventory().quantityOf(HELMET));
    }

    @Test
    void equippingAGearNotInTheInventoryIsRefused() {
        startWithFourUnits();
        Unit unit = service.fleet().units().get(0);

        assertThrows(IllegalStateException.class, () -> service.equip(unit, HELMET));
    }

    /** Contenuto minimo: i quattro rifornimenti noti, un tratto e una notte di due fasi con una chiamata. */
    private static final class StubContent implements ContentCatalog {

        private final Map<String, Item> items = new HashMap<>();
        private final Shift shift;

        StubContent() {
            for (String id : List.of("energy_bar", "refill_kit", "extinguisher", "aed")) {
                items.put(id, new RecoverySupply(id, id, "", GaugeKind.ENDURANCE, 10));
            }
            HazardTemplate easy = new HazardTemplate("easy", "Facile", 10, 5,
                    Map.of(InterventionType.TECHNICAL, 1.0), false, 1, 0, "steady", 20);
            Scenario call = new Scenario("call", "Chiamata", "Zona", "Testo", List.of(easy), 3, Requirements.NONE,
                    Optional.empty(), "ok", "ko", 8, 12, 1, Optional.empty());
            shift = new Shift(GameService.SHIFT_ID, List.of(new Phase("22:00", PhaseType.CALLS, List.of(call)),
                    new Phase("23:00", PhaseType.CALLS, List.of())));
        }

        @Override
        public HazardTemplate hazard(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Item item(String id) {
            return items.get(id);
        }

        @Override
        public Trait trait(String id) {
            return NO_BONUS;
        }

        @Override
        public List<Trait> traits() {
            return List.of(NO_BONUS);
        }

        @Override
        public Shift shift(String id) {
            return shift;
        }
    }

    /** Repository in memoria: ogni slot tiene una copia dell'ultimo stato salvato. */
    private static final class MemoryRepository implements GameRepository {

        private final Map<String, GameState> saved = new HashMap<>();

        @Override
        public void save(String slot, GameState state) {
            saved.put(slot, copyOf(state));
        }

        @Override
        public GameState load(String slot) throws PersistenceException {
            GameState state = saved.get(slot);
            if (state == null) {
                throw new PersistenceException("Slot inesistente: " + slot);
            }
            return copyOf(state);
        }

        @Override
        public List<SaveSlotInfo> listSlots() {
            return List.of();
        }

        @Override
        public void delete(String slot) {
            saved.remove(slot);
        }

        private static GameState copyOf(GameState state) {
            return new GameState(state.fleet(), state.shift(), state.phaseIndex(),
                    new CrisisIndex(state.crisisIndex().value()), state.callLog(), state.isAirAmbulanceUsed());
        }
    }
}
