package com.example.kursova.common.patterns.command;

import com.example.kursova.common.model.MusicPlayer;

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
