package org.example.service_order_application;

import org.example.service_order_application.server.ServerService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class OrderApplication2
{
    public static void main( String[] args ) throws SQLException {
        final int port = 54323;
        final int portRmi = 1800;
        final String serviceOrderInstance = "serviceOrderInstance2";

        Map<String, String> proxies = new HashMap<>();
        proxies.put("1700", "serviceOrderInstance1");

        System.setProperty("DB_URL", "jdbc:postgresql://localhost:5432/service-soo-backup");
        System.setProperty("DB_USER", "service-soo");
        System.setProperty("DB_PASSWORD", "service-soo");

        ServerService serverService = new ServerService(port, portRmi, serviceOrderInstance, proxies);
        serverService.initializeServer();
    }
}



