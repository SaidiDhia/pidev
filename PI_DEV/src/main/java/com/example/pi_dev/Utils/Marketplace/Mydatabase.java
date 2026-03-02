package com.example.pi_dev.Utils.Marketplace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {

    public static Mydatabase instance;
    private Connection con;
    public static Mydatabase getInstance() {
        if (instance == null) {
            instance = new Mydatabase();
        }
        return instance;
    }
    private Mydatabase() {

        try {
            con= DriverManager.getConnection("jdbc:mysql://localhost:3306/wonderlust_db","root","");
            System.out.println("connected");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Connection getConnection() {
        return con;
    }

}
