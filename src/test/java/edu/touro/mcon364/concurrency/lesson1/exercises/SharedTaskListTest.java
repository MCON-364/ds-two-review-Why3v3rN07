package edu.touro.mcon364.concurrency.lesson1.exercises;

import edu.touro.mcon364.concurrency.common.model.Priority;
import edu.touro.mcon364.concurrency.common.model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Exercise 4 – SharedTaskList.
 *
 * These tests verify that students replace the bare ArrayList with
 * Collections.synchronizedList() so that concurrent adds are safe.
 */
public class SharedTaskListTest {

    private static Task task(int id) {
        return new Task(id, "Task " + id, Priority.MEDIUM);
    }

    // ------------------------------------------------------------------ basic
    @Test
    void addAndSizeWorkForSingleThreadedUsage() {
        var list = new SharedTaskList();
        list.add(task(1));
        list.add(task(2));

        assertEquals(2, list.size());
    }

    @Test
    void freshListIsEmpty() {
        assertEquals(0, new SharedTaskList().size());
    }

    // ------------------------------------------------------------------ concurrency
    /**
     * Many threads add tasks at the same time.
     * With a plain ArrayList some adds can be silently lost due to the race on
     * the backing array.  With synchronizedList every add is visible.
     */
    @Test
    void concurrentAddsNeverLoseTasks() throws InterruptedException {
        var list = new SharedTaskList();
        int threadCount = 20;
        int addsPerThread = 1_000;

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int worker = i;
            threads.add(new Thread(() -> {
                for (int j = 1; j <= addsPerThread; j++) {
                    list.add(task(worker * 100_000 + j));
                }
            }));
        }

        threads.forEach(Thread::start);
        for (Thread t : threads) t.join();

        assertEquals(threadCount * addsPerThread, list.size(),
                "Every task added by every thread must be present – no add must be lost");
    }

    /**
     * A plain ArrayList will sometimes throw ArrayIndexOutOfBoundsException or
     * NullPointerException under concurrent modification.  The fixed list must
     * not throw.
     */
    @Test
    void concurrentAddsDoNotThrow() {
        assertDoesNotThrow(() -> {
            var list = new SharedTaskList();
            int threadCount = 10;
            int addsPerThread = 2_000;

            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                final int worker = i;
                threads.add(new Thread(() -> {
                    for (int j = 1; j <= addsPerThread; j++) {
                        list.add(task(worker * 100_000 + j));
                    }
                }));
            }

            threads.forEach(Thread::start);
            for (Thread t : threads) t.join();
        }, "Concurrent adds must not throw – use Collections.synchronizedList()");
    }
}

