package com.picobase.persistence.resolver;



public class ResultCouple<T> {
    private final T result;

    private Error error = null;

    public ResultCouple(T result) {
        this.result = result;
    }



    public ResultCouple(T result, Error error) {
        this.result = result;
        this.error = error;
    }

    public T getResult() {
        return result;
    }

    public Error getError() {
        return error;
    }

    public ResultCouple<T> setError(Error error) {
        this.error = error;
        return this;
    }
}