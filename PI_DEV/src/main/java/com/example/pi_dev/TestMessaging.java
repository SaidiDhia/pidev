package com.example.pi_dev;


import com.example.pi_dev.model.Message;
import com.example.pi_dev.repository.MessageRepository;

import java.sql.SQLException;

public class TestMessaging {

    public static void main(String[] args) throws SQLException {
        MessageRepository repo = new MessageRepository();

        Message msg = new Message(
                1L, // conversation_id (must exist in DB)
                1L, // sender_id (fake for now)
                "Hello from test"
        );

        repo.create(msg);
        System.out.println("Message inserted successfully!");
    }
}
