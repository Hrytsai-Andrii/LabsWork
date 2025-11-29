package com.example.kursova.common.patterns.command;

import com.example.kursova.common.model.MusicPlayer;

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