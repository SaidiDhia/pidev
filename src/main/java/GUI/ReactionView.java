package GUI;

import Entities.Reaction;
import Entities.Post;
import Entities.Commentaire;
import Services.Reaction_Services;
import Services.Posting_Services;
import Services.Commentaire_Services;
import Iservices.IreactionServices;
import Iservices.IpostServices;
import Iservices.IcommentaireServices;
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

public class ReactionView {

    private IreactionServices reactionService;
    private IpostServices postService;
    private IcommentaireServices commentaireService;
    private TableView<Reaction> tableView;
    private ObservableList<Reaction> reactionsList;

    private ComboBox<String> cbType;
    private ComboBox<Post> cbPost;
    private ComboBox<Commentaire> cbCommentaire;
    private RadioButton rbPost;
    private RadioButton rbCommentaire;
    private Label lblValidation;

    private Label lblTotalReactions;
    private Label lblLikes;
    private Label lblLoves;
    private Label lblHahas;

    private Reaction selectedReaction = null;

    public ReactionView() {
        reactionService = new Reaction_Services();
        postService = new Posting_Services();
        commentaireService = new Commentaire_Services();
        reactionsList = FXCollections.observableArrayList();
    }

    public VBox getView() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        Label sectionTitle = new Label("Gestion des Reactions");
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

        lblTotalReactions = new Label("0");
        lblLikes = new Label("0");
        lblLoves = new Label("0");
        lblHahas = new Label("0");

        statsBox.getChildren().addAll(
                createStatCard("Total Reactions", lblTotalReactions, "#667eea"),
                createStatCard("Likes", lblLikes, "#3b82f6"),
                createStatCard("Loves", lblLoves, "#ef4444"),
                createStatCard("Hahas", lblHahas, "#f59e0b")
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

        Label formTitle = new Label("Formulaire Reaction");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        formTitle.setTextFill(Color.web("#333333"));

        Separator separator = new Separator();

        // Type de réaction
        Label lblType = new Label("Type de Reaction *");
        lblType.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        cbType = new ComboBox<>();
        cbType.getItems().addAll("LIKE", "LOVE", "HAHA", "WOW", "SAD", "ANGRY");
        cbType.setPromptText("Choisissez une reaction");
        cbType.setPrefWidth(Double.MAX_VALUE);
        cbType.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        // Choix entre Post ou Commentaire
        Label lblCible = new Label("Reagir sur *");
        lblCible.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        ToggleGroup toggleGroup = new ToggleGroup();

        rbPost = new RadioButton("Post");
        rbPost.setToggleGroup(toggleGroup);
        rbPost.setSelected(true);
        rbPost.setFont(Font.font("Arial", 12));

        rbCommentaire = new RadioButton("Commentaire");
        rbCommentaire.setToggleGroup(toggleGroup);
        rbCommentaire.setFont(Font.font("Arial", 12));

        HBox radioBox = new HBox(20, rbPost, rbCommentaire);

        // ComboBox Post
        Label lblPost = new Label("Post");
        lblPost.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        cbPost = new ComboBox<>();
        cbPost.setPromptText("Selectionnez un post");
        cbPost.setPrefWidth(Double.MAX_VALUE);
        cbPost.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        ObservableList<Post> posts = FXCollections.observableArrayList(postService.afficherPosts());
        cbPost.setItems(posts);
        cbPost.setConverter(new javafx.util.StringConverter<Post>() {
            @Override
            public String toString(Post post) {
                return post != null ? "Post #" + post.getIdPost() + " - " +
                        (post.getContenu().length() > 40 ? post.getContenu().substring(0, 40) + "..." : post.getContenu())
                        : "";
            }

            @Override
            public Post fromString(String string) {
                return null;
            }
        });

        // ComboBox Commentaire
        Label lblCommentaire = new Label("Commentaire");
        lblCommentaire.setFont(Font.font("Arial", FontWeight.MEDIUM, 13));

        cbCommentaire = new ComboBox<>();
        cbCommentaire.setPromptText("Selectionnez un commentaire");
        cbCommentaire.setPrefWidth(Double.MAX_VALUE);
        cbCommentaire.setStyle(
                "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );
        cbCommentaire.setDisable(true);

        ObservableList<Commentaire> commentaires = FXCollections.observableArrayList(commentaireService.afficherCommentaires());
        cbCommentaire.setItems(commentaires);
        cbCommentaire.setConverter(new javafx.util.StringConverter<Commentaire>() {
            @Override
            public String toString(Commentaire commentaire) {
                return commentaire != null ? "Comment #" + commentaire.getIdCommentaire() + " - " +
                        (commentaire.getContenu().length() > 40 ? commentaire.getContenu().substring(0, 40) + "..." : commentaire.getContenu())
                        : "";
            }

            @Override
            public Commentaire fromString(String string) {
                return null;
            }
        });

        // Gestion du RadioButton
        rbPost.setOnAction(e -> {
            cbPost.setDisable(false);
            cbCommentaire.setDisable(true);
            cbCommentaire.setValue(null);
        });

        rbCommentaire.setOnAction(e -> {
            cbPost.setDisable(true);
            cbCommentaire.setDisable(false);
            cbPost.setValue(null);
        });

        lblValidation = new Label("");
        lblValidation.setFont(Font.font("Arial", FontWeight.MEDIUM, 12));
        lblValidation.setWrapText(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnAjouter = createStyledButton("Ajouter", "#667eea", "#764ba2");
        Button btnModifier = createStyledButton("Modifier", "#48bb78", "#38a169");
        Button btnAnnuler = createStyledButton("Annuler", "#718096", "#4a5568");

        btnAjouter.setOnAction(e -> ajouterReaction());
        btnModifier.setOnAction(e -> modifierReaction());
        btnAnnuler.setOnAction(e -> clearForm());

        buttonBox.getChildren().addAll(btnAjouter, btnModifier, btnAnnuler);

        formCard.getChildren().addAll(
                formTitle, separator,
                lblType, cbType,
                lblCible, radioBox,
                lblPost, cbPost,
                lblCommentaire, cbCommentaire,
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

        Label tableTitle = new Label("Liste des Reactions");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setTextFill(Color.web("#333333"));

        Separator separator = new Separator();

        tableView = new TableView<>();
        tableView.setStyle(
                "-fx-background-color: white;" +
                        "-fx-table-cell-border-color: #e0e0e0;"
        );

        TableColumn<Reaction, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idReaction"));
        colId.setPrefWidth(50);

        TableColumn<Reaction, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(100);

        TableColumn<Reaction, Integer> colIdPost = new TableColumn<>("Post ID");
        colIdPost.setCellValueFactory(new PropertyValueFactory<>("idPost"));
        colIdPost.setPrefWidth(80);

        TableColumn<Reaction, Integer> colIdCommentaire = new TableColumn<>("Comment ID");
        colIdCommentaire.setCellValueFactory(new PropertyValueFactory<>("idCommentaire"));
        colIdCommentaire.setPrefWidth(100);

        TableColumn<Reaction, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(180);

        TableColumn<Reaction, Void> colActions = new TableColumn<>("Actions");
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
                    Reaction reaction = getTableView().getItems().get(getIndex());
                    loadReactionToForm(reaction);
                });

                btnDelete.setOnAction(e -> {
                    Reaction reaction = getTableView().getItems().get(getIndex());
                    supprimerReaction(reaction);
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

        tableView.getColumns().addAll(colId, colType, colIdPost, colIdCommentaire, colDate, colActions);
        tableView.setItems(reactionsList);

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

    private void ajouterReaction() {
        if (validateForm()) {
            Reaction reaction;

            if (rbPost.isSelected()) {
                reaction = new Reaction(
                        cbType.getValue(),
                        cbPost.getValue().getIdPost(),
                        null
                );
            } else {
                reaction = new Reaction(
                        cbType.getValue(),
                        null,
                        cbCommentaire.getValue().getIdCommentaire()
                );
            }

            reactionService.ajouterReaction(reaction);
            showValidationMessage("Reaction ajoutee avec succes!", true);
            clearForm();
            refreshTable();
            updateStatistics();
        }
    }

    private void modifierReaction() {
        if (selectedReaction != null && validateForm()) {
            selectedReaction.setType(cbType.getValue());

            if (rbPost.isSelected()) {
                selectedReaction.setIdPost(cbPost.getValue().getIdPost());
                selectedReaction.setIdCommentaire(null);
            } else {
                selectedReaction.setIdPost(null);
                selectedReaction.setIdCommentaire(cbCommentaire.getValue().getIdCommentaire());
            }

            reactionService.modifierReaction(selectedReaction);
            showValidationMessage("Reaction modifiee avec succes!", true);
            clearForm();
            refreshTable();
            updateStatistics();
        } else {
            showValidationMessage("Veuillez selectionner une reaction a modifier", false);
        }
    }

    private void supprimerReaction(Reaction reaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la reaction");
        alert.setContentText("Etes-vous sur de vouloir supprimer cette reaction?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reactionService.supprimerReaction(reaction.getIdReaction());
                refreshTable();
                updateStatistics();
                showValidationMessage("Reaction supprimee avec succes!", true);
            }
        });
    }

    private void loadReactionToForm(Reaction reaction) {
        selectedReaction = reaction;
        cbType.setValue(reaction.getType());

        if (reaction.getIdPost() != null) {
            rbPost.setSelected(true);
            cbPost.setDisable(false);
            cbCommentaire.setDisable(true);

            for (Post post : cbPost.getItems()) {
                if (post.getIdPost() == reaction.getIdPost()) {
                    cbPost.setValue(post);
                    break;
                }
            }
        } else if (reaction.getIdCommentaire() != null) {
            rbCommentaire.setSelected(true);
            cbPost.setDisable(true);
            cbCommentaire.setDisable(false);

            for (Commentaire commentaire : cbCommentaire.getItems()) {
                if (commentaire.getIdCommentaire() == reaction.getIdCommentaire()) {
                    cbCommentaire.setValue(commentaire);
                    break;
                }
            }
        }

        showValidationMessage("Reaction chargee pour modification", true);
    }

    private boolean validateForm() {
        if (cbType.getValue() == null) {
            showValidationMessage("Le type de reaction est obligatoire!", false);
            return false;
        }

        if (rbPost.isSelected() && cbPost.getValue() == null) {
            showValidationMessage("Le post est obligatoire!", false);
            return false;
        }

        if (rbCommentaire.isSelected() && cbCommentaire.getValue() == null) {
            showValidationMessage("Le commentaire est obligatoire!", false);
            return false;
        }

        return true;
    }

    private void showValidationMessage(String message, boolean isSuccess) {
        lblValidation.setText(message);
        lblValidation.setTextFill(isSuccess ? Color.web("#48bb78") : Color.web("#e53e3e"));
    }

    private void clearForm() {
        cbType.setValue(null);
        cbPost.setValue(null);
        cbCommentaire.setValue(null);
        rbPost.setSelected(true);
        cbPost.setDisable(false);
        cbCommentaire.setDisable(true);
        lblValidation.setText("");
        selectedReaction = null;
    }

    private void refreshTable() {
        reactionsList.clear();
        reactionsList.addAll(reactionService.afficherReactions());
    }

    private void updateStatistics() {
        int total = reactionsList.size();
        int likes = (int) reactionsList.stream().filter(r -> "LIKE".equals(r.getType())).count();
        int loves = (int) reactionsList.stream().filter(r -> "LOVE".equals(r.getType())).count();
        int hahas = (int) reactionsList.stream().filter(r -> "HAHA".equals(r.getType())).count();

        lblTotalReactions.setText(String.valueOf(total));
        lblLikes.setText(String.valueOf(likes));
        lblLoves.setText(String.valueOf(loves));
        lblHahas.setText(String.valueOf(hahas));
    }
}