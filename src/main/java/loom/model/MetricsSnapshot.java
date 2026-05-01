package loom.model;

/**
 * Point-in-time JVM metrics captured when sampling stops.
 */
public record MetricsSnapshot(int peakLiveThreads, long heapSpikeBytes) {}
