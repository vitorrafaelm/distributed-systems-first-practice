package org.example.service_order_application.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.service_order_application.RMI.InterfaceServidorImpl;
import org.example.service_order_application.RMI.RMIServidor;
import org.example.service_order_application.database.ConnectionJDBC;
import org.example.service_order_proxy.rmi.RMIService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.sql.SQLException;
import java.util.Map;

public class ServerService {

    private static final String server = ""; // Ip victor
    int port;
    int portRmi;
    String serviceOrderInstance;
    Map<String, String> proxies;

    public ServerService(int port, int portRmi, String serviceOrderInstance, Map<String, String> proxies) throws SQLException {
        this.port = port;
        this.portRmi = portRmi;
        this.serviceOrderInstance = serviceOrderInstance;
        this.proxies = proxies;

        new ConnectionJDBC();
    }

    public void initializeServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server initiated in port: " + port);

            RequestRedirect requestRedirect = new RequestRedirect();
            requestRedirect.setUpAllowedOperations();

            // Registrar o serviço RMI
            LocateRegistry.createRegistry(portRmi); // Porta diferente para o RMI do backup
            RMIServidor appServerService = new InterfaceServidorImpl(requestRedirect);
            Naming.rebind("rmi://localhost:" + portRmi + "/" + serviceOrderInstance, appServerService);

            System.out.println("Servidor RMI iniciado no servidor de aplicação backup");

            while (true) {
                Socket socket = serverSocket.accept();

                BufferedReader income =
                       new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));

                PrintWriter outcome =
                        new PrintWriter(socket.getOutputStream(), true);

                String requisicao = income.readLine();
                System.out.println("Requisição recebida: " + requisicao);

                final JsonObject jsonObject = JsonParser.parseString(requisicao).getAsJsonObject();
                final String operation = jsonObject.get("operation").getAsString();

                if ("add".equals(operation) || "update".equals(operation) || "delete".equals(operation)) {
                    for (String key : proxies.keySet()) {
                        RMIServidor rmiService = (RMIServidor) Naming.lookup("rmi://localhost:" + key + "/" + proxies.get(key));
                        rmiService.processRequest(requisicao);
                    }
                }

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
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

    }
}
