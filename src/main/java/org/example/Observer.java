package org.example;

public interface Observer<T> {
    void onChange(T newData);
}
