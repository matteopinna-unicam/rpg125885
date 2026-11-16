package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.CallRecord;
import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftOutcome;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Rapporto dell'alba: com'è finita la notte, conteggio per esito e unità più efficaci. */
final class ShiftReportView extends VBox {

    private static final double SPACING = 14;
    private static final int TOP_UNITS = 3;

    ShiftReportView(GameService game, SceneNavigator navigator) {
        super(SPACING);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(SPACING));

        Label title = new Label(game.outcome() == ShiftOutcome.VICTORY
                ? "Alba a Castelvento: la notte è superata"
                : "La centrale è andata in crisi: notte persa");
        title.getStyleClass().add("title");

        List<CallRecord> log = game.callLog();
        Map<CallOutcome, Long> counts = log.stream()
                .collect(Collectors.groupingBy(CallRecord::outcome, Collectors.counting()));
        Label summary = new Label("Risolte: " + counts.getOrDefault(CallOutcome.RESOLVED, 0L)
                + " · Fallite: " + counts.getOrDefault(CallOutcome.FAILED, 0L)
                + " · Ignorate: " + counts.getOrDefault(CallOutcome.IGNORED, 0L)
                + "\nIndice di Crisi finale: " + game.crisisIndex().value());

        Label best = new Label("Unità più efficaci:\n" + bestUnits(log));
        Button menu = new Button("Menu principale");
        menu.setOnAction(event -> navigator.showMainMenu());
        getChildren().addAll(title, summary, best, menu);
    }

    private static String bestUnits(List<CallRecord> log) {
        Map<String, Long> resolvedByUnit = log.stream()
                .filter(record -> record.outcome() == CallOutcome.RESOLVED)
                .flatMap(record -> record.unitNames().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        String text = resolvedByUnit.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_UNITS)
                .map(entry -> entry.getKey() + " — " + entry.getValue() + " interventi risolti")
                .collect(Collectors.joining("\n"));
        return text.isEmpty() ? "nessuna" : text;
    }
}
