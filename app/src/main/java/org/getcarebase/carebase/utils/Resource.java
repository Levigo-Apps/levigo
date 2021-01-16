package org.getcarebase.carebase.utils;

/**
 * A immutable wrapper class that carries information about the data requested
 * @param <T> data's type
 */
public class Resource<T> {
    private T data;
    private Request request;

    public Resource (T data, Request request) {
        this.data = data;
        this.request = request;
    }

    public T getData() {
        return data;
    }

    public Request getRequest() {
        return request;
    }
}

