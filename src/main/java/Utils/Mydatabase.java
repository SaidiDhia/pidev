package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {

    private static Mydatabase instance;
    private final Connection con;

    private Mydatabase() {
        try {
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/wonderlustt_db",
                    "root",
                    ""
            );
            System.out.println("connexion etablie");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized Mydatabase getInstance() {
        if (instance == null) {
            instance = new Mydatabase();
        }
        return instance;
    }

    public Connection getConnextion() {
        return con;
    }
}