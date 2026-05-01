package loom.config;

import java.time.Duration;

/**
 * Shared settings for Loom-Meter runs (task shape, pool sizing, sampling cadence).
 */
public record BenchmarkConstants(
        int taskCount,
        Duration simulatedDbLatency,
        int fixedPoolThreads,
        Duration metricsSampleInterval
) {
    public static final BenchmarkConstants DEFAULT = new BenchmarkConstants(
            10_000,
            Duration.ofMillis(200),
            256,
            Duration.ofMillis(10)
    );
}
