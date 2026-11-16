package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.app.AppContext;
import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class RpgApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            AppContext context = AppContext.create();
            SceneNavigator navigator = new SceneNavigator(stage, context.gameService());
            navigator.showMainMenu();
            stage.show();
        } catch (PersistenceException e) {
            new Alert(Alert.AlertType.ERROR, "Impossibile avviare il gioco: " + e.getMessage()).showAndWait();
            Platform.exit();
        }
    }
}
