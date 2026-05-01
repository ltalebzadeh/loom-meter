package loom.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import loom.config.BenchmarkConstants;
import loom.model.MetricsSnapshot;

/**
 * Background sampler that tracks peak {@link ThreadMXBean#getThreadCount()} and heap usage
 * over a benchmark window using {@link ManagementFactory#getThreadMXBean()}.
 */
public final class MetricsSamplerSession {

    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final AtomicLong peakThreads = new AtomicLong(0);
    private final AtomicLong peakHeapUsed = new AtomicLong(0);
    private final long baselineHeapUsed;
    private final BenchmarkConstants constants;
    private final ExecutorService samplerPool = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("loom-meter-sampler", 0).factory());
    private volatile Future<?> samplingTask;

    MetricsSamplerSession(BenchmarkConstants constants, long baselineHeapUsed) {
        this.constants = constants;
        this.baselineHeapUsed = baselineHeapUsed;
        this.peakHeapUsed.set(baselineHeapUsed);
        this.samplingTask = samplerPool.submit(this::sampleLoop);
    }

    static MetricsSamplerSession start(BenchmarkConstants constants) {
        long baseline = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        return new MetricsSamplerSession(constants, baseline);
    }

    private void sampleLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int live = threads.getThreadCount();
                peakThreads.updateAndGet(cur -> Math.max(cur, live));

                long used = memory.getHeapMemoryUsage().getUsed();
                peakHeapUsed.updateAndGet(cur -> Math.max(cur, used));

                Thread.sleep(constants.metricsSampleInterval());
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public MetricsSnapshot stopAndSnapshot() throws Exception {
        samplingTask.cancel(true);
        samplerPool.shutdownNow();

        long maxHeap = peakHeapUsed.get();
        long spike = Math.max(0, maxHeap - baselineHeapUsed);
        int peak = (int) Math.min(Integer.MAX_VALUE, peakThreads.get());
        if (peak == 0) {
            peak = threads.getThreadCount();
        }
        return new MetricsSnapshot(peak, spike);
    }
}
