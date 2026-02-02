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

        conversationList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectedConversation = newVal;
                    loadMessages();
                });

        messageList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(msg.getContent() + "\n" +
                            msg.getCreatedAt().toLocalTime().withSecond(0));

                    if (msg.getSenderId() == Session.getCurrentUserId())
                    {
                        setStyle("-fx-alignment: CENTER-RIGHT;");
                    } else {
                        setStyle("-fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

    }

    private void loadConversations() {
        try {
            conversationList.getItems().setAll(
                    conversationRepo.findAll()
            );
        } catch (SQLException e) {
            showError("Failed to load conversations", e.getMessage());
        }
    }

    private void loadMessages() {
        if (selectedConversation == null) return;

        try {
            messageList.getItems().setAll(
                    messageRepo.findByConversation(selectedConversation.getId())
            );
        } catch (SQLException e) {
            showError("Failed to load messages", e.getMessage());
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
            showError("Failed to send message", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
