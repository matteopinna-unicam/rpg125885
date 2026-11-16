package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.engine.dispatch.Assignment;
import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftOutcome;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

/** Cambia la schermata mostrata nella finestra; è l'unico punto che conosce tutte le view. */
public final class SceneNavigator {

    private static final String TITLE = "Il Turno Rosso";
    private static final String STYLESHEET = "/ui/style.css";
    private static final double WIDTH = 1100;
    private static final double HEIGHT = 720;

    private final Stage stage;
    private final GameService game;
    private final BackgroundTasks background = new BackgroundTasks();

    public SceneNavigator(Stage stage, GameService game) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.game = Objects.requireNonNull(game, "game");
        stage.setTitle(TITLE);
    }

    public void showMainMenu() {
        show(new MainMenuView(this));
    }

    public void showFleetBuilder() {
        show(new FleetBuilderView(game, this));
    }

    public void showLoadGame() {
        show(new LoadGameView(game, this, background));
    }

    public void showFleet() {
        show(new FleetView(game, this, background));
    }

    public void showDispatch() {
        show(new DispatchView(game, this));
    }

    /** Mostra la schermata della fase corrente: sala operativa, base o rapporto finale. */
    public void showPhase() {
        if (game.outcome() != ShiftOutcome.IN_PROGRESS) {
            show(new ShiftReportView(game, this));
            return;
        }
        switch (game.currentPhase().type()) {
            case BASE -> show(new BaseView(game, this));
            case CALLS, FINALE -> showDispatch();
        }
    }

    /** Gioca il prossimo intervento assegnato; senza interventi residui chiude la fase. */
    public void showNextIntervention() {
        if (game.outcome() == ShiftOutcome.IN_PROGRESS) {
            Optional<Assignment> next = game.nextAssignment();
            if (next.isPresent()) {
                show(new InterventionView(game, this, next.get()));
                return;
            }
            try {
                game.closePhase();
            } catch (IllegalStateException e) {
                showError(e.getMessage());
                return;
            }
        }
        showPhase();
    }

    /** Mostra un errore all'utente senza interrompere il gioco. */
    public void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void show(Parent root) {
        if (stage.getScene() == null) {
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource(STYLESHEET), "Foglio di stile mancante").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }
    }
}
