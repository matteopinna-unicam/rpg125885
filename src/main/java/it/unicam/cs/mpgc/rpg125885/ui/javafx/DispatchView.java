package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.engine.dispatch.Assignment;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.DispatchAssessment;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.shift.CrisisIndex;
import it.unicam.cs.mpgc.rpg125885.model.shift.Scenario;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Sala operativa: ora, Indice di Crisi, chiamate e assegnazione delle unità con anteprima del malus. */
final class DispatchView extends BorderPane {

    private static final double SPACING = 12;
    private static final double CRISIS_BAR_WIDTH = 300;
    private static final double CALL_PANE_WIDTH = 380;
    private static final String PERCENT_FORMAT = "%.0f%%";
    private static final double PERCENT = 100.0;

    private final GameService game;
    private final SceneNavigator navigator;

    private final Label timeLabel = new Label();
    private final ProgressBar crisisBar = new ProgressBar();
    private final Label crisisLabel = new Label();
    private final ListView<Scenario> callList = new ListView<>();
    private final Label callDetails = new Label("Seleziona una chiamata");
    private final VBox unitBoxes = new VBox(SPACING / 2);
    private final Label previewLabel = new Label();
    private final Button assignButton = new Button("Assegna");
    private final Button unassignButton = new Button("Annulla assegnazione");
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    DispatchView(GameService game, SceneNavigator navigator) {
        this.game = game;
        this.navigator = navigator;
        setPadding(new Insets(SPACING));
        setTop(buildHeader());
        setLeft(buildCallPane());
        setCenter(buildAssignmentPane());
        setBottom(buildFooter());
        refreshHeader();
        callList.getItems().setAll(game.calls());
        // All'apertura nessuna chiamata è selezionata: i pulsanti partono disabilitati
        showCall(null);
    }

    private HBox buildHeader() {
        crisisBar.setPrefWidth(CRISIS_BAR_WIDTH);
        HBox header = new HBox(SPACING, timeLabel, new Label("Indice di Crisi"), crisisBar, crisisLabel);
        header.setPadding(new Insets(0, 0, SPACING, 0));
        return header;
    }

    private VBox buildCallPane() {
        callList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Scenario scenario, boolean empty) {
                super.updateItem(scenario, empty);
                setText(empty || scenario == null ? null : label(scenario));
            }
        });
        callList.getSelectionModel().selectedItemProperty().addListener((obs, old, scenario) -> showCall(scenario));
        VBox pane = new VBox(SPACING, new Label("Chiamate in arrivo"), callList);
        pane.setPrefWidth(CALL_PANE_WIDTH);
        return pane;
    }

    private VBox buildAssignmentPane() {
        callDetails.setWrapText(true);
        previewLabel.setWrapText(true);
        assignButton.setOnAction(event -> assignSelected());
        unassignButton.setOnAction(event -> unassignSelected());
        VBox pane = new VBox(SPACING, callDetails, new Label("Unità (massimo " + game.maxUnitsPerCall() + ")"),
                unitBoxes, previewLabel, new HBox(SPACING, assignButton, unassignButton));
        pane.setPadding(new Insets(0, 0, 0, SPACING));
        return pane;
    }

    private HBox buildFooter() {
        Button fleet = new Button("Flotta e salvataggio");
        fleet.setOnAction(event -> navigator.showFleet());
        Button start = new Button("Avvia gli interventi");
        start.setOnAction(event -> navigator.showNextIntervention());
        HBox footer = new HBox(SPACING, fleet, start);
        footer.setPadding(new Insets(SPACING, 0, 0, 0));
        return footer;
    }

    private void showCall(Scenario scenario) {
        unitBoxes.getChildren().clear();
        checkBoxes.clear();
        if (scenario == null) {
            callDetails.setText("Seleziona una chiamata");
            updateButtons(null);
            return;
        }
        callDetails.setText(scenario.title() + " — " + scenario.district() + "\n\n" + scenario.callText()
                + "\n\nPericoli: " + hazardNames(scenario) + "\nRound disponibili: " + scenario.rounds()
                + "\nRequisiti: " + requirementsText(scenario));
        List<Unit> current = currentUnits(scenario);
        List<Unit> selectable = game.availableUnits();
        for (Unit unit : game.fleet().units()) {
            CheckBox box = new CheckBox(unitLabel(unit));
            box.setUserData(unit);
            box.setSelected(current.contains(unit));
            box.setDisable(!selectable.contains(unit) && !current.contains(unit));
            box.selectedProperty().addListener((obs, old, selected) -> updatePreview(scenario));
            checkBoxes.add(box);
            unitBoxes.getChildren().add(box);
        }
        updatePreview(scenario);
    }

    private void updatePreview(Scenario scenario) {
        List<Unit> chosen = chosenUnits();
        if (chosen.isEmpty()) {
            previewLabel.setText("Nessuna unità selezionata.");
        } else {
            DispatchAssessment assessment = game.preview(scenario, chosen);
            previewLabel.setText(assessment.isSatisfied()
                    ? "Requisiti soddisfatti: efficacia piena."
                    : "Manca: " + String.join(", ", assessment.missing())
                    + "\nEfficacia: " + String.format(PERCENT_FORMAT, assessment.efficiency() * PERCENT));
        }
        updateButtons(scenario);
    }

    private void updateButtons(Scenario scenario) {
        List<Unit> chosen = chosenUnits();
        assignButton.setDisable(scenario == null || chosen.isEmpty() || chosen.size() > game.maxUnitsPerCall());
        unassignButton.setDisable(scenario == null || currentUnits(scenario).isEmpty());
    }

    private void assignSelected() {
        Scenario scenario = callList.getSelectionModel().getSelectedItem();
        if (scenario == null) {
            return;
        }
        try {
            game.assign(scenario, chosenUnits());
        } catch (IllegalArgumentException | IllegalStateException e) {
            navigator.showError(e.getMessage());
        }
        refreshCalls(scenario);
    }

    private void unassignSelected() {
        Scenario scenario = callList.getSelectionModel().getSelectedItem();
        if (scenario == null) {
            return;
        }
        game.unassign(scenario);
        refreshCalls(scenario);
    }

    private void refreshCalls(Scenario scenario) {
        callList.refresh();
        showCall(scenario);
    }

    private void refreshHeader() {
        CrisisIndex crisis = game.crisisIndex();
        timeLabel.setText("Ore " + game.currentPhase().time());
        crisisBar.setProgress((double) crisis.value() / CrisisIndex.MAX);
        crisisLabel.setText(crisis.value() + " / " + CrisisIndex.MAX);
    }

    private List<Unit> chosenUnits() {
        return checkBoxes.stream().filter(CheckBox::isSelected).map(box -> (Unit) box.getUserData()).toList();
    }

    private List<Unit> currentUnits(Scenario scenario) {
        return findAssignment(scenario).map(Assignment::units).orElse(List.of());
    }

    private Optional<Assignment> findAssignment(Scenario scenario) {
        return game.assignments().stream().filter(a -> a.scenario().equals(scenario)).findFirst();
    }

    private String label(Scenario scenario) {
        String marker = findAssignment(scenario).isPresent() ? "[assegnata] " : "";
        return marker + scenario.title() + " — " + scenario.district();
    }

    private static String hazardNames(Scenario scenario) {
        return String.join(", ", scenario.hazards().stream().map(h -> h.name() + " (" + h.gravity() + ")").toList());
    }

    private String requirementsText(Scenario scenario) {
        List<String> parts = new ArrayList<>();
        scenario.requirements().minimumByAgency()
                .forEach((agency, min) -> parts.add(agency.displayName() + " ≥ " + min));
        scenario.requirements().requiredVehicleTypes().stream()
                .map(game::vehicleTypeName)
                .sorted()
                .forEach(parts::add);
        return parts.isEmpty() ? "nessuno" : String.join(", ", parts);
    }

    private static String unitLabel(Unit unit) {
        return unit.name() + " (" + unit.crewLeader() + ") — Res. " + unit.endurance().current() + "/"
                + unit.endurance().max() + ", Ris. " + unit.resources().current() + "/" + unit.resources().max();
    }
}
