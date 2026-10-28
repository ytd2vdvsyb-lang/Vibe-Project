package vibe.launcher.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/** Параллельные загрузки с общим прогрессом. */
final class Parallel {
    private static final int THREADS = 8;

    private Parallel() {
    }

    static void run(List<Callable<Void>> tasks, Progress progress) throws IOException {
        if (tasks.isEmpty()) return;
        ExecutorService pool = Executors.newFixedThreadPool(THREADS, r -> {
            Thread t = new Thread(r, "vibe-download");
            t.setDaemon(true);
            return t;
        });
        AtomicLong done = new AtomicLong();
        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : tasks) {
                futures.add(pool.submit(() -> {
                    task.call();
                    progress.progress(done.incrementAndGet(), tasks.size());
                    return null;
                }));
            }
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) throw (IOException) cause;
                    throw new IOException(cause);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Установка прервана", e);
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
