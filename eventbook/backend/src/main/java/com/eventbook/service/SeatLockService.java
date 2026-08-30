package com.eventbook.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the distributed seat-lock mechanism backed by Redis.
 *
 * Why Redis instead of a plain DB row lock:
 *  - A SETNX-style atomic operation lets us claim a seat in a single round trip,
 *    without holding a DB transaction open for the entire "select seats" UX flow
 *    (which can take a user a couple of minutes).
 *  - TTL gives us free, automatic expiry: if a user abandons checkout, the lock
 *    self-releases without needing a cleanup job to run on time.
 *  - It scales horizontally across multiple backend instances, which a plain
 *    in-memory Java lock (e.g. ConcurrentHashMap) could never do.
 *
 * The DB `Seat.status` column is still the source of truth for anything that
 * outlives the hold window (BOOKED is permanent); Redis only governs the
 * short-lived HELD window and is what makes concurrent requests for the same
 * seat resolve safely.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${booking.hold-duration-minutes:5}")
    private long holdDurationMinutes;

    private String lockKey(Long seatId) {
        return "seat-lock:" + seatId;
    }

    /**
     * Attempts to atomically acquire a lock for every seat in {@code seatIds}.
     * Uses Redis SETNX (setIfAbsent) per seat, which is atomic at the Redis level.
     * If ANY seat in the batch is already locked, all previously-acquired locks
     * in this call are rolled back so we never leave a partial hold behind.
     *
     * @return list of seatIds that could NOT be locked (empty list = full success)
     */
    public List<Long> tryLockSeats(List<Long> seatIds, String bookingReference) {
        List<Long> acquired = new ArrayList<>();
        List<Long> failed = new ArrayList<>();

        for (Long seatId : seatIds) {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey(seatId), bookingReference, Duration.ofMinutes(holdDurationMinutes));
            if (Boolean.TRUE.equals(success)) {
                acquired.add(seatId);
            } else {
                failed.add(seatId);
            }
        }

        if (!failed.isEmpty()) {
            // Roll back any locks we did manage to acquire in this batch attempt
            log.warn("Failed to lock seats {} for booking {}. Rolling back {} acquired locks.",
                    failed, bookingReference, acquired.size());
            releaseSeats(acquired);
            return failed;
        }

        log.info("Locked seats {} for booking {} (TTL {} min)", seatIds, bookingReference, holdDurationMinutes);
        return List.of();
    }

    /** Releases locks unconditionally (used on rollback / explicit cancellation). */
    public void releaseSeats(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            redisTemplate.delete(lockKey(seatId));
        }
    }

    /**
     * Releases a lock only if it still belongs to the given booking reference —
     * prevents accidentally releasing a lock that a *different* booking has since
     * legitimately acquired for the same seat after this one expired.
     */
    public boolean releaseIfOwnedBy(Long seatId, String bookingReference) {
        String current = redisTemplate.opsForValue().get(lockKey(seatId));
        if (bookingReference.equals(current)) {
            redisTemplate.delete(lockKey(seatId));
            return true;
        }
        return false;
    }

    public boolean isOwnedBy(Long seatId, String bookingReference) {
        String current = redisTemplate.opsForValue().get(lockKey(seatId));
        return bookingReference.equals(current);
    }
}
