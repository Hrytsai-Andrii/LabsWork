package com.example.labs.common.patterns.command;

import com.example.labs.common.model.MusicPlayer;

public class StopCommand implements Command {
    private MusicPlayer musicPlayer;

    public StopCommand(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    @Override
    public void execute() {
        musicPlayer.stop();
    }
}