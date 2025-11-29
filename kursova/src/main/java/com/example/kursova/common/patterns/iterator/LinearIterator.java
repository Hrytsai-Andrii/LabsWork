package com.example.kursova.common.patterns.iterator;

import java.util.List;

public class LinearIterator<T> implements AudioIterator<T> {
    private List<T> list;
    private int index = -1;

    public LinearIterator(List<T> list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return !list.isEmpty() && index < list.size() - 1;
    }

    @Override
    public T next() {
        if (!hasNext()) return null;
        index++;
        return list.get(index);
    }

    @Override
    public boolean hasPrevious() {
        return !list.isEmpty() && index > 0;
    }

    @Override
    public T previous() {
        if (!hasPrevious()) return null;
        index--;
        return list.get(index);
    }

    @Override
    public void setCursor(T element) {
        this.index = list.indexOf(element);
    }
}
