package it.unicam.cs.mpgc.rpg125885.persistence;

/** Errore di lettura, scrittura o validazione dei dati; il messaggio è pensato per essere mostrato all'utente. */
public class PersistenceException extends Exception {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
