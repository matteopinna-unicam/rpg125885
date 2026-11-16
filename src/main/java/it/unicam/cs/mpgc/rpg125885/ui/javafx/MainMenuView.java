package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Schermata iniziale: nuova partita, caricamento, uscita. */
final class MainMenuView extends VBox {

    private static final double SPACING = 16;
    private static final double BUTTON_WIDTH = 240;

    MainMenuView(SceneNavigator navigator) {
        super(SPACING);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(SPACING));

        Label title = new Label("Il Turno Rosso");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Una notte alla centrale operativa di Castelvento");

        getChildren().addAll(title, subtitle,
                button("Nuova partita", navigator::showFleetBuilder),
                button("Carica partita", navigator::showLoadGame),
                button("Esci", Platform::exit));
    }

    private static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefWidth(BUTTON_WIDTH);
        button.setOnAction(event -> action.run());
        return button;
    }
}
