package com.example.pi_dev.messaging.messagingrepository;

import com.example.pi_dev.messaging.messagingmodel.Conversation;
import com.example.pi_dev.messaging.messagingmodel.Message;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageRepository
 * Tests CRUD operations for messages
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageRepositoryTest {

    private static MessageRepository messageRepo;
    private static ConversationRepository conversationRepo;
    private static String testUserId;
    private static Long testConversationId;
    private static Long testMessageId;

    /**
     * Setup before all tests
     * Creates a test conversation to use for message tests
     */
    @BeforeAll
    static void setup() throws SQLException {
        messageRepo = new MessageRepository();
        conversationRepo = new ConversationRepository();

        // Generate a unique test user ID
        testUserId = UUID.randomUUID().toString();

        // Create a test conversation to use for messages
        Conversation conversation = new Conversation();
        conversation.setName("Test Conversation for Messages");
        conversation.setType("PERSONAL");
        testConversationId = conversationRepo.create(conversation);

        System.out.println("Test User ID: " + testUserId);
        System.out.println("Test Conversation ID: " + testConversationId);
    }



    /**
     * Test 2: Find messages by conversation
     * Verifies that we can retrieve messages for a specific conversation
     */
    @Test
    @Order(2)
    void testFindByConversation() throws SQLException {
        System.out.println("\n=== Test 2: Find Messages By Conversation ===");

        // Execute find
        List<Message> messages = messageRepo.findByConversation(testConversationId, testUserId);

        // Verify results
        assertNotNull(messages, "Message list should not be null");
        System.out.println("✅ Found " + messages.size() + " messages in conversation");

        // Display messages found
        for (Message msg : messages) {
            System.out.println("   - " + msg.toString());
        }
    }

    /**
     * Test 3: Update a message
     * Verifies that we can edit a message's content
     */
    @Test
    @Order(3)
    void testUpdateMessage() throws SQLException {
        System.out.println("\n=== Test 3: Update Message ===");

        // Skip if no message was created
        if (testMessageId == null) {
            System.out.println("⚠️ Skipping test: No message to update");
            return;
        }

        String updatedContent = "This is the updated message content!";

        // Execute update
        messageRepo.update(testMessageId, testUserId, updatedContent);

        // Verify by finding the message
        List<Message> messages = messageRepo.findByConversation(testConversationId, testUserId);

        boolean found = messages.stream()
                .anyMatch(m -> m.getId() == testMessageId &&
                        m.getContent().equals(updatedContent));

        assertTrue(found, "Updated message should have new content");
        System.out.println("✅ Message updated successfully");
    }

    /**
     * Test 4: Delete a message
     * Verifies that we can delete a message
     */
    @Test
    @Order(4)
    void testDeleteMessage() throws SQLException {
        System.out.println("\n=== Test 4: Delete Message ===");

        // Skip if no message was created
        if (testMessageId == null) {
            System.out.println("⚠️ Skipping test: No message to delete");
            return;
        }

        // Execute delete
        messageRepo.delete(testMessageId, testUserId);

        // Verify deletion
        List<Message> messages = messageRepo.findByConversation(testConversationId, testUserId);

        boolean found = messages.stream()
                .anyMatch(m -> m.getId() == testMessageId);

        assertFalse(found, "Deleted message should not be found");
        System.out.println("✅ Message deleted successfully");
    }

    /**
     * Cleanup after all tests
     * Deletes the test conversation
     */
    @AfterAll
    static void cleanup() throws SQLException {
        System.out.println("\n=== Cleaning up test data ===");

        // Delete the test conversation (this will cascade delete messages)
        if (testConversationId != null) {
            conversationRepo.delete(testConversationId);
            System.out.println("✅ Test conversation deleted");
        }
    }
}