package org.example.service_order_application;

import org.example.service_order_application.server.ServerService;

public class App
{
    public static void main( String[] args )
    {
        ServerService serverService = new ServerService();
        serverService.initializeServer();
    }
}
