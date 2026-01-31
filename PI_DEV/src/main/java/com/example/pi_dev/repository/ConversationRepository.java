package com.example.pi_dev.repository;

import com.example.pi_dev.model.Conversation;

import java.util.List;

public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Conversation findById(Long id);
    List<Conversation> findAll();
    void update(Conversation conversation);
    void delete(Long id);
}