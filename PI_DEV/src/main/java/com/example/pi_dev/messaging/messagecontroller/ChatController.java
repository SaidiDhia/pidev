package com.example.pi_dev.messaging.messagecontroller;

import com.example.pi_dev.messaging.messagingmodel.Conversation;
import com.example.pi_dev.messaging.messagingmodel.Message;
import com.example.pi_dev.messaging.messagingrepository.ConversationRepository;
import com.example.pi_dev.messaging.messagingrepository.ConversationUserRepository;
import com.example.pi_dev.messaging.messagingrepository.MessageRepository;
import com.example.pi_dev.messaging.messagingsession.Session;

import javafx.fxml.FXML;
import javafx.geometry.Side;
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
    @FXML
    private Button themeBtn;

    private final ConversationRepository conversationRepo = new ConversationRepository();
    private final MessageRepository messageRepo = new MessageRepository();
    private final ConversationUserRepository conversationUserRepo = new ConversationUserRepository();

    private Conversation selectedConversation;
    private boolean darkMode = false;

    @FXML
    public void initialize() {

        loadConversations();

        // UI improvements - consistent sizing
        conversationList.setFixedCellSize(50);
        messageList.setFixedCellSize(-1);

        // Placeholder when no conversation is selected
        messageList.setPlaceholder(new Label("Select a conversation to start chatting"));
        // Placeholder when conversation has no messages
        // messageList.setPlaceholder(new Label("No messages yet 👋")); // Note: only last placeholder will apply

        conversationList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectedConversation = newVal;
                    loadMessages();
                });

        // Custom cell factory for conversations with better styling
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

        // Custom cell factory for messages with edit/delete functionality
        messageList.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                System.out.println("Message sender: " + msg.getSenderId()); // Debug output

                String currentUser = Session.getCurrentUserId();
                boolean isMine = msg.getSenderId().equals(currentUser);

                // Three dots menu button for edit/delete
                Button menuBtn = new Button("⋮");
                menuBtn.getStyleClass().add("menu-button");

                // Context menu with edit and delete options
                ContextMenu menu = new ContextMenu();
                MenuItem edit = new MenuItem("Edit");
                MenuItem delete = new MenuItem("Delete");
                menu.getItems().addAll(edit, delete);
                menuBtn.setOnAction(e -> menu.show(menuBtn, Side.BOTTOM, 0, 0));

                // Only show menu button for user's own messages (permission check)
                menuBtn.setVisible(isMine);

                // Header with username and time
                Label header = new Label(
                        "User " + msg.getSenderId() + " • " +
                                msg.getCreatedAt().toLocalTime().withNano(0)
                );
                header.getStyleClass().add("message-header");

                // Delete functionality with confirmation dialog
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
                                        currentUser
                                );
                                loadMessages();
                            } catch (SQLException ex) {
                                showError(ex.getMessage());
                            }
                        }
                    });
                });

                // Edit functionality with input dialog
                edit.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(msg.getContent());
                    dialog.setHeaderText("Edit message");

                    dialog.showAndWait().ifPresent(newText -> {
                        try {
                            messageRepo.update(
                                    msg.getId(),
                                    currentUser,
                                    newText
                            );
                            loadMessages();
                        } catch (SQLException ex) {
                            showError(ex.getMessage());
                        }
                    });
                });

                // Message content with styling
                Label content = new Label(msg.getContent());
                content.setWrapText(true);
                content.setMaxWidth(300);
                content.getStyleClass().add("message-bubble");

                VBox bubble = new VBox(header, content);
                bubble.setSpacing(3);

                // Container that holds bubble + menu button
                // Layout differs based on whether message is from current user
                HBox messageRow;

                if (isMine) {
                    // For own messages: menu on LEFT, bubble on RIGHT
                    messageRow = new HBox(menuBtn, bubble);
                    messageRow.setAlignment(Pos.CENTER_RIGHT);
                    bubble.getStyleClass().add("mine");
                } else {
                    // For others' messages: bubble on LEFT, menu on RIGHT
                    messageRow = new HBox(bubble, menuBtn);
                    messageRow.setAlignment(Pos.CENTER_LEFT);
                    bubble.getStyleClass().add("theirs");
                }

                messageRow.setSpacing(6);

                // Final container with proper padding and alignment
                HBox container = new HBox(messageRow);
                container.setPadding(new Insets(5));
                container.setAlignment(
                        isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT
                );

                setGraphic(container);
            }
        });

        // Pressing Enter in message input sends the message
        messageInput.setOnAction(e -> sendMessage());
    }

    private void loadConversations() {
        try {
            conversationList.getItems().setAll(
                    conversationRepo.findByUser(Session.getCurrentUserId())
            );
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void loadMessages() {
        if (selectedConversation == null) return;

        try {
            messageList.getItems().setAll(
                    messageRepo.findByConversation(
                            selectedConversation.getId(),
                            Session.getCurrentUserId()
                    )
            );
            // Auto-scroll to the latest message
            messageList.scrollTo(messageList.getItems().size() - 1);
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
        System.out.println("Current session user: " + Session.getCurrentUserId()); // Debug output
    }

    @FXML
    private void handleCreateConversation() throws SQLException {

        String otherUserId = "987fcdeb-1234-5678-9abc-def012345678"; // temporary for testing

        Conversation c = new Conversation();
        c.setType("Private Chat");

        long conversationId = conversationRepo.create(c);

        conversationUserRepo.addUserToConversation(conversationId, Session.getCurrentUserId());
        conversationUserRepo.addUserToConversation(conversationId, otherUserId);

        System.out.println("Conversation created with ID: " + conversationId); // Debug output
        loadConversations();
    }

    @FXML
    private void toggleTheme() {
        Scene scene = themeBtn.getScene();

        // Clear existing stylesheets to avoid conflicts
        scene.getStylesheets().clear();

        if (darkMode) {
            // Switch to light mode
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/pi_dev/messagingchat.css").toExternalForm()
            );
            themeBtn.setText("🌙");
        } else {
            // Switch to dark mode
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/pi_dev/messagingchat-dark.css").toExternalForm()
            );
            themeBtn.setText("☀️");
        }

        darkMode = !darkMode;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Database Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}