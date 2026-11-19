# Il Turno Rosso

Gioco di ruolo a turni ambientato in una centrale operativa d'emergenza, scritto in Java con interfaccia JavaFX.

Il giocatore compone una flotta di mezzi di soccorso: Vigili del Fuoco, 118 e Polizia. Ogni mezzo ha il suo equipaggio, le sue statistiche e il suo livello.

Durante una notte di allerta rossa, dalle 22:00 alle 06:00:

- si sceglie quali mezzi mandare alle chiamate al 112;
- si affrontano i pericoli in interventi a turni;
- si cerca di arrivare all'alba prima che l'Indice di Crisi della città raggiunga 100.

Le partite si salvano in file XML.

---

## Autore e proprietà intellettuale

**Matteo Pinna** (matricola 125885) — Ideatore e sviluppatore del progetto.

Progetto realizzato nell'ambito dell'esame di Metodologie di Programmazione, A.A. 2025/26, Università di Camerino.

Il progetto, la sua ideazione, l'architettura software e il codice sviluppato nell'ambito del lavoro sono attribuiti al suo autore.

**© 2026 Matteo Pinna. Tutti i diritti riservati**, fatti salvi i diritti e le licenze applicabili ai componenti e alle librerie di terze parti utilizzati.

La documentazione completa è disponibile nella [Wiki](https://github.com/matteopinna-unicam/rpg125885/wiki).

> Enti, luoghi e persone sono generici e immaginari. Il progetto non è affiliato ad alcun ente reale.

---

## Come eseguire il progetto

### Prerequisiti

- Un JDK 17 o successivo, per avviare Gradle.
- Java 25: se manca, Gradle lo scarica in automatico (toolchain con il plugin foojay). Serve una connessione a Internet al primo avvio.
- Gradle non va installato: il repository contiene il wrapper (Gradle 9.8.2).

### Clonare il repository

```bash
git clone https://github.com/matteopinna-unicam/rpg125885.git
cd rpg125885
```

### Build (compilazione e test)

```bash
./gradlew build
```

### Esecuzione

```bash
./gradlew run
```

Su Windows usare il wrapper `.bat`:

- da cmd: `gradlew.bat build` e `gradlew.bat run`;
- da PowerShell: `.\gradlew.bat build` e `.\gradlew.bat run`.

I salvataggi vengono scritti in `~/.rpg125885/saves/`, cioè nella cartella home dell'utente.

---

## Come si gioca, in breve

1. **Nuova partita**: si compone la flotta con un budget di punti, scegliendo i mezzi e il carattere dei capi-equipaggio.
2. **Sala operativa**: per ogni fase della notte arrivano le chiamate. A ogni chiamata si assegnano fino a tre unità (quattro nella fase finale). L'anteprima mostra se i requisiti sono soddisfatti e, se mancano mezzi, quanto cala l'efficacia.
3. **Interventi**: si giocano a turni contro i pericoli della chiamata, usando le manovre delle unità e i consumabili. Le dotazioni equipaggiate modificano le statistiche.
4. **Base**: tra una fase e l'altra si scelgono riposo o rifornimento, si gestiscono le dotazioni e si può salvare.
5. **Alba**: il rapporto finale mostra le chiamate risolte, fallite e ignorate, l'Indice di Crisi finale e le unità più efficaci.

Le regole complete sono nella pagina [Regole del gioco](https://github.com/matteopinna-unicam/rpg125885/wiki/Regole-del-gioco) della Wiki.

---

## Uso di strumenti di AI

Nella realizzazione del progetto sono stati utilizzati:

- **Claude (Anthropic)**, anche tramite Claude Code sulla copia locale del progetto, per:
  - analizzare i requisiti e discutere l'architettura;
  - generare una prima versione di alcune classi e i test;
  - proporre refactoring;
  - chiarire errori di configurazione di Gradle e JavaFX;
  - redigere bozze di README e Wiki.
- **Gemini (Google)** per proposte sull'ambientazione e sulle meccaniche di gioco, che ho poi selezionato e adattato.

Le scelte di progetto sono mie: tipo di gioco, ambientazione, GUI JavaFX e persistenza XML. Ho letto, compilato, testato con JUnit e provato in gioco il codice generato.

Gli strumenti di AI sono stati utilizzati come supporto durante il processo di sviluppo e non sostituiscono l'autorialità e la responsabilità personale sul progetto e sulle scelte progettuali.

La dichiarazione dettagliata, con il registro delle sessioni, è nella pagina [Uso dell'AI](https://github.com/matteopinna-unicam/rpg125885/wiki/Uso-dell'AI) della Wiki.
