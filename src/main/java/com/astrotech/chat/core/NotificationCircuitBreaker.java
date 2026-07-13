package com.astrotech.chat.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;


@Slf4j
@Component
public class NotificationCircuitBreaker {
    private int failureCount = 0;
    private long lastFailureTime = 0L;
    private State state = State.CLOSED;
    private final Queue<Runnable> retryQueue = new ConcurrentLinkedQueue<>();

    public synchronized <T> void execute(Supplier<T> operation) {
        var now = Instant.now().getEpochSecond();
        if (state == State.OPEN) {

            var cooldown = currentRecoveryTime();
            long elapsed = now - lastFailureTime;

            if (elapsed < cooldown) {

                throw new IllegalStateException(
                        "Notification service unavailable. Retry in "
                                + (cooldown - elapsed) + " seconds"
                );
            }

            state = State.HALF_OPEN;
        }

        try {

            T result = operation.get();


            close();

            flushRetryQueue();

        } catch (Exception ex) {

            registerFailure(ex);

            retryQueue.add(() -> operation.get());

            throw ex;
        }
    }

    private long currentRecoveryTime() {
        return 15;

    }

    private void flushRetryQueue() {

        while (!retryQueue.isEmpty()) {

            Runnable task = retryQueue.poll();

            try {

                if (task != null) {
                    task.run();
                }

            } catch (Exception ex) {

                retryQueue.add(task);

                break;
            }
        }
    }

    private synchronized void close() {

        state = State.CLOSED;
        failureCount = 0;

        log.info("Circuit breaker closed");
    }

    private synchronized void registerFailure(Exception ex) {

        failureCount++;

        log.error(
                "Notification failure count: {}",
                failureCount,
                ex
        );

        int failureThreshold = 3;
        if (failureCount >= failureThreshold) {
            open();
        }
    }

    private synchronized void open() {

        state = State.OPEN;
        lastFailureTime = Instant.now().getEpochSecond();

        log.warn("Circuit breaker opened");
    }

    enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
