package com.example.kursova.common.patterns.command;

import com.example.kursova.common.model.MusicPlayer;

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
