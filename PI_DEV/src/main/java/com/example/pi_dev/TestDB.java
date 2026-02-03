package com.example.pi_dev;

import com.example.pi_dev.messaging.messagingdatabase.DatabaseConnection;
import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            System.out.println(" Connected to MySQL successfully");
        } catch (Exception e) {
            System.out.println("Connection failed");
            e.printStackTrace();
        }
    }
}
