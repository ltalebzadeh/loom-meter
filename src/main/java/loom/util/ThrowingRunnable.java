package loom.util;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
