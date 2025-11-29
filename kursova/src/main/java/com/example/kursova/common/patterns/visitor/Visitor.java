package com.example.kursova.common.patterns.visitor;

import com.example.kursova.common.model.Track;

public interface Visitor {
    void visitTrack(Track track);
}