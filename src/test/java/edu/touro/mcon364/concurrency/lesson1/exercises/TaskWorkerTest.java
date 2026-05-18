package edu.touro.mcon364.concurrency.lesson1.exercises;

import edu.touro.mcon364.concurrency.common.model.Priority;
import edu.touro.mcon364.concurrency.common.model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Exercise 2 – TaskWorker.
 *
 * These tests verify that students correctly:
 *   - create a Thread with the supplied name
 *   - start() the thread (so work actually happens)
 *   - join() the thread so run() blocks until the work is done
 */
public class TaskWorkerTest {

    private static Task task(int id) {
        return new Task(id, "Task " + id, Priority.MEDIUM);
    }

    // ------------------------------------------------------------------ basic
    @Test
    void processesAllTasksInTheList() throws InterruptedException {
        var tasks = List.of(task(1), task(2), task(3));
        var worker = new TaskWorker(tasks, "test-worker");

        worker.run();

        assertEquals(3, worker.getProcessedCount(),
                "processedCount must equal the number of tasks supplied");
    }

    @Test
    void processesEmptyListWithoutError() throws InterruptedException {
        var worker = new TaskWorker(List.of(), "empty-worker");

        worker.run();

        assertEquals(0, worker.getProcessedCount());
    }

    // ------------------------------------------------------------------ thread name
    @Test
    void workerThreadHasTheSuppliedName() throws InterruptedException {
        var tasks = List.of(task(10));
        var worker = new TaskWorker(tasks, "my-named-thread");

        worker.run();

        assertEquals("my-named-thread", worker.getWorkerName(),
                "The background thread must be given the name passed to the constructor");
    }

    // ------------------------------------------------------------------ join semantics
    @Test
    void runBlocksUntilAllTasksAreProcessed() throws InterruptedException {
        // Use a large list to make it unlikely that run() returns before the
        // thread has actually finished if join() is missing.
        List<Task> tasks = new java.util.ArrayList<>();
        for (int i = 1; i <= 10_000; i++) tasks.add(task(i));

        var worker = new TaskWorker(tasks, "bulk-worker");

        worker.run(); // must not return until the thread is done

        assertEquals(10_000, worker.getProcessedCount(),
                "run() must join() the thread – returning early would leave the count incomplete");
    }

    // ------------------------------------------------------------------ independent runs
    @Test
    void eachRunIsIndependent() throws InterruptedException {
        var w1 = new TaskWorker(List.of(task(1), task(2)), "worker-a");
        var w2 = new TaskWorker(List.of(task(3)), "worker-b");

        w1.run();
        w2.run();

        assertEquals(2, w1.getProcessedCount());
        assertEquals(1, w2.getProcessedCount());
        assertEquals("worker-a", w1.getWorkerName());
        assertEquals("worker-b", w2.getWorkerName());
    }
}

