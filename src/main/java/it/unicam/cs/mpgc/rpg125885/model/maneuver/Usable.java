package it.unicam.cs.mpgc.rpg125885.model.maneuver;

import it.unicam.cs.mpgc.rpg125885.model.combat.EffectCalculator;
import it.unicam.cs.mpgc.rpg125885.model.combat.InterventionEvent;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;

import java.util.List;

/** Qualunque cosa un'unità possa usare su dei bersagli: manovre e rifornimenti. */
public interface Usable {

    String name();

    TargetScope scope();

    /** Applica l'effetto e restituisce gli eventi prodotti. Non spende costi: lo fa chi lo invoca. */
    List<InterventionEvent> use(Unit user, Targets targets, EffectCalculator calculator);
}
