package GUI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Container principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Header avec gradient violet
        VBox header = createHeader();
        root.setTop(header);

        // Menu de navigation
        VBox navigationMenu = createNavigationMenu(root);
        root.setLeft(navigationMenu);

        // Vue par défaut : Posts
        root.setCenter(new PostView().getView());

        // Scene
        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setTitle("Gestion Blog - Wonderlust");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );

        Label title = new Label("📝 Gestion de Blog");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Gérez vos posts, commentaires et réactions");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.web("#e0e0e0"));

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createNavigationMenu(BorderPane root) {
        VBox menu = new VBox(15);
        menu.setPadding(new Insets(20));
        menu.setPrefWidth(220);
        menu.setStyle(
                "-fx-background-color: white;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 2, 0);"
        );

        Label menuTitle = new Label("Navigation");
        menuTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        menuTitle.setTextFill(Color.web("#333333"));

        Separator separator = new Separator();

        Button btnPosts = createMenuButton("📄 Posts", true);
        Button btnComments = createMenuButton("💬 Commentaires", false);
        Button btnReactions = createMenuButton("❤️ Réactions", false);

        // Actions des boutons
        btnPosts.setOnAction(e -> {
            root.setCenter(new PostView().getView());
            updateMenuButtonStyles(btnPosts, btnComments, btnReactions);
        });

        btnComments.setOnAction(e -> {
            root.setCenter(new CommentaireView().getView());
            updateMenuButtonStyles(btnComments, btnPosts, btnReactions);
        });

        btnReactions.setOnAction(e -> {
            root.setCenter(new ReactionView().getView());
            updateMenuButtonStyles(btnReactions, btnPosts, btnComments);
        });

        menu.getChildren().addAll(menuTitle, separator, btnPosts, btnComments, btnReactions);
        return menu;
    }

    private Button createMenuButton(String text, boolean active) {
        Button button = new Button(text);
        button.setPrefWidth(180);
        button.setPrefHeight(45);
        button.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));
        button.setAlignment(Pos.CENTER_LEFT);

        if (active) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 8, 0, 0, 2);"
            );
        } else {
            button.setStyle(
                    "-fx-background-color: #f0f0f0;" +
                            "-fx-text-fill: #333333;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );
        }

        button.setOnMouseEntered(e -> {
            if (!button.getStyle().contains("667eea")) {
                button.setStyle(
                        "-fx-background-color: #e8e8e8;" +
                                "-fx-text-fill: #333333;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        button.setOnMouseExited(e -> {
            if (!button.getStyle().contains("667eea")) {
                button.setStyle(
                        "-fx-background-color: #f0f0f0;" +
                                "-fx-text-fill: #333333;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        return button;
    }

    private void updateMenuButtonStyles(Button activeButton, Button... inactiveButtons) {
        activeButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(102,126,234,0.4), 8, 0, 0, 2);"
        );

        for (Button btn : inactiveButtons) {
            btn.setStyle(
                    "-fx-background-color: #f0f0f0;" +
                            "-fx-text-fill: #333333;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}