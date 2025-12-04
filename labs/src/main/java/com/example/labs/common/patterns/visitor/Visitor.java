package com.example.labs.common.patterns.visitor;

import com.example.labs.common.model.Track;

public interface Visitor {
    void visitTrack(Track track);
}