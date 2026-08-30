package com.eventbook.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatLockServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SeatLockService seatLockService;

    @BeforeEach
    void setUp() {
        seatLockService = new SeatLockService(redisTemplate);
        ReflectionTestUtils.setField(seatLockService, "holdDurationMinutes", 5L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void tryLockSeats_allAvailable_returnsEmptyFailureList() {
        when(valueOperations.setIfAbsent(anyString(), eq("booking:1"), any(Duration.class)))
                .thenReturn(true);

        List<Long> failed = seatLockService.tryLockSeats(List.of(1L, 2L, 3L), "booking:1");

        assertTrue(failed.isEmpty());
        verify(valueOperations, times(3)).setIfAbsent(anyString(), eq("booking:1"), any(Duration.class));
    }

    @Test
    void tryLockSeats_oneSeatAlreadyLocked_rollsBackAllAndReturnsFailures() {
        when(valueOperations.setIfAbsent(eq("seat-lock:1"), eq("booking:2"), any(Duration.class))).thenReturn(true);
        when(valueOperations.setIfAbsent(eq("seat-lock:2"), eq("booking:2"), any(Duration.class))).thenReturn(false); // taken

        List<Long> failed = seatLockService.tryLockSeats(List.of(1L, 2L), "booking:2");

        assertEquals(List.of(2L), failed);
        // Rollback: seat 1's lock, which WAS acquired, must be released since the batch failed overall
        verify(redisTemplate, times(1)).delete("seat-lock:1");
    }

    @Test
    void releaseIfOwnedBy_correctOwner_releasesAndReturnsTrue() {
        when(valueOperations.get("seat-lock:5")).thenReturn("booking:9");

        boolean released = seatLockService.releaseIfOwnedBy(5L, "booking:9");

        assertTrue(released);
        verify(redisTemplate).delete("seat-lock:5");
    }

    @Test
    void releaseIfOwnedBy_differentOwner_doesNotReleaseAndReturnsFalse() {
        when(valueOperations.get("seat-lock:5")).thenReturn("booking:OTHER");

        boolean released = seatLockService.releaseIfOwnedBy(5L, "booking:9");

        assertFalse(released);
        verify(redisTemplate, never()).delete(anyString());
    }
}
