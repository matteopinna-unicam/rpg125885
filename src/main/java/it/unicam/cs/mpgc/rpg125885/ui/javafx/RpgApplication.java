package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RpgApplication extends Application {

    private static final String TITLE = "Il Turno Rosso";

    @Override
    public void start(Stage stage) {
        stage.setTitle(TITLE);
        stage.setScene(new Scene(new StackPane(new Label(TITLE)), 800, 600));
        stage.show();
    }
}