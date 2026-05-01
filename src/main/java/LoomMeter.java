package loom;

import java.util.ArrayList;
import java.util.List;

import loom.benchmark.BenchmarkRunnerService;
import loom.benchmark.IoBenchmarkService;
import loom.config.BenchmarkConstants;
import loom.metrics.MetricsSamplerService;
import loom.model.ScenarioResult;
import loom.report.BenchmarkReportService;

/**
 * Entry point for Loom-Meter — wires configuration and services, then runs the comparison.
 * <p>
 * Run: {@code mvn -q exec:java} or {@code java -jar target/loom-meter-*.jar} after {@code mvn package}.
 */
public final class LoomMeter {

    public static void main(String[] args) throws Exception {
        BenchmarkConstants config = BenchmarkConstants.DEFAULT;

        MetricsSamplerService metricsSampler = new MetricsSamplerService(config);
        IoBenchmarkService ioBenchmark = new IoBenchmarkService(config);
        BenchmarkRunnerService runner = new BenchmarkRunnerService(metricsSampler);
        BenchmarkReportService report = new BenchmarkReportService(config);

        report.printHeader();

        List<ScenarioResult> rows = new ArrayList<>();
        rows.add(runner.runScenario(
                "Platform threads (" + config.fixedPoolThreads() + "-thread fixed pool)",
                ioBenchmark::runFixedThreadPool));
        BenchmarkRunnerService.suggestGcBetweenRuns();
        rows.add(runner.runScenario(
                "Virtual threads (per-task executor)",
                ioBenchmark::runVirtualThreadsPerTask));

        report.printComparisonTable(rows);
    }

    private LoomMeter() {}
}
