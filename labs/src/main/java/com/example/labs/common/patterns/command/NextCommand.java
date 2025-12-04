package com.example.labs.common.patterns.command;

import com.example.labs.common.model.MusicPlayer;

public class NextCommand implements Command {
    private MusicPlayer musicPlayer;

    public NextCommand(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    @Override
    public void execute() {
        musicPlayer.next();
    }
}
