package org.reliability.generic;

public interface Observer<T> {
    void onChange(T newData);
}
