package it.unicam.cs.mpgc.rpg125885.model.item;

import java.util.Objects;

/** Oggetto del gioco, identificato dall'id: due oggetti con lo stesso id sono uguali. */
public abstract class Item {

    private final String id;
    private final String name;
    private final String description;

    protected Item(String id, String name, String description) {
        this.id = requireNonBlank(id, "id");
        this.name = requireNonBlank(name, "name");
        this.description = Objects.requireNonNull(description, "description");
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Item item && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Il campo " + field + " non può essere vuoto");
        }
        return value;
    }
}
