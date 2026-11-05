package it.unicam.cs.mpgc.rpg125885.persistence.xml;

import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Ambulance;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.FireEngine;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallRecord;
import it.unicam.cs.mpgc.rpg125885.model.shift.CrisisIndex;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg125885.persistence.SaveSlotInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlGameRepositoryTest {

    private static final Instant MORNING = Instant.parse("2026-11-12T17:30:00Z");
    private static final Instant EVENING = Instant.parse("2026-11-12T21:45:30Z");
    private static final int CRISIS = 41;

    @TempDir
    Path directory;

    private VehicleTypeRegistry vehicleTypes;
    private XmlContentCatalog catalog;
    private XmlGameRepository repository;

    @BeforeEach
    void setUp() throws PersistenceException {
        vehicleTypes = PersistenceFixture.vehicleTypes();
        catalog = new XmlContentCatalog(vehicleTypes);
        repository = repositoryAt(MORNING);
    }

    @Test
    void savedGameIsLoadedBackIdentical() throws PersistenceException {
        GameState original = sampleState();

        repository.save("notte uno", original);
        GameState loaded = repository.load("notte uno");

        assertEquals(original.shift().id(), loaded.shift().id());
        assertEquals(original.phaseIndex(), loaded.phaseIndex());
        assertEquals(CRISIS, loaded.crisisIndex().value());
        assertTrue(loaded.isAirAmbulanceUsed());
        assertEquals(original.callLog(), loaded.callLog());
        assertEquals(2, loaded.fleet().units().size());
        for (Unit expected : original.fleet().units()) {
            assertSameUnit(expected, unitNamed(loaded.fleet(), expected.name()));
        }
        assertEquals(2, loaded.fleet().inventory().quantityOf(catalog.item("energy_bar")));
        assertEquals(1, loaded.fleet().inventory().quantityOf(catalog.item("foam_canister")));
    }

    @Test
    void savingAgainReplacesTheSlotAndLeavesNoTemporaryFile() throws PersistenceException, IOException {
        GameState state = sampleState();
        repository.save("notte", state);
        state.crisisIndex().raise(10);

        repository.save("notte", state);

        assertEquals(CRISIS + 10, repository.load("notte").crisisIndex().value());
        assertFalse(Files.exists(directory.resolve("notte.tmp")));
    }

    @Test
    void listSlotsSummarizesTheSavesFromTheMostRecent() throws PersistenceException, IOException {
        repository.save("alfa", sampleState());
        repositoryAt(EVENING).save("beta", sampleState());
        Files.writeString(directory.resolve("rotto.xml"), "<savegame", StandardCharsets.UTF_8);

        List<SaveSlotInfo> slots = repository.listSlots();

        assertEquals(List.of("beta", "alfa"), slots.stream().map(SaveSlotInfo::slot).toList());
        SaveSlotInfo alfa = slots.get(1);
        assertEquals("alfa", alfa.fleetName());
        assertEquals(LocalDateTime.of(2026, 11, 12, 17, 30), alfa.savedAt());
        assertEquals(1, alfa.phaseIndex());
        assertEquals(CRISIS, alfa.crisis());
        assertEquals(2, alfa.unitCount());
    }

    @Test
    void listSlotsIsEmptyBeforeTheFirstSave() throws PersistenceException {
        XmlGameRepository fresh = new XmlGameRepository(directory.resolve("nuova"), catalog, vehicleTypes);

        assertTrue(fresh.listSlots().isEmpty());
    }

    @Test
    void damagedFileIsReportedOnLoad() throws IOException {
        Files.writeString(directory.resolve("rotto.xml"), "<savegame", StandardCharsets.UTF_8);

        assertThrows(PersistenceException.class, () -> repository.load("rotto"));
    }

    @Test
    void fileThatViolatesTheSchemaIsRejected() throws IOException {
        Files.writeString(directory.resolve("vuoto.xml"),
                "<savegame version=\"1\" savedAt=\"2026-11-12T18:30:00\"/>", StandardCharsets.UTF_8);

        PersistenceException error = assertThrows(PersistenceException.class, () -> repository.load("vuoto"));

        assertTrue(error.getMessage().contains("File non valido"));
    }

    @Test
    void referenceToUnknownContentIsReportedAsInconsistent() throws PersistenceException, IOException {
        repository.save("notte", sampleState());
        Path file = directory.resolve("notte.xml");
        Files.writeString(file, Files.readString(file).replace("trait=\"veteran\"", "trait=\"fantasma\""));

        PersistenceException error = assertThrows(PersistenceException.class, () -> repository.load("notte"));

        assertTrue(error.getMessage().contains("incoerente"));
    }

    @Test
    void loadingAMissingSlotFails() {
        assertThrows(PersistenceException.class, () -> repository.load("assente"));
    }

    @Test
    void deleteRemovesTheSlotAndFailsIfMissing() throws PersistenceException {
        repository.save("notte", sampleState());

        repository.delete("notte");

        assertTrue(repository.listSlots().isEmpty());
        assertThrows(PersistenceException.class, () -> repository.delete("notte"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "../fuori", "a/b", "x:y", "uno troppo lungo per essere un nome di slot ragionevole"})
    void slotNamesThatCouldEscapeTheFolderAreRejected(String slot) {
        GameState state = sampleState();

        assertThrows(PersistenceException.class, () -> repository.save(slot, state));
        assertThrows(PersistenceException.class, () -> repository.load(slot));
    }

    private XmlGameRepository repositoryAt(Instant instant) throws PersistenceException {
        return new XmlGameRepository(directory, catalog, vehicleTypes, Clock.fixed(instant, ZoneOffset.UTC));
    }

    /** Due unità con livelli, dotazioni, danni e occupazione diversi, un inventario e un registro. */
    private GameState sampleState() {
        Unit engine = new Unit("APS 1", "Rossi", new FireEngine(), catalog.trait("veteran"));
        engine.levelUp();
        engine.levelUp();
        engine.addExperience(40);
        engine.equip((Gear) catalog.item("helmet"));
        engine.endurance().decrease(30);
        engine.resources().decrease(5);
        engine.occupyUntil(2);
        Unit ambulance = new Unit("AMB 1", "Bianchi", new Ambulance(), catalog.trait("empathic"));

        Fleet fleet = new Fleet(List.of(engine, ambulance));
        fleet.inventory().add(catalog.item("energy_bar"), 2);
        fleet.inventory().add(catalog.item("foam_canister"), 1);
        List<CallRecord> log = List.of(
                new CallRecord("via_tigli_gas", CallOutcome.RESOLVED, List.of("APS 1", "AMB 1")),
                new CallRecord("via_roma_crash", CallOutcome.IGNORED, List.of()));
        return new GameState(fleet, catalog.shift("red_shift"), 1, new CrisisIndex(CRISIS), log, true);
    }

    private static Unit unitNamed(Fleet fleet, String name) {
        return fleet.units().stream().filter(unit -> unit.name().equals(name)).findFirst().orElseThrow();
    }

    private static void assertSameUnit(Unit expected, Unit actual) {
        assertEquals(expected.crewLeader(), actual.crewLeader());
        assertEquals(expected.type().id(), actual.type().id());
        assertEquals(expected.trait(), actual.trait());
        assertEquals(expected.level(), actual.level());
        assertEquals(expected.experience(), actual.experience());
        assertEquals(expected.endurance().current(), actual.endurance().current());
        assertEquals(expected.endurance().max(), actual.endurance().max());
        assertEquals(expected.resources().current(), actual.resources().current());
        assertEquals(expected.busyUntil(), actual.busyUntil());
        assertEquals(expected.effectiveStats(), actual.effectiveStats());
        for (GearSlot slot : GearSlot.values()) {
            assertEquals(expected.gearIn(slot), actual.gearIn(slot));
        }
    }
}
