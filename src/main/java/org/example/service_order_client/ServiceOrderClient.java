package org.example.service_order_client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.JsonObject;

public class ServiceOrderClient {

    private static final String LOCATION_SERVER_HOST = "localhost";
    private static final int LOCATION_SERVER_PORT = 8000;
    private static final String PROXY_AUTH_TOKEN = "90e476f3-5ccc-4143-8e30-f4b82b6dd131";

    private String applicationServerProxyHost;
    private int applicationServerProxyPort;
    private Scanner scanner;

    public ServiceOrderClient() {
        scanner = new Scanner(System.in);
        this.connectToLocationServer();
        this.insertInitialServiceOrders(); // Inserir 100 OS automaticamente
    }

    private void connectToLocationServer() {
        try (Socket socket = new Socket(LOCATION_SERVER_HOST, LOCATION_SERVER_PORT)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("GET_APPLICATION_SERVER");
            String response = in.readLine();
            String[] serverInfo = response.split(":");
            this.applicationServerProxyHost = serverInfo[0];
            this.applicationServerProxyPort = Integer.parseInt(serverInfo[1]);
            System.out.println("Servidor de aplicação encontrado em " + applicationServerProxyHost + ":" + applicationServerProxyPort);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor de localização: " + e.getMessage());
            System.exit(1);
        }
    }

    private String sendRequest(String request) {
        try (
                Socket socket = new Socket(applicationServerProxyHost, applicationServerProxyPort); PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(request);
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o servidor: " + e.getMessage());
            return "ERRO: " + e.getMessage();
        }
    }

    private void insertInitialServiceOrders() {
        System.out.println("Inserindo 100 ordens de serviço iniciais...");
        for (int i = 1; i <= 100; i++) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("operation", "add");
            jsonObject.addProperty("code", "OS" + i);
            jsonObject.addProperty("name", "Ordem de Serviço " + i);
            jsonObject.addProperty("description", "Descrição da OS " + i);
            jsonObject.addProperty("Authorization", PROXY_AUTH_TOKEN);

            String operation = jsonObject.toString();
            String result = sendRequest(operation);

            System.out.println("Inserindo OS " + i + ": " + result);
        }
        System.out.println("Inserção de 100 ordens de serviço concluída.");
    }
}
