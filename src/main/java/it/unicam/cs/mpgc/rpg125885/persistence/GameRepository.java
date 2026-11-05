package it.unicam.cs.mpgc.rpg125885.persistence;

import it.unicam.cs.mpgc.rpg125885.model.shift.GameState;

import java.util.List;

/** Contratto di persistenza delle partite, indipendente dal formato di memorizzazione. */
public interface GameRepository {

    /** Salva la partita nello slot, sostituendo il contenuto precedente. */
    void save(String slot, GameState state) throws PersistenceException;

    /**
     * @throws PersistenceException se lo slot non esiste o il file non è leggibile o valido
     */
    GameState load(String slot) throws PersistenceException;

    /** Slot leggibili, dal salvataggio più recente; quelli danneggiati sono ignorati. */
    List<SaveSlotInfo> listSlots() throws PersistenceException;

    /**
     * @throws PersistenceException se lo slot non esiste
     */
    void delete(String slot) throws PersistenceException;
}
