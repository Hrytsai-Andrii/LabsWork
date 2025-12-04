package com.example.labs.common.model;

import javafx.collections.ObservableList;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.MediaPlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Equalizer implements Serializable {
    private List<Double> currentGains;
    private static final int BAND_COUNT = 10;

    public Equalizer() {
        this.currentGains = new ArrayList<>();
        for (int i = 0; i < BAND_COUNT; i++) {
            currentGains.add(0.0);
        }
    }

    public void attachToPlayer(MediaPlayer player) {
        if (player == null) return;

        player.getAudioEqualizer().setEnabled(true);
        ObservableList<EqualizerBand> bands = player.getAudioEqualizer().getBands();

        for (int i = 0; i < bands.size() && i < currentGains.size(); i++) {
            bands.get(i).setGain(currentGains.get(i));
        }
    }

    public void setBandGain(int index, double gain, MediaPlayer currentPlayer) {
        if (index >= 0 && index < currentGains.size()) {
            currentGains.set(index, gain);

            if (currentPlayer != null) {
                ObservableList<EqualizerBand> bands = currentPlayer.getAudioEqualizer().getBands();
                if (index < bands.size()) {
                    bands.get(index).setGain(gain);
                }
            }
        }
    }

    public List<Double> getGains() {
        return new ArrayList<>(currentGains);
    }

    public void restoreGains(List<Double> savedGains) {
        if (savedGains != null && !savedGains.isEmpty()) {
            for (int i = 0; i < savedGains.size() && i < currentGains.size(); i++) {
                currentGains.set(i, savedGains.get(i));
            }
        }
    }
}