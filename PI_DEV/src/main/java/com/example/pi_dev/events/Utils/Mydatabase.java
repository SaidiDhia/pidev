package com.example.pi_dev.events.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {

    Connection con;
    public static Mydatabase instance;

    private Mydatabase(){
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/wonderlust_db","root","saididhia");
            System.out.println("connexion etablie");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Mydatabase getInstance(){
        if(instance == null){
            instance = new Mydatabase();
        }
        return instance;
    }

    public Connection getConnextion(){
        return con;
    }
}