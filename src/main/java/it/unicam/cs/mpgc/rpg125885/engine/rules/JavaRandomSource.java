package it.unicam.cs.mpgc.rpg125885.engine.rules;

import java.util.Random;

/** {@link RandomSource} basata su {@link java.util.Random}. */
public final class JavaRandomSource implements RandomSource {

    private final Random random;

    public JavaRandomSource() {
        this.random = new Random();
    }

    /** Con seme fisso la sequenza è ripetibile. */
    public JavaRandomSource(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
