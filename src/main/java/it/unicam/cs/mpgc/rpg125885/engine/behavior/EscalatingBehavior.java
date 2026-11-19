package it.unicam.cs.mpgc.rpg125885.engine.behavior;

import it.unicam.cs.mpgc.rpg125885.engine.intervention.EscalateAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.Intervention;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.InterventionAction;
import it.unicam.cs.mpgc.rpg125885.engine.intervention.StressAction;
import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;
import it.unicam.cs.mpgc.rpg125885.model.hazard.Hazard;

import java.util.List;

/**
 * Peggiora se nessuno l'ha affrontato dall'inizio del round precedente; altrimenti stress a un'unità casuale.
 * Conta anche il round in corso: un pericolo lento, che agisce dopo le unità, è già stato affrontato in questo round.
 */
public final class EscalatingBehavior implements HazardBehavior {

    @Override
    public InterventionAction chooseAction(Hazard hazard, Intervention intervention) {
        if (!hazard.wasFacedSince(intervention.round() - 1)) {
            return new EscalateAction(hazard);
        }
        List<Unit> units = intervention.activeUnits();
        Unit target = units.get(intervention.random().nextInt(units.size()));
        return new StressAction(hazard, target);
    }
}
