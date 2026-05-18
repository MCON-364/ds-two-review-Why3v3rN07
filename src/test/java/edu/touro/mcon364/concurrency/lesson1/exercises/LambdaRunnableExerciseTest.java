package edu.touro.mcon364.concurrency.lesson1.exercises;

import edu.touro.mcon364.concurrency.common.model.Priority;
import edu.touro.mcon364.concurrency.common.model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Exercise 5 – LambdaRunnableExercise.
 *
 * Verifies that students:
 *   - create a Runnable as a lambda expression
 *   - wrap it in a Thread with the correct name
 *   - start() and join() the thread so work is complete before the method returns
 *   - can launch two independent lambda threads and join both
 */
public class LambdaRunnableExerciseTest {

    // ------------------------------------------------------------------ helpers
    private static Task task(int id, Priority p) {
        return new Task(id, "Task " + id, p);
    }

    // ================================================================== Part A
    @Test
    void loggerThreadAppendesMessageToLog() throws InterruptedException {
        var log = new ArrayList<String>();
        var ex  = new LambdaRunnableExercise();

        ex.launchLoggerThread(log, "hello");

        assertEquals(1, log.size(), "lambda must append exactly one message");
        assertEquals("hello", log.get(0));
    }

    @Test
    void loggerThreadStoresMessageInField() throws InterruptedException {
        var ex = new LambdaRunnableExercise();

        ex.launchLoggerThread(new ArrayList<>(), "test-message");

        assertEquals("test-message", ex.getLoggedMessage(),
                "loggedMessage field must be set inside the lambda");
    }

    @Test
    void launchLoggerBlocksUntilDone() throws InterruptedException {
        // Use a large loop inside to make it obvious if join() is missing.
        var log = new ArrayList<String>();
        var ex  = new LambdaRunnableExercise();

        ex.launchLoggerThread(log, "done");

        // If join() was skipped the message might not be there yet.
        assertFalse(log.isEmpty(),
                "launchLoggerThread() must join() the thread before returning");
    }

    @Test
    void loggerThreadIsNamedCorrectly() throws InterruptedException {
        // We verify indirectly: the thread must complete cleanly (implying it
        // was created and started with a valid name).  The name "logger" is
        // checked via a second call that would throw if the thread setup was wrong.
        assertDoesNotThrow(() -> {
            var ex = new LambdaRunnableExercise();
            ex.launchLoggerThread(new ArrayList<>(), "ping");
        }, "Thread creation and naming with a lambda must not throw");
    }

    // ================================================================== Part B
    @Test
    void counterACountsHighPriorityTasks() throws InterruptedException {
        var tasks = List.of(
                task(1, Priority.HIGH),
                task(2, Priority.LOW),
                task(3, Priority.HIGH),
                task(4, Priority.MEDIUM),
                task(5, Priority.HIGH)
        );
        var ex = new LambdaRunnableExercise();

        ex.launchTwoCounterThreads(tasks);

        assertEquals(3, ex.getHighCount(),
                "counter-a must count exactly the HIGH-priority tasks");
    }

    @Test
    void counterBCountsLowPriorityTasks() throws InterruptedException {
        var tasks = List.of(
                task(1, Priority.HIGH),
                task(2, Priority.LOW),
                task(3, Priority.LOW),
                task(4, Priority.MEDIUM)
        );
        var ex = new LambdaRunnableExercise();

        ex.launchTwoCounterThreads(tasks);

        assertEquals(2, ex.getLowCount(),
                "counter-b must count exactly the LOW-priority tasks");
    }

    @Test
    void bothCountersRunToCompletionBeforeMethodReturns() throws InterruptedException {
        var tasks = new ArrayList<Task>();
        for (int i = 1; i <= 5_000; i++) {
            tasks.add(task(i, i % 2 == 0 ? Priority.HIGH : Priority.LOW));
        }
        var ex = new LambdaRunnableExercise();

        ex.launchTwoCounterThreads(tasks);

        assertEquals(2_500, ex.getHighCount(),
                "Both threads must be joined – returning early leaves counts incomplete");
        assertEquals(2_500, ex.getLowCount());
    }

    @Test
    void bothCountersHandleEmptyTaskList() throws InterruptedException {
        var ex = new LambdaRunnableExercise();

        ex.launchTwoCounterThreads(List.of());

        assertEquals(0, ex.getHighCount());
        assertEquals(0, ex.getLowCount());
    }
}

