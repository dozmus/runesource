package com.rs.util;

public class OverflowException extends RuntimeException {

    private final int remainder;

    public OverflowException(int remainder) {
        super("Overflow by: " + remainder);
        this.remainder = remainder;
    }

    public int getRemainder() {
        return remainder;
    }
}
