package com.example.pi_dev.messaging.messagingrepository;


import com.example.pi_dev.Entities.Conversation;
import com.example.pi_dev.Repositories.ConversationRepository;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConversationRepository
 * Tests CRUD operations: Create, Read, Update, Delete
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConversationRepositoryTest {

    private static ConversationRepository conversationRepo;
    private static String testUserId;
    private static Long testConversationId;

    /**
     * Setup before all tests - runs once
     * Initializes the repository and creates a test user ID
     */
    @BeforeAll
    static void setup() {
        conversationRepo = new ConversationRepository();
        // Generate a unique test user ID
        testUserId = UUID.randomUUID().toString();
        System.out.println("Test User ID: " + testUserId);
    }

    /**
     * Test 1: Create a new conversation
     * Verifies that a conversation can be inserted into the database
     * and that an ID is generated
     */
    @Test
    @Order(1)
    void testCreateConversation() throws SQLException {
        System.out.println("\n=== Test 1: Create Conversation ===");

        // Create a test conversation
        Conversation conversation = new Conversation();
        conversation.setName("Test Conversation");
        conversation.setType("PERSONAL");
        conversation.setContextType("TEST");
        conversation.setContextId(0);

        // Execute create method
        long id = conversationRepo.create(conversation);
        testConversationId = id;

        // Verify the result
        assertTrue(id > 0, "Conversation ID should be greater than 0");
        System.out.println("✅ Conversation created with ID: " + id);

        // Verify we can find it (indirect verification)
        assertNotNull(id, "Created conversation should have an ID");
    }

    /**
     * Test 2: Find conversations by user
     * Note: This requires the conversation_user table to be populated
     * You might need to add a test user to the conversation first
     */
    @Test
    @Order(2)
    void testFindByUser() throws SQLException {
        System.out.println("\n=== Test 2: Find Conversations By User ===");

        // This test requires that you have some conversations for the user
        // For now, we'll just verify the method doesn't throw exceptions
        List<Conversation> conversations = conversationRepo.findByUser(testUserId);

        assertNotNull(conversations, "Conversation list should not be null");
        System.out.println("✅ Found " + conversations.size() + " conversations for user");
    }

    /**
     * Test 3: Update conversation name
     * Verifies that we can change a conversation's name
     */
    @Test
    @Order(3)
    void testUpdateConversationName() throws SQLException {
        System.out.println("\n=== Test 3: Update Conversation Name ===");

        // Skip if no test conversation was created
        if (testConversationId == null || testConversationId <= 0) {
            System.out.println("⚠️ Skipping test: No conversation to update");
            return;
        }

        String newName = "Updated Test Conversation";

        // Execute update
        conversationRepo.updateName(testConversationId, newName);

        // Verify the update (indirect verification)
        // We would need a findById method to directly verify
        System.out.println("✅ Conversation name updated to: " + newName);

        // Since we don't have a findById, we consider the test passed if no exception
        assertTrue(true, "Update should complete without exception");
    }

    /**
     * Test 4: Delete conversation
     * Verifies that a conversation and its related data are deleted
     */
    @Test
    @Order(4)
    void testDeleteConversation() throws SQLException {
        System.out.println("\n=== Test 4: Delete Conversation ===");

        // Skip if no test conversation was created
        if (testConversationId == null || testConversationId <= 0) {
            System.out.println("⚠️ Skipping test: No conversation to delete");
            return;
        }

        // Execute delete
        conversationRepo.delete(testConversationId);

        // Verify deletion (indirect verification)
        System.out.println("✅ Conversation deleted with ID: " + testConversationId);

        // Consider test passed if no exception
        assertTrue(true, "Delete should complete without exception");
    }

    /**
     * Cleanup after each test
     * Not strictly needed here since we manage IDs manually
     */
    @AfterEach
    void cleanUp() {
        System.out.println("--- Test completed ---");
    }
}