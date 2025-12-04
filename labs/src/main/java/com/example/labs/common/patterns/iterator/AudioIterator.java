package com.example.labs.common.patterns.iterator;

public interface AudioIterator<T> {
    boolean hasNext();
    T next();
    boolean hasPrevious();
    T previous();
    void setCursor(T element);
}
