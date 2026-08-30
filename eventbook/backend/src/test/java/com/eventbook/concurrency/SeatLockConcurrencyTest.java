package com.eventbook.concurrency;

import com.eventbook.service.SeatLockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * *** THIS IS THE TEST THAT PROVES THE CORE CLAIM OF THE PROJECT ***
 *
 * Fires 50 concurrent "hold seat" requests at the SAME seat and asserts that
 * exactly ONE succeeds. This is the automated proof behind the resume line
 * "load-tested with 50 concurrent requests per seat, zero double-bookings."
 *
 * Runs against a real embedded Redis instance (not mocks) so the atomicity
 * of SETNX is genuinely exercised under thread contention.
 */
@ExtendWith(EmbeddedRedisExtension.class)
class SeatLockConcurrencyTest {

    private LettuceConnectionFactory connectionFactory;

    private SeatLockService buildServiceAgainstRealRedis() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6380);
        connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        SeatLockService service = new SeatLockService(template);
        ReflectionTestUtils.setField(service, "holdDurationMinutes", 5L);
        return service;
    }

    @AfterEach
    void tearDown() {
        if (connectionFactory != null) connectionFactory.destroy();
    }

    @Test
    void fiftyConcurrentRequestsForSameSeat_onlyOneSucceeds() throws InterruptedException {
        SeatLockService seatLockService = buildServiceAgainstRealRedis();
        Long contestedSeatId = 42L;
        int threadCount = 50;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1); // ensures all threads fire as close to simultaneously as possible
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String bookingRef = "booking:" + i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    List<Long> failed = seatLockService.tryLockSeats(List.of(contestedSeatId), bookingRef);
                    if (failed.isEmpty()) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startGate.countDown(); // release all 50 threads at once
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(true, completed, "All threads should complete within timeout");
        assertEquals(1, successCount.get(),
                "Exactly one of the 50 concurrent requests should win the lock for the contested seat");
    }

    @Test
    void concurrentRequestsForDisjointSeats_allSucceed() throws InterruptedException {
        SeatLockService seatLockService = buildServiceAgainstRealRedis();
        int threadCount = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final long seatId = 1000L + i; // each thread claims a DIFFERENT seat
            final String bookingRef = "booking:" + i;
            executor.submit(() -> {
                List<Long> failed = seatLockService.tryLockSeats(List.of(seatId), bookingRef);
                if (failed.isEmpty()) successCount.incrementAndGet();
                doneLatch.countDown();
            });
        }

        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(threadCount, successCount.get(), "Non-overlapping seat requests should never block each other");
    }
}
