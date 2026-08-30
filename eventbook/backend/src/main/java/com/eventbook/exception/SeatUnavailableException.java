package com.eventbook.exception;

/** Thrown when one or more requested seats are already held or booked by someone else. */
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
