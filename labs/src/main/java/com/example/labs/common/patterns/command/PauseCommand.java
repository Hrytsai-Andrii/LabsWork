package com.example.labs.common.patterns.command;

import com.example.labs.common.model.MusicPlayer;

public class PauseCommand implements Command {
    private MusicPlayer musicPlayer;

    public PauseCommand(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    @Override
    public void execute() {
        musicPlayer.pause();
    }
}
