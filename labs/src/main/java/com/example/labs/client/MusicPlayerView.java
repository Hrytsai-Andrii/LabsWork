package com.example.labs.client;

import com.example.labs.common.model.Playlist;
import com.example.labs.common.model.Track;
import com.example.labs.common.model.User;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class MusicPlayerView {

    private BorderPane root;
    private List<Slider> eqSliders = new ArrayList<>();
    private Label statusLabel;
    private Label currentTrackLabel;

    private Label userNameLabel; 
    private Button loginBtn;
    private Button registerBtn;
    private Button logoutBtn;

    private TableView<Track> libraryTable;
    private ListView<Playlist> playlistListView;

    private Slider volumeSlider;
    private Button playBtn, pauseBtn, stopBtn, nextBtn, prevBtn;
    private Button repeatBtn, shuffleBtn;

    private Button refreshBtn, playLibraryBtn, addFileBtn, addToPlaylistBtn;
    private Button createPlaylistBtn, playPlaylistBtn;
    private TextField newPlaylistNameField;

    private Function<Playlist, List<Track>> trackLoader;
    private BiConsumer<Playlist, Track> onPlayTrackFromPlaylist;
    private Consumer<Playlist> onAddTrackToPlaylist;
    private Consumer<Playlist> onEditPlaylist;
    private Consumer<Playlist> onDeletePlaylist;
    private Function<Playlist, String> durationCalculator;
    private BiConsumer<Playlist, Track> onDeleteTrackFromPlaylist;

    public MusicPlayerView() {
        createUI();
    }

    public Parent getView() {
        return root;
    }

    private void createUI() {
        root = new BorderPane();
        root.setPadding(new Insets(10));

        
        VBox topContainer = new VBox(10);
        topContainer.setPadding(new Insets(0, 0, 10, 0));
        topContainer.setAlignment(Pos.CENTER);

        HBox authBox = new HBox(10);
        authBox.setAlignment(Pos.CENTER_RIGHT);

        userNameLabel = new Label();
        userNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-font-size: 14px; -fx-padding: 0 10 0 0;");

        loginBtn = new Button("Вхід");
        registerBtn = new Button("Реєстрація");
        logoutBtn = new Button("Вихід");

        loginBtn.setStyle("-fx-base: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        registerBtn.setStyle("-fx-base: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
        logoutBtn.setStyle("-fx-base: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

        authBox.getChildren().addAll(userNameLabel, loginBtn, registerBtn, logoutBtn);

        currentTrackLabel = new Label("Немає активного треку");
        currentTrackLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        statusLabel = new Label("Статус: Зупинено");
        statusLabel.setStyle("-fx-font-size: 12px;");

        topContainer.getChildren().addAll(authBox, currentTrackLabel, statusLabel);
        root.setTop(topContainer);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Бібліотека", createLibraryTab()),
                new Tab("Плейлисти", createPlaylistTab()),
                new Tab("Еквалайзер", createEqualizerTab()) // Нова вкладка
        );
        tabPane.getTabs().forEach(t -> t.setClosable(false));
        root.setCenter(tabPane);

        root.setBottom(createControlsBox());
    }

    
    public void updateAuthUI(User currentUser) {
        
        boolean isDefaultUser = (currentUser.getUserID() == 1);

        if (isDefaultUser) {
            userNameLabel.setVisible(false);
            userNameLabel.setManaged(false);
            logoutBtn.setVisible(false);
            logoutBtn.setManaged(false);
            loginBtn.setVisible(true);
            loginBtn.setManaged(true);
            registerBtn.setVisible(true);
            registerBtn.setManaged(true);
        } else {
            userNameLabel.setText(currentUser.getName());
            userNameLabel.setVisible(true);
            userNameLabel.setManaged(true);
            logoutBtn.setVisible(true);
            logoutBtn.setManaged(true);
            loginBtn.setVisible(true);
            loginBtn.setManaged(true);
            registerBtn.setVisible(false);
            registerBtn.setManaged(false);
        }
    }

    private VBox createLibraryTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        HBox buttonBox = new HBox(10);
        playLibraryBtn = new Button("▶ Грати бібліотеку");
        playLibraryBtn.setStyle("-fx-base: #b6e7c9;");

        addFileBtn = new Button("➕ Додати трек");
        addToPlaylistBtn = new Button("📂 У плейлист...");
        refreshBtn = new Button("🔄 Оновити");

        buttonBox.getChildren().addAll(playLibraryBtn, addFileBtn, addToPlaylistBtn, refreshBtn);
        libraryTable = new TableView<>();

        TableColumn<Track, String> titleCol = new TableColumn<>("Назва");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Track, String> artistCol = new TableColumn<>("Виконавець");
        artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));

        TableColumn<Track, Double> durationCol = getTrackDoubleTableColumn();

        libraryTable.getColumns().addAll(titleCol, artistCol, durationCol);
        libraryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(libraryTable, Priority.ALWAYS);

        box.getChildren().addAll(buttonBox, libraryTable);
        return box;
    }

    private static TableColumn<Track, Double> getTrackDoubleTableColumn() {
        TableColumn<Track, Double> durationCol = new TableColumn<>("Тривалість");
        durationCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDuration()));
        durationCol.setCellFactory(column -> new TableCell<Track, Double>() {
            @Override
            protected void updateItem(Double seconds, boolean empty) {
                super.updateItem(seconds, empty);
                if (empty || seconds == null) {
                    setText(null);
                } else {
                    int min = (int) (seconds / 60);
                    int sec = (int) (seconds % 60);
                    setText(String.format("%d:%02d", min, sec));
                }
            }
        });
        return durationCol;
    }

    private VBox createPlaylistTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f4f4f4;");

        playlistListView = new ListView<>();
        playlistListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #f4f4f4;");
        VBox.setVgrow(playlistListView, Priority.ALWAYS);

        playlistListView.setCellFactory(param -> new ListCell<>() {
            private final TableView<Track> innerTable = new TableView<>();
            private final VBox root = new VBox(0);
            private final HBox cardHeader = new HBox(10);
            private boolean isExpanded = false;

            {
                cardHeader.setAlignment(Pos.CENTER_LEFT);
                cardHeader.setPadding(new Insets(10));
                cardHeader.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1); -fx-cursor: hand;");

                TableColumn<Track, String> titleCol = new TableColumn<>("Назва");
                titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

                TableColumn<Track, String> artistCol = new TableColumn<>("Виконавець");
                artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));

                TableColumn<Track, String> timeCol = new TableColumn<>("Час");
                timeCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("%d:%02d", (int) data.getValue().getDuration() / 60, (int) data.getValue().getDuration() % 60)));
                timeCol.setPrefWidth(60);

                innerTable.getColumns().addAll(titleCol, artistCol, timeCol);
                innerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                innerTable.setPrefHeight(200);
                innerTable.setVisible(false);
                innerTable.setManaged(false);

                innerTable.setRowFactory(tv -> {
                    TableRow<Track> row = new TableRow<>();
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem deleteItem = new MenuItem("Видалити з плейлиста");

                    deleteItem.setOnAction(event -> {
                        Track track = row.getItem();
                        
                        if (track != null && getItem() != null && onDeleteTrackFromPlaylist != null) {
                            onDeleteTrackFromPlaylist.accept(getItem(), track);
                        }
                    });

                    contextMenu.getItems().add(deleteItem);

                    row.contextMenuProperty().bind(
                            javafx.beans.binding.Bindings.when(row.emptyProperty())
                                    .then((ContextMenu) null)
                                    .otherwise(contextMenu)
                    );

                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && (!row.isEmpty()) && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                            Track selectedTrack = row.getItem();
                            if (onPlayTrackFromPlaylist != null && getItem() != null) {
                                onPlayTrackFromPlaylist.accept(getItem(), selectedTrack);
                            }
                        }
                    });

                    return row;
                });

                root.getChildren().addAll(cardHeader, innerTable);
            }

            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    cardHeader.getChildren().clear();

                    Label icon = new Label(isExpanded ? "📂" : "📁");
                    icon.setStyle("-fx-font-size: 24px; -fx-text-fill: #555;");

                    VBox info = new VBox(3);
                    Label nameLabel = new Label(item.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

                    String infoText = "Треків: " + item.getTracks().size();
                    if (durationCalculator != null) {
                        infoText += " • " + durationCalculator.apply(item);
                    }
                    Label detailsLabel = new Label(infoText);
                    detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
                    info.getChildren().addAll(nameLabel, detailsLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button editBtn = createIconButton("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z", "#95a5a6", "#f39c12", e -> {
                        if (onEditPlaylist != null) onEditPlaylist.accept(item);
                    });
                    Button deleteBtn = createIconButton("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z", "#95a5a6", "#e74c3c", e -> {
                        if (onDeletePlaylist != null) onDeletePlaylist.accept(item);
                    });
                    Button addBtn = createIconButton("M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z", "#95a5a6", "#2ecc71", e -> {
                        if (onAddTrackToPlaylist != null) onAddTrackToPlaylist.accept(item);
                    });

                    Label arrow = new Label(isExpanded ? "▲" : "▼");
                    arrow.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-padding: 0 0 0 10;");

                    cardHeader.getChildren().addAll(icon, info, spacer, addBtn, editBtn, deleteBtn, arrow);

                    cardHeader.setOnMouseClicked(event -> {
                        if (event.getTarget() instanceof Button || event.getTarget() instanceof SVGPath) return;
                        isExpanded = !isExpanded;
                        updateTableVisibility(item, icon, arrow);
                    });

                    if (isExpanded) {
                        updateTableVisibility(item, icon, arrow);
                    }

                    innerTable.setVisible(isExpanded);
                    innerTable.setManaged(isExpanded);
                    setGraphic(root);
                }
            }

            private void updateTableVisibility(Playlist item, Label icon, Label arrow) {
                innerTable.setVisible(isExpanded);
                innerTable.setManaged(isExpanded);
                icon.setText(isExpanded ? "📂" : "📁");
                arrow.setText(isExpanded ? "▲" : "▼");
                if (isExpanded && trackLoader != null) {
                    List<Track> tracks = trackLoader.apply(item);
                    innerTable.setItems(FXCollections.observableArrayList(tracks));
                }
            }

            private Button createIconButton(String svgPath, String colorDefault, String colorHover, Consumer<ActionEvent> action) {
                Button btn = new Button();
                SVGPath icon = new SVGPath();
                icon.setContent(svgPath);
                icon.setFill(Color.web(colorDefault));
                btn.setGraphic(icon);
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0 5 0 5;");
                btn.setOnMouseEntered(e -> icon.setFill(Color.web(colorHover)));
                btn.setOnMouseExited(e -> icon.setFill(Color.web(colorDefault)));
                btn.setOnAction(e -> {
                    action.accept(e);
                    e.consume();
                });
                return btn;
            }
        });

        HBox createBox = new HBox(10);
        createBox.setAlignment(Pos.CENTER);
        createBox.setPadding(new Insets(15));
        createBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #ddd; -fx-border-radius: 10; -fx-border-style: dashed;");

        newPlaylistNameField = new TextField();
        newPlaylistNameField.setPromptText("Назва нового плейлиста...");
        HBox.setHgrow(newPlaylistNameField, Priority.ALWAYS);

        createPlaylistBtn = new Button("Створити");
        createBox.getChildren().addAll(newPlaylistNameField, createPlaylistBtn);

        box.getChildren().addAll(playlistListView, createBox);
        return box;
    }

    private HBox createControlsBox() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");

        shuffleBtn = new Button("🔀");
        prevBtn = new Button("⏮");
        stopBtn = new Button("⏹");
        playBtn = new Button("▶");
        playBtn.setStyle("-fx-font-weight: bold; -fx-base: #b6e7c9;");
        pauseBtn = new Button("⏸");
        nextBtn = new Button("⏭");
        repeatBtn = new Button("🔁");

        volumeSlider = new Slider(0, 100, 50);

        box.getChildren().addAll(
                shuffleBtn,
                prevBtn,
                stopBtn,
                playBtn,
                pauseBtn,
                nextBtn,
                repeatBtn,
                new Label("Vol:"), volumeSlider);
        return box;
    }

    public Track showTrackSelectionDialog(List<Track> allTracks) {
        Dialog<Track> dialog = new Dialog<>();
        dialog.setTitle("Додати трек до плейлиста");
        dialog.setHeaderText("Виберіть трек зі списку");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TableView<Track> table = new TableView<>();
        TableColumn<Track, String> colTitle = new TableColumn<>("Назва");
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        TableColumn<Track, String> colArtist = new TableColumn<>("Виконавець");
        colArtist.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArtist()));

        table.getColumns().addAll(colTitle, colArtist);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(FXCollections.observableArrayList(allTracks));
        table.setPrefHeight(300);
        table.setPrefWidth(400);

        dialog.getDialogPane().setContent(table);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return table.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        return dialog.showAndWait().orElse(null);
    }

    public File showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть аудіофайл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav", "*.flac"));
        return fileChooser.showOpenDialog(root.getScene().getWindow());
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private VBox createEqualizerTab() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #f4f4f4;");

        HBox slidersBox = new HBox(15);
        slidersBox.setAlignment(Pos.CENTER);

        // Стандартні частоти (10 смуг)
        String[] freqs = {"32", "64", "125", "250", "500", "1k", "2k", "4k", "8k", "16k"};

        for (String freq : freqs) {
            VBox band = new VBox(5);
            band.setAlignment(Pos.CENTER);

            Slider slider = new Slider(-12, 12, 0); // Gain від -12dB до +12dB
            slider.setOrientation(Orientation.VERTICAL);
            slider.setShowTickMarks(true);
            slider.setShowTickLabels(false);
            slider.setPrefHeight(200);

            eqSliders.add(slider);

            Label label = new Label(freq);
            label.setStyle("-fx-font-size: 10px;");

            band.getChildren().addAll(slider, label);
            slidersBox.getChildren().add(band);
        }

        Label title = new Label("Налаштування звуку (dB)");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        box.getChildren().addAll(title, slidersBox);
        return box;
    }

    // Геттер для контролера:
    public List<Slider> getEqSliders() { return eqSliders; }

    public void setTrackLoader(Function<Playlist, List<Track>> trackLoader) {
        this.trackLoader = trackLoader;
    }

    public void setOnPlayTrackFromPlaylist(BiConsumer<Playlist, Track> onPlayTrackFromPlaylist) {
        this.onPlayTrackFromPlaylist = onPlayTrackFromPlaylist;
    }

    public void setOnAddTrackToPlaylist(Consumer<Playlist> onAddTrackToPlaylist) {
        this.onAddTrackToPlaylist = onAddTrackToPlaylist;
    }

    public void setOnEditPlaylist(Consumer<Playlist> onEditPlaylist) {
        this.onEditPlaylist = onEditPlaylist;
    }

    public void setOnDeletePlaylist(Consumer<Playlist> onDeletePlaylist) {
        this.onDeletePlaylist = onDeletePlaylist;
    }

    public void setDurationCalculator(Function<Playlist, String> durationCalculator) {
        this.durationCalculator = durationCalculator;
    }

    public void setOnDeleteTrackFromPlaylist(BiConsumer<Playlist, Track> callback) {
        this.onDeleteTrackFromPlaylist = callback;
    }

    public TableView<Track> getLibraryTable() { return libraryTable; }
    public ListView<Playlist> getPlaylistListView() { return playlistListView; }
    public Label getStatusLabel() { return statusLabel; }
    public Label getCurrentTrackLabel() { return currentTrackLabel; }
    public Slider getVolumeSlider() { return volumeSlider; }
    public TextField getNewPlaylistNameField() { return newPlaylistNameField; }

    public Button getPlayBtn() { return playBtn; }
    public Button getPauseBtn() { return pauseBtn; }
    public Button getStopBtn() { return stopBtn; }
    public Button getNextBtn() { return nextBtn; }
    public Button getPrevBtn() { return prevBtn; }
    public Button getRefreshBtn() { return refreshBtn; }
    public Button getPlayLibraryBtn() { return playLibraryBtn; }
    public Button getAddFileBtn() { return addFileBtn; }
    public Button getAddToPlaylistBtn() { return addToPlaylistBtn; }
    public Button getCreatePlaylistBtn() { return createPlaylistBtn; }
    public Button getPlayPlaylistBtn() { return playPlaylistBtn; }
    public Button getRepeatBtn() { return repeatBtn; }
    public Button getShuffleBtn() { return shuffleBtn; }

    public Button getLoginBtn() { return loginBtn; }
    public Button getRegisterBtn() { return registerBtn; }
    public Button getLogoutBtn() { return logoutBtn; }
}