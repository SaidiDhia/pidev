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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the chat interface.
 * Manages conversations, messages, and user interactions in the messaging system.
 */
public class ChatController {

    // ==================== FXML UI Components ====================

    @FXML private ListView<Conversation> conversationList;  // Left panel: list of user's conversations
    @FXML private ListView<Message> messageList;            // Right panel: messages in selected conversation
    @FXML private TextField messageInput;                    // Input field for typing new messages
    @FXML private Button themeBtn;                            // Button to toggle between light/dark theme

    // NEW: Add these to your FXML if you want separate buttons
    // If not, we'll use the context menu in the conversation cells
    @FXML private TextField conversationNameField;           // For updating conversation name
    @FXML private Button updateConversationBtn;              // Button to trigger update
    @FXML private Button deleteConversationBtn;              // Button to trigger delete

    // ==================== Repositories (Data Access Layer) ====================

    private final ConversationRepository conversationRepo = new ConversationRepository();        // Handles conversation DB operations
    private final MessageRepository messageRepo = new MessageRepository();                      // Handles message DB operations
    private final ConversationUserRepository conversationUserRepo = new ConversationUserRepository(); // Handles conversation-user relationships

    // ==================== State Variables ====================

    private Conversation selectedConversation;  // Currently selected conversation
    private boolean darkMode = false;            // Current theme state (false = light mode, true = dark mode)

    /**
     * Initializes the controller after FXML loading.
     * Sets up UI components, listeners, and cell factories.
     */
    @FXML
    public void initialize() {

        // Load initial conversation list for the current user
        loadConversations();

        // UI improvements: set fixed cell sizes for consistent appearance
        conversationList.setFixedCellSize(60);   // Increased height for better touch target
        messageList.setFixedCellSize(-1);         // Dynamic height for message items (wraps content)

        // Placeholder shown when no conversation is selected
        messageList.setPlaceholder(new Label("Select a conversation to start chatting"));

        /**
         * Listener for conversation selection changes.
         * When user clicks a different conversation:
         * 1. Load its messages
         * 2. Auto-fill name field for editing (if name field exists)
         */
        conversationList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectedConversation = newVal;
                    loadMessages();  // Load messages for newly selected conversation

                    // Bonus: Auto-fill name field when conversation is selected
                    if (newVal != null && conversationNameField != null) {
                        conversationNameField.setText(newVal.getName());
                    }
                });

        /**
         * Custom cell factory for conversation list items.
         * Displays conversation name (or ID as fallback) and type with proper styling.
         * Includes 3-dot menu for rename and delete operations.
         */
        conversationList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation c, boolean empty) {
                super.updateItem(c, empty);

                // Handle empty cells
                if (empty || c == null) {
                    setGraphic(null);
                    return;
                }

                // Conversation title - use name if available, otherwise fallback to ID
                Label title = new Label(
                        c.getName() != null ? c.getName() : "Conversation " + c.getId()
                );
                title.getStyleClass().add("conv-title");

                // Conversation type subtitle (PERSONAL/GROUP)
                Label subtitle = new Label(c.getType());
                subtitle.getStyleClass().add("conv-subtitle");

                // Vertical layout for conversation text
                VBox textBox = new VBox(title, subtitle);
                textBox.setSpacing(3);

                // ========== Three-dots menu button for conversation actions ==========
                Button menuBtn = new Button("⋮");
                menuBtn.getStyleClass().add("menu-button");

                // Create context menu with edit/delete options
                ContextMenu menu = new ContextMenu();
                MenuItem rename = new MenuItem("Rename");
                MenuItem delete = new MenuItem("Delete");
                menu.getItems().addAll(rename, delete);

                // Show menu when button is clicked
                menuBtn.setOnAction(e -> menu.show(menuBtn, Side.BOTTOM, 0, 0));

                // ========== Rename action ==========
                rename.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(c.getName());
                    dialog.setTitle("Rename Conversation");
                    dialog.setHeaderText("Enter new name for conversation");

                    dialog.showAndWait().ifPresent(newName -> {
                        try {
                            // Use the repository's updateName method
                            conversationRepo.updateName(c.getId(), newName.trim());
                            loadConversations();  // Refresh the list
                            showInfo("Conversation renamed successfully!");
                        } catch (SQLException ex) {
                            showError("Failed to rename: " + ex.getMessage());
                        }
                    });
                });

                // ========== Delete action with confirmation ==========
                delete.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Conversation");
                    confirm.setHeaderText("Are you sure you want to delete this conversation?");
                    confirm.setContentText("This will delete all messages and cannot be undone.");

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                // Use the repository's delete method (handles messages & participants)
                                conversationRepo.delete(c.getId());
                                loadConversations();  // Refresh list
                                messageList.getItems().clear();  // Clear message view
                                selectedConversation = null;  // Clear selection
                                showInfo("Conversation deleted successfully!");
                            } catch (SQLException ex) {
                                showError("Failed to delete: " + ex.getMessage());
                            }
                        }
                    });
                });

                // Horizontal layout: [text content] [menu button]
                HBox row = new HBox(textBox, menuBtn);
                row.setSpacing(10);
                row.setAlignment(Pos.CENTER_LEFT);

                setGraphic(row);
            }
        });

        /**
         * Custom cell factory for message list items.
         * Complex layout with different alignments for sent/received messages,
         * edit/delete functionality, and contextual menus.
         */
        messageList.setCellFactory(list -> new ListCell<>() {

            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                // Handle empty cells
                if (empty || msg == null) {
                    setGraphic(null);
                    return;
                }

                // Determine if this message was sent by the current user
                String currentUser = Session.getCurrentUserId();
                boolean isMine = msg.getSenderId().equals(currentUser);

                // ========== Three-dots menu button for message actions ==========
                Button menuBtn = new Button("⋮");
                menuBtn.getStyleClass().add("menu-button");

                // Create context menu with edit/delete options
                ContextMenu menu = new ContextMenu();
                MenuItem edit = new MenuItem("Edit");
                MenuItem delete = new MenuItem("Delete");
                menu.getItems().addAll(edit, delete);

                // Show menu when button is clicked
                menuBtn.setOnAction(e -> menu.show(menuBtn, Side.BOTTOM, 0, 0));

                // Only show menu button for user's own messages (privacy/permission)
                menuBtn.setVisible(isMine);

                // ========== Message header with sender info and timestamp ==========
                Label header = new Label(
                        "User " + msg.getSenderId() + " • " +
                                msg.getCreatedAt().toLocalTime().withNano(0)  // Format time without nanoseconds
                );
                header.getStyleClass().add("message-header");

                // ========== Delete action with confirmation dialog ==========
                delete.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete message");
                    confirm.setHeaderText("Are you sure?");
                    confirm.setContentText("This action cannot be undone.");

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                // Hard delete from database (requires permission)
                                messageRepo.delete(msg.getId(), currentUser);
                                loadMessages();  // Refresh message list
                            } catch (SQLException ex) {
                                showError(ex.getMessage());
                            }
                        }
                    });
                });

                // ========== Edit action with input dialog ==========
                edit.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(msg.getContent());
                    dialog.setHeaderText("Edit message");

                    dialog.showAndWait().ifPresent(newText -> {
                        try {
                            messageRepo.update(msg.getId(), currentUser, newText);
                            loadMessages();  // Refresh message list
                        } catch (SQLException ex) {
                            showError(ex.getMessage());
                        }
                    });
                });

                // ========== Message content bubble ==========
                Label content = new Label(msg.getContent());
                content.setWrapText(true);           // Allow text wrapping
                content.setMaxWidth(300);             // Limit bubble width for readability
                content.getStyleClass().add("message-bubble");

                // Combine header and content in vertical layout
                VBox bubble = new VBox(header, content);
                bubble.setSpacing(3);

                // ========== Layout based on message sender ==========
                HBox messageRow;

                if (isMine) {
                    // Current user's messages: menu on LEFT, bubble on RIGHT
                    messageRow = new HBox(menuBtn, bubble);
                    messageRow.setAlignment(Pos.CENTER_RIGHT);
                    bubble.getStyleClass().add("mine");  // CSS class for "my" messages
                } else {
                    // Other users' messages: bubble on LEFT, menu on RIGHT
                    messageRow = new HBox(bubble, menuBtn);
                    messageRow.setAlignment(Pos.CENTER_LEFT);
                    bubble.getStyleClass().add("theirs");  // CSS class for "their" messages
                }

                messageRow.setSpacing(6);  // Spacing between bubble and menu

                // Final container with padding
                HBox container = new HBox(messageRow);
                container.setPadding(new Insets(5));
                container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                setGraphic(container);
            }
        });

        /**
         * Allow sending messages by pressing Enter key in the input field.
         */
        messageInput.setOnAction(e -> sendMessage());

        // If you have separate update/delete buttons, set their actions
        if (updateConversationBtn != null) {
            updateConversationBtn.setOnAction(e -> handleUpdateConversation());
        }
        if (deleteConversationBtn != null) {
            deleteConversationBtn.setOnAction(e -> handleDeleteConversation());
        }
    }

    /**
     * Loads all conversations for the current user from the database.
     * Updates the conversationList UI component.
     */
    private void loadConversations() {
        try {
            conversationList.getItems().setAll(
                    conversationRepo.findByUser(Session.getCurrentUserId())
            );
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Loads all messages for the currently selected conversation.
     * Auto-scrolls to the latest message.
     */
    private void loadMessages() {
        if (selectedConversation == null) return;

        try {
            messageList.getItems().setAll(
                    messageRepo.findByConversation(
                            selectedConversation.getId(),
                            Session.getCurrentUserId()
                    )
            );
            // Auto-scroll to the bottom (latest message)
            if (!messageList.getItems().isEmpty()) {
                messageList.scrollTo(messageList.getItems().size() - 1);
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Sends a new message in the current conversation.
     * Triggered by clicking send button or pressing Enter.
     */
    @FXML
    private void sendMessage() {

        // Validate that a conversation is selected and message is not empty
        if (selectedConversation == null || messageInput.getText().isBlank())
            return;

        // Create new message object
        Message msg = new Message(
                selectedConversation.getId(),
                Session.getCurrentUserId(),
                messageInput.getText()
        );

        try {
            // Save to database
            messageRepo.create(msg);
            messageInput.clear();      // Clear input field
            loadMessages();             // Refresh message list (will auto-scroll)
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Handles the creation of a new conversation with proper validation flow:
     * VALIDATE ALL EMAILS → CREATE → ADD PARTICIPANTS
     * This prevents partial conversation creation if any email is invalid.
     */
    @FXML
    private void handleCreateConversation() {

        // ========== Create and configure the dialog ==========
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Conversation");
        dialog.setHeaderText("Start a new conversation");

        // ========== Input fields ==========
        // Conversation name field
        TextField nameField = new TextField();
        nameField.setPromptText("Conversation name");

        // Conversation type choice (PERSONAL or GROUP)
        ChoiceBox<String> typeChoice = new ChoiceBox<>();
        typeChoice.getItems().addAll("PERSONAL", "GROUP");
        typeChoice.setValue("PERSONAL");

        // Email input for adding participants
        TextField emailField = new TextField();
        emailField.setPromptText("Participant email");

        // List to display added participants
        ListView<String> participantsList = new ListView<>();
        participantsList.setPrefHeight(100);

        // Button to add participant by email
        Button addBtn = new Button("Add Participant");
        addBtn.setMaxWidth(Double.MAX_VALUE);

        /**
         * Add participant action:
         * - Validates email is not blank
         * - Checks for duplicates
         * - Adds to list and clears input field
         */
        addBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (!email.isBlank() && !participantsList.getItems().contains(email)) {
                participantsList.getItems().add(email);
                emailField.clear();
            }
        });

        // Allow pressing Enter in email field to add participant
        emailField.setOnAction(e -> addBtn.fire());

        // ========== Layout construction ==========
        VBox content = new VBox(10);  // 10px spacing between elements
        content.setPadding(new Insets(20));
        content.getChildren().addAll(
                new Label("Conversation Name:"),
                nameField,
                new Label("Conversation Type:"),
                typeChoice,
                new Label("Add Participants by Email:"),
                emailField,
                addBtn,
                new Label("Participants:"),
                participantsList
        );

        // Add content and buttons to dialog
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // ========== Get OK button and add validation ==========
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Validate before allowing dialog to close
            if (nameField.getText().trim().isEmpty()) {
                showError("Conversation name cannot be empty.");
                event.consume();  // Prevent dialog from closing
            } else if (participantsList.getItems().isEmpty()) {
                showError("Please add at least one participant.");
                event.consume();  // Prevent dialog from closing
            }
        });

        // ========== Handle dialog result ==========
        dialog.showAndWait().ifPresent(result -> {

            // Only proceed if OK was clicked
            if (result != ButtonType.OK) return;

            String name = nameField.getText().trim();
            String type = typeChoice.getValue();

            // Validate again (in case validation filter was bypassed)
            if (name.isBlank()) {
                showError("Conversation name cannot be empty.");
                return;
            }

            /**
             * Business rule validation:
             * PERSONAL conversations must have exactly 1 participant (plus current user)
             * Note: The current user is added automatically, so we check for 1 additional participant
             */
            if (type.equals("PERSONAL") && participantsList.getItems().size() != 1) {
                showError("Personal conversation must have exactly 1 participant.");
                return;
            }

            try {
                // ========== STEP 1: VALIDATE ALL EMAILS FIRST ==========
                List<String> validatedUserIds = new ArrayList<>();

                for (String email : participantsList.getItems()) {
                    // Find user ID by email
                    String userId = conversationUserRepo.findUserIdByEmail(email);

                    if (userId == null) {
                        // If ANY email is invalid, STOP completely - no conversation created
                        showError("User not found with email: " + email);
                        return; // ⛔ STOP - DO NOT CREATE CONVERSATION
                    }

                    validatedUserIds.add(userId);
                }

                // ========== STEP 2: ONLY NOW CREATE CONVERSATION ==========
                Conversation c = new Conversation();
                c.setName(name);
                c.setType(type);

                long conversationId = conversationRepo.create(c);

                if (conversationId == -1) {
                    showError("Failed to create conversation.");
                    return;
                }

                // ========== STEP 3: ADD ALL PARTICIPANTS ==========
                // Add current user first
                String currentUser = Session.getCurrentUserId();
                conversationUserRepo.addUserToConversation(conversationId, currentUser);

                // Add all validated participants
                for (String userId : validatedUserIds) {
                    conversationUserRepo.addUserToConversation(conversationId, userId);
                }

                // Refresh the conversation list to show the new conversation
                loadConversations();

                // Show success message
                showInfo("Conversation created successfully!");

            } catch (Exception ex) {
                showError("Failed to create conversation: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    /**
     * Handles updating the selected conversation's name.
     * Uses the repository's updateName method.
     */
    @FXML
    private void handleUpdateConversation() {
        // Check if a conversation is selected
        if (selectedConversation == null) {
            showError("Please select a conversation first.");
            return;
        }

        // Check if we have a name field (either from FXML or create a dialog)
        String newName;

        if (conversationNameField != null) {
            // Use the FXML field if available
            newName = conversationNameField.getText().trim();
            if (newName.isEmpty()) {
                showError("Please enter a new name.");
                return;
            }
        } else {
            // Otherwise show a dialog
            TextInputDialog dialog = new TextInputDialog(selectedConversation.getName());
            dialog.setTitle("Update Conversation");
            dialog.setHeaderText("Enter new name for conversation");

            var result = dialog.showAndWait();
            if (result.isEmpty()) return;
            newName = result.get().trim();
            if (newName.isEmpty()) {
                showError("Name cannot be empty.");
                return;
            }
        }

        try {
            // Use the repository's updateName method
            conversationRepo.updateName(selectedConversation.getId(), newName);
            loadConversations();  // Refresh the list
            showInfo("Conversation updated successfully!");
        } catch (SQLException e) {
            showError("Failed to update conversation: " + e.getMessage());
        }
    }

    /**
     * Handles deleting the selected conversation.
     * Uses the repository's delete method which handles cascading deletes.
     */
    @FXML
    private void handleDeleteConversation() {
        // Check if a conversation is selected
        if (selectedConversation == null) {
            showError("Please select a conversation first.");
            return;
        }

        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Conversation");
        confirm.setHeaderText("Delete " + selectedConversation.getName() + "?");
        confirm.setContentText("This will delete all messages and cannot be undone.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // Use the repository's delete method
                    conversationRepo.delete(selectedConversation.getId());
                    loadConversations();  // Refresh list
                    messageList.getItems().clear();  // Clear message view
                    selectedConversation = null;  // Clear selection

                    // Clear name field if it exists
                    if (conversationNameField != null) {
                        conversationNameField.clear();
                    }

                    showInfo("Conversation deleted successfully!");
                } catch (SQLException e) {
                    showError("Failed to delete conversation: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Toggles between light and dark theme.
     * Switches CSS stylesheets and updates button icon.
     */
    @FXML
    private void toggleTheme() {

        // Get the scene from the theme button
        Scene scene = themeBtn.getScene();

        // Clear existing stylesheets to avoid conflicts
        scene.getStylesheets().clear();

        if (darkMode) {
            // Switch to light mode
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/pi_dev/messagingchat.css").toExternalForm()
            );
            themeBtn.setText("🌙");  // Moon icon for dark mode (indicates can switch to dark)
        } else {
            // Switch to dark mode
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/pi_dev/messagingchat-dark.css").toExternalForm()
            );
            themeBtn.setText("☀️");  // Sun icon for light mode (indicates can switch to light)
        }

        // Toggle the state
        darkMode = !darkMode;
    }

    /**
     * Displays an error alert dialog.
     * @param msg The error message to display
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Displays an information alert dialog.
     * @param msg The information message to display
     */
    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}