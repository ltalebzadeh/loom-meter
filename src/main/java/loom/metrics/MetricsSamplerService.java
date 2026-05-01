package loom.metrics;

import loom.config.BenchmarkConstants;

/**
 * Factory for {@link MetricsSamplerSession} instances scoped to a {@link BenchmarkConstants} profile.
 */
public final class MetricsSamplerService {

    private final BenchmarkConstants constants;

    public MetricsSamplerService(BenchmarkConstants constants) {
        this.constants = constants;
    }

    public MetricsSamplerSession begin() {
        return MetricsSamplerSession.start(constants);
    }
}
