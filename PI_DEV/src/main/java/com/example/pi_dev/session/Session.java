package com.example.pi_dev.session;

public class Session {

    private static long currentUserId;

    private Session() {}

    public static void login(long userId) {
        currentUserId = userId;
    }

    public static long getCurrentUserId() {
        return currentUserId;
    }
}
