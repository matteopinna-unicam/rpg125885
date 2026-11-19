package it.unicam.cs.mpgc.rpg125885.app;

import it.unicam.cs.mpgc.rpg125885.ui.javafx.RpgApplication;

/** Punto d'ingresso: collega la composition root all'interfaccia JavaFX e la avvia. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        RpgApplication.launchWith(() -> AppContext.create().gameService(), args);
    }
}
