package it.unicam.cs.mpgc.rpg125885.model.combat;

import it.unicam.cs.mpgc.rpg125885.model.fleet.GaugeKind;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;
import it.unicam.cs.mpgc.rpg125885.model.hazard.InterventionType;

/**
 * Formule degli effetti. Un'istanza vale per un intervento: l'eventuale malus sui requisiti
 * non soddisfatti è una sua proprietà, non un parametro dei singoli calcoli.
 */
public interface EffectCalculator {

    /** Quanta gravità toglie {@code actor} a {@code target} con un effetto di data potenza. */
    EffectResult gravityReduction(Unit actor, Hazard target, InterventionType type,
                                  double power, boolean ignoresResistance);

    /** Stress effettivamente subito da {@code target} per uno stress di potenza data. */
    int stressTaken(Unit target, int stressPower);

    /** Quanto recupera il contatore {@code kind} per un recupero di data potenza di {@code actor}. */
    int recovery(Unit actor, GaugeKind kind, double power);
}
