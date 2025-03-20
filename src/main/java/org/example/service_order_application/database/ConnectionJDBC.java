package org.example.service_order_application.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionJDBC {
    private static ConnectionJDBC instance;
    private static Connection connection;

    public ConnectionJDBC() throws SQLException {
        String url = System.getProperty("DB_URL");
        String username = System.getProperty("DB_USER");
        String password = System.getProperty("DB_PASSWORD");

        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database Connection Created With success!!" + this.connection.toString());
        } catch (ClassNotFoundException ex) {
            System.out.println("Database Connection Creation Failed : " + ex.getMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static ConnectionJDBC getInstance() throws SQLException {
        if (instance == null) {
            instance = new ConnectionJDBC();
        } else if (instance.getConnection().isClosed()) {
            instance = new ConnectionJDBC();
        }

        return instance;
    }
}
