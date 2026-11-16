package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.persistence.SaveSlotInfo;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/** Elenco dei salvataggi: lettura, caricamento ed eliminazione avvengono in background. */
final class LoadGameView extends BorderPane {

    private static final double SPACING = 12;

    private final GameService game;
    private final SceneNavigator navigator;
    private final BackgroundTasks background;

    private final ListView<SaveSlotInfo> slotList = new ListView<>();
    private final Button loadButton = new Button("Carica");
    private final Button deleteButton = new Button("Elimina");
    private final Label status = new Label("Lettura dei salvataggi…");

    LoadGameView(GameService game, SceneNavigator navigator, BackgroundTasks background) {
        this.game = game;
        this.navigator = navigator;
        this.background = background;
        setPadding(new Insets(SPACING));
        slotList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SaveSlotInfo info, boolean empty) {
                super.updateItem(info, empty);
                setText(empty || info == null ? null : info.slot() + " — " + info.fleetName()
                        + " · fase " + (info.phaseIndex() + 1) + " · crisi " + info.crisis()
                        + " · " + info.unitCount() + " unità · " + info.savedAt());
            }
        });
        slotList.getSelectionModel().selectedItemProperty().addListener((obs, old, info) -> updateButtons());
        loadButton.setOnAction(event -> load());
        deleteButton.setOnAction(event -> delete());
        Button back = new Button("Indietro");
        back.setOnAction(event -> navigator.showMainMenu());
        setTop(new Label("Partite salvate"));
        setCenter(new VBox(SPACING, slotList, status));
        setBottom(new HBox(SPACING, back, loadButton, deleteButton));
        updateButtons();
        reload();
    }

    private void reload() {
        background.supply(game::saveSlots, this::showSlots, this::fail);
    }

    private void showSlots(List<SaveSlotInfo> slots) {
        slotList.getItems().setAll(slots);
        status.setText(slots.isEmpty() ? "Nessun salvataggio." : "");
        updateButtons();
    }

    private void load() {
        String slot = slotList.getSelectionModel().getSelectedItem().slot();
        setBusy(true);
        background.run(() -> game.loadGame(slot), navigator::showPhase, this::fail);
    }

    private void delete() {
        String slot = slotList.getSelectionModel().getSelectedItem().slot();
        setBusy(true);
        background.run(() -> game.deleteSlot(slot), () -> {
            setBusy(false);
            reload();
        }, this::fail);
    }

    private void fail(Throwable error) {
        setBusy(false);
        status.setText("");
        navigator.showError(error.getMessage());
    }

    private void setBusy(boolean busy) {
        loadButton.setDisable(busy);
        deleteButton.setDisable(busy);
    }

    private void updateButtons() {
        boolean none = slotList.getSelectionModel().isEmpty();
        loadButton.setDisable(none);
        deleteButton.setDisable(none);
    }
}
