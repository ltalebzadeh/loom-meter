# Loom-Meter

A small Java 21 program that compares **platform threads** (fixed-size pool) and **virtual threads** (JDK *Project Loom*) on the same **blocking I/O–style** workload, then prints a side-by-side table: wall-clock time, peak live thread count, and heap usage spike.

## Why this project

Traditional **platform threads** are relatively heavy: each blocked task typically needs its own OS-backed thread if you want concurrency. Under **high concurrency and lots of blocking** (network, databases, `Thread.sleep`-like waits), a large fixed pool caps throughput, while scaling the pool up increases **thread count and memory pressure**.

**Virtual threads** are cheap: you can schedule very large numbers of blocking tasks without creating a matching number of platform threads. This project exists to **make that trade-off visible** in one run: same number of blocking “calls,” two executors, and simple JVM metrics so a reader can see how **peak threads** and **reported heap movement** differ—not as a production benchmark, but as a **clear, reproducible illustration** of why virtual threads matter for **blocking, I/O-heavy** server workloads.

## What it actually does

1. **Workload** — Submits **10,000** tasks (configurable in code). Each task **simulates a blocking call** (e.g. remote DB) with `Thread.sleep(200 ms)`.
2. **Scenario A** — Runs all tasks on a **256-thread fixed `ExecutorService`** (platform worker threads).
3. **Scenario B** — Runs the same workload on **`Executors.newVirtualThreadPerTaskExecutor()`**.
4. **Between scenarios** — Suggests `System.gc()` and a short pause so the second run does not inherit a noisy heap baseline.
5. **Metrics** — While each scenario runs, a background sampler (every **10 ms**) records:
    - **Peak live threads** — `ThreadMXBean#getThreadCount()`
    - **Heap spike** — maximum heap used during the run minus heap used at scenario start (`MemoryMXBean`)
6. **Output** — A formatted **comparison table** to standard output with brief footnotes explaining the metrics.

All defaults live in `loom.config.BenchmarkConstants` (`DEFAULT`).

## Requirements

- **JDK 21 or newer** (virtual threads are a Java 21 feature in the standard library)
- **Apache Maven** (to build and run)

## How to run

From the project root:

```bash
mvn -q exec:java
```

Or build a runnable JAR and execute it:

```bash
mvn -q package
java -jar target/loom-meter-1.0.0-SNAPSHOT.jar
```

## Project layout (brief)

| Area | Role |
|------|------|
| `loom.LoomMeter` | Entry point: wires services, runs both scenarios, prints the report |
| `loom.benchmark` | Workload and executor strategies |
| `loom.metrics` | Sampling session around each scenario |
| `loom.report` | Console table and formatting |
| `loom.config` | Task count, sleep duration, pool size, sample interval |

## Limitations

- The workload is **synthetic** (`sleep`), not real sockets or JDBC.
- **Thread and heap numbers** are influenced by the JVM, GC, and sampling; treat them as **indicative**, not exact capacity planning.
- Tuning `BenchmarkConstants` changes behavior dramatically; this repo favors **defaults that exaggerate the contrast** for teaching.

For production decisions, use **real traffic**, **profilers**, and **load tests** tailored to your stack.
