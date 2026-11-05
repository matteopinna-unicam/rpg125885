package it.unicam.cs.mpgc.rpg125885.service;

import it.unicam.cs.mpgc.rpg125885.content.ContentCatalog;
import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.Assignment;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.BaseOption;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.DispatchAssessment;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.FleetBuilder;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.InterventionReport;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.RequirementPolicy;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.ScenarioFactory;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.ShiftDirector;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.progression.ProgressionService;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Fleet;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallRecord;
import it.unicam.cs.mpgc.rpg125885.model.shift.CrisisIndex;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;
import it.unicam.cs.mpgc.rpg125885.model.shift.Phase;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftOutcome;
import it.unicam.cs.mpgc.rpg125885.persistence.GameRepository;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg125885.persistence.SaveSlotInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Punto d'ingresso unico per qualsiasi interfaccia: nasconde motore, contenuto e persistenza.
 * Una UI web o testuale si scrive sopra questa classe senza toccare il resto.
 */
public final class GameService {

    public static final String SHIFT_ID = "red_shift";
    public static final int INITIAL_CRISIS = 20;

    private static final Map<String, Integer> STARTING_SUPPLIES = Map.of(
            "energy_bar", 3, "refill_kit", 2, "extinguisher", 1);
    private static final Map<String, Integer> RESUPPLY_PACKAGE = Map.of(
            "energy_bar", 2, "refill_kit", 2, "extinguisher", 1, "aed", 1);

    private final ContentCatalog content;
    private final VehicleTypeRegistry vehicleTypes;
    private final GameRepository repository;
    private final RequirementPolicy requirementPolicy;
    private final ScenarioFactory scenarioFactory;
    private final ProgressionService progression;
    private ShiftDirector director;

    public GameService(ContentCatalog content, VehicleTypeRegistry vehicleTypes, GameRepository repository,
                       RequirementPolicy requirementPolicy, ScenarioFactory scenarioFactory,
                       ProgressionService progression) {
        this.content = Objects.requireNonNull(content, "content");
        this.vehicleTypes = Objects.requireNonNull(vehicleTypes, "vehicleTypes");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.requirementPolicy = Objects.requireNonNull(requirementPolicy, "requirementPolicy");
        this.scenarioFactory = Objects.requireNonNull(scenarioFactory, "scenarioFactory");
        this.progression = Objects.requireNonNull(progression, "progression");
    }

    // --- Nuova partita ---

    public List<VehicleType> purchasableVehicleTypes() {
        return vehicleTypes.purchasable();
    }

    public List<Trait> traits() {
        return content.traits();
    }

    /** Composizione della flotta con budget, minimo di unità e rifornimenti iniziali della partita. */
    public FleetBuilder newFleetBuilder() {
        return new FleetBuilder(FleetBuilder.DEFAULT_BUDGET, FleetBuilder.DEFAULT_MINIMUM_UNITS,
                resolve(STARTING_SUPPLIES));
    }

    /**
     * Inizia la notte con la flotta composta.
     *
     * @throws IllegalStateException se la flotta non ha abbastanza unità
     */
    public void startNewGame(FleetBuilder builder) {
        Fleet fleet = builder.build();
        director = newDirector(new GameState(fleet, content.shift(SHIFT_ID), 0,
                new CrisisIndex(INITIAL_CRISIS), List.of(), false));
    }

    // --- Salvataggi ---

    public List<SaveSlotInfo> saveSlots() throws PersistenceException {
        return repository.listSlots();
    }

    public void loadGame(String slot) throws PersistenceException {
        director = newDirector(repository.load(slot));
    }

    /**
     * @throws IllegalStateException se ci sono assegnazioni o interventi in corso: si salva solo tra una fase e l'altra
     */
    public void saveGame(String slot) throws PersistenceException {
        if (!currentDirector().isBetweenPhases()) {
            throw new IllegalStateException("Si può salvare solo tra una fase e l'altra");
        }
        repository.save(slot, currentDirector().state());
    }

    public void deleteSlot(String slot) throws PersistenceException {
        repository.delete(slot);
    }

    public boolean hasGame() {
        return director != null;
    }

    // --- Stato della notte ---

    public Fleet fleet() {
        return currentDirector().state().fleet();
    }

    public CrisisIndex crisisIndex() {
        return currentDirector().state().crisisIndex();
    }

    public Phase currentPhase() {
        return currentDirector().currentPhase();
    }

    public int phaseIndex() {
        return currentDirector().state().phaseIndex();
    }

    public List<CallRecord> callLog() {
        return currentDirector().state().callLog();
    }

    public ShiftOutcome outcome() {
        return currentDirector().outcome();
    }

    // --- Sala operativa ---

    public List<Scenario> calls() {
        return currentDirector().calls();
    }

    public List<Unit> availableUnits() {
        return currentDirector().availableUnits();
    }

    public int maxUnitsPerCall() {
        return currentDirector().maxUnitsPerCall();
    }

    public DispatchAssessment preview(Scenario scenario, List<Unit> units) {
        return currentDirector().preview(scenario, units);
    }

    public void assign(Scenario scenario, List<Unit> units) {
        currentDirector().assign(scenario, units);
    }

    public void unassign(Scenario scenario) {
        currentDirector().unassign(scenario);
    }

    public List<Assignment> assignments() {
        return currentDirector().assignments();
    }

    // --- Interventi ---

    public Optional<Assignment> nextAssignment() {
        return currentDirector().nextAssignment();
    }

    /** Prepara il prossimo intervento; chi lo gioca aggiunge i listener e chiama {@link Intervention#start()}. */
    public Intervention beginIntervention(Optional<DecisionOption> choice) {
        return currentDirector().begin(choice);
    }

    public InterventionReport finishIntervention() {
        return currentDirector().finish();
    }

    public void closePhase() {
        currentDirector().closePhase();
    }

    // --- Base e flotta ---

    public void applyBase(BaseOption option) {
        currentDirector().applyBase(option);
    }

    /**
     * Equipaggia una dotazione presa dall'inventario; quella che occupava lo slot torna in inventario.
     *
     * @throws IllegalStateException se la dotazione non è in inventario o la notte è in pieno svolgimento
     */
    public void equip(Unit unit, Gear gear) {
        requireBetweenPhases();
        fleet().inventory().remove(gear, 1);
        unit.equip(gear).ifPresent(previous -> fleet().inventory().add(previous, 1));
    }

    /** Toglie la dotazione dallo slot e la rimette in inventario. */
    public void unequip(Unit unit, GearSlot slot) {
        requireBetweenPhases();
        unit.unequip(slot).ifPresent(removed -> fleet().inventory().add(removed, 1));
    }

    private ShiftDirector newDirector(GameState state) {
        return new ShiftDirector(state, requirementPolicy, scenarioFactory, progression, resolve(RESUPPLY_PACKAGE));
    }

    private Map<Item, Integer> resolve(Map<String, Integer> quantitiesById) {
        Map<Item, Integer> resolved = new LinkedHashMap<>();
        quantitiesById.forEach((id, quantity) -> resolved.put(content.item(id), quantity));
        return resolved;
    }

    private void requireBetweenPhases() {
        if (!currentDirector().isBetweenPhases()) {
            throw new IllegalStateException("La flotta si gestisce solo tra una fase e l'altra");
        }
    }

    private ShiftDirector currentDirector() {
        if (director == null) {
            throw new IllegalStateException("Nessuna partita in corso");
        }
        return director;
    }
}
