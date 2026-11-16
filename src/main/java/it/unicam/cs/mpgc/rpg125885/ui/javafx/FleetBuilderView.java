package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.engine.dispatch.FleetBuilder;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Composizione della flotta a punti: sceglie tipo, capo-equipaggio e tratto di ogni unità. */
final class FleetBuilderView extends BorderPane {

    private static final double SPACING = 12;
    private static final String DEFAULT_LEADER = "Capo squadra";

    private final GameService game;
    private final SceneNavigator navigator;
    private final FleetBuilder builder;

    private final ListView<VehicleType> typeList = new ListView<>();
    private final Label typeDetails = new Label("Seleziona un mezzo");
    private final TextField leaderField = new TextField();
    private final ComboBox<Trait> traitBox = new ComboBox<>();
    private final Button addButton = new Button("Aggiungi alla flotta");
    private final ListView<Unit> fleetList = new ListView<>();
    private final Button removeButton = new Button("Rimuovi");
    private final Button startButton = new Button("Inizia la notte");
    private final Label budgetLabel = new Label();

    FleetBuilderView(GameService game, SceneNavigator navigator) {
        this.game = game;
        this.navigator = navigator;
        this.builder = game.newFleetBuilder();
        setPadding(new Insets(SPACING));
        setTop(budgetLabel);
        setLeft(buildCatalogPane());
        setRight(buildFleetPane());
        setBottom(buildActions());
        refresh();
    }

    private VBox buildCatalogPane() {
        typeList.getItems().setAll(game.purchasableVehicleTypes());
        typeList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(VehicleType type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty || type == null ? null
                        : type.displayName() + " — " + type.cost() + " pt (" + type.agency().displayName() + ")");
            }
        });
        typeList.getSelectionModel().selectedItemProperty().addListener((obs, old, type) -> {
            typeDetails.setText(type == null ? "Seleziona un mezzo" : describe(type));
            refresh();
        });

        leaderField.setPromptText("Capo-equipaggio");
        leaderField.textProperty().addListener((obs, old, text) -> refresh());
        traitBox.getItems().setAll(game.traits());
        traitBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Trait trait) {
                return trait == null ? "" : trait.name();
            }

            @Override
            public Trait fromString(String text) {
                return null;
            }
        });
        traitBox.getSelectionModel().selectFirst();
        addButton.setOnAction(event -> addSelected());

        VBox pane = new VBox(SPACING, new Label("Mezzi disponibili"), typeList, typeDetails,
                leaderField, traitBox, addButton);
        pane.setPrefWidth(440);
        return pane;
    }

    private VBox buildFleetPane() {
        fleetList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Unit unit, boolean empty) {
                super.updateItem(unit, empty);
                setText(empty || unit == null ? null
                        : unit.name() + " — " + unit.crewLeader() + " (" + unit.trait().name() + ")");
            }
        });
        fleetList.getSelectionModel().selectedItemProperty().addListener((obs, old, unit) -> refresh());
        removeButton.setOnAction(event -> {
            builder.remove(fleetList.getSelectionModel().getSelectedItem());
            refresh();
        });
        VBox pane = new VBox(SPACING, new Label("La tua flotta"), fleetList, removeButton);
        pane.setPrefWidth(440);
        return pane;
    }

    private HBox buildActions() {
        startButton.setOnAction(event -> startGame());
        Button back = new Button("Indietro");
        back.setOnAction(event -> navigator.showMainMenu());
        HBox actions = new HBox(SPACING, back, startButton);
        actions.setPadding(new Insets(SPACING, 0, 0, 0));
        return actions;
    }

    private void addSelected() {
        String leader = leaderField.getText().isBlank() ? DEFAULT_LEADER : leaderField.getText().trim();
        builder.add(typeList.getSelectionModel().getSelectedItem(), leader, traitBox.getValue());
        leaderField.clear();
        refresh();
    }

    private void startGame() {
        try {
            game.startNewGame(builder);
            navigator.showDispatch();
        } catch (IllegalStateException e) {
            navigator.showError(e.getMessage());
        }
    }

    private void refresh() {
        budgetLabel.setText("Budget residuo: " + builder.remainingBudget() + " / " + builder.budget()
                + " punti — unità: " + builder.units().size() + " (minimo " + builder.minimumUnits() + ")");
        fleetList.getItems().setAll(builder.units());
        VehicleType selected = typeList.getSelectionModel().getSelectedItem();
        addButton.setDisable(selected == null || !builder.canAfford(selected) || traitBox.getValue() == null);
        removeButton.setDisable(fleetList.getSelectionModel().isEmpty());
        startButton.setDisable(!builder.isComplete());
    }

    private static String describe(VehicleType type) {
        Stats stats = type.baseStats();
        return type.displayName() + "\nResistenza " + stats.maxEndurance() + " · Risorse " + stats.maxResources()
                + "\nTecnica " + stats.technique() + " · Medicina " + stats.medicine()
                + " · Coordinamento " + stats.coordination()
                + "\nSangue freddo " + stats.composure() + " · Prontezza " + stats.readiness();
    }
}
