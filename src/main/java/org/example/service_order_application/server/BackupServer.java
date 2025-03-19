package org.example.service_order_application.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.sql.SQLException;

import org.example.service_order_application.RMI.InterfaceServidorImpl;
import org.example.service_order_application.RMI.RMIServidor;

public class BackupServer {

    private static final String server = "localhost";
    private static final int port = 54323; // Porta diferente para o backup

    public void initializeBackupServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Backup Server initiated in port: " + port);

            RequestRedirect requestRedirect = new RequestRedirect();
            requestRedirect.setUpAllowedOperations();

            // Registrar o serviço RMI
            LocateRegistry.createRegistry(1700); // Porta diferente para o RMI do backup
            RMIServidor appServerService = new InterfaceServidorImpl(requestRedirect);
            Naming.rebind("rmi://localhost:1700/AppServerService", appServerService);

            System.out.println("Servidor RMI iniciado no servidor de aplicação backup");

            while (true) {
                Socket socket = serverSocket.accept();

                BufferedReader income = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter outcome = new PrintWriter(socket.getOutputStream(), true);

                String requisicao = income.readLine();
                System.out.println("Requisição recebida: " + requisicao);

                String response = requestRedirect.redirect(requisicao);
                outcome.println(response);

                outcome.close();
                income.close();
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Erro ao configurar RMI: " + e.getMessage());
        }
    }
}
