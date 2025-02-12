package com.legv8.simulator.response;

/**
 * <code>ResultWrapper</code> is an implementation of the Results pattern.
 * Used as a wrapper for methods that can return either a success or a failure.
 *
 * @see CPUSnapshot
 * @see LineError
 *
 * @author Rodrigo Bautista Hernández, 2025
 */
public class ResultWrapper<T, E> {
    private final T value;
    private final E error;


    private ResultWrapper(T value, E error) {
        this.value = value;
        this.error = error;
    }

    public static <T,E> ResultWrapper<T,E> success (T value) {
        return new ResultWrapper<>(value, null);
    }

    public static <T,E> ResultWrapper<T,E> failure (E error) {
        return new ResultWrapper<>(null, error);
    }

    public boolean isSuccess() {
        return value != null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T getValue() {
        return value;
    }

    public E getError() {
        return error;
    }
}
