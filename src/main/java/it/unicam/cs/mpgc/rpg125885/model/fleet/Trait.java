package it.unicam.cs.mpgc.rpg125885.model.fleet;

import java.util.Objects;

/**
 * Tratto del capo-equipaggio, letto da {@code traits.xml}.
 *
 * @param bonus                bonus alle statistiche
 * @param recoveryMultiplier   moltiplicatore dei recuperi (1,0 = nessun effetto)
 * @param experienceMultiplier moltiplicatore dell'esperienza guadagnata (1,0 = nessun effetto)
 */
public record Trait(
        String id,
        String name,
        Stats bonus,
        double recoveryMultiplier,
        double experienceMultiplier) {

    public Trait {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(bonus, "bonus");
        if (recoveryMultiplier <= 0 || experienceMultiplier <= 0) {
            throw new IllegalArgumentException("I moltiplicatori devono essere positivi");
        }
    }
}
