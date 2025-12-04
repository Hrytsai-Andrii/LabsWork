package com.example.labs.common.patterns.memento;

import java.util.Stack;

public class Caretaker {
    private final Stack<PlayerMemento> history = new Stack<>();

    public void save(PlayerMemento memento) {
        history.push(memento);
    }

    public PlayerMemento undo() {
        if (!history.isEmpty()) {
            return history.pop();
        }
        return null;
    }

    public boolean hasHistory() {
        return !history.isEmpty();
    }
}