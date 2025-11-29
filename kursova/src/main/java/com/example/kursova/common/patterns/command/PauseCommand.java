package com.example.kursova.common.patterns.command;

import com.example.kursova.common.model.MusicPlayer;

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
