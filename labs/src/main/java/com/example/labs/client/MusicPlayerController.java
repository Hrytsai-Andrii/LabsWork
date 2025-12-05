package com.example.labs.client;

import com.example.labs.common.model.Library;
import com.example.labs.common.model.MusicPlayer;
import com.example.labs.common.model.Playlist;
import com.example.labs.common.model.Track;
import com.example.labs.common.model.User;
import com.example.labs.common.patterns.command.*;
import com.example.labs.common.patterns.memento.Caretaker;
import com.example.labs.common.patterns.memento.PlayerMemento;
import com.example.labs.common.patterns.visitor.DurationVisitor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MusicPlayerController {

    private final MusicPlayerView view;
    private MusicPlayer musicPlayer;
    private Caretaker caretaker = new Caretaker();

    private final MusicSystemFacade systemFacade;

    private Library mainLibrary;

    public MusicPlayerController(MusicPlayerView view) {
        this.view = view;
        this.systemFacade = new MusicSystemFacade();
        this.musicPlayer = systemFacade.getPlayer();
        this.musicPlayer.setOnTrackChanged(() -> updateStatusUI());

        attachEventHandlers();
        refreshData();
        loadLastState();
    }

    private void attachEventHandlers() {
        Command playCmd = new PlayCommand(musicPlayer);
        Command pauseCmd = new PauseCommand(musicPlayer);
        Command stopCmd = new StopCommand(musicPlayer);
        Command nextCmd = new NextCommand(musicPlayer);
        Command prevCmd = new PreviousCommand(musicPlayer);

        view.getPlayBtn().setOnAction(e -> {
            playCmd.execute();
            updateStatusUI();
        });
        view.getPauseBtn().setOnAction(e -> {
            pauseCmd.execute();
            updateStatusUI();
        });
        view.getStopBtn().setOnAction(e -> {
            stopCmd.execute();
            updateStatusUI();
        });
        view.getNextBtn().setOnAction(e -> {
            caretaker.save(musicPlayer.saveState());
            nextCmd.execute();
            updateStatusUI();
        });
        view.getPrevBtn().setOnAction(e -> {
            prevCmd.execute();
            updateStatusUI();
        });

        view.getVolumeSlider().valueProperty().addListener((obs, oldVal, newVal) ->
                musicPlayer.setVolume(newVal.intValue()));

        view.getRefreshBtn().setOnAction(e -> refreshData());

        view.getPlayLibraryBtn().setOnAction(e -> {
            musicPlayer.setPlayableSource(mainLibrary);
            musicPlayer.play();
            updateStatusUI();
        });

        view.getAddFileBtn().setOnAction(e -> handleAddTrack());
        view.getAddToPlaylistBtn().setOnAction(e -> handleAddToPlaylist());
        view.getCreatePlaylistBtn().setOnAction(e -> handleCreatePlaylist());

        view.getRepeatBtn().setOnAction(e -> {
            musicPlayer.toggleRepeat();
            if (musicPlayer.isRepeat()) view.getRepeatBtn().setStyle("-fx-base: #b6e7c9;");
            else view.getRepeatBtn().setStyle("");
        });

        view.getShuffleBtn().setOnAction(e -> {
            boolean newState = !musicPlayer.isShuffle();
            musicPlayer.setShuffle(newState);
            if (newState) view.getShuffleBtn().setStyle("-fx-base: #b6e7c9;");
            else view.getShuffleBtn().setStyle("");
        });

        if (view.getLoginBtn() != null) view.getLoginBtn().setOnAction(e -> handleLogin());
        if (view.getRegisterBtn() != null) view.getRegisterBtn().setOnAction(e -> handleRegister());
        if (view.getLogoutBtn() != null) view.getLogoutBtn().setOnAction(e -> handleLogout());

        view.getLibraryTable().setRowFactory(tv -> {
            TableRow<Track> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Редагувати");
            MenuItem deleteItem = new MenuItem("Видалити");

            editItem.setOnAction(event -> handleEditTrack(row.getItem()));
            deleteItem.setOnAction(event -> handleDeleteTrack(row.getItem()));

            contextMenu.getItems().addAll(editItem, deleteItem);
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    Track selectedTrack = row.getItem();
                    musicPlayer.setPlayableSource(mainLibrary);
                    musicPlayer.playTrack(selectedTrack);
                    updateStatusUI();
                }
            });
            return row;
        });

        view.getPlaylistListView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Playlist selectedPlaylist = view.getPlaylistListView().getSelectionModel().getSelectedItem();
                if (selectedPlaylist != null) {
                    musicPlayer.setPlayableSource(selectedPlaylist);
                    musicPlayer.play();
                    updateStatusUI();
                }
            }
        });

        view.setOnEditPlaylist(playlist -> {
            TextInputDialog dialog = new TextInputDialog(playlist.getName());
            dialog.setTitle("Редагування плейлиста");
            dialog.setHeaderText("Змінити назву плейлиста");
            dialog.setContentText("Нова назва:");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(newName -> {
                if (!newName.trim().isEmpty()) {
                    playlist.setName(newName);
                    systemFacade.getPlaylistService().updatePlaylist(playlist);
                    refreshData();
                }
            });
        });

        view.setOnDeletePlaylist(playlist -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Видалення плейлиста");
            alert.setHeaderText("Видалити плейлист '" + playlist.getName() + "'?");
            alert.setContentText("Цю дію неможливо скасувати.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (musicPlayer.getCurrentUser() != null) musicPlayer.stop();
                systemFacade.getPlaylistService().deletePlaylist(playlist.getPlaylistID());
                refreshData();
            }
        });

        view.setTrackLoader(playlist -> {
            List<Track> tracksInPlaylist = new ArrayList<>();
            for (Integer trackId : playlist.getTracks()) {
                Track t = systemFacade.getTrackService().getTrackByID(trackId);
                if (t != null) tracksInPlaylist.add(t);
            }
            return tracksInPlaylist;
        });

        view.setOnPlayTrackFromPlaylist((playlist, track) -> {
            musicPlayer.setPlayableSource(playlist);
            musicPlayer.playTrack(track);
            updateStatusUI();
        });

        view.setOnAddTrackToPlaylist(playlist -> {
            List<Track> allTracks = systemFacade.getCurrentUserTracks();
            Track selectedTrack = view.showTrackSelectionDialog(allTracks);

            if (selectedTrack != null) {
                if (playlist.getTracks().contains(selectedTrack.getTrackID())) {
                    view.showAlert("Увага", "Трек вже є у плейлисті.");
                } else {
                    playlist.addTrack(selectedTrack.getTrackID());
                    systemFacade.getPlaylistService().updatePlaylist(playlist);
                    refreshData();
                }
            }
        });

        view.setDurationCalculator(playlist -> {
            DurationVisitor visitor = new DurationVisitor();
            List<Integer> trackIds = playlist.getTracks();
            for (Integer id : trackIds) {
                Track track = systemFacade.getTrackService().getTrackByID(id);
                if (track != null) {
                    track.accept(visitor);
                }
            }
            return visitor.getFormattedDuration();
        });

        view.setOnDeleteTrackFromPlaylist((playlist, track) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Видалення з плейлиста");
            alert.setHeaderText("Видалити '" + track.getTitle() + "' з плейлиста '" + playlist.getName() + "'?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                playlist.removeTrack(track.getTrackID());
                systemFacade.getPlaylistService().updatePlaylist(playlist);
                refreshData();
            }
        });

        List<Slider> sliders = view.getEqSliders();
        for (int i = 0; i < sliders.size(); i++) {
            final int index = i;
            sliders.get(i).valueProperty().addListener((obs, oldVal, newVal) -> {
                musicPlayer.updateEqBand(index, newVal.doubleValue());
            });
        }
    }

    private void refreshData() {
        if (musicPlayer.getCurrentUser() == null) return;


        Platform.runLater(() -> {
            view.updateAuthUI(musicPlayer.getCurrentUser());
        });



        List<Track> tracks = systemFacade.getCurrentUserTracks();

        Platform.runLater(() -> {
            view.getLibraryTable().setItems(FXCollections.observableArrayList(tracks));
            mainLibrary = new Library();
            tracks.forEach(mainLibrary::addTrack);
        });

        List<Playlist> playlists = systemFacade.getCurrentUserPlaylists();

        Platform.runLater(() -> {
            view.getPlaylistListView().setItems(FXCollections.observableArrayList(playlists));
            view.getStatusLabel().setText((musicPlayer.isPlaying() ? "Грає..." : "Пауза"));
        });
    }

    private void updateStatusUI() {

        Platform.runLater(() -> {
            if (musicPlayer.isPlaying()) {
                view.getStatusLabel().setText("Відтворення");
                view.getStatusLabel().setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
            } else {
                view.getStatusLabel().setText("Зупинено");
                view.getStatusLabel().setStyle("-fx-text-fill: black; -fx-font-size: 12px;");
            }

            Track current = musicPlayer.getCurrentTrack();
            if (current != null) {
                String artist = (current.getArtist() == null || current.getArtist().isEmpty()) ?
                        "Невідомий" : current.getArtist();
                String title = (current.getTitle() == null || current.getTitle().isEmpty()) ? "Без назви" : current.getTitle();
                view.getCurrentTrackLabel().setText(artist + " - " + title);
            } else {
                view.getCurrentTrackLabel().setText("Немає активного треку");
            }
        });
    }

    private void handleLogin() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Вхід");
        dialog.setHeaderText("Введіть Email та пароль");
        ButtonType loginButtonType = new ButtonType("Увійти", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField email = new TextField(); email.setPromptText("Email");
        PasswordField password = new PasswordField(); password.setPromptText("Password");

        grid.add(new Label("Email:"), 0, 0); grid.add(email, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(password, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) return new Pair<>(email.getText(), password.getText());
            return null;
        });

        dialog.showAndWait().ifPresent(credentials -> {
            saveCurrentUserState();

            User user = systemFacade.authenticate(credentials.getKey(), credentials.getValue());
            if (user != null) {
                musicPlayer.stop();
                refreshData();
                loadLastState();
            } else {
                view.showAlert("Помилка", "Невірний логін або пароль");
            }
        });
        updateStatusUI();
    }

    private void handleRegister() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Реєстрація");
        dialog.setHeaderText("Створіть новий акаунт");
        ButtonType regButtonType = new ButtonType("Реєстрація", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField name = new TextField(); name.setPromptText("Ім'я");
        TextField email = new TextField(); email.setPromptText("Email");
        PasswordField password = new PasswordField(); password.setPromptText("Password");
        grid.add(new Label("Ім'я:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(email, 1, 1);
        grid.add(new Label("Password:"), 0, 2); grid.add(password, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == regButtonType) return new String[]{name.getText(), email.getText(), password.getText()};
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            saveCurrentUserState();

            User newUser = systemFacade.register(data[0], data[1], data[2]);
            if (newUser != null) {
                musicPlayer.stop();
                refreshData();
                loadLastState();
            } else {
                view.showAlert("Помилка", "Email вже використовується.");
            }
        });
        updateStatusUI();
    }

    private void handleLogout() {
        saveCurrentUserState();
        systemFacade.logout();
        refreshData();
        loadLastState();
        updateStatusUI();
    }

    private void handleAddTrack() {
        File file = view.showFileChooser();
        if (file != null) {
            Dialog<Track> dialog = new Dialog<>();
            dialog.setTitle("Додати трек");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);

            TextField titleField = new TextField(file.getName().replaceFirst("[.][^.]+$", ""));
            TextField artistField = new TextField("Невідомий");
            TextField albumField = new TextField("Невідомий");

            grid.add(new Label("Назва:"), 0, 0); grid.add(titleField, 1, 0);
            grid.add(new Label("Виконавець:"), 0, 1); grid.add(artistField, 1, 1);
            grid.add(new Label("Альбом:"), 0, 2); grid.add(albumField, 1, 2);

            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return new Track(0,
                            titleField.getText(),
                            artistField.getText(),
                            albumField.getText(),
                            0.0,
                            file.getAbsolutePath());
                }
                return null;
            });
            Optional<Track> result = dialog.showAndWait();
            result.ifPresent(this::saveTrackWithDuration);
        }
    }

    private void saveTrackWithDuration(Track track) {

        javafx.concurrent.Task<String> conversionTask = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {

                return AudioConverter.getPlayableURI(track.getFilePath());
            }
        };


        conversionTask.setOnSucceeded(event -> {
            String uri = conversionTask.getValue();

            if (uri == null) {

                saveTrackToDB(track);
                return;
            }

            try {
                Media media = new Media(uri);
                MediaPlayer tempPlayer = new MediaPlayer(media);

                tempPlayer.setOnReady(() -> {
                    track.setDuration(media.getDuration().toSeconds());
                    tempPlayer.dispose();
                    saveTrackToDB(track);
                });

                tempPlayer.setOnError(() -> {
                    System.err.println("Media error: " + tempPlayer.getError());
                    tempPlayer.dispose();
                    saveTrackToDB(track);
                });

            } catch (Exception e) {
                e.printStackTrace();
                saveTrackToDB(track);
            }
        });


        conversionTask.setOnFailed(event -> {
            Throwable e = conversionTask.getException();
            System.err.println("Помилка у фоновому потоці конвертації: " + e.getMessage());
            saveTrackToDB(track);
        });


        new Thread(conversionTask).start();
    }

    private void saveTrackToDB(Track track) {

        systemFacade.addTrackToLibrary(track);
        refreshData();
    }

    private void handleAddToPlaylist() {
        Track selectedTrack = view.getLibraryTable().getSelectionModel().getSelectedItem();
        if (selectedTrack == null) {
            view.showAlert("Помилка", "Виберіть трек!");
            return;
        }

        List<Playlist> playlists = systemFacade.getCurrentUserPlaylists();
        if (playlists.isEmpty()) {
            view.showAlert("Помилка", "Немає плейлистів.");
            return;
        }

        ChoiceDialog<Playlist> dialog = new ChoiceDialog<>(playlists.get(0), playlists);
        dialog.setTitle("Додати до плейлиста");
        dialog.setHeaderText("Додати: " + selectedTrack.getTitle());
        dialog.setContentText("Плейлист:");

        dialog.showAndWait().ifPresent(playlist -> {
            playlist.addTrack(selectedTrack.getTrackID());
            systemFacade.getPlaylistService().updatePlaylist(playlist);
            refreshData();
        });
    }

    private void handleCreatePlaylist() {
        String name = view.getNewPlaylistNameField().getText();
        if (!name.isEmpty()) {
            Playlist pl = new Playlist(0, name);

            Playlist createdPl = systemFacade.getPlaylistService().createPlaylist(pl);


            if (musicPlayer.getCurrentUser() != null && createdPl != null) {
                systemFacade.getPlaylistService().linkPlaylistToUser(
                        musicPlayer.getCurrentUser().getUserID(),
                        createdPl.getPlaylistID()
                );
            }

            view.getNewPlaylistNameField().clear();
            refreshData();
        }
    }

    private void handleEditTrack(Track track) {
        if (track == null) return;
        Dialog<Track> dialog = new Dialog<>();
        dialog.setTitle("Редагувати трек");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField titleField = new TextField(track.getTitle());
        TextField artistField = new TextField(track.getArtist());
        TextField albumField = new TextField(track.getAlbum());

        grid.add(new Label("Назва:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Виконавець:"), 0, 1); grid.add(artistField, 1, 1);
        grid.add(new Label("Альбом:"), 0, 2); grid.add(albumField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                track.setTitle(titleField.getText());
                track.setArtist(artistField.getText());
                track.setAlbum(albumField.getText());
                return track;
            }
            return null;
        });

        Optional<Track> result = dialog.showAndWait();
        result.ifPresent(updatedTrack -> {
            systemFacade.getTrackService().updateTrack(updatedTrack);
            refreshData();
        });
    }

    private void handleDeleteTrack(Track track) {
        if (track == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Видалення треку");
        alert.setHeaderText("Видалити трек?");
        alert.setContentText(track.getArtist() + " - " + track.getTitle());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (musicPlayer.getCurrentTrack() != null && musicPlayer.getCurrentTrack().equals(track)) {
                musicPlayer.stop();
            }
            systemFacade.getTrackService().deleteTrack(track.getTrackID());
            refreshData();
        }
    }

    private void loadLastState() {
        if (musicPlayer.getCurrentUser() == null) return;
        PlayerMemento savedState = systemFacade.loadState(musicPlayer.getCurrentUser().getUserID());

        if (savedState != null) {
            musicPlayer.restoreState(savedState);
            view.getVolumeSlider().setValue(savedState.getVolume());

            if (savedState.isShuffle()) view.getShuffleBtn().setStyle("-fx-base: #b6e7c9;");
            else view.getShuffleBtn().setStyle("");

            if (savedState.isRepeat()) view.getRepeatBtn().setStyle("-fx-base: #b6e7c9;");
            else view.getRepeatBtn().setStyle("");

            List<Double> savedEq = savedState.getEqBands(); // Отримуємо збережені частоти
            List<Slider> uiSliders = view.getEqSliders();   // Отримуємо слайдери з інтерфейсу

            if (savedEq != null && !savedEq.isEmpty() && uiSliders.size() == savedEq.size()) {
                for (int i = 0; i < uiSliders.size(); i++) {
                    // Встановлюємо повзунок у збережену позицію
                    uiSliders.get(i).setValue(savedEq.get(i));
                }
            }

            updateStatusUI();
        } else {
            resetPlayerToDefaults();
        }
    }
    private void resetPlayerToDefaults() {
        // 1. Скидаємо гучність
        musicPlayer.setVolume(50);
        view.getVolumeSlider().setValue(50);

        // 2. Скидаємо кнопки режимів
        musicPlayer.setShuffle(false);
        view.getShuffleBtn().setStyle("");

        // Якщо repeat був увімкнений, вимикаємо (через toggle або напряму, якщо є сетер)
        if (musicPlayer.isRepeat()) musicPlayer.toggleRepeat();
        view.getRepeatBtn().setStyle("");

        // 3. Скидаємо Еквалайзер (І логіку, і UI)
        List<Slider> uiSliders = view.getEqSliders();
        for (int i = 0; i < uiSliders.size(); i++) {
            uiSliders.get(i).setValue(0.0); // Скидаємо UI в 0
            musicPlayer.updateEqBand(i, 0.0); // Скидаємо логіку плеєра в 0
        }

        updateStatusUI();
    }

    private void saveCurrentUserState() {
        if (musicPlayer.getCurrentUser() != null) {
            PlayerMemento currentState = musicPlayer.saveState();

            systemFacade.saveState(musicPlayer.getCurrentUser().getUserID(), currentState);
        }
    }

    public void onAppClose() {
        saveCurrentUserState();
        musicPlayer.stop();
        Platform.exit();
    }
}