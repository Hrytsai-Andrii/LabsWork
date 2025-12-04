package com.example.labs.client;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

public class AudioConverter {
    public static String getPlayableURI(String filePath) {
        if (filePath.toLowerCase().endsWith(".flac")) {
            return convertFlacToWav(filePath);
        }
        return new File(filePath).toURI().toString();
    }

    private static String convertFlacToWav(String flacPath) {
        try {
            File flacFile = new File(flacPath);
            File tempWav = File.createTempFile("converted_track_", ".wav");
            tempWav.deleteOnExit();

            AudioInputStream flacStream = AudioSystem.getAudioInputStream(flacFile);

            AudioSystem.write(flacStream, AudioFileFormat.Type.WAVE, tempWav);

            flacStream.close();
            return tempWav.toURI().toString();
        } catch (Exception e) {
            System.err.println("Помилка конвертації FLAC: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}