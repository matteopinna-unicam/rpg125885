package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Stats;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/** Tabella delle sette statistiche: accanto a ogni valore, in verde o rosso, ciò che aggiunge o toglie il tratto. */
final class StatsTable extends GridPane {

    private static final double GAP = 12;
    private static final int NAME_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;
    private static final int CHANGE_COLUMN = 2;
    private static final String UP_STYLE = "stat-up";
    private static final String DOWN_STYLE = "stat-down";
    private static final String MULTIPLIER_FORMAT = "×%.1f";
    private static final double NEUTRAL_MULTIPLIER = 1.0;

    /** Nomi nello stesso ordine di {@link #valuesOf(Stats)}. */
    private static final List<String> NAMES = List.of("Resistenza", "Risorse", "Tecnica", "Medicina",
            "Coordinamento", "Sangue freddo", "Prontezza");

    private final Label multipliers = new Label();

    StatsTable() {
        setHgap(GAP);
        setVgap(GAP / 4);
    }

    /** I moltiplicatori che non si vedono nelle statistiche (recuperi, esperienza). */
    Label multipliersLabel() {
        return multipliers;
    }

    /**
     * @param base  statistiche senza il tratto: si mostrano così come sono, con accanto la variazione del tratto
     * @param trait tratto di cui evidenziare l'effetto
     */
    void show(Stats base, Trait trait) {
        getChildren().clear();
        int[] values = valuesOf(base);
        int[] changes = valuesOf(trait.bonus());
        for (int row = 0; row < NAMES.size(); row++) {
            addRow(row, NAMES.get(row), values[row], changes[row]);
        }
        multipliers.setText(String.join(" · ", multiplierTexts(trait)));
    }

    void clear() {
        getChildren().clear();
        multipliers.setText("");
    }

    private void addRow(int row, String name, int value, int change) {
        add(new Label(name), NAME_COLUMN, row);
        add(new Label(String.valueOf(value)), VALUE_COLUMN, row);
        if (change != 0) {
            Label changeLabel = new Label(String.format("%+d", change));
            changeLabel.getStyleClass().add(change > 0 ? UP_STYLE : DOWN_STYLE);
            add(changeLabel, CHANGE_COLUMN, row);
        }
    }

    private static int[] valuesOf(Stats stats) {
        return new int[]{stats.maxEndurance(), stats.maxResources(), stats.technique(), stats.medicine(),
                stats.coordination(), stats.composure(), stats.readiness()};
    }

    private static List<String> multiplierTexts(Trait trait) {
        List<String> texts = new ArrayList<>();
        if (trait.recoveryMultiplier() != NEUTRAL_MULTIPLIER) {
            texts.add("Recuperi " + String.format(MULTIPLIER_FORMAT, trait.recoveryMultiplier()));
        }
        if (trait.experienceMultiplier() != NEUTRAL_MULTIPLIER) {
            texts.add("Esperienza " + String.format(MULTIPLIER_FORMAT, trait.experienceMultiplier()));
        }
        return texts;
    }
}
