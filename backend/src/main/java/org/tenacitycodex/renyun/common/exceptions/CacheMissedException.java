package org.tenacitycodex.renyun.common.exceptions;

public class CacheMissedException extends RuntimeException {
    public CacheMissedException(String message) {
        super(message);
    }
}
