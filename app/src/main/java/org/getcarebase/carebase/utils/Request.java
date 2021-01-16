package org.getcarebase.carebase.utils;

/**
 * An immutable object that contains information about a request made
 * ie. updating an item's quantity
 */
public class Request {
    public enum Status {
        SUCCESS,
        LOADING,
        ERROR
    }

    private final Integer resourceString;
    private final Status status;

    public Request(Integer resourceString, Status status) {
        this.resourceString = resourceString;
        this.status = status;
    }

    public Integer getResourceString() { return resourceString; }

    public Status getStatus() {
        return this.status;
    }
}
