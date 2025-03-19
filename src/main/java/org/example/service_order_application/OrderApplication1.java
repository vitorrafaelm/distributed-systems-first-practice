package org.example.service_order_application;

import org.example.service_order_application.server.ServerService;

import java.util.HashMap;
import java.util.Map;

public class OrderApplication1
{
    public static void main( String[] args )
    {
        final int port = 54322;
        final int portRmi = 1700;
        final String serviceOrderInstance = "serviceOrderInstance1";

        Map<String, String> proxies = new HashMap<>();
        proxies.put("1800", "serviceOrderInstance2");

        System.setProperty("DB_URL", "jdbc:postgresql://localhost:5432/service-soo");
        System.setProperty("DB_USER", "service-soo");
        System.setProperty("DB_PASSWORD", "service-soo");

        ServerService serverService = new ServerService(port, portRmi, serviceOrderInstance, proxies);
        serverService.initializeServer();
    }
}
