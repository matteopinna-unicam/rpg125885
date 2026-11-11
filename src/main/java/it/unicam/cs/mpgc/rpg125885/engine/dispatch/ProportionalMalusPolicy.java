package it.unicam.cs.mpgc.rpg125885.engine.dispatch;

import it.unicam.cs.mpgc.rpg125885.content.VehicleTypeRegistry;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Agency;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.shift.Requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Ogni requisito non soddisfatto toglie il 20% di efficacia, fino a un minimo del 40%. */
public final class ProportionalMalusPolicy implements RequirementPolicy {

    private static final double MALUS_PER_MISSING = 0.20;
    private static final double MINIMUM_EFFICIENCY = 0.40;

    private final VehicleTypeRegistry vehicleTypes;

    /**
     * @param vehicleTypes serve a dare un nome leggibile ai tipi di mezzo mancanti
     */
    public ProportionalMalusPolicy(VehicleTypeRegistry vehicleTypes) {
        this.vehicleTypes = Objects.requireNonNull(vehicleTypes, "vehicleTypes");
    }

    @Override
    public DispatchAssessment assess(Requirements requirements, List<Unit> units) {
        List<String> missing = new ArrayList<>();
        missing.addAll(missingAgencies(requirements.minimumByAgency(), units));
        missing.addAll(missingVehicleTypes(requirements.requiredVehicleTypes(), units));
        double efficiency = Math.max(MINIMUM_EFFICIENCY, 1.0 - MALUS_PER_MISSING * missing.size());
        return new DispatchAssessment(missing, efficiency);
    }

    private List<String> missingAgencies(Map<Agency, Integer> minimumByAgency, List<Unit> units) {
        Map<Agency, Long> presentByAgency = units.stream()
                .collect(Collectors.groupingBy(unit -> unit.type().agency(), Collectors.counting()));
        return minimumByAgency.entrySet().stream()
                .filter(entry -> presentByAgency.getOrDefault(entry.getKey(), 0L) < entry.getValue())
                .map(entry -> entry.getValue() + " × " + entry.getKey().displayName())
                .sorted()
                .toList();
    }

    private List<String> missingVehicleTypes(Set<String> requiredTypeIds, List<Unit> units) {
        Set<String> presentIds = units.stream().map(unit -> unit.type().id()).collect(Collectors.toSet());
        return requiredTypeIds.stream()
                .filter(id -> !presentIds.contains(id))
                .map(id -> vehicleTypes.byId(id).displayName())
                .sorted()
                .toList();
    }
}
