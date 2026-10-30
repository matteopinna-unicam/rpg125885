package it.unicam.cs.mpgc.rpg125885.engine.progression;

/** Curva lineare: il livello successivo costa {@code step × livello attuale} di esperienza totale. */
public final class LinearLevelCurve implements LevelCurve {

    private static final int DEFAULT_STEP = 50;

    private final int step;

    public LinearLevelCurve() {
        this(DEFAULT_STEP);
    }

    public LinearLevelCurve(int step) {
        if (step <= 0) {
            throw new IllegalArgumentException("Il passo deve essere positivo: " + step);
        }
        this.step = step;
    }

    @Override
    public int experienceToAdvanceFrom(int level) {
        return step * level;
    }
}
