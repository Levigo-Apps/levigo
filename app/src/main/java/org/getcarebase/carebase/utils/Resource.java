package org.getcarebase.carebase.utils;

/**
 * A wrapper class that carries information about the data requested
 * @param <T> data type
 */
public class Resource<T> {
    public enum Status {
        SUCCESS,
        LOADING,
        ERROR
    }

    public T data = null;
    public Integer resourceString = null;
    public Status status;

    public Resource (T data, Integer resourceString, Status status) {
        this.data = data;
        this.resourceString = resourceString;
        this.status = status;
    }
}

