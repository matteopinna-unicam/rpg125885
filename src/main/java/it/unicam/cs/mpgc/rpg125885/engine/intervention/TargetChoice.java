package it.unicam.cs.mpgc.rpg125885.engine.intervention;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.Optional;

/** Bersaglio scelto dal giocatore; l'ambito dell'effetto decide quale parte serve. */
public record TargetChoice(Optional<Hazard> hazard, Optional<Unit> unit) {

    private static final TargetChoice NONE = new TargetChoice(Optional.empty(), Optional.empty());

    /** Nessuna scelta: basta per gli effetti con ambito multiplo o su se stessi. */
    public static TargetChoice none() {
        return NONE;
    }

    public static TargetChoice ofHazard(Hazard hazard) {
        return new TargetChoice(Optional.of(hazard), Optional.empty());
    }

    public static TargetChoice ofUnit(Unit unit) {
        return new TargetChoice(Optional.empty(), Optional.of(unit));
    }
}
