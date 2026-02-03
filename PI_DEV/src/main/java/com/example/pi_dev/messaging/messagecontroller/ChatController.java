package com.example.pi_dev.messaging.messagecontroller;

import com.example.pi_dev.messaging.messagingmodel.Conversation;
import com.example.pi_dev.messaging.messagingmodel.Message;
import com.example.pi_dev.messaging.messagingrepository.ConversationRepository;
import com.example.pi_dev.messaging.messagingrepository.MessageRepository;

import com.example.pi_dev.messaging.messagingsession.Session;
import javafx.fxml.FXML;
import javafx.geometry.Side; //hethy le side button
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.sql.SQLException;

public class ChatController {

    @FXML
    private ListView<Conversation> conversationList;
    @FXML
    private ListView<Message> messageList;

    @FXML
    private TextField messageInput;

    private final ConversationRepository conversationRepo = new ConversationRepository();
    private final MessageRepository messageRepo = new MessageRepository();

    private Conversation selectedConversation;

    @FXML
    public void initialize() {
        loadConversations();
        conversationList.setFixedCellSize(50);//same
        messageList.setFixedCellSize(-1); //addec these as an ui improvement on 2/2 also
        //Zeyda zeda When no conversation selected:
        messageList.setPlaceholder(
                new Label("Select a conversation to start chatting")
        );
        //zeyeda 5taer 9ale9 When no messages:
        messageList.setPlaceholder(
                new Label("No messages yet 👋")
        );

        conversationList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectedConversation = newVal;
                    loadMessages();
                });
        //I added this later after the message oine it contains the convos and alows me to add css
        conversationList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation c, boolean empty) {
                super.updateItem(c, empty);

                if (empty || c == null) {
                    setGraphic(null);
                    return;
                }

                Label title = new Label("Conversation " + c.getId());
                title.getStyleClass().add("conv-title");

                Label subtitle = new Label(c.getContextType());
                subtitle.getStyleClass().add("conv-subtitle");

                VBox box = new VBox(title, subtitle);
                box.setSpacing(3);

                setGraphic(box);
            }
        });

        messageList.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Button menuBtn = new Button("⋮");//add the button change or delete
                menuBtn.getStyleClass().add("menu-button");

                //this is the actual menue and buttons it contains
                ContextMenu menu = new ContextMenu();

                MenuItem edit = new MenuItem("Edit");
                MenuItem delete = new MenuItem("Delete");

                menu.getItems().addAll(edit, delete);
                menuBtn.setOnAction(e -> menu.show(menuBtn, Side.BOTTOM, 0, 0));

                //the option to delete lezemha persmission 5ater hethy 5demha hard delete donc ahaya persission
                boolean isMine = msg.getSenderId() == Session.getCurrentUserId();
                menuBtn.setVisible(isMine);

                // Header: username + time
                Label header = new Label(
                        "User " + msg.getSenderId() + " • " +
                                msg.getCreatedAt().toLocalTime().withNano(0)
                );
                header.getStyleClass().add("message-header");
                //hethia function ta delete w lklem li feha houa lUI ta pop up message de confirmation
                delete.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete message");
                    confirm.setHeaderText("Are you sure?");
                    confirm.setContentText("This action cannot be undone.");

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                messageRepo.delete(
                                        msg.getId(),
                                        Session.getCurrentUserId()
                                );
                                loadMessages();
                            } catch (SQLException ex) {
                                showError(ex.getMessage());
                            }
                        }
                    });
                });

                //function ta update
                edit.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(msg.getContent());
                    dialog.setHeaderText("Edit message");

                    dialog.showAndWait().ifPresent(newText -> {
                        try {
                            messageRepo.update(
                                    msg.getId(),
                                    Session.getCurrentUserId(),
                                    newText
                            );
                            loadMessages();
                        } catch (SQLException ex) {
                            showError(ex.getMessage());
                        }
                    });
                });

                // Message content just zina hetha
                Label content = new Label(msg.getContent());
                content.setWrapText(true);
                content.setMaxWidth(300);
                content.getStyleClass().add("message-bubble");

                VBox bubble = new VBox(header, content);
                bubble.setSpacing(3);

                // container that holds bubble + menu button
                HBox messageRow = new HBox(bubble, menuBtn);
                if (msg.getSenderId() == Session.getCurrentUserId()) {
                    messageRow = new HBox(menuBtn, bubble); // menu on LEFT
                    messageRow.setAlignment(Pos.CENTER_RIGHT);
                    bubble.getStyleClass().add("mine");
                } else {
                    messageRow = new HBox(bubble, menuBtn); // menu on RIGHT
                    messageRow.setAlignment(Pos.CENTER_LEFT);
                    bubble.getStyleClass().add("theirs");
                }

                messageRow.setSpacing(6);
                //hetha llmenue li zedneh zina zeda bch yemchy fih e css
                HBox container = new HBox(messageRow);
                container.setPadding(new Insets(5));
                container.setAlignment(
                        msg.getSenderId() == Session.getCurrentUserId()
                                ? Pos.CENTER_RIGHT
                                : Pos.CENTER_LEFT
                );

                setGraphic(container);
            }
        });
        messageInput.setOnAction(e -> sendMessage()); // added this so when I press entrer it works

    }

    private void loadConversations() {
        try {
            conversationList.getItems().setAll(
                    conversationRepo.findAll()
            );
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void loadMessages() {
        if (selectedConversation == null) return;

        try {
            messageList.getItems().setAll(
                    messageRepo.findByConversation(selectedConversation.getId())
            );
            messageList.scrollTo(messageList.getItems().size() - 1);//added this line to force scroll down
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        if (selectedConversation == null || messageInput.getText().isBlank())
            return;

        Message msg = new Message(
                selectedConversation.getId(),
                Session.getCurrentUserId(),
                messageInput.getText()
        );

        try {
            messageRepo.create(msg);
            messageInput.clear();
            loadMessages();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Database Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
    //added this on 2/2 after the UI I added in chatfxml
    @FXML
    private void createConversation() {
        try {
            Conversation c = new Conversation();
            c.setType("PERSONAL");
            c.setContextType("POST");
            c.setContextId(0);

            conversationRepo.create(c);
            loadConversations();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    //okay wa9t lapp tlanci testaaml e sheet ta css li mawjoud fel main donc kima 3malt fe react
    //bedhabt 7abbit naalm mode sombre donc zedt variable marbout b boutton w ylanci external form w ye9lb loun
    @FXML
    private Button themeBtn;
    private boolean darkMode = false;

    @FXML
    private void toggleTheme() {
        Scene scene = themeBtn.getScene();

        //hethy bch me yekrachich ylanci ania li y7eb mellouel jdid
        scene.getStylesheets().clear();

        if (darkMode) {
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/pi_dev/messagingchat.css").toExternalForm()
            );
            themeBtn.setText("🌙");
        } else {
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/pi_dev/messagingchat-dark.css").toExternalForm()
            );
            themeBtn.setText("☀️");
        }

        darkMode = !darkMode;
    }




}
