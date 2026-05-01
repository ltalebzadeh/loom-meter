package loom.benchmark;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import loom.metrics.MetricsSamplerService;
import loom.metrics.MetricsSamplerSession;
import loom.model.MetricsSnapshot;
import loom.model.ScenarioResult;
import loom.util.ThrowingRunnable;

/**
 * Orchestrates a single scenario: timing, metrics sampling, and {@link ScenarioResult} assembly.
 */
public final class BenchmarkRunnerService {

    private final MetricsSamplerService metricsSamplerService;

    public BenchmarkRunnerService(MetricsSamplerService metricsSamplerService) {
        this.metricsSamplerService = Objects.requireNonNull(metricsSamplerService);
    }

    public ScenarioResult runScenario(String label, ThrowingRunnable work) throws Exception {
        MetricsSamplerSession session = metricsSamplerService.begin();
        Instant start = Instant.now();
        work.run();
        Duration elapsed = Duration.between(start, Instant.now());
        MetricsSnapshot snap = session.stopAndSnapshot();
        return new ScenarioResult(label, elapsed, snap.peakLiveThreads(), snap.heapSpikeBytes());
    }

    /**
     * Best-effort pause between scenarios so the second run starts from a cleaner heap baseline.
     */
    public static void suggestGcBetweenRuns() {
        System.gc();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
