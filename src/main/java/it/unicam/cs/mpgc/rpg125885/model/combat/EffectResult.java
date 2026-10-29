package it.unicam.cs.mpgc.rpg125885.model.combat;

/**
 * Esito di un calcolo di riduzione di gravità.
 *
 * @param amount    gravità da sottrarre, almeno 1
 * @param exemplary true se l'intervento è stato esemplare (critico)
 */
public record EffectResult(int amount, boolean exemplary) {
}
