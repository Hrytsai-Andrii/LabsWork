package com.example.labs.common.patterns.command;

import com.example.labs.common.model.MusicPlayer;

public class PreviousCommand implements Command {
    private MusicPlayer musicPlayer;

    public PreviousCommand(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    @Override
    public void execute() {
        musicPlayer.previous();
    }
}
