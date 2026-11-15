package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.engine.dispatch.BaseOption;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Sosta in base: la flotta si riposa oppure si rifornisce. */
final class BaseView extends VBox {

    private static final double SPACING = 14;

    BaseView(GameService game, SceneNavigator navigator) {
        super(SPACING);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(SPACING));

        Label title = new Label("Ore " + game.currentPhase().time() + " — Sosta in base");
        title.getStyleClass().add("title");
        Label crisis = new Label("Indice di Crisi: " + game.crisisIndex().value());

        Button fleet = new Button("Flotta e salvataggio");
        fleet.setOnAction(event -> navigator.showFleet());
        getChildren().addAll(title, crisis,
                option(game, navigator, BaseOption.REST, "Riposo",
                        "Resistenza e Risorse al massimo, tutte le unità tornano disponibili."),
                option(game, navigator, BaseOption.RESUPPLY, "Rifornimento",
                        "Pacchetto di consumabili e un quarto della Resistenza."),
                fleet);
    }

    private static VBox option(GameService game, SceneNavigator navigator, BaseOption option,
                               String name, String description) {
        Button button = new Button(name);
        button.setOnAction(event -> {
            game.applyBase(option);
            navigator.showPhase();
        });
        return new VBox(SPACING / 2, button, new Label(description));
    }
}
