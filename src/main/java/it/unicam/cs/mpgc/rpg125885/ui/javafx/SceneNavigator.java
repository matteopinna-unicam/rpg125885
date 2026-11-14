package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;

/** Cambia la schermata mostrata nella finestra; è l'unico punto che conosce tutte le view. */
public final class SceneNavigator {

    private static final String TITLE = "Il Turno Rosso";
    private static final double WIDTH = 1100;
    private static final double HEIGHT = 720;

    private final Stage stage;
    private final GameService game;

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

    public void showDispatch() {
        show(new DispatchView(game, this));
    }

    /** Mostra un errore all'utente senza interrompere il gioco. */
    public void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void show(Parent root) {
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, WIDTH, HEIGHT));
        } else {
            stage.getScene().setRoot(root);
        }
    }
}
