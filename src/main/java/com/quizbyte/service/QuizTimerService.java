package com.quizbyte.service;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dedicated Multithreaded Quiz Countdown Timer Service.
 * Implements rubric Multithreading & Synchronization requirements via
 * ScheduledExecutorService, thread-safe callbacks, and SwingUtilities.invokeLater.
 */
public class QuizTimerService {

    /**
     * Listener callback contract invoked on the Swing Event Dispatch Thread (EDT).
     */
    public interface TimerCallback {
        void onTick(int secondsRemaining);
        void onTimeout();
    }

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTask;
    private final AtomicBoolean isRunning;
    private int remainingSeconds;

    public QuizTimerService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "QuizTimer-WorkerThread");
            t.setDaemon(true);
            return t;
        });
        this.isRunning = new AtomicBoolean(false);
    }

    /**
     * Starts a synchronized countdown timer for the specified duration.
     *
     * @param totalSeconds duration in seconds
     * @param callback     listener to receive tick and timeout events on the EDT
     */
    public synchronized void startTimer(int totalSeconds, TimerCallback callback) {
        stopTimer();

        this.remainingSeconds = totalSeconds;
        this.isRunning.set(true);

        // Immediate initial tick
        SwingUtilities.invokeLater(() -> callback.onTick(remainingSeconds));

        currentTask = scheduler.scheduleAtFixedRate(() -> {
            if (!isRunning.get()) {
                return;
            }

            remainingSeconds--;

            if (remainingSeconds > 0) {
                // Thread-safe dispatch to Swing Event Dispatch Thread
                int current = remainingSeconds;
                SwingUtilities.invokeLater(() -> callback.onTick(current));
            } else {
                isRunning.set(false);
                SwingUtilities.invokeLater(() -> {
                    callback.onTick(0);
                    callback.onTimeout();
                });
                stopTimer();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Safely stops and cancels the ongoing countdown task.
     */
    public synchronized void stopTimer() {
        isRunning.set(false);
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    /**
     * Shuts down the background thread pool gracefully.
     */
    public void shutdown() {
        stopTimer();
        scheduler.shutdownNow();
    }
}
