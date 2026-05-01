package loom.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import loom.config.BenchmarkConstants;

/**
 * Runs the simulated I/O workload (sleeping tasks) on platform vs virtual thread executors.
 */
public final class IoBenchmarkService {

    private final BenchmarkConstants constants;

    public IoBenchmarkService(BenchmarkConstants constants) {
        this.constants = constants;
    }

    public void runFixedThreadPool() throws InterruptedException {
        try (ExecutorService pool = Executors.newFixedThreadPool(constants.fixedPoolThreads())) {
            submitAllTasks(pool);
        }
    }

    public void runVirtualThreadsPerTask() throws InterruptedException {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            submitAllTasks(pool);
        }
    }

    private void submitAllTasks(ExecutorService pool) throws InterruptedException {
        int n = constants.taskCount();
        CountDownLatch done = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                try {
                    simulatedDbCall();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();
    }

    private void simulatedDbCall() throws InterruptedException {
        Thread.sleep(constants.simulatedDbLatency());
    }
}
