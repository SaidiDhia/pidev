package com.example.pi_dev;

import com.example.pi_dev.session.Session;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Session.login(2L); //na77iha ta nwalli ena the other guy lanci ta tefhem
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/pi_dev/chat.fxml")
        );


        Scene scene = new Scene(loader.load(), 800, 600);
        stage.setTitle("WonderLust - Messaging");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
