package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.item.Gear;
import it.unicam.cs.mpgc.rpg125885.model.item.GearSlot;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.stream.Collectors;

/** Schede delle unità, rifornimenti, dotazioni e salvataggio (consentito solo tra una fase e l'altra). */
final class FleetView extends BorderPane {

    private static final double SPACING = 12;
    private static final String DEFAULT_SLOT = "slot1";

    private final GameService game;
    private final SceneNavigator navigator;
    private final BackgroundTasks background;

    private final ListView<Unit> unitList = new ListView<>();
    private final Label details = new Label("Seleziona un'unità");
    private final HBox unequipButtons = new HBox(SPACING / 2);
    private final ComboBox<Gear> gearBox = new ComboBox<>();
    private final Button equipButton = new Button("Equipaggia");
    private final Label inventoryLabel = new Label();
    private final TextField slotField = new TextField(DEFAULT_SLOT);
    private final Button saveButton = new Button("Salva partita");

    FleetView(GameService game, SceneNavigator navigator, BackgroundTasks background) {
        this.game = game;
        this.navigator = navigator;
        this.background = background;
        setPadding(new Insets(SPACING));
        setLeft(buildUnitPane());
        setCenter(buildDetailPane());
        setRight(inventoryPane());
        setBottom(buildActions());
        refresh();
    }

    private VBox buildUnitPane() {
        unitList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Unit unit, boolean empty) {
                super.updateItem(unit, empty);
                setText(empty || unit == null ? null : unit.name() + " — " + unit.crewLeader());
            }
        });
        unitList.getSelectionModel().selectedItemProperty().addListener((obs, old, unit) -> showUnit(unit));
        VBox pane = new VBox(SPACING, new Label("Unità"), unitList);
        pane.setPrefWidth(260);
        return pane;
    }

    private VBox buildDetailPane() {
        gearBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Gear gear) {
                return gear == null ? "" : gear.name();
            }

            @Override
            public Gear fromString(String text) {
                return null;
            }
        });
        equipButton.setOnAction(event -> equipSelected());
        VBox pane = new VBox(SPACING, details, unequipButtons, new HBox(SPACING, gearBox, equipButton));
        pane.setPadding(new Insets(0, SPACING, 0, SPACING));
        return pane;
    }

    private VBox inventoryPane() {
        inventoryLabel.setWrapText(true);
        VBox pane = new VBox(SPACING, new Label("Inventario"), inventoryLabel);
        pane.setPrefWidth(260);
        return pane;
    }

    private HBox buildActions() {
        saveButton.setOnAction(event -> save());
        Button back = new Button("Indietro");
        back.setOnAction(event -> navigator.showPhase());
        HBox actions = new HBox(SPACING, back, new Label("Nome salvataggio:"), slotField, saveButton);
        actions.setPadding(new Insets(SPACING, 0, 0, 0));
        return actions;
    }

    private void refresh() {
        Unit selected = unitList.getSelectionModel().getSelectedItem();
        unitList.getItems().setAll(game.fleet().units());
        if (selected != null) {
            unitList.getSelectionModel().select(selected);
        }
        inventoryLabel.setText(game.fleet().inventory().stream()
                .map(entry -> entry.getKey().name() + " ×" + entry.getValue())
                .collect(Collectors.joining("\n")));
        showUnit(selected);
    }

    private void showUnit(Unit unit) {
        unequipButtons.getChildren().clear();
        gearBox.getItems().setAll(game.spareGear());
        gearBox.getSelectionModel().selectFirst();
        equipButton.setDisable(unit == null || gearBox.getItems().isEmpty());
        if (unit == null) {
            details.setText("Seleziona un'unità");
            return;
        }
        Stats stats = unit.effectiveStats();
        details.setText(unit.name() + " — " + unit.crewLeader() + " (" + unit.trait().name() + ")"
                + "\nLivello " + unit.level() + " · Esperienza " + unit.experience()
                + "\nResistenza " + unit.endurance().current() + "/" + unit.endurance().max()
                + " · Risorse " + unit.resources().current() + "/" + unit.resources().max()
                + "\nTecnica " + stats.technique() + " · Medicina " + stats.medicine()
                + " · Coordinamento " + stats.coordination()
                + "\nSangue freddo " + stats.composure() + " · Prontezza " + stats.readiness());
        for (GearSlot slot : GearSlot.values()) {
            Button button = new Button(slot + ": " + unit.gearIn(slot).map(Item::name).orElse("—"));
            button.setDisable(unit.gearIn(slot).isEmpty());
            button.setOnAction(event -> unequip(unit, slot));
            unequipButtons.getChildren().add(button);
        }
    }

    private void equipSelected() {
        try {
            game.equip(unitList.getSelectionModel().getSelectedItem(), gearBox.getValue());
        } catch (IllegalStateException e) {
            navigator.showError(e.getMessage());
        }
        refresh();
    }

    private void unequip(Unit unit, GearSlot slot) {
        try {
            game.unequip(unit, slot);
        } catch (IllegalStateException e) {
            navigator.showError(e.getMessage());
        }
        refresh();
    }

    private void save() {
        String slot = slotField.getText().trim();
        saveButton.setDisable(true);
        background.run(() -> game.saveGame(slot),
                () -> saveButton.setDisable(false),
                error -> {
                    saveButton.setDisable(false);
                    navigator.showError(error.getMessage());
                });
    }
}
