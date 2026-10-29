package it.unicam.cs.mpgc.rpg125885.model.fleet;

/** Ente a cui appartiene un tipo di mezzo. */
public enum Agency {
    FIRE_BRIGADE("Vigili del Fuoco"),
    EMS("118"),
    POLICE("Polizia");

    private final String displayName;

    Agency(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
