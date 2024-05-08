package com.picobase.model;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Store<T> {
    private Map<String, T> data = new ConcurrentHashMap<>(50);

    public Store() {
    }

    public Store(Map<String, T> data) {
        this.reset(data);
    }

    public void reset(Map<String, T> newData) {
        data.clear();
        newData.forEach((k, v) -> this.set(k, v));
    }

    public int length() {
        return data.size();
    }

    public void removeAll() {
        data.clear();
    }

    public void remove(String key) {
        data.remove(key);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public T get(String key) {
        return data.getOrDefault(key, null);
    }

    /**
     * GetAll returns a shallow copy of the current store data.
     */
    public Map<String, T> getAll() {
        return new ConcurrentHashMap<>(data);
    }

    public void set(String key, T value) {
        data.put(key, Objects.isNull(value) ? (T) "" : value);
    }

    public boolean setIfLessThanLimit(String key, T value, int maxAllowedElements) {
        if (!data.containsKey(key) && data.size() >= maxAllowedElements) {
            return false;
        }
        data.put(key, value);
        return true;
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public boolean isEmpty() {
        return this.length() == 0;
    }

}
