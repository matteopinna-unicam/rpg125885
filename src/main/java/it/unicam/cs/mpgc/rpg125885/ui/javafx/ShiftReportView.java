package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.model.shift.CallOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftOutcome;
import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftReport;
import it.unicam.cs.mpgc.rpg125885.service.GameService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

/** Rapporto dell'alba: mostra il {@link ShiftReport} calcolato dal servizio. */
final class ShiftReportView extends VBox {

    private static final double SPACING = 14;

    ShiftReportView(GameService game, SceneNavigator navigator) {
        super(SPACING);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(SPACING));

        ShiftReport report = game.shiftReport();
        Label title = new Label(report.outcome() == ShiftOutcome.VICTORY
                ? "Alba a Castelvento: la notte è superata"
                : "La centrale è andata in crisi: notte persa");
        title.getStyleClass().add("title");

        Label summary = new Label("Risolte: " + report.count(CallOutcome.RESOLVED)
                + " · Fallite: " + report.count(CallOutcome.FAILED)
                + " · Ignorate: " + report.count(CallOutcome.IGNORED)
                + "\nIndice di Crisi finale: " + report.finalCrisis());

        Label best = new Label("Unità più efficaci:\n" + bestUnitsText(report));
        Button menu = new Button("Menu principale");
        menu.setOnAction(event -> navigator.showMainMenu());
        getChildren().addAll(title, summary, best, menu);
    }

    private static String bestUnitsText(ShiftReport report) {
        if (report.bestUnits().isEmpty()) {
            return "nessuna";
        }
        return report.bestUnits().stream()
                .map(score -> score.unitName() + " — " + score.resolvedCalls() + " interventi risolti")
                .collect(Collectors.joining("\n"));
    }
}
