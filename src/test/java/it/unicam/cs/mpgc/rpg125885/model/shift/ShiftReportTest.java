package it.unicam.cs.mpgc.rpg125885.model.shift;

import it.unicam.cs.mpgc.rpg125885.model.shift.ShiftReport.UnitScore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShiftReportTest {

    @Test
    void countsCallsByOutcome() {
        ShiftReport report = ShiftReport.of(ShiftOutcome.VICTORY, 35, List.of(
                new CallRecord("a", CallOutcome.RESOLVED, List.of("APS 1")),
                new CallRecord("b", CallOutcome.RESOLVED, List.of("APS 1")),
                new CallRecord("c", CallOutcome.IGNORED, List.of())));

        assertEquals(2, report.count(CallOutcome.RESOLVED));
        assertEquals(0, report.count(CallOutcome.FAILED));
        assertEquals(1, report.count(CallOutcome.IGNORED));
        assertEquals(35, report.finalCrisis());
    }

    @Test
    void ranksUnitsByResolvedCallsOnly() {
        ShiftReport report = ShiftReport.of(ShiftOutcome.DEFEAT, 100, List.of(
                new CallRecord("a", CallOutcome.RESOLVED, List.of("Pattuglia 1", "Ambulanza 1")),
                new CallRecord("b", CallOutcome.RESOLVED, List.of("Ambulanza 1")),
                new CallRecord("c", CallOutcome.FAILED, List.of("Pattuglia 1", "Pattuglia 1"))));

        assertEquals(List.of(new UnitScore("Ambulanza 1", 2), new UnitScore("Pattuglia 1", 1)),
                report.bestUnits());
    }

    @Test
    void keepsTopUnitsOrderingTiesByName() {
        ShiftReport report = ShiftReport.of(ShiftOutcome.VICTORY, 10, List.of(
                new CallRecord("a", CallOutcome.RESOLVED, List.of("D", "C", "B", "A"))));

        assertEquals(ShiftReport.TOP_UNITS, report.bestUnits().size());
        assertEquals(List.of("A", "B", "C"), report.bestUnits().stream().map(UnitScore::unitName).toList());
    }

    @Test
    void emptyLogHasNoBestUnits() {
        ShiftReport report = ShiftReport.of(ShiftOutcome.DEFEAT, 100, List.of());

        assertTrue(report.bestUnits().isEmpty());
        assertEquals(0, report.count(CallOutcome.RESOLVED));
    }
}
