package org.example.service_order_application;

import org.example.service_order_application.server.BackupServer;

public class App2 {

    public static void main(String[] args) {
        BackupServer backupServer = new BackupServer();
        backupServer.initializeBackupServer();
    }
}
