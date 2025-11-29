package com.example.kursova.common.patterns.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleIterator<T> implements AudioIterator<T> {
    private List<T> shuffledTracks;
    private int currentPosition = -1;

    public ShuffleIterator(List<T> originalTracks) {
        this.shuffledTracks = new ArrayList<>(originalTracks);
        Collections.shuffle(this.shuffledTracks);
    }

    @Override
    public boolean hasNext() {
        return !shuffledTracks.isEmpty() && currentPosition < shuffledTracks.size() - 1;
    }

    @Override
    public T next() {
        if (hasNext()) {
            currentPosition++;
            return shuffledTracks.get(currentPosition);
        }
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return !shuffledTracks.isEmpty() && currentPosition > 0;
    }

    @Override
    public T previous() {
        if (hasPrevious()) {
            currentPosition--;
            return shuffledTracks.get(currentPosition);
        }
        return null;
    }

    @Override
    public void setCursor(T element) {
        this.currentPosition = shuffledTracks.indexOf(element);
    }
}