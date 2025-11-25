package org.example.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

/**
 * Manages concurrent database operations using ExecutorService
 * Implements Singleton pattern for centralized thread pool management
 */
public class DatabaseExecutorService {
    private static DatabaseExecutorService instance;
    private final ExecutorService executorService;

    // Number of threads in the pool
    private static final int THREAD_POOL_SIZE = 4;

    /**
     * Private constructor - Singleton pattern
     */
    private DatabaseExecutorService() {
        // Create a fixed thread pool for database operations
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        System.out.println("✅ Database ExecutorService initialized with " + THREAD_POOL_SIZE + " threads");
    }

    /**
     * Get singleton instance
     */
    public static synchronized DatabaseExecutorService getInstance() {
        if (instance == null) {
            instance = new DatabaseExecutorService();
        }
        return instance;
    }

    /**
     * Submit a task to the executor service
     * @param task The task to execute
     * @return Future object to track task completion
     */
    public <T> Future<T> submitTask(Callable<T> task) {
        return executorService.submit(task);
    }

    /**
     * Execute a task without return value
     * @param task The task to execute
     */
    public void executeTask(Runnable task) {
        executorService.execute(task);
    }

    /**
     * Shutdown the executor service gracefully
     */
    public void shutdown() {
        executorService.shutdown();
        System.out.println("✅ Database ExecutorService shutdown initiated");
    }

    /**
     * Force shutdown the executor service
     */
    public void shutdownNow() {
        executorService.shutdownNow();
        System.out.println("⚠️ Database ExecutorService forced shutdown");
    }

    /**
     * Check if executor service is shutdown
     */
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    /**
     * Check if all tasks are completed
     */
    public boolean isTerminated() {
        return executorService.isTerminated();
    }
}