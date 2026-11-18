package it.unicam.cs.mpgc.rpg125885.model.shift;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rapporto dell'alba: esito della notte, chiamate per esito, unità più efficaci e Indice di Crisi finale.
 *
 * @param bestUnits le unità con più chiamate risolte, al massimo {@link #TOP_UNITS}, dalla migliore
 */
public record ShiftReport(ShiftOutcome outcome, Map<CallOutcome, Long> callsByOutcome, List<UnitScore> bestUnits,
                          int finalCrisis) {

    public static final int TOP_UNITS = 3;

    /** Un'unità e il numero di chiamate risolte a cui ha partecipato. */
    public record UnitScore(String unitName, long resolvedCalls) {
    }

    public ShiftReport {
        Objects.requireNonNull(outcome, "outcome");
        callsByOutcome = Map.copyOf(callsByOutcome);
        bestUnits = List.copyOf(bestUnits);
    }

    /** Costruisce il rapporto dal registro delle chiamate della notte. */
    public static ShiftReport of(ShiftOutcome outcome, int finalCrisis, List<CallRecord> log) {
        return new ShiftReport(outcome, countByOutcome(log), bestUnits(log), finalCrisis);
    }

    /** Numero di chiamate con l'esito indicato; zero se non ce ne sono. */
    public long count(CallOutcome callOutcome) {
        return callsByOutcome.getOrDefault(callOutcome, 0L);
    }

    private static Map<CallOutcome, Long> countByOutcome(List<CallRecord> log) {
        return log.stream().collect(Collectors.groupingBy(CallRecord::outcome,
                () -> new EnumMap<>(CallOutcome.class), Collectors.counting()));
    }

    private static List<UnitScore> bestUnits(List<CallRecord> log) {
        Map<String, Long> resolvedByUnit = log.stream()
                .filter(record -> record.outcome() == CallOutcome.RESOLVED)
                .flatMap(record -> record.unitNames().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        // A parità di chiamate risolte si ordina per nome, così il rapporto non dipende dall'ordine della mappa
        return resolvedByUnit.entrySet().stream()
                .map(entry -> new UnitScore(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(UnitScore::resolvedCalls).reversed()
                        .thenComparing(UnitScore::unitName))
                .limit(TOP_UNITS)
                .toList();
    }
}
