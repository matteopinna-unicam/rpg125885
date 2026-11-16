package it.unicam.cs.mpgc.rpg125885.ui.javafx;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Esegue salvataggio e caricamento fuori dal thread della GUI; i risultati tornano sul thread JavaFX. */
final class BackgroundTasks {

    /** Operazione senza risultato che può fallire. */
    @FunctionalInterface
    interface Work {
        void run() throws Exception;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "rpg-background");
        thread.setDaemon(true);
        return thread;
    });

    void run(Work work, Runnable onSuccess, Consumer<Throwable> onFailure) {
        supply(() -> {
            work.run();
            return null;
        }, ignored -> onSuccess.run(), onFailure);
    }

    <T> void supply(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> onFailure.accept(task.getException()));
        executor.execute(task);
    }
}
