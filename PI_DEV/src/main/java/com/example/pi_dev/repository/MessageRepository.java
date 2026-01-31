package com.example.pi_dev.repository;

import com.example.pi_dev.model.Message;

import java.util.List;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findByConversationId(Long conversationId);
    void delete(Long id);
}
