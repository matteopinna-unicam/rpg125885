package it.unicam.cs.mpgc.rpg125885.content;

import it.unicam.cs.mpgc.rpg125885.model.fleet.Trait;
import it.unicam.cs.mpgc.rpg125885.model.hazard.HazardTemplate;
import it.unicam.cs.mpgc.rpg125885.model.item.Item;
import it.unicam.cs.mpgc.rpg125885.model.shift.Shift;

import java.util.List;

/** Contenuto di gioco in sola lettura: pericoli, oggetti, tratti e turno, cercati per id. */
public interface ContentCatalog {

    /**
     * @throws IllegalArgumentException se l'id non esiste (vale per tutti i metodi)
     */
    HazardTemplate hazard(String id);

    Item item(String id);

    Trait trait(String id);

    /** Tutti i tratti, nell'ordine del file: servono alla composizione della flotta. */
    List<Trait> traits();

    Shift shift(String id);
}
