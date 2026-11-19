package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Applicazione JavaFX. Non conosce la composition root: riceve da {@link #launchWith} il modo di costruire
 * il {@link GameService}, che viene invocato in {@link #init()}, fuori dal thread della GUI, così la finestra
 * non si blocca durante la lettura dei file.
 */
public class RpgApplication extends Application {

    /** Costruisce il servizio di gioco; può fallire se il contenuto non si legge. */
    @FunctionalInterface
    public interface ServiceFactory {
        GameService create() throws PersistenceException;
    }

    // JavaFX istanzia l'Application con il costruttore vuoto: la factory si passa per forza in modo statico
    private static ServiceFactory serviceFactory;

    private GameService game;
    private PersistenceException startupError;

    /** Avvia l'interfaccia; ritorna quando la finestra viene chiusa. */
    public static void launchWith(ServiceFactory factory, String... args) {
        serviceFactory = Objects.requireNonNull(factory, "factory");
        Application.launch(RpgApplication.class, args);
    }

    @Override
    public void init() {
        if (serviceFactory == null) {
            throw new IllegalStateException("Avviare l'applicazione con RpgApplication.launchWith");
        }
        try {
            game = serviceFactory.create();
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
        SceneNavigator navigator = new SceneNavigator(stage, game);
        navigator.showMainMenu();
        stage.show();
    }
}
