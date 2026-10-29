package it.unicam.cs.mpgc.rpg125885.content;

import it.unicam.cs.mpgc.rpg125885.model.fleet.VehicleType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Associa gli id ai tipi di mezzo: è l'unico punto in cui si registra un nuovo mezzo. */
public final class VehicleTypeRegistry {

    private final Map<String, VehicleType> types = new LinkedHashMap<>();

    /**
     * @throws IllegalArgumentException se l'id è già registrato
     */
    public void register(VehicleType type) {
        Objects.requireNonNull(type, "type");
        if (types.putIfAbsent(type.id(), type) != null) {
            throw new IllegalArgumentException("Tipo di mezzo già registrato: " + type.id());
        }
    }

    /**
     * @throws IllegalArgumentException se l'id non è registrato
     */
    public VehicleType byId(String id) {
        VehicleType type = types.get(id);
        if (type == null) {
            throw new IllegalArgumentException("Tipo di mezzo sconosciuto: " + id);
        }
        return type;
    }

    public Collection<VehicleType> all() {
        return List.copyOf(types.values());
    }

    /** I tipi che il giocatore può scegliere nella composizione della flotta. */
    public List<VehicleType> purchasable() {
        return types.values().stream().filter(VehicleType::isPurchasable).toList();
    }
}
