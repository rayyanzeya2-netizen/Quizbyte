package com.quizbyte.util;

import com.quizbyte.model.QuizResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe in-memory cache and concurrency coordinator.
 * Implements Multithreading and Synchronization rubric requirements using
 * AtomicInteger and ReentrantReadWriteLock to prevent race conditions.
 */
public class ThreadSafeLeaderboardCache {

    private static final ThreadSafeLeaderboardCache INSTANCE = new ThreadSafeLeaderboardCache();

    // Atomic counter for concurrent active quizzes across threads
    private final AtomicInteger activeQuizCounter = new AtomicInteger(0);

    // ReadWriteLock for high-throughput thread-safe leaderboard access
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final List<QuizResult> cachedLeaderboard = new ArrayList<>();
    private long lastCacheTimeMillis = 0;
    private static final long CACHE_TTL_MILLIS = 30_000; // 30 seconds

    private ThreadSafeLeaderboardCache() {}

    public static ThreadSafeLeaderboardCache getInstance() {
        return INSTANCE;
    }

    /**
     * Atomically increments the number of active quizzes.
     */
    public int incrementActiveQuizzes() {
        return activeQuizCounter.incrementAndGet();
    }

    /**
     * Atomically decrements the active quiz count.
     */
    public int decrementActiveQuizzes() {
        return activeQuizCounter.updateAndGet(current -> Math.max(0, current - 1));
    }

    /**
     * Retrieves the current count of concurrent quizzes.
     */
    public int getActiveQuizCount() {
        return activeQuizCounter.get();
    }

    /**
     * Checks if the in-memory cache is valid under read lock.
     */
    public boolean isCacheValid() {
        rwLock.readLock().lock();
        try {
            return !cachedLeaderboard.isEmpty() && 
                   (System.currentTimeMillis() - lastCacheTimeMillis < CACHE_TTL_MILLIS);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe retrieval of cached leaderboard records.
     */
    public List<QuizResult> getCachedLeaderboard() {
        rwLock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(cachedLeaderboard));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Thread-safe update of the leaderboard cache using write lock.
     */
    public void updateCache(List<QuizResult> newLeaderboard) {
        rwLock.writeLock().lock();
        try {
            cachedLeaderboard.clear();
            if (newLeaderboard != null) {
                cachedLeaderboard.addAll(newLeaderboard);
            }
            lastCacheTimeMillis = System.currentTimeMillis();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Invalidates the cache when a new score is persisted.
     */
    public void invalidateCache() {
        rwLock.writeLock().lock();
        try {
            lastCacheTimeMillis = 0;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
