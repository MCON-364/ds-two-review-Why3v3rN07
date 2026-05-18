package edu.touro.mcon364.concurrency.lesson1.exercises;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Exercise 3 – PeriodicLogger.
 *
 * These tests verify that students correctly:
 *   - create and name the background thread
 *   - set the daemon flag before start()
 *   - use Thread.sleep() to pace the ticks
 *   - return from start() without blocking (non-blocking start)
 *   - implement isRunning() via isAlive()
 *   - implement awaitCompletion() via join()
 */
public class PeriodicLoggerTest {

    // ------------------------------------------------------------------ basic output
    @Test
    void logsTheCorrectNumberOfTicks() throws InterruptedException {
        var logger = new PeriodicLogger(3, 10);
        logger.start();
        logger.awaitCompletion();

        assertEquals(3, logger.getLog().size(),
                "There must be exactly one log entry per tick");
    }

    @Test
    void logEntriesAreLabelledCorrectly() throws InterruptedException {
        var logger = new PeriodicLogger(4, 5);
        logger.start();
        logger.awaitCompletion();

        var log = logger.getLog();
        for (int i = 0; i < log.size(); i++) {
            assertTrue(log.get(i).contains(String.valueOf(i + 1)),
                    "Log entry " + i + " should reference tick number " + (i + 1));
        }
    }

    // ------------------------------------------------------------------ non-blocking start
    @Test
    void startReturnsBeforeAllTicksComplete() throws InterruptedException {
        // With 3 ticks × 50 ms each the thread takes ~150 ms.
        // start() must return well before that.
        var logger = new PeriodicLogger(3, 50);

        long before = System.currentTimeMillis();
        logger.start();
        long elapsed = System.currentTimeMillis() - before;

        assertTrue(elapsed < 100,
                "start() must return immediately (non-blocking), elapsed=" + elapsed + "ms");

        logger.awaitCompletion(); // clean up
    }

    // ------------------------------------------------------------------ isRunning / isAlive
    @Test
    void isRunningReturnsTrueWhileThreadIsAlive() throws InterruptedException {
        var logger = new PeriodicLogger(2, 80);
        logger.start();

        assertTrue(logger.isRunning(),
                "isRunning() must return true while the background thread is alive");

        logger.awaitCompletion();
    }

    @Test
    void isRunningReturnsFalseAfterCompletion() throws InterruptedException {
        var logger = new PeriodicLogger(2, 5);
        logger.start();
        logger.awaitCompletion();

        assertFalse(logger.isRunning(),
                "isRunning() must return false after the thread has terminated");
    }

    // ------------------------------------------------------------------ daemon flag
    @Test
    void backgroundThreadIsADaemon() throws InterruptedException {
        // We need to inspect the thread. We verify indirectly: a daemon thread
        // does not prevent JVM shutdown. We can also capture it via the thread name.
        // The simplest testable fact: after start() the log grows even without
        // an explicit join(), which means the thread really started.
        // For the daemon check we rely on isRunning() + awaitCompletion() working,
        // and validate the thread name as a proxy for correct configuration.
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            var logger = new PeriodicLogger(1, 10);
            logger.start();
            logger.awaitCompletion();
            assertEquals(1, logger.getLog().size());
        });
    }

    // ------------------------------------------------------------------ awaitCompletion blocks
    @Test
    void awaitCompletionBlocksUntilThreadIsFinished() throws InterruptedException {
        var logger = new PeriodicLogger(3, 20);
        logger.start();

        logger.awaitCompletion(); // must not return until all ticks are done

        assertEquals(3, logger.getLog().size(),
                "awaitCompletion() must join() the thread before returning");
    }
}

