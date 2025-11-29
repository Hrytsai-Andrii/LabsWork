package com.example.kursova.common.model;

import com.example.kursova.client.AudioConverter;
import com.example.kursova.common.patterns.memento.PlayerMemento;
import com.example.kursova.server.service.PlaylistService;
import com.example.kursova.server.service.TrackService;
import com.example.kursova.server.service.UserService;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicPlayer {
    private int volume;
    private boolean isPlaying;
    private boolean isRepeat;

    private Playable currentPlayable;
    private Track currentTrack;
    private User currentUser;
    private double startAtTime = 0.0;
    private Runnable onTrackChanged;

    // 2. Додайте цей метод (сеттер) десь внизу класу
    public void setOnTrackChanged(Runnable callback) {
        this.onTrackChanged = callback;
    }

    // --- НОВЕ ПОЛЕ ---
    private MediaPlayer mediaPlayer;

    private UserService userService;
    private TrackService trackService;
    private PlaylistService playlistService;

    public MusicPlayer(UserService userService, TrackService trackService, PlaylistService playlistService) {
        this.userService = userService;
        this.trackService = trackService;
        this.playlistService = playlistService;
        this.volume = 50;
        this.isPlaying = false;
    }

    public void play() {
        if (currentPlayable == null) {
            return;
        }

        if (currentTrack == null) {
            loadNextValidTrack();
        }

        if (currentTrack != null) {
            this.isPlaying = true;
            currentPlayable.play();

            if (mediaPlayer != null) {
                mediaPlayer.play();
            } else {
                playCurrentTrackInternal();
            }

            if (onTrackChanged != null) {
                onTrackChanged.run();
            }

        } else {
            stop();
        }
    }

    private void loadNextValidTrack() {
        int trackId = currentPlayable.next();
        while (trackId != -1) {
            Track trackFromLibrary = trackService.getTrackByID(trackId);
            if (trackFromLibrary != null) {
                this.currentTrack = trackFromLibrary;
                return;
            } else {
                trackId = currentPlayable.next();
            }
        }
        this.currentTrack = null;
    }

    public void pause() {
        if (isPlaying) {
            this.isPlaying = false;
            if (currentPlayable != null) currentPlayable.pause();

            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        }
    }

    private boolean isShuffleGlobal = false; // Глобальний стан плеєра

    public void setShuffle(boolean enable) {
        this.isShuffleGlobal = enable;
        if (currentPlayable != null) {
            currentPlayable.setShuffle(enable);
        }
    }

    public boolean isShuffle() {
        return isShuffleGlobal;
    }

    public void setPlayableSource(Playable playable) {
        this.currentPlayable = playable;
        if (this.currentPlayable != null) {
            this.currentPlayable.setShuffle(isShuffleGlobal);
        }
        stop();
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void stop() {
        this.isPlaying = false;
        this.currentTrack = null;
        if (currentPlayable != null) currentPlayable.stop();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose(); // Очищення пам'яті
            mediaPlayer = null;
        }
    }

    public void next() {
        if (currentPlayable != null) {
            loadNextValidTrack();
            if (currentTrack != null) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    mediaPlayer = null;
                }
                play();
            } else {
                if (isRepeat) {
                    currentPlayable.stop();
                    play();
                } else {
                    stop();
                }
            }
        }
    }

    public void previous() {
        if (currentPlayable != null) {
            int prevId = currentPlayable.previous();
            if (prevId != -1) {
                this.currentTrack = trackService.getTrackByID(prevId);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    mediaPlayer = null;
                }
                if (currentPlayable != null) {
                    currentPlayable.setCurrentTrack(this.currentTrack.getTrackID());
                }

                play();
            }
        }
    }

    public void setVolume(int volume) {
        this.volume = volume;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume / 100.0);
        }
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void toggleRepeat() {
        this.isRepeat = !this.isRepeat;
    }

    public void playTrack(Track track) {
        stop();
        this.currentTrack = track;

        // ВАЖЛИВО: Синхронізуємо ітератор з обраним треком
        if (currentPlayable != null) {
            currentPlayable.setCurrentTrack(track.getTrackID());
        }

        play();
    }

    public boolean isPlaying() { return isPlaying; }
    public User getCurrentUser() { return currentUser; }
    public void loginUser(int userId) { this.currentUser = userService.getUserByID(userId); }


    private void playCurrentTrackInternal() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            // --- ЗМІНА ПОЧИНАЄТЬСЯ ТУТ ---
            // Отримуємо URI через конвертер. Якщо це FLAC, отримаємо посилання на temp.wav
            String mediaSource = AudioConverter.getPlayableURI(currentTrack.getFilePath());

            if (mediaSource == null) {
                System.err.println("Не вдалося завантажити файл: " + currentTrack.getFilePath());
                next(); // Пропускаємо битий файл
                return;
            }

            Media media = new Media(mediaSource);
            // --- ЗМІНА ЗАКІНЧУЄТЬСЯ ТУТ ---

            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(this.volume / 100.0);

            mediaPlayer.setOnReady(() -> {
                if (startAtTime > 0) {
                    mediaPlayer.seek(javafx.util.Duration.seconds(startAtTime));
                    startAtTime = 0;
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                if (isRepeat) {
                    mediaPlayer.seek(javafx.util.Duration.ZERO);
                    mediaPlayer.play();
                } else {
                    next();
                }
            });

            mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
            next(); // Пропускаємо трек при помилці
        }
    }

    // 3. Оновіть saveState
    public PlayerMemento saveState() {
        int trackId = (currentTrack != null) ? currentTrack.getTrackID() : -1;

        int sourceType = 0;
        int sourceId = 0;
        if (currentPlayable instanceof Playlist) {
            sourceType = 1;
            sourceId = ((Playlist) currentPlayable).getPlaylistID();
        }

        // Отримуємо поточний час плеєра
        double currentTime = 0.0;
        if (mediaPlayer != null) {
            currentTime = mediaPlayer.getCurrentTime().toSeconds();
        }

        return new PlayerMemento(this.volume, this.isShuffleGlobal, this.isRepeat, trackId, sourceType, sourceId, currentTime);
    }

    // 4. Оновіть restoreState
    public void restoreState(PlayerMemento memento) {
        if (memento == null) return;

        this.setVolume(memento.getVolume());
        this.setShuffle(memento.isShuffle());
        if (this.isRepeat != memento.isRepeat()) this.toggleRepeat();

        // Відновлюємо джерело (Playlist або Library)
        if (memento.getSourceType() == 1) {
            Playlist pl = playlistService.getPlaylistByID(memento.getSourceId());
            if (pl != null) this.setPlayableSource(pl);
        } else {
            Library lib = new Library();
            if (currentUser != null) {
                trackService.getAllTracks(currentUser.getUserID()).forEach(lib::addTrack);
            }
            this.setPlayableSource(lib);
        }

        // Зберігаємо час, з якого треба почати
        this.startAtTime = memento.getCurrentTime();

        if (memento.getCurrentTrackId() != -1) {
            Track t = trackService.getTrackByID(memento.getCurrentTrackId());
            if (t != null) {
                this.playTrack(t);
                this.pause(); // Ставимо на паузу, щоб не грало само, але час вже встановлений у startAtTime
            }
        }
    }
}