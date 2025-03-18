package org.example.service_order_proxy.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LocationClient {
    private static final String LOCATION_SERVER_HOST = "localhost";
    private static final int LOCATION_SERVER_PORT = 8000;
    
    /**
     * Conecta ao servidor de localização e obtém o endereço do servidor de aplicação
     * @return String contendo o endereço do servidor no formato "host:porta"
     * @throws IOException se ocorrer um erro de comunicação
     */
    public String getApplicationServerAddress() throws IOException {
        try (
            Socket socket = new Socket(LOCATION_SERVER_HOST, LOCATION_SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Envia requisição para o servidor de localização
            out.println("GET_APPLICATION_SERVER");
            
            // Lê a resposta (endereço do proxy)
            String response = in.readLine();
            System.out.println("Servidor de aplicação localizado em: " + response);
            
            return response;
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor de localização: " + e.getMessage());
            throw e;
        }
    }
}