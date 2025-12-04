package com.example.labs.common.patterns.command;

import com.example.labs.common.model.MusicPlayer;

public class PlayCommand implements Command {
    private MusicPlayer musicPlayer;

    public PlayCommand(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    @Override
    public void execute() {
        musicPlayer.play();
    }
}