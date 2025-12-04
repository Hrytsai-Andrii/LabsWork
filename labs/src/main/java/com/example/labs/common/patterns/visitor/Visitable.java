package com.example.labs.common.patterns.visitor;

public interface Visitable {
    void accept(Visitor visitor);
}