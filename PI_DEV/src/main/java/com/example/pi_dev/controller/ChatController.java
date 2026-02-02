package com.example.pi_dev.controller;

import com.example.pi_dev.model.Conversation;
import com.example.pi_dev.model.Message;
import com.example.pi_dev.repository.ConversationRepository;
import com.example.pi_dev.repository.MessageRepository;

import com.example.pi_dev.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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

        conversationList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectedConversation = newVal;
                    loadMessages();
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

                // Header: username + time
                Label header = new Label(
                        "User " + msg.getSenderId() + " • " +
                                msg.getCreatedAt().toLocalTime().withNano(0)
                );
                header.getStyleClass().add("message-header");

                // Message content
                Label content = new Label(msg.getContent());
                content.setWrapText(true);
                content.setMaxWidth(300);
                content.getStyleClass().add("message-bubble");

                VBox bubble = new VBox(header, content);
                bubble.setSpacing(3);

                HBox container = new HBox(bubble);
                container.setPadding(new Insets(5));

                if (msg.getSenderId() == Session.getCurrentUserId()) {
                    container.setAlignment(Pos.CENTER_RIGHT);
                    bubble.getStyleClass().add("mine");
                } else {
                    container.setAlignment(Pos.CENTER_LEFT);
                    bubble.getStyleClass().add("theirs");
                }

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




}
