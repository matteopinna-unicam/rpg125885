package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.engine.dispatch.Assignment;
import it.unicam.cs.mpgc.rpg125885.engine.dispatch.InterventionReport;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionStatus;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterveneAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.ManeuverAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.SupplyAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.TakeCoverAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.TargetChoice;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionListener;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Combatant;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Gauge;
import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.item.Supply;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.Maneuver;
import it.unicam.cs.mpgc.rpg125885.model.maneuver.TargetScope;
import it.unicam.cs.mpgc.rpg125885.model.shift.Decision;
import it.unicam.cs.mpgc.rpg125885.model.shift.DecisionOption;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/** Intervento a turni: scelta narrativa iniziale, barre dei contatori, azioni e registro. È un ascoltatore degli eventi. */
final class InterventionView extends BorderPane implements InterventionListener {

    private static final double SPACING = 10;
    private static final double SIDE_WIDTH = 280;
    private static final double PERCENT = 100.0;

    private final GameService game;
    private final SceneNavigator navigator;
    private final Assignment assignment;

    private final Label header = new Label();
    private final VBox hazardBox = new VBox(SPACING);
    private final VBox unitBox = new VBox(SPACING);
    private final TextArea logArea = new TextArea();
    private final VBox actionPane = new VBox(SPACING);
    private Intervention intervention;

    InterventionView(GameService game, SceneNavigator navigator, Assignment assignment) {
        this.game = game;
        this.navigator = navigator;
        this.assignment = assignment;
        setPadding(new Insets(SPACING));
        header.getStyleClass().add("section-title");
        logArea.setEditable(false);
        logArea.setWrapText(true);
        setTop(header);
        setLeft(sidePane("Pericoli", hazardBox));
        setRight(sidePane("Squadre", unitBox));
        setCenter(logArea);
        setBottom(actionPane);
        actionPane.setPadding(new Insets(SPACING, 0, 0, 0));
        header.setText(assignment.scenario().title() + " — " + assignment.scenario().district());
        logArea.appendText(assignment.scenario().callText() + "\n");
        assignment.scenario().decision().ifPresentOrElse(this::askDecision, () -> begin(Optional.empty()));
    }

    @Override
    public void onEvent(InterventionEvent event) {
        String text = describe(event);
        if (text != null) {
            logArea.appendText(text + "\n");
        }
    }

    private void askDecision(Decision decision) {
        Label question = new Label(decision.question());
        question.setWrapText(true);
        actionPane.getChildren().setAll(question);
        for (DecisionOption option : decision.options()) {
            Button button = new Button(option.text());
            button.setOnAction(event -> begin(Optional.of(option)));
            actionPane.getChildren().add(button);
        }
    }

    private void begin(Optional<DecisionOption> choice) {
        try {
            intervention = game.beginIntervention(choice);
        } catch (IllegalStateException | IllegalArgumentException e) {
            navigator.showError(e.getMessage());
            return;
        }
        intervention.addListener(this);
        intervention.start();
        refresh();
    }

    private void refresh() {
        header.setText(assignment.scenario().title() + " — round " + intervention.round()
                + ", ne restano " + intervention.roundsRemaining());
        hazardBox.getChildren().setAll(intervention.activeHazards().stream().map(this::hazardRow).toList());
        unitBox.getChildren().setAll(assignment.units().stream().map(this::unitRow).toList());
        if (intervention.status() == InterventionStatus.ONGOING) {
            intervention.currentUnit().ifPresent(this::showActions);
        } else {
            conclude();
        }
    }

    private void showActions(Unit unit) {
        Label turn = new Label("Turno di " + unit.name() + " (" + unit.crewLeader() + ")");
        Button intervene = new Button("Intervieni");
        intervene.setOnAction(event -> pick("Pericolo bersaglio", intervention.activeHazards())
                .ifPresent(hazard -> submit(new InterveneAction(unit, hazard))));
        Button cover = new Button("Riparo");
        cover.setOnAction(event -> submit(new TakeCoverAction(unit)));
        actionPane.getChildren().setAll(turn, new HBox(SPACING, intervene, maneuverMenu(unit), supplyMenu(unit), cover));
    }

    private MenuButton maneuverMenu(Unit unit) {
        MenuButton menu = new MenuButton("Manovra");
        for (Maneuver maneuver : unit.type().maneuversAt(unit.level())) {
            MenuItem item = new MenuItem(maneuver.name() + " (" + maneuver.resourceCost() + " Risorse)");
            item.setDisable(unit.resources().current() < maneuver.resourceCost());
            item.setOnAction(event -> targetFor(maneuver.scope())
                    .ifPresent(choice -> submit(new ManeuverAction(unit, maneuver, choice))));
            menu.getItems().add(item);
        }
        menu.setDisable(menu.getItems().isEmpty());
        return menu;
    }

    private MenuButton supplyMenu(Unit unit) {
        MenuButton menu = new MenuButton("Rifornimento");
        for (Supply supply : game.supplies()) {
            int quantity = game.fleet().inventory().quantityOf(supply);
            MenuItem item = new MenuItem(supply.name() + " ×" + quantity);
            item.setOnAction(event -> targetFor(supply.scope()).ifPresent(choice ->
                    submit(new SupplyAction(unit, supply, choice, game.fleet().inventory()))));
            menu.getItems().add(item);
        }
        menu.setDisable(menu.getItems().isEmpty());
        return menu;
    }

    private Optional<TargetChoice> targetFor(TargetScope scope) {
        return switch (scope) {
            case SINGLE_HAZARD -> pick("Pericolo bersaglio", intervention.activeHazards()).map(TargetChoice::ofHazard);
            case SINGLE_UNIT -> pick("Unità bersaglio", intervention.activeUnits()).map(TargetChoice::ofUnit);
            case ALL_HAZARDS, ALL_UNITS, SELF -> Optional.of(TargetChoice.none());
        };
    }

    private <T extends Combatant> Optional<T> pick(String title, List<T> options) {
        if (options.size() == 1) {
            return Optional.of(options.getFirst());
        }
        List<String> labels = options.stream()
                .map(option -> (options.indexOf(option) + 1) + ". " + option.name()).toList();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(labels.getFirst(), labels);
        dialog.setHeaderText(title);
        return dialog.showAndWait().map(labels::indexOf).map(options::get);
    }

    private void submit(InterventionAction action) {
        try {
            intervention.submit(action);
        } catch (IllegalStateException | IllegalArgumentException e) {
            navigator.showError(e.getMessage());
            return;
        }
        refresh();
    }

    private void conclude() {
        InterventionReport report = game.finishIntervention();
        logArea.appendText("\n" + report.text() + "\n");
        report.levelUps().forEach(up ->
                logArea.appendText(up.unit().name() + " sale al livello " + up.newLevel() + "\n"));
        report.rewardText().ifPresent(text -> logArea.appendText("Premio: " + text + "\n"));
        Button next = new Button("Continua");
        next.setOnAction(event -> navigator.showNextIntervention());
        actionPane.getChildren().setAll(next);
    }

    private VBox hazardRow(Hazard hazard) {
        return row(hazard.name(), hazard.gravity(), "Gravità");
    }

    private VBox unitRow(Unit unit) {
        VBox box = row(unit.name(), unit.endurance(), "Resistenza");
        Gauge resources = unit.resources();
        box.getChildren().addAll(bar(resources), new Label("Risorse " + resources.current() + "/" + resources.max()));
        return box;
    }

    private static VBox row(String name, Gauge gauge, String gaugeName) {
        return new VBox(SPACING / 2, new Label(name), bar(gauge),
                new Label(gaugeName + " " + gauge.current() + "/" + gauge.max()));
    }

    private static ProgressBar bar(Gauge gauge) {
        ProgressBar bar = new ProgressBar(gauge.fillPercent() / PERCENT);
        bar.setMaxWidth(Double.MAX_VALUE);
        return bar;
    }

    private static VBox sidePane(String title, VBox content) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        VBox pane = new VBox(SPACING, label, content);
        pane.setPrefWidth(SIDE_WIDTH);
        pane.setPadding(new Insets(0, SPACING, 0, SPACING));
        return pane;
    }

    private static String gaugeName(GaugeKind kind) {
        return switch (kind) {
            case ENDURANCE -> "Resistenza";
            case RESOURCES -> "Risorse";
        };
    }

    private static String describe(InterventionEvent event) {
        return switch (event) {
            case InterventionEvent.RoundStarted e -> "— Round " + e.round() + " —";
            case InterventionEvent.TurnStarted ignored -> null;
            case InterventionEvent.GravityReduced e -> e.source().name() + " riduce di " + e.amount() + " «"
                    + e.target().name() + "»" + (e.exemplary() ? " (colpo esemplare!)" : "");
            case InterventionEvent.GravityEscalated e -> e.hazard().name() + " peggiora di " + e.amount();
            case InterventionEvent.StressInflicted e -> e.source().name() + " mette sotto stress "
                    + e.target().name() + " (-" + e.amount() + " Resistenza)";
            case InterventionEvent.Recovered e -> e.source().name() + " ripristina " + e.amount() + " "
                    + gaugeName(e.kind()) + " a " + e.target().name();
            case InterventionEvent.TookCover e -> e.unit().name() + " si mette al riparo";
            case InterventionEvent.UnitExhausted e -> e.unit().name() + " è esausta";
            case InterventionEvent.HazardResolved e -> e.hazard().name() + " è risolto";
            case InterventionEvent.InterventionEnded e -> e.successful() ? "Intervento concluso." : "Intervento fallito.";
        };
    }
}
