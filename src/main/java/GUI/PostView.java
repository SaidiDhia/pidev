package GUI;

import Entities.Post;
import Services.Posting_Services;
import Iservices.IpostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;

public class PostView {

    private IpostServices postService;
    private TableView<Post> tableView;
    private ObservableList<Post> postsList;

    private TextArea txtContenu;
    private TextField txtMedia;
    private ComboBox<String> cbStatut;
    private Label lblCharCount;
    private Label lblValidation;

    private Label lblTotalPosts;
    private Label lblPublies;
    private Label lblBrouillons;
    private Label lblArchives;

    private Post selectedPost = null;

    public PostView() {
        postService = new Posting_Services();
        postsList = FXCollections.observableArrayList();
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        Label sectionTitle = new Label("Gestion des Posts");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        sectionTitle.setTextFill(Color.web("#333333"));

        HBox statsBox = createStatsBox();

        HBox contentBox = new HBox(20);
        contentBox.getChildren().addAll(createFormCard(), createTableCard());
        HBox.setHgrow(createTableCard(), Priority.ALWAYS);

        mainContainer.getChildren().addAll(sectionTitle, statsBox, contentBox);

        refreshTable();
        updateStatistics();

        return mainContainer;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);

        lblTotalPosts = new Label("0");
        lblPublies = new Label("0");
        lblBrouillons = new Label("0");
        lblArchives = new Label("0");

        statsBox.getChildren().addAll(
                createStatCard("Total Posts", lblTotalPosts, "#667eea"),
                createStatCard("Publies", lblPublies, "#48bb78"),
                createStatCard("Brouillons", lblBrouillons, "#ed8936"),
                createStatCard("Archives", lblArchives, "#718096")
        );

        return statsBox;
    }

    private VBox createStatCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));
        titleLabel.setTextFill(Color.web("#666666"));

        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox createFormCard() {
        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(25));
        formCard.setPrefWidth(400);
        formCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label formTitle = new Label("Formulaire Post");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web("#333333"));

        Separator separator = new Separator();

        Label lblContenu = new Label("Contenu *");
        lblContenu.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        txtContenu = new TextArea();
        txtContenu.setPromptText("Ecrivez votre post ici... (10-500 caracteres)");
        txtContenu.setPrefRowCount(5);
        txtContenu.setWrapText(true);
        txtContenu.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;"
        );

        lblCharCount = new Label("0/500");
        lblCharCount.setFont(Font.font("Arial", 11));
        lblCharCount.setTextFill(Color.web("#999999"));

        txtContenu.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            lblCharCount.setText(length + "/500");

            if (length < 10) {
                lblCharCount.setTextFill(Color.web("#e53e3e"));
                txtContenu.setStyle(
                        "-fx-border-color: #e53e3e;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 10;"
                );
            } else if (length > 500) {
                lblCharCount.setTextFill(Color.web("#e53e3e"));
                txtContenu.setStyle(
                        "-fx-border-color: #e53e3e;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 10;"
                );
            } else {
                lblCharCount.setTextFill(Color.web("#48bb78"));
                txtContenu.setStyle(
                        "-fx-border-color: #48bb78;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 10;"
                );
            }
        });

        Label lblMedia = new Label("Media (URL/Fichier)");
        lblMedia.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        txtMedia = new TextField();
        txtMedia.setPromptText("URL de l'image ou chemin du fichier");
        txtMedia.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;"
        );

        Label lblStatut = new Label("Statut *");
        lblStatut.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        cbStatut = new ComboBox<>();
        cbStatut.getItems().addAll("publie", "brouillon", "archive");
        cbStatut.setValue("brouillon");
        cbStatut.setPrefWidth(Double.MAX_VALUE);
        cbStatut.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        lblValidation = new Label("");
        lblValidation.setFont(Font.font("Arial", FontWeight.MEDIUM, 12));
        lblValidation.setWrapText(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnAjouter = createStyledButton("Ajouter", "#667eea", "#764ba2");
        Button btnModifier = createStyledButton("Modifier", "#48bb78", "#38a169");
        Button btnAnnuler = createStyledButton("Annuler", "#718096", "#4a5568");

        btnAjouter.setOnAction(e -> ajouterPost());
        btnModifier.setOnAction(e -> modifierPost());
        btnAnnuler.setOnAction(e -> clearForm());

        buttonBox.getChildren().addAll(btnAjouter, btnModifier, btnAnnuler);

        formCard.getChildren().addAll(
                formTitle, separator,
                lblContenu, txtContenu, lblCharCount,
                lblMedia, txtMedia,
                lblStatut, cbStatut,
                lblValidation,
                buttonBox
        );

        return formCard;
    }

    private VBox createTableCard() {
        VBox tableCard = new VBox(15);
        tableCard.setPadding(new Insets(25));
        tableCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label tableTitle = new Label("Liste des Posts");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#333333"));

        Separator separator = new Separator();

        tableView = new TableView<>();
        tableView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-table-cell-border-color: #e0e0e0;"
        );

        TableColumn<Post, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idPost"));
        colId.setPrefWidth(50);

        TableColumn<Post, String> colContenu = new TableColumn<>("Contenu");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setPrefWidth(300);

        TableColumn<Post, String> colMedia = new TableColumn<>("Media");
        colMedia.setCellValueFactory(new PropertyValueFactory<>("media"));
        colMedia.setPrefWidth(150);

        TableColumn<Post, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colDate.setPrefWidth(150);

        TableColumn<Post, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);

        TableColumn<Post, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");

            {
                btnEdit.setStyle(
                        "-fx-background-color: #667eea;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 5 10;"
                );
                btnDelete.setStyle(
                        "-fx-background-color: #e53e3e;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 5 10;"
                );

                btnEdit.setOnAction(e -> {
                    Post post = getTableView().getItems().get(getIndex());
                    loadPostToForm(post);
                });

                btnDelete.setOnAction(e -> {
                    Post post = getTableView().getItems().get(getIndex());
                    supprimerPost(post);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, btnEdit, btnDelete);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        tableView.getColumns().addAll(colId, colContenu, colMedia, colDate, colStatut, colActions);
        tableView.setItems(postsList);

        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableCard.getChildren().addAll(tableTitle, separator, tableView);

        return tableCard;
    }

    private Button createStyledButton(String text, String color1, String color2) {
        Button button = new Button(text);
        button.setPrefWidth(110);
        button.setPrefHeight(35);
        button.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));
        button.setStyle(
                "-fx-background-color: linear-gradient(to right, " + color1 + " 0%, " + color2 + " 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        return button;
    }

    private void ajouterPost() {
        if (validateForm()) {
            Post post = new Post(
                    txtContenu.getText().trim(),
                    txtMedia.getText().trim().isEmpty() ? null : txtMedia.getText().trim(),
                    cbStatut.getValue()
            );

            postService.ajouterPost(post);
            showValidationMessage("Post ajoute avec succes!", true);
            clearForm();
            refreshTable();
            updateStatistics();
        }
    }

    private void modifierPost() {
        if (selectedPost != null && validateForm()) {
            selectedPost.setContenu(txtContenu.getText().trim());
            selectedPost.setMedia(txtMedia.getText().trim().isEmpty() ? null : txtMedia.getText().trim());
            selectedPost.setStatut(cbStatut.getValue());

            postService.modifierPost(selectedPost);
            showValidationMessage("Post modifie avec succes!", true);
            clearForm();
            refreshTable();
            updateStatistics();
        } else {
            showValidationMessage("Veuillez selectionner un post a modifier", false);
        }
    }

    private void supprimerPost(Post post) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le post");
        alert.setContentText("Etes-vous sur de vouloir supprimer ce post?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                postService.supprimerPost(post.getIdPost());
                refreshTable();
                updateStatistics();
                showValidationMessage("Post supprime avec succes!", true);
            }
        });
    }

    private void loadPostToForm(Post post) {
        selectedPost = post;
        txtContenu.setText(post.getContenu());
        txtMedia.setText(post.getMedia() != null ? post.getMedia() : "");
        cbStatut.setValue(post.getStatut());
        showValidationMessage("Post charge pour modification", true);
    }

    private boolean validateForm() {
        String contenu = txtContenu.getText().trim();

        if (contenu.isEmpty()) {
            showValidationMessage("Le contenu est obligatoire!", false);
            return false;
        }

        if (contenu.length() < 10) {
            showValidationMessage("Le contenu doit contenir au moins 10 caracteres!", false);
            return false;
        }

        if (contenu.length() > 500) {
            showValidationMessage("Le contenu ne doit pas depasser 500 caracteres!", false);
            return false;
        }

        if (cbStatut.getValue() == null) {
            showValidationMessage("Le statut est obligatoire!", false);
            return false;
        }

        return true;
    }

    private void showValidationMessage(String message, boolean isSuccess) {
        lblValidation.setText(message);
        lblValidation.setTextFill(isSuccess ? Color.web("#48bb78") : Color.web("#e53e3e"));
    }

    private void clearForm() {
        txtContenu.clear();
        txtMedia.clear();
        cbStatut.setValue("brouillon");
        lblValidation.setText("");
        lblCharCount.setText("0/500");
        lblCharCount.setTextFill(Color.web("#999999"));
        txtContenu.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;"
        );
        selectedPost = null;
    }

    private void refreshTable() {
        postsList.clear();
        postsList.addAll(postService.afficherPosts());
    }

    private void updateStatistics() {
        int total = postsList.size();
        int publies = (int) postsList.stream().filter(p -> "publie".equals(p.getStatut())).count();
        int brouillons = (int) postsList.stream().filter(p -> "brouillon".equals(p.getStatut())).count();
        int archives = (int) postsList.stream().filter(p -> "archive".equals(p.getStatut())).count();

        lblTotalPosts.setText(String.valueOf(total));
        lblPublies.setText(String.valueOf(publies));
        lblBrouillons.setText(String.valueOf(brouillons));
        lblArchives.setText(String.valueOf(archives));
    }
}