package com.example.kursova.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusicPlayerApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        MusicPlayerView view = new MusicPlayerView();
        MusicPlayerController controller = new MusicPlayerController(view);

        Scene scene = new Scene(view.getView(), 800, 600);
        primaryStage.setTitle("Music Player");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            controller.onAppClose(); // Зберігаємо стан в БД
            javafx.application.Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}