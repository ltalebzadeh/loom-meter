package loom.report;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import loom.config.BenchmarkConstants;
import loom.model.ScenarioResult;

/**
 * Renders the comparison table and formatting helpers for stdout.
 */
public final class BenchmarkReportService {

    private final BenchmarkConstants constants;

    public BenchmarkReportService(BenchmarkConstants constants) {
        this.constants = Objects.requireNonNull(constants);
    }

    public void printHeader() {
        System.out.println("Loom-Meter — "
                + constants.taskCount()
                + " tasks × "
                + constants.simulatedDbLatency().toMillis()
                + "ms sleep each\n");
    }

    public void printComparisonTable(List<ScenarioResult> rows) {
        String timeH = "Time taken";
        String threadH = "Peak live threads";
        String memH = "Heap spike (vs start)";
        int c0 = 42;
        int c1 = 18;
        int c2 = 22;
        int c3 = 24;

        String line = "+" + "-".repeat(c0 + 2) + "+" + "-".repeat(c1 + 2) + "+" + "-".repeat(c2 + 2) + "+" + "-".repeat(c3 + 2) + "+";
        System.out.println(line);
        System.out.printf(Locale.US, "| %-" + c0 + "s | %-" + c1 + "s | %-" + c2 + "s | %-" + c3 + "s |%n",
                "Scenario", timeH, threadH, memH);
        System.out.println(line);
        for (ScenarioResult r : rows) {
            System.out.printf(Locale.US, "| %-" + c0 + "s | %-" + c1 + "s | %," + c2 + "d | %-" + c3 + "s |%n",
                    r.label(),
                    formatDuration(r.elapsed()),
                    r.peakLiveThreads(),
                    formatBytes(r.heapSpikeBytes()));
        }
        System.out.println(line);
        System.out.println();
        System.out.println("Notes:");
        System.out.println("  • Peak live threads: max ThreadMXBean#getThreadCount() sampled every "
                + constants.metricsSampleInterval().toMillis()
                + "ms during the run.");
        System.out.println("  • Heap spike: max heap used (MemoryMXBean) minus heap used at run start.");
    }

    static String formatDuration(Duration d) {
        long ms = d.toMillis();
        if (ms < 10_000) {
            return ms + " ms";
        }
        return String.format(Locale.US, "%.2f s", ms / 1000.0);
    }

    static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "n/a";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.US, "%.1f KiB", kb);
        }
        return String.format(Locale.US, "%.2f MiB", kb / 1024.0);
    }
}
