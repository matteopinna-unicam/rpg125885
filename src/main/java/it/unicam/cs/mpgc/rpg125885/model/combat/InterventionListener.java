package it.unicam.cs.mpgc.rpg125885.model.combat;

/** Riceve gli eventi di un intervento, nell'ordine in cui accadono. */
@FunctionalInterface
public interface InterventionListener {

    void onEvent(InterventionEvent event);
}
