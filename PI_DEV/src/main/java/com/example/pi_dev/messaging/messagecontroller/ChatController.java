package com.example.pi_dev.messaging.messagecontroller;

import com.example.pi_dev.messaging.messagingmodel.Conversation;
import com.example.pi_dev.messaging.messagingmodel.Message;
import com.example.pi_dev.messaging.messagingrepository.ConversationRepository;
import com.example.pi_dev.messaging.messagingrepository.ConversationUserRepository;
import com.example.pi_dev.messaging.messagingrepository.MessageRepository;
import com.example.pi_dev.messaging.messagingrepository.UserRepository;
import com.example.pi_dev.messaging.messagingsession.Session;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the chat interface.
 * Manages conversations, messages, and user interactions in the messaging system.
 */
public class ChatController {

    // ==================== FXML UI Components ====================

    // Header Components
    @FXML private Button themeBtn;                            // Button to toggle between light/dark theme

    // Chat Tab Components
    @FXML private ListView<Conversation> conversationList;    // Left panel: list of user's conversations
    @FXML private ListView<Message> messageList;              // Right panel: messages in selected conversation
    @FXML private TextField messageInput;                      // Input field for typing new messages
    @FXML private Label conversationTitleLabel;                // Title of selected conversation
    @FXML private Button updateConversationBtn;                // Button to trigger update
    @FXML private Button deleteConversationBtn;                // Button to trigger delete
    @FXML private TextField conversationSearchField;           // Search field for conversations

    // Contacts Tab Components
    @FXML private Tab contactsTab;                             // Contacts tab
    @FXML private ScrollPane contactsScrollPane;               // Scroll pane for contacts
    @FXML private FlowPane contactsFlow;                       // Flow pane for contact cards
    @FXML private Tab archivedTab;
    @FXML private ListView<Conversation> archivedConversationList;
    @FXML private Button unarchiveAllBtn;

    private ObservableList<Conversation> archivedConversations = FXCollections.observableArrayList();
    // ==================== Repositories (Data Access Layer) ====================

    private final ConversationRepository conversationRepo = new ConversationRepository();        // Handles conversation DB operations
    private final MessageRepository messageRepo = new MessageRepository();                      // Handles message DB operations
    private final ConversationUserRepository conversationUserRepo = new ConversationUserRepository(); // Handles conversation-user relationships
    private final UserRepository userRepo = new UserRepository();

    // ==================== State Variables ====================

    private Conversation selectedConversation;  // Currently selected conversation
    private boolean darkMode = false;            // Current theme state (false = light mode, true = dark mode)
    private ObservableList<Conversation> conversations = FXCollections.observableArrayList();  // Observable list for conversations

    /**
     * Initializes the controller after FXML loading.
     * Sets up UI components, listeners, and cell factories.
     */
    @FXML
    public void initialize() {

        // Load initial conversation list for the current user
        loadConversations();

        // Set up conversation list with observable list
        conversationList.setItems(conversations);

        // UI improvements: set fixed cell sizes for consistent appearance
        conversationList.setFixedCellSize(60);   // Increased height for better touch target
        messageList.setFixedCellSize(-1);         // Dynamic height for message items (wraps content)

        // Apply CSS classes to existing components
        messageList.getStyleClass().add("message-list");
        conversationSearchField.getStyleClass().add("search-field");

        // Set placeholder with CSS class
        Label placeholderLabel = new Label("Select a conversation to start chatting");
        placeholderLabel.getStyleClass().add("placeholder-text");
        messageList.setPlaceholder(placeholderLabel);

        // Set up search functionality
        if (conversationSearchField != null) {
            conversationSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterConversations(newVal);
            });
        }

        /**
         * Listener for conversation selection changes.
         * When user clicks a different conversation:
         * 1. Update title label
         * 2. Load its messages
         * 3. Show/hide update/delete buttons
         */
        conversationList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    selectedConversation = newVal;

                    if (newVal != null) {
                        // Update conversation title label
                        if (conversationTitleLabel != null) {
                            conversationTitleLabel.setText(newVal.getName() != null ?
                                    newVal.getName() : "Conversation " + newVal.getId());
                            conversationTitleLabel.getStyleClass().add("conversation-title");
                        }

                        // Show update/delete buttons
                        if (updateConversationBtn != null) {
                            updateConversationBtn.setVisible(true);
                            updateConversationBtn.getStyleClass().add("button-secondary");
                        }
                        if (deleteConversationBtn != null) {
                            deleteConversationBtn.setVisible(true);
                            deleteConversationBtn.getStyleClass().add("button-danger");
                        }
                    } else {
                        // Hide buttons when no conversation selected
                        if (updateConversationBtn != null) {
                            updateConversationBtn.setVisible(false);
                        }
                        if (deleteConversationBtn != null) {
                            deleteConversationBtn.setVisible(false);
                        }
                        if (conversationTitleLabel != null) {
                            conversationTitleLabel.setText("Select a conversation");
                        }
                    }

                    loadMessages();  // Load messages for newly selected conversation
                });

        /**
         * Custom cell factory for conversation list items.
         * Displays conversation name (or ID as fallback) and type with proper styling.
         * Includes 3-dot menu for rename, pin, archive, mute, and manage participants.
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

                // Last message preview (optional)
                Label lastMsg = new Label("Click to view messages");
                lastMsg.getStyleClass().add("last-message-preview");

                // Vertical layout for conversation text
                VBox textBox = new VBox(title, subtitle, lastMsg);
                textBox.setSpacing(2);

                // ========== Three-dots menu button for conversation actions ==========
                Button menuBtn = new Button("⋮");
                menuBtn.getStyleClass().add("menu-button");

                // Create context menu with all options (DELETE OPTION REMOVED)
                ContextMenu menu = new ContextMenu();
                MenuItem rename = new MenuItem("Rename");
                MenuItem manageParticipants = new MenuItem("Manage Participants");

                // Add Pin/Unpin option
                MenuItem pinItem = new MenuItem(c.isPinned() ? "Unpin" : "Pin");
                pinItem.setOnAction(e -> {
                    try {
                        boolean newPinState = !c.isPinned();
                        conversationRepo.updatePinStatus(c.getId(), Session.getCurrentUserId(), newPinState);
                        c.setPinned(newPinState);
                        loadConversations();
                        showInfo(newPinState ? "Conversation pinned!" : "Conversation unpinned!");
                    } catch (SQLException ex) {
                        showError("Failed to update pin status: " + ex.getMessage());
                    }
                });

                // Add Archive/Unarchive option
                MenuItem archiveItem = new MenuItem(c.isArchived() ? "Unarchive" : "Archive");
                // In the archive menu item:
                archiveItem.setOnAction(e -> {
                    try {
                        boolean newArchiveState = !c.isArchived();
                        conversationRepo.updateArchiveStatus(c.getId(), Session.getCurrentUserId(), newArchiveState);
                        c.setArchived(newArchiveState);
                        loadConversations(); // Refresh main list
                        if (archivedTab != null) {
                            loadArchivedConversations(); // Refresh archived list
                        }
                        showInfo(newArchiveState ? "Conversation archived!" : "Conversation unarchived!");
                    } catch (SQLException ex) {
                        showError("Failed to update archive status: " + ex.getMessage());
                    }
                });


                // Add Mute/Unmute option
                MenuItem muteItem = new MenuItem(c.isMuteNotifications() ? "Unmute" : "Mute");
                muteItem.setOnAction(e -> {
                    try {
                        boolean newMuteState = !c.isMuteNotifications();
                        conversationRepo.updateMuteStatus(c.getId(), Session.getCurrentUserId(), newMuteState);
                        c.setMuteNotifications(newMuteState);
                        showInfo(newMuteState ? "Notifications muted!" : "Notifications unmuted!");
                    } catch (SQLException ex) {
                        showError("Failed to update mute status: " + ex.getMessage());
                    }
                });

                // Add separator for organization
                SeparatorMenuItem separator = new SeparatorMenuItem();

                // Add items to menu in logical order - WITHOUT DELETE OPTION
                menu.getItems().addAll(rename, pinItem, archiveItem, muteItem, separator, manageParticipants);

                // Add action for manage participants
                manageParticipants.setOnAction(e -> {
                    if (c.getType().equals("GROUP")) {
                        handleManageParticipants();
                    } else {
                        showInfo("Personal conversations don't have participant management.");
                    }
                });

                // Show menu when button is clicked
                menuBtn.setOnAction(e -> menu.show(menuBtn, Side.BOTTOM, 0, 0));

                // ========== Rename action ==========
                rename.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(c.getName());
                    dialog.setTitle("Rename Conversation");
                    dialog.setHeaderText("Enter new name for conversation");
                    dialog.setContentText("Name:");

                    // Style dialog
                    DialogPane dialogPane = dialog.getDialogPane();
                    dialogPane.getStyleClass().add("dialog-pane");

                    dialog.showAndWait().ifPresent(newName -> {
                        try {
                            conversationRepo.updateName(c.getId(), newName.trim());
                            loadConversations();
                            showInfo("Conversation renamed successfully!");
                        } catch (SQLException ex) {
                            showError("Failed to rename: " + ex.getMessage());
                        }
                    });
                });

                // Horizontal layout: [text content] [menu button]
                HBox row = new HBox(textBox, menuBtn);
                row.setSpacing(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5));
                HBox.setHgrow(textBox, Priority.ALWAYS);

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

                // ========== Get sender's full name ==========
                String senderName = "User " + msg.getSenderId(); // default
                try {
                    String fullName = userRepo.getUserFullName(msg.getSenderId());
                    if (fullName != null && !fullName.isEmpty()) {
                        senderName = fullName;
                    }
                } catch (SQLException e) {
                    // Ignore, use default
                }

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

                // Only show menu button for user's own messages
                menuBtn.setVisible(isMine);

                // ========== Message header with sender info and timestamp ==========
                Label header = new Label(
                        senderName + " • " +
                                msg.getCreatedAt().toLocalTime().withNano(0)
                );
                header.getStyleClass().add("message-header");

                // ========== Delete action with confirmation dialog ==========
                delete.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete message");
                    confirm.setHeaderText("Are you sure?");
                    confirm.setContentText("This action cannot be undone.");

                    DialogPane dialogPane = confirm.getDialogPane();
                    dialogPane.getStyleClass().add("dialog-pane");

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                messageRepo.delete(msg.getId(), currentUser);
                                loadMessages();
                            } catch (SQLException ex) {
                                showError(ex.getMessage());
                            }
                        }
                    });
                });

                // ========== Edit action with input dialog ==========
                edit.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(msg.getContent());
                    dialog.setTitle("Edit Message");
                    dialog.setHeaderText("Edit your message");
                    dialog.setContentText("New content:");

                    DialogPane dialogPane = dialog.getDialogPane();
                    dialogPane.getStyleClass().add("dialog-pane");

                    dialog.showAndWait().ifPresent(newText -> {
                        try {
                            messageRepo.update(msg.getId(), currentUser, newText);
                            loadMessages();
                        } catch (SQLException ex) {
                            showError(ex.getMessage());
                        }
                    });
                });

                // ========== Message content bubble ==========
                Label content = new Label(msg.getContent());
                content.setWrapText(true);
                content.setMaxWidth(300);
                content.getStyleClass().add("message-bubble");

                // Combine header and content in vertical layout
                VBox bubble = new VBox(header, content);
                bubble.setSpacing(2);

                // Add appropriate style class based on sender
                if (isMine) {
                    bubble.getStyleClass().add("mine");
                } else {
                    bubble.getStyleClass().add("theirs");
                }

                // ========== Layout based on message sender ==========
                HBox messageRow;

                if (isMine) {
                    messageRow = new HBox(menuBtn, bubble);
                    messageRow.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    messageRow = new HBox(bubble, menuBtn);
                    messageRow.setAlignment(Pos.CENTER_LEFT);
                }

                messageRow.setSpacing(6);

                // Final container with padding
                HBox container = new HBox(messageRow);
                container.setPadding(new Insets(5));
                container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                HBox.setHgrow(messageRow, isMine ? Priority.NEVER : Priority.NEVER);

                setGraphic(container);
            }
        });

        /**
         * Allow sending messages by pressing Enter key in the input field.
         */
        messageInput.setOnAction(e -> sendMessage());
        messageInput.getStyleClass().add("message-input");

        // Set up update/delete button actions
        if (updateConversationBtn != null) {
            updateConversationBtn.setOnAction(e -> handleUpdateConversation());
            updateConversationBtn.setVisible(false);
            updateConversationBtn.getStyleClass().add("button-secondary");
        }
        if (deleteConversationBtn != null) {
            deleteConversationBtn.setOnAction(e -> handleDeleteConversation());
            deleteConversationBtn.setVisible(false);
            deleteConversationBtn.getStyleClass().add("button-danger");
        }

        // Style theme button
        if (themeBtn != null) {
            themeBtn.getStyleClass().add("theme-button");
        }

        // Load contacts if contacts tab exists
        if (contactsTab != null) {
            loadContacts();
        }
        // Initialize archived tab
        if (archivedTab != null) {
            loadArchivedConversations();

            // Set up unarchive all button
            if (unarchiveAllBtn != null) {
                unarchiveAllBtn.setOnAction(e -> handleUnarchiveAll());
            }
        }
    }
    /**
     * Custom cell factory for archived conversation list.
     */
    private class ArchivedConversationCell extends ListCell<Conversation> {
        @Override
        protected void updateItem(Conversation c, boolean empty) {
            super.updateItem(c, empty);

            if (empty || c == null) {
                setGraphic(null);
                return;
            }

            HBox container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(10));
            container.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #e0e0e0 transparent;");

            // Archive icon
            Label archiveIcon = new Label("📦");
            archiveIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #6c757d;");

            // Conversation info
            VBox infoBox = new VBox(3);
            Label nameLabel = new Label(c.getName() != null ? c.getName() : "Conversation " + c.getId());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #6c757d;");

            Label typeLabel = new Label(c.getType() + " • Archived");
            typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

            infoBox.getChildren().addAll(nameLabel, typeLabel);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            // Unarchive button
            Button unarchiveBtn = new Button("Unarchive");
            unarchiveBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 15;");
            unarchiveBtn.setOnAction(e -> {
                try {
                    conversationRepo.updateArchiveStatus(c.getId(), Session.getCurrentUserId(), false);
                    loadConversations(); // Refresh main list
                    loadArchivedConversations(); // Refresh archived list
                    showInfo("Conversation unarchived!");
                } catch (SQLException ex) {
                    showError("Failed to unarchive: " + ex.getMessage());
                }
            });

            container.getChildren().addAll(archiveIcon, infoBox, unarchiveBtn);
            setGraphic(container);
        }
    }
    /**
     * Unarchive all conversations at once.
     */
    private void handleUnarchiveAll() {
        if (archivedConversations.isEmpty()) {
            showInfo("No archived conversations to unarchive.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Unarchive All");
        confirm.setHeaderText("Unarchive all conversations?");
        confirm.setContentText("This will move all archived conversations back to your main list.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    for (Conversation c : archivedConversations) {
                        conversationRepo.updateArchiveStatus(c.getId(), Session.getCurrentUserId(), false);
                    }
                    loadConversations();
                    loadArchivedConversations();
                    showInfo("All conversations unarchived!");
                } catch (SQLException e) {
                    showError("Failed to unarchive all: " + e.getMessage());
                }
            }
        });
    }
    /**
     * Filters conversations based on search text.
     * @param searchText The text to search for
     */
    private void filterConversations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            conversationList.setItems(conversations);
        } else {
            ObservableList<Conversation> filtered = FXCollections.observableArrayList();
            for (Conversation c : conversations) {
                if (c.getName() != null && c.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    filtered.add(c);
                } else if (c.getType().toLowerCase().contains(searchText.toLowerCase())) {
                    filtered.add(c);
                }
            }
            conversationList.setItems(filtered);
        }
    }

    /**
     * Loads contacts for the contacts tab.
     */
    private void loadContacts() {
        if (contactsFlow != null) {
            contactsFlow.getChildren().clear();

            // Sample contacts - replace with actual data from database
            String[] sampleContacts = {
                    "Ahmed Ben Salem", "Sarra Trabelsi", "Mehdi Khemiri",
                    "Nour Ben Ali", "Yasmine Mhiri", "Omar Jelliti"
            };

            for (String contactName : sampleContacts) {
                VBox contactCard = createContactCard(contactName);
                contactsFlow.getChildren().add(contactCard);
            }
        }
    }

    /**
     * Creates a contact card for the contacts tab.
     * @param name The contact's name
     * @return A VBox containing the contact card
     */
    private VBox createContactCard(String name) {
        VBox card = new VBox(10);
        card.getStyleClass().add("contact-card");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);

        Label avatar = new Label("👤");
        avatar.getStyleClass().add("contact-avatar");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("contact-name");

        Label status = new Label("Online");
        status.getStyleClass().add("contact-status");

        Button messageBtn = new Button("Message");
        messageBtn.getStyleClass().add("button-secondary");
        messageBtn.setPrefWidth(120);

        card.getChildren().addAll(avatar, nameLabel, status, messageBtn);

        return card;
    }

    private String getCurrentUserEmail() {
        try {
            return userRepo.getUserEmail(Session.getCurrentUserId());
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Loads all conversations for the current user from the database.
     */
    private void loadConversations() {
        try {
            conversations.setAll(conversationRepo.findByUser(Session.getCurrentUserId()));
            conversationList.setItems(conversations);
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Loads all messages for the currently selected conversation.
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
            if (!messageList.getItems().isEmpty()) {
                messageList.scrollTo(messageList.getItems().size() - 1);
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Sends a new message in the current conversation.
     */
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

    /**
     * Handles the creation of a new conversation.
     */
    @FXML
    private void handleCreateConversation() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Conversation");
        dialog.setHeaderText("Start a new conversation");

        TextField nameField = new TextField();
        nameField.setPromptText("Conversation name");
        nameField.getStyleClass().add("dialog-field");

        ChoiceBox<String> typeChoice = new ChoiceBox<>();
        typeChoice.getItems().addAll("PERSONAL", "GROUP");
        typeChoice.setValue("PERSONAL");
        typeChoice.getStyleClass().add("dialog-field");

        TextField emailField = new TextField();
        emailField.setPromptText("Participant email");
        emailField.getStyleClass().add("dialog-field");

        ListView<String> participantsList = new ListView<>();
        participantsList.setPrefHeight(100);
        participantsList.getStyleClass().add("participants-list");

        Button addBtn = new Button("Add Participant");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.getStyleClass().addAll("button", "button-secondary");

        String currentUserEmail = getCurrentUserEmail();

        addBtn.setOnAction(e -> {
            String email = emailField.getText().trim();

            if (email.isBlank()) {
                showError("Please enter an email address.");
                return;
            }

            if (email.equals(currentUserEmail)) {
                showError("You cannot add yourself to the conversation.");
                return;
            }

            if (participantsList.getItems().contains(email)) {
                showError("This participant is already added.");
                return;
            }

            participantsList.getItems().add(email);
            emailField.clear();
        });

        emailField.setOnAction(e -> addBtn.fire());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("dialog-content");
        content.getChildren().addAll(
                createStyledLabel("Conversation Name:"),
                nameField,
                createStyledLabel("Conversation Type:"),
                typeChoice,
                createStyledLabel("Add Participants by Email:"),
                emailField,
                addBtn,
                createStyledLabel("Participants:"),
                participantsList
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("button");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("button-secondary");

        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) {
                showError("Conversation name cannot be empty.");
                event.consume();
            } else if (participantsList.getItems().isEmpty()) {
                showError("Please add at least one participant.");
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;

            String name = nameField.getText().trim();
            String type = typeChoice.getValue();

            if (name.isBlank()) {
                showError("Conversation name cannot be empty.");
                return;
            }

            if (type.equals("PERSONAL") && participantsList.getItems().size() != 1) {
                showError("Personal conversation must have exactly 1 participant.");
                return;
            }

            try {
                List<String> validatedUserIds = new ArrayList<>();

                for (String email : participantsList.getItems()) {
                    String userId = conversationUserRepo.findUserIdByEmail(email);

                    if (userId == null) {
                        showError("User not found with email: " + email);
                        return;
                    }

                    validatedUserIds.add(userId);
                }

                Conversation c = new Conversation();
                c.setName(name);
                c.setType(type);

                String currentUser = Session.getCurrentUserId();
                long conversationId = conversationRepo.create(c, currentUser);

                if (conversationId == -1) {
                    showError("Failed to create conversation.");
                    return;
                }

                conversationUserRepo.addUserToConversation(conversationId, currentUser, "CREATOR", currentUser);

                for (String userId : validatedUserIds) {
                    conversationUserRepo.addUserToConversation(conversationId, userId, "MEMBER", currentUser);
                }

                loadConversations();
                showInfo("Conversation created successfully!");

            } catch (Exception ex) {
                showError("Failed to create conversation: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    /**
     * Creates a styled label for dialog sections.
     */
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("dialog-label");
        return label;
    }

    /**
     * Handles updating the selected conversation's name.
     */
    @FXML
    private void handleUpdateConversation() {
        if (selectedConversation == null) {
            showError("Please select a conversation first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedConversation.getName());
        dialog.setTitle("Update Conversation");
        dialog.setHeaderText("Rename conversation");
        dialog.setContentText("New name:");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("dialog-pane");

        var result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String newName = result.get().trim();
        if (newName.isEmpty()) {
            showError("Name cannot be empty.");
            return;
        }

        try {
            conversationRepo.updateName(selectedConversation.getId(), newName);
            loadConversations();

            if (conversationTitleLabel != null) {
                conversationTitleLabel.setText(newName);
            }

            showInfo("Conversation updated successfully!");
        } catch (SQLException e) {
            showError("Failed to update conversation: " + e.getMessage());
        }
    }
    private void loadArchivedConversations() {
        try {
            archivedConversations.setAll(conversationRepo.findArchivedByUser(Session.getCurrentUserId()));
            archivedConversationList.setItems(archivedConversations);
            archivedConversationList.setCellFactory(list -> new ArchivedConversationCell());
        } catch (SQLException e) {
            showError("Failed to load archived conversations: " + e.getMessage());
        }
    }
    /**
     * Handles deleting the selected conversation.
     * Only the creator can delete a conversation.
     */
    @FXML
    private void handleDeleteConversation() {
        if (selectedConversation == null) {
            showError("Please select a conversation first.");
            return;
        }

        try {
            boolean isCreator = conversationRepo.isUserCreator(selectedConversation.getId(),
                    Session.getCurrentUserId());

            if (!isCreator) {
                showError("Only the conversation creator can delete it.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Conversation");
            confirm.setHeaderText("Delete \"" + selectedConversation.getName() + "\"?");
            confirm.setContentText("This will delete all messages and cannot be undone.");

            DialogPane dialogPane = confirm.getDialogPane();
            dialogPane.getStyleClass().add("dialog-pane");

            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        conversationRepo.delete(selectedConversation.getId());
                        loadConversations();
                        messageList.getItems().clear();

                        selectedConversation = null;
                        if (conversationTitleLabel != null) {
                            conversationTitleLabel.setText("Select a conversation");
                        }

                        if (deleteConversationBtn != null) {
                            deleteConversationBtn.setVisible(false);
                        }
                        if (updateConversationBtn != null) {
                            updateConversationBtn.setVisible(false);
                        }

                        showInfo("Conversation deleted successfully!");
                    } catch (SQLException e) {
                        showError("Failed to delete conversation: " + e.getMessage());
                    }
                }
            });
        } catch (SQLException e) {
            showError("Failed to check permissions: " + e.getMessage());
        }
    }

    /**
     * Toggles between light and dark theme.
     */
    @FXML
    private void toggleTheme() {
        Scene scene = themeBtn.getScene();
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

    /**
     * Opens the participant management dialog for the selected conversation.
     */
    @FXML
    private void handleManageParticipants() {
        if (selectedConversation == null) {
            showError("Please select a conversation first.");
            return;
        }

        try {
            ParticipantManagementDialog dialog = new ParticipantManagementDialog(selectedConversation);
            dialog.show();
            loadConversations();
        } catch (SQLException e) {
            showError("Failed to load participants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays an error alert dialog.
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    /**
     * Displays an information alert dialog.
     */
    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }
}