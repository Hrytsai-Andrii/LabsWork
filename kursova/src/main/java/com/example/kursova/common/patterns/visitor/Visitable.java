package com.example.kursova.common.patterns.visitor;

public interface Visitable {
    void accept(Visitor visitor);
}