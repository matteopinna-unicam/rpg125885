package it.unicam.cs.mpgc.rpg125885.app;

import it.unicam.cs.mpgc.rpg125885.ui.javafx.RpgApplication;
import javafx.application.Application;

/** Punto d'ingresso: avvia l'interfaccia JavaFX. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(RpgApplication.class, args);
    }
}