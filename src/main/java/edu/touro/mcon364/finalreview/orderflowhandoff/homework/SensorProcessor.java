package edu.touro.mcon364.finalreview.orderflowhandoff.homework;

import edu.touro.mcon364.finalreview.model.SensorReading;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Homework 2 — Sensor reading processor.
 * <p>
 * A monitoring system receives readings from sensors over time. One part of the
 * program submits readings as they arrive. Another part of the program processes
 * those readings using one or more background workers.
 * <p>
 * This class is responsible for coordinating that handoff and for keeping a
 * summary of the readings that were actually processed.
 * <p>
 * The important question is not only "How do we calculate the stats?" It is also:
 * "What happens when readings are being submitted and processed by different
 * threads at the same time?"
 * <p>
 * Requirements:
 * - submit(reading) accepts one new sensor reading for later processing.
 * - start(workerCount) starts workerCount background workers.
 * - workerCount must be greater than 0.
 * - Workers should process submitted readings until the processor is stopped and
 * all already-submitted readings have been handled.
 * - stop() tells the processor to stop accepting/processing future work and waits
 * until the workers finish the remaining work.
 * - getTotalProcessed() returns how many readings have been processed so far.
 * - getStats() returns summary statistics for the processed reading values:
 * count, minimum, maximum, sum, and average.
 * - Public reporting methods must not expose mutable internal state.
 * <p>
 * Before coding, think about:
 * - Which object or objects represent work waiting to be processed?
 * - Which object or objects represent work that has already been processed?
 * - Which state can be accessed by more than one thread?
 * - How will workers know when to keep working and when to stop?
 * - What should happen if getStats() is called while workers are still running?
 * - Is it better to store all processed readings and calculate stats later, or
 * update numeric summary state as each reading is processed?
 * - If several workers update the same stats, how will those updates stay correct?
 */
public class SensorProcessor {
    BlockingQueue<SensorReading> incomingReadings = new LinkedBlockingQueue<>();
    AtomicInteger totalProcessed = new AtomicInteger();
    ExecutorService workerPool;
    volatile boolean running = false;
    DoubleSummaryStatistics stats = new DoubleSummaryStatistics(); //does this need locking?


    /**
     * Accept one sensor reading for processing.
     *
     * @param reading the reading to process later
     */
    public void submit(SensorReading reading) {
        // TODO: decide where submitted readings should be stored
        incomingReadings.offer(reading);
    }

    /**
     * Start background workers that process submitted readings.
     *
     * @param workerCount number of worker threads to start
     * @throws IllegalArgumentException if workerCount is not positive
     */
    public void start(int workerCount) {
        // TODO: validate workerCount
        if (workerCount <= 0) throw new IllegalArgumentException("workerCount must be positive");
        // TODO: start the requested number of workers
        running = true;
        workerPool = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++) {
            workerPool.submit(this::workerLoop);
        }
    }

    /**
     * Logic run by each worker.
     * <p>
     * This method is private because callers should not run worker logic directly.
     * The worker should repeatedly look for work, process it when available, and
     * eventually exit when the processor is stopping and no work remains.
     */
    private void workerLoop() {
        // TODO: implement the worker behavior
        while (running || !incomingReadings.isEmpty()) {
            try {
                stats.accept(incomingReadings.take().value());
                totalProcessed.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Stop the processor and wait for workers to finish.
     *
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public void stop() throws InterruptedException {
        // TODO: signal that work should stop
        running = false;
        if (workerPool == null) return; //not started
        workerPool.shutdown();
        // TODO: wait for all workers to finish
        workerPool.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);
        while (!incomingReadings.isEmpty()) { //catch stragglers
            SensorReading reading = incomingReadings.poll();
            if (reading != null) {
                stats.accept(reading.value());
                totalProcessed.incrementAndGet();
            }
        }
    }

    /**
     * Return the number of readings processed so far.
     */
    public int getTotalProcessed() {
        // TODO: return the processed count safely
        return totalProcessed.get();
    }

    /**
     * Return summary statistics for the processed reading values.
     * <p>
     * If no readings have been processed yet, return an empty
     * DoubleSummaryStatistics object.
     */
    public DoubleSummaryStatistics getStats() {
        // TODO: calculate or return the current statistics safely
        return stats;
    }
}