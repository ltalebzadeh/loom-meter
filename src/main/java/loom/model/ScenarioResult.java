package loom.model;

import java.time.Duration;

/**
 * Outcome of one benchmark scenario: wall-clock time, peak threads observed, heap delta.
 */
public record ScenarioResult(String label, Duration elapsed, int peakLiveThreads, long heapSpikeBytes) {}
