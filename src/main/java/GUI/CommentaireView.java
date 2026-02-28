package GUI;

import Entities.Commentaire;
import Entities.Post;
import Services.Commentaire_Services;
import Services.Posting_Services;
import Iservices.IcommentaireServices;
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

public class CommentaireView {

    private IcommentaireServices commentaireService;
    private IpostServices postService;
    private TableView<Commentaire> tableView;
    private ObservableList<Commentaire> commentairesList;

    private TextArea txtContenu;
    private ComboBox<Post> cbPost;
    private Label lblCharCount;
    private Label lblValidation;

    private Label lblTotalCommentaires;

    private Commentaire selectedCommentaire = null;

    public CommentaireView() {
        commentaireService = new Commentaire_Services();
        postService = new Posting_Services();
        commentairesList = FXCollections.observableArrayList();
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        Label sectionTitle = new Label("Gestion des Commentaires");
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

        lblTotalCommentaires = new Label("0");

        statsBox.getChildren().addAll(
                createStatCard("Total Commentaires", lblTotalCommentaires, "#667eea")
        );

        return statsBox;
    }

    private VBox createStatCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250);
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

        Label formTitle = new Label("Formulaire Commentaire");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web("#333333"));

        Separator separator = new Separator();

        Label lblContenu = new Label("Contenu *");
        lblContenu.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        txtContenu = new TextArea();
        txtContenu.setPromptText("Ecrivez votre commentaire ici... (5-300 caracteres)");
        txtContenu.setPrefRowCount(4);
        txtContenu.setWrapText(true);
        txtContenu.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;"
        );

        lblCharCount = new Label("0/300");
        lblCharCount.setFont(Font.font("Arial", 11));
        lblCharCount.setTextFill(Color.web("#999999"));

        txtContenu.textProperty().addListener((obs, oldVal, newVal) -> {
            int length = newVal.length();
            lblCharCount.setText(length + "/300");

            if (length < 5) {
                lblCharCount.setTextFill(Color.web("#e53e3e"));
                txtContenu.setStyle(
                        "-fx-border-color: #e53e3e;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 10;"
                );
            } else if (length > 300) {
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

        Label lblPost = new Label("Post *");
        lblPost.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        cbPost = new ComboBox<>();
        cbPost.setPromptText("Selectionnez un post");
        cbPost.setPrefWidth(Double.MAX_VALUE);
        cbPost.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        // Charger les posts
        ObservableList<Post> posts = FXCollections.observableArrayList(postService.afficherPosts());
        cbPost.setItems(posts);
        cbPost.setConverter(new javafx.util.StringConverter<Post>() {
            @Override
            public String toString(Post post) {
                return post != null ? "Post #" + post.getIdPost() + " - " +
                        (post.getContenu().length() > 50 ? post.getContenu().substring(0, 50) + "..." : post.getContenu())
                        : "";
            }

            @Override
            public Post fromString(String string) {
                return null;
            }
        });

        lblValidation = new Label("");
        lblValidation.setFont(Font.font("Arial", FontWeight.MEDIUM, 12));
        lblValidation.setWrapText(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnAjouter = createStyledButton("Ajouter", "#667eea", "#764ba2");
        Button btnModifier = createStyledButton("Modifier", "#48bb78", "#38a169");
        Button btnAnnuler = createStyledButton("Annuler", "#718096", "#4a5568");

        btnAjouter.setOnAction(e -> ajouterCommentaire());
        btnModifier.setOnAction(e -> modifierCommentaire());
        btnAnnuler.setOnAction(e -> clearForm());

        buttonBox.getChildren().addAll(btnAjouter, btnModifier, btnAnnuler);

        formCard.getChildren().addAll(
                formTitle, separator,
                lblContenu, txtContenu, lblCharCount,
                lblPost, cbPost,
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

        Label tableTitle = new Label("Liste des Commentaires");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#333333"));

        Separator separator = new Separator();

        tableView = new TableView<>();
        tableView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-table-cell-border-color: #e0e0e0;"
        );

        TableColumn<Commentaire, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idCommentaire"));
        colId.setPrefWidth(50);

        TableColumn<Commentaire, String> colContenu = new TableColumn<>("Contenu");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setPrefWidth(350);

        TableColumn<Commentaire, Integer> colIdPost = new TableColumn<>("Post ID");
        colIdPost.setCellValueFactory(new PropertyValueFactory<>("idPost"));
        colIdPost.setPrefWidth(80);

        TableColumn<Commentaire, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(180);

        TableColumn<Commentaire, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(180);
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
                    Commentaire commentaire = getTableView().getItems().get(getIndex());
                    loadCommentaireToForm(commentaire);
                });

                btnDelete.setOnAction(e -> {
                    Commentaire commentaire = getTableView().getItems().get(getIndex());
                    supprimerCommentaire(commentaire);
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

        tableView.getColumns().addAll(colId, colContenu, colIdPost, colDate, colActions);
        tableView.setItems(commentairesList);

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

    private void ajouterCommentaire() {
        if (validateForm()) {
            Commentaire commentaire = new Commentaire(
                    txtContenu.getText().trim(),
                    cbPost.getValue().getIdPost()
            );

            commentaireService.ajouterCommentaire(commentaire);
            showValidationMessage("Commentaire ajoute avec succes!", true);
            clearForm();
            refreshTable();
            updateStatistics();
        }
    }

    private void modifierCommentaire() {
        if (selectedCommentaire != null && validateForm()) {
            selectedCommentaire.setContenu(txtContenu.getText().trim());
            selectedCommentaire.setIdPost(cbPost.getValue().getIdPost());

            commentaireService.modifierCommentaire(selectedCommentaire);
            showValidationMessage("Commentaire modifie avec succes!", true);
            clearForm();
            refreshTable();
            updateStatistics();
        } else {
            showValidationMessage("Veuillez selectionner un commentaire a modifier", false);
        }
    }

    private void supprimerCommentaire(Commentaire commentaire) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le commentaire");
        alert.setContentText("Etes-vous sur de vouloir supprimer ce commentaire?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                commentaireService.supprimerCommentaire(commentaire.getIdCommentaire());
                refreshTable();
                updateStatistics();
                showValidationMessage("Commentaire supprime avec succes!", true);
            }
        });
    }

    private void loadCommentaireToForm(Commentaire commentaire) {
        selectedCommentaire = commentaire;
        txtContenu.setText(commentaire.getContenu());

        // Sélectionner le post correspondant
        for (Post post : cbPost.getItems()) {
            if (post.getIdPost() == commentaire.getIdPost()) {
                cbPost.setValue(post);
                break;
            }
        }

        showValidationMessage("Commentaire charge pour modification", true);
    }

    private boolean validateForm() {
        String contenu = txtContenu.getText().trim();

        if (contenu.isEmpty()) {
            showValidationMessage("Le contenu est obligatoire!", false);
            return false;
        }

        if (contenu.length() < 5) {
            showValidationMessage("Le contenu doit contenir au moins 5 caracteres!", false);
            return false;
        }

        if (contenu.length() > 300) {
            showValidationMessage("Le contenu ne doit pas depasser 300 caracteres!", false);
            return false;
        }

        if (cbPost.getValue() == null) {
            showValidationMessage("Le post est obligatoire!", false);
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
        cbPost.setValue(null);
        lblValidation.setText("");
        lblCharCount.setText("0/300");
        lblCharCount.setTextFill(Color.web("#999999"));
        txtContenu.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;"
        );
        selectedCommentaire = null;
    }

    private void refreshTable() {
        commentairesList.clear();
        commentairesList.addAll(commentaireService.afficherCommentaires());
    }

    private void updateStatistics() {
        lblTotalCommentaires.setText(String.valueOf(commentairesList.size()));
    }
}