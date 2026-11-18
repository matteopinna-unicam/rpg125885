package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.app.AppContext;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Applicazione JavaFX. Il contenuto di gioco si legge in {@link #init()}, che non gira sul thread della GUI,
 * così la finestra non si blocca durante la lettura dei file.
 */
public class RpgApplication extends Application {

    private AppContext context;
    private PersistenceException startupError;

    @Override
    public void init() {
        try {
            context = AppContext.create();
        } catch (PersistenceException e) {
            startupError = e;
        }
    }

    @Override
    public void start(Stage stage) {
        if (startupError != null) {
            new Alert(Alert.AlertType.ERROR, "Impossibile avviare il gioco: " + startupError.getMessage())
                    .showAndWait();
            Platform.exit();
            return;
        }
        SceneNavigator navigator = new SceneNavigator(stage, context.gameService());
        navigator.showMainMenu();
        stage.show();
    }
}
