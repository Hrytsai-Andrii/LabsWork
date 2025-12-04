package com.example.labs.common.patterns.visitor;

import com.example.labs.common.model.Track;

public class DurationVisitor implements Visitor {
    private double totalDurationInSeconds = 0;
    private int trackCount = 0;

    @Override
    public void visitTrack(Track track) {
        if (track != null) {
            totalDurationInSeconds += track.getDuration();
            trackCount++;
        }
    }

    public double getTotalDurationInSeconds() {
        return totalDurationInSeconds;
    }

    public String getFormattedDuration() {
        int hours = (int) (totalDurationInSeconds / 3600);
        int remainder = (int) (totalDurationInSeconds % 3600);
        int minutes = remainder / 60;
        int seconds = remainder % 60;

        if (hours > 0) {
            return String.format("%d год %d хв %d сек", hours, minutes, seconds);
        } else {
            return String.format("%d хв %d сек", minutes, seconds);
        }
    }

    public int getTrackCount() {
        return trackCount;
    }
}