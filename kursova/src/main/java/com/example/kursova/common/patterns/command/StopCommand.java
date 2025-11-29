package com.example.kursova.common.patterns.command;

import com.example.kursova.common.model.MusicPlayer;

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