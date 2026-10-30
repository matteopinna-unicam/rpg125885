package it.unicam.cs.mpgc.rpg125885.engine.progression;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Assegna l'esperienza di un intervento riuscito e fa salire di livello le unità. */
public final class ProgressionService {

    private final LevelCurve curve;

    public ProgressionService(LevelCurve curve) {
        this.curve = Objects.requireNonNull(curve, "curve");
    }

    /**
     * Divide l'esperienza tra le unità partecipanti ancora attive; il tratto di ognuna può aumentarla.
     * Quando un'unità supera la soglia sale di livello (anche più di uno).
     *
     * @return i livelli guadagnati, nell'ordine in cui sono avvenuti
     */
    public List<LevelUp> awardExperience(List<Unit> participants, int totalExperience) {
        List<Unit> active = participants.stream().filter(Unit::isActive).toList();
        List<LevelUp> levelUps = new ArrayList<>();
        if (active.isEmpty()) {
            return levelUps;
        }
        int share = totalExperience / active.size();
        for (Unit unit : active) {
            unit.addExperience((int) Math.round(share * unit.trait().experienceMultiplier()));
            while (unit.experience() >= curve.experienceToAdvanceFrom(unit.level())) {
                unit.levelUp();
                levelUps.add(new LevelUp(unit, unit.level()));
            }
        }
        return levelUps;
    }
}
